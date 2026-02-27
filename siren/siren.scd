(
s.freeAll;
Server.default.options.inDevice_("");
Server.default.options.outDevice_("JLab GO Sport+");
s.waitForBoot({
  SynthDef.new(\siren, {
    var sig, mod, env, freq;
    freq = \freq.kr(440);
    env = EnvGen.kr(
      Env.asr(0.1, 1.0, 3.0),
      gate: \gate.kr(0),
      levelScale: 0.2,
      doneAction: 2
    );
    mod = SinOsc.ar(\modFreq.kr(1)).range(0, 1);
    sig = SinOsc.ar(freq + mod * (freq - 1)) * env;
    sig = Pan2.ar(sig, \pan.kr(0));
    Out.ar(\out.kr(0), sig);
  }).add;

  s.sync;

  x = Synth(\siren);
  // x.set(\gate, 1);
})
)

x.set(\gate, 1);
x.set(\gate, 0);
x.set(\modFreq, 10);


{ SinOsc.ar(440 + 439 * SinOsc.ar(\modFreq.kr(200)).range(0, 1)) }.plot()

(
{
    var sig, mod, env, freq;
    freq = \freq.kr(440);
    env = EnvGen.kr(
      Env.asr(0, 1.0, 3.0),
      gate: \gate.kr(0),
      levelScale: 0.2,
      doneAction: 2
    );
    mod = SinOsc.ar(\modFreq.kr(1)).range(0, 1);
    sig = SinOsc.ar(freq + mod) * env;
    sig = Pan2.ar(sig, \pan.kr(0));
    Out.ar(\out.kr(0), sig);
}.plot()
)