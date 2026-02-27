(
SynthDef(\first, {
  var sig1, sig2, env, note;
  note = \note.kr(60);
  env = EnvGen.kr(Env.asr(0.1, 1.0, 1.0), doneAction: 2);
  sig1 = SinOsc.ar(freq: note.midicps, mul: 0.1) * env;
  sig2 = SinOsc.ar(freq: (note + \shift.kr(4)).midicps, mul: 0.1) * env;
  Out.ar(0, [sig1, sig2]);
}).add;

)

(
f = Synth(\first);
f.set(\note, 69);
f.set(\shift, 0.22);
f.free();
)

(
{
  var sig, env;
  env = EnvGen.kr(Env.asr, doneAction: 2);
  sig = SinOsc.ar(freq: 60.midicps ! 2, mul: 0.1);
  sig = sig * env;
}
).play;