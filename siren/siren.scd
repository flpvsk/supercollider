(
s.freeAll;
// Server.default.options.inDevice_("");
// Server.default.options.outDevice_("JLab GO Sport+");
s.waitForBoot({
  SynthDef.new(\siren, {
    var sig, mod, env,
    modSq, modSin,
    freq, modWave, modAmt, modFreq,
    slewT;

    slewT = 0.001;
    freq = \freq.kr(440);
    modAmt = \modAmt.kr(1);
    modFreq = \modFreq.kr(1);
    modWave = \modWave.kr(0).clip(0, 1);
    env = EnvGen.kr(
      Env.asr(\rise.kr(0.01), 1.0, \fall.kr(0.3)),
      gate: \gate.kr(0),
      levelScale: 0.2,
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
    mod = (modSin * (1.0 - modWave)) + (modSq * modWave);
    sig = SinOsc.ar(freq + mod) * env;
    sig = Pan2.ar(sig, \pan.kr(0));
    Out.ar(\out.kr(0), sig);
  }).add;

  s.sync;

  x = Synth(\siren);
  x.set(\gate, 1);
})
)

x.set(\gate, 1);
x.set(\gate, 0);
x.set(\modFreq, 10);
x.set(\modWave, 0);
x.set(\modAmt, 0.8);

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