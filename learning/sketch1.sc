s.boot
(
SynthDef.new(\tone, {
	arg gate = 1,
	  freq = 432,
	  amp = 0.2,
	  bus = 0;
	var env = EnvGen.kr(
		envelope: Env(
			levels: [0, 1, 0.4, 0.4, 0],
			times: [0.01, 0.1, 2, 1],
			// releaseNode: 2
		),
		gate: gate,
		doneAction: 2,
	);
	var sin = SinOsc.ar(freq, mul: amp) ! 2;
	Out.ar(bus, sin * env);
}).add;
)

x = Synth.new(\tone, [gate: 1, freq: rrand(60, 70).midicps, amp: 0.1]);
x.set(\gate, 0);

s.meter;
s.plotTree;
s.makeGui;

b = Buffer.read(s, "audio/beat1.wav".resolveRelative);
b.play
b.free;

(
SynthDef.new(\buf, {
	arg bufnum = 0,
	bus = 0,
	amp = 0.1,
	start = 0,
	rate = 1,
	gate = 1,
	time = 1;

	var sig = PlayBuf.ar(
		numChannels: 1,
		bufnum: bufnum,
		startPos: start,
		trigger: EnvGen.kr(
			gate: gate,
			envelope: Env.circle(
				levels: [-1, 1],
				times: [time]
		  )
		),
		rate: BufRateScale.kr(bufnum) * rate
	) ! 2;
	Out.ar(bus, sig * amp);
}).add
);
x = Synth.new(\buf, [ bufnum: b.bufnum, start: 3 * b.sampleRate, rate: 1.midiratio ]);
x.free
x.set(\gate, 0)

s.boot

{ EnvGen.kr(envelope: Env.circle(levels: [-1, 1], times: [0.1]), gate: 1) }.plot(1)