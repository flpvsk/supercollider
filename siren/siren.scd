(
s.freeAll;

~varOsc = {
  var sine, saw, sq, freq, phase;
  phase = \phase.kr(0);
  freq = \freq.kr(440);
  sine = SinOsc.ar(freq: freq);
  saw = Saw.ar(freq: freq);
  sq = Pulse.ar(freq: freq);
  SelectX.ar(\shape.kr(0) * 2, [ sine, saw, sq ]);
};

// Server.default.options.inDevice_("");
// Server.default.options.outDevice_("JLab GO Sport+");
s.waitForBoot({
  SynthDef.new(\siren, {
    var sig, mod, env,
    modSq, modSin, modSaw,
    freq, sigSin, sigSaw, sigSq,
    modShape, modAmt, modFreq,
    slewT;

    slewT = 0.001;
    freq = \freq.kr(440);
    modAmt = \modAmt.kr(1);
    modFreq = \modFreq.kr(10);
    modShape = \modShape.kr(0).clip(0, 1);
    env = EnvGen.kr(
      Env.asr(
        \rise.kr(0.001),
        1.0,
        modFreq.reciprocal.clip(0.5, 2),
        curve: [-4, 4]
      ),
      gate: \gate.kr(1),
      levelScale: \amp.kr(0.2),
      doneAction: Done.freeSelf,
    );
    modSq = EnvGen.ar(
      Env(
        levels: [0, 1, 1, 0, 0, 0],
        times: [
          slewT,
          modFreq.reciprocal * 0.5 - slewT, slewT,
          modFreq.reciprocal * 0.5 - slewT,
          slewT
        ],
        curve: \lin,
        releaseNode: 4,
        loopNode: 0
      ),
      gate: env > 0
    ).range(0, modAmt * freq);
    modSin = EnvGen.ar(
      Env.circle(
        levels: [1, 0],
        times: modFreq.reciprocal * 0.5,
        curve: \sine
      ),
      gate: env > 0,
    ).range(0, modAmt * freq);
    modSaw = EnvGen.ar(
      Env(
        levels: [1, 0, 1, 0],
        times: [
          modFreq.reciprocal - slewT,
          slewT,
          slewT
        ],
        curve: \lin,
        releaseNode: 2,
        loopNode: 0
      ),
      gate: env > 0
    ).range(0, modAmt * freq);

    mod = SelectX.ar(\modShape.kr(0) * 2, [ modSin, modSaw, modSq ]);

    sigSin = SinOsc.ar(freq: freq + mod);
    sigSaw = Saw.ar(freq: freq + mod);
    sigSq = Pulse.ar(freq: freq + mod);
    sig = SelectX.ar(\shape.kr(0) * 2, [ sigSin, sigSaw, sigSq ]) * env;
    sig = Pan2.ar(sig, \pan.kr(0));
    Out.ar(\out.kr(0), sig);
  }).add;

  s.sync;

  // x = Synth(\siren);
  // x.set(\gate, 1);

  SynthDef.new(\moog, {
    var sig;
    sig = In.ar(\in.kr(2), 2);
    sig = MoogFF.ar(sig, \freq.kr(76.midicps), \gain.kr(2));
    Out.ar(\out.kr(0), sig);
  }).add;

  SynthDef.new(\rev, {
    var sig, clean;
    clean = In.ar(\in.kr(2), 2);
    sig = FreeVerb2.ar(clean[0], clean[1], 1, \room.kr(0.5), \damp.kr(0.5));
    Out.ar(\out.kr(0), XFade2.ar(clean, sig, \mix.kr(0)));
  }).add;
})
)

~rev = Synth(\rev, [\in, 4, \out, 0]);
~moog = Synth(\moog, [\in, 2, \out, 4]);

~moog.set(\freq, 100.midicps);
~moog.set(\gain, 3);

~rev.set(\room, 0.4);
~rev.set(\damp, 1);
~rev.set(\mix, -0.5);

(
p = Pbind(
  \type, \note,
  \instrument, \siren,
  \dur, 3,
  \sustain, 1,
  \out, 2,
  \amp, 0.5,
  \midinote, Prand([40, 52, 64, 76, 28], 4),
  \shape, Prand([0, 0.2, 0.4, 0.5, 0.7, 0.9, 1], inf),
  \modFreq, Prand([0.1, 0.4, 0.8, 1.6, 4, 8, 16, 32], inf),
  \modShape, Prand([0, 0.5, 1], inf),
).play
)

p.stop;

s.plotTree;
s.freeAll;

x = Synth(\siren);
x.set(\gate, 1);
x.set(\gate, 0);
x.set(\fall, 3);
x.set(\shape, 0);
x.set(\freq, 52.midicps); // E
x.set(\amp, 0.2);
x.set(\modFreq, 10);
x.set(\modWave, 1);
x.set(\modAmt, 0.8);

s.scope

Pbind;

({
  var gate = LFPulse.kr(2);
  EnvGen.kr(
    Env(
      levels: [0, 1, 0, 0],
      times: [1 / 2, 1 / 2, 1 / 2],
      curve: \lin,
      releaseNode: 2,
      loopNode: 0
    ).plot(),
    gate: gate
  )
}.plot(4))


({
  var gate = LFPulse.kr(2);
  EnvGen.kr(
    Env(
      levels: [1, 1, 0],
      times: [
        0.0001,
        0.5,
      ],
      curve: \lin,
      releaseNode: 2,
      loopNode: 1
    ).plot(),
    gate: gate
  )
}.plot(4))


({
  var gate = LFPulse.kr(2);
  EnvGen.kr(
    Env.circle(
      levels: [1, 0],
      times: 1 / 2,
      curve: \sine
    )
  )
}.plot(4))

( // Plot the envelope using *circle and -circle
var loopBackTime = 0.06;
{ [
    EnvGen.kr(
        Env.circle([0.5, 1, 0.3], [0.01, 0.03, loopBackTime])
    ),
    EnvGen.kr(
        Env.new([0.5, 1, 0.3], [0.01, 0.03]).circle(loopBackTime)
    )
] }.plot(0.1 * 4 - 0.01).plotMode_(\dlines)
)


// Plugins.installPlugin("portedplugins");