(
var musicThingFaders = Array.series(41 - 34 + 1, 34, 1);
var musicThingAcc = Array.series(49 - 42 + 1, 42, 1);

s.freeAll;

// MIDI
MIDIClient.init;
MIDIIn.connectAll;
// MIDIFunc.trace(false);

// Buses
s.newBusAllocators;
~fadersBus = Bus.control(s, 8);
~positionBus = Bus.control(s, 8);
~fbBus = Bus.audio(s, 2);
~limiterBus = Bus.audio(s, 2);

// MIDI Defs
MIDIdef.cc(\cc, {
	arg val, num, chan, src;
	val = val.clip(0, 127);
	// ((num - 34).asString ++ ": " ++ val.asString).postln;
	~fadersBus.setAt(num - 34, val.linlin(0, 127, 0.0, 1.0));
}, musicThingFaders, 0).permanent_(true);

// Server Setup
s.waitForBoot({
	// SynthDefs
	SynthDef(\limiter, {
		arg in = 3,
		out = 0,
		thresh = 0.3;

		var in2 = In.ar(in, 2);

		// audio band pass
		var sig = LPF.ar(in2, 20e3);
		sig = HPF.ar(sig, 20);

		// hard limiter
		sig = Compander.ar(
			in: sig,
			control: sig,
			thresh: thresh,
			slopeBelow: 1,
			slopeAbove: 0.001,
			clampTime: 0.001,
			relaxTime: 0.01
		);

		Out.ar(out, sig);
	}).add;

	SynthDef(\osc, {
		arg out = 0,
		freq = 440,
		amp = 0.1,
		atk = 0.01,
		gate = 1,
		rls = 1;

		var sig = SinOsc.ar(
			freq: freq,
			mul: EnvGen.kr(
				gate: gate,
				doneAction: 2,
				envelope: Env.asr(
					attackTime: atk,
					releaseTime: rls,
					sustainLevel: amp
				)
			)
		);

		Out.ar(out, sig ! 2);
	}).add;

	SynthDef(\crackle, {
		arg out = 0,
		chaos = 1,
		amp = 0.1,
		atk = 0.01,
		rls = 1;

		var sig = Crackle.ar(
			chaosParam: 1,
			mul: EnvGen.kr(
				doneAction: 2,
				envelope: Env.perc(
					attackTime: atk,
					releaseTime: rls,
					level: amp
				)
			)
		);

		Out.ar(out, sig ! 2);
	}).add;


	SynthDef(\patch2a, {
		arg in = 2, out = 0,
		ctrl;
		var mul = In.kr(ctrl, 2);
		Out.ar(out, In.ar(in, 2) * mul);
	}).add;


	SynthDef(\patch2aConst, {
		arg in, out, amp = 1.0;
		Out.ar(out, In.ar(in, 2) * amp);
	}).add;

	SynthDef(\fb, {
		arg in, out, ctrl,
		decay = 15, freeze = 0,
		lf = 15000, hf = 60,
		freq = 10000, res = 0,
		amp = 0.1;

		var sig = In.ar(in, 2) * amp;


		sig = DelayN.ar(sig, 0.048);

		sig = 7.collect({
			CombL.ar(
				sig,
				maxdelaytime: 0.1,
				delaytime: LFNoise1.kr(0.01.rand, 0.04, 0.05),
				decaytime: decay.lag(1)
			)
		}).sum;

		4.do({
			sig = AllpassN.ar(
				sig,
				maxdelaytime: 0.05,
				delaytime: [ 0.05.rand, 0.05.rand],
				decaytime: (decay / 15).lag(1)
			);
		});

		sig = LPF.ar(sig, lf.lag(1));
		sig = HPF.ar(sig, hf.lag(1));
		sig = MoogFF.ar(
			sig,
			freq: freq,
			gain: res.linexp(0, 1, 0.0001, 4),
			mul: 4
		);

		8.do({
			arg i;
			sig = BPeakEQ.ar(
				sig,
				freq: ((18000 - 40) * i.lincurve(0, 7, 0.001, 1, 4) + 40),
				rq: 1.2,
				db: (40 * In.kr(ctrl + i) - 40),
				mul: 1.5,
			);
		});

		Out.ar(out, sig);
	}).add;



	SynthDef(\keys, {
		arg in = 2, out = 0,
		gain = 1, clip = 0.1, pan = 0,
		hf = 40, lf = 18000;
		var sig = Balance2.ar(
			In.ar(in, 1),
			In.ar(in + 1, 1),
			pan.clip(-1, 1),
		);
		sig = sig * gain;
		sig = HPF.ar(sig, hf);
		sig = LPF.ar(sig, lf);
		Out.ar(out, sig.clip(clip.neg, clip));
	}).add;

	s.sync;

	// Nodes
	~addNodes = {
		~mixGroup = Group.new();
		~fxGroup = Group.new(~mixGroup, addAction: \addBefore);
		~patchGroup = Group.new(~fxGroup, addAction: \addBefore);
		~srcGroup = Group.new(~patchGroup, addAction: \addBefore);

		~limiter = Synth(
			\limiter,
			[ in: ~limiterBus, out: 0, thresh: -1.dbamp ],
			~mixGroup
		);

		"-- Nodes added --".postln;
	};

	ServerTree.removeAll;
	ServerTree.add(~addNodes);
	ServerTree.run;

	CmdPeriod.removeAll;
	CmdPeriod.add({ "-- Restart -- ".postln });
	// s.plotTree;

	Window.closeAll;
	~scopeWin = Window.new("scope", Rect(900, 900, 300, 300)).layout_(
		HLayout()
	);
	Stethoscope(s, numChannels: 8, view: ~scopeWin);
	~scopeWin.front;
	~scopeWin.alwaysOnTop_(true);

	~fadersBus.value = 0.5;
})
)

(
~keysSynthMain.free;
~keysSynthMain = Synth(
	\keys,
	[
		out: ~limiterBus,
		hf: 120,
		lf: 18000,
		gain: [30, 30],
		clip: 0.1,
		pan: 0.1
	]
);
)

(
~fbSynth.free;
~fbSynth = Synth(
	\fb,
	[
		in: ~fbBus,
		out: ~limiterBus,
		ctrl: ~fadersBus,
		decay: 1,
		amp: 1.2
	],
	~fxGroup
);
)


TempoClock.default.tempo = 0.5;

(
p.stop;
p.free;
p = Pbind(
	\instrument, \crackle,
	// \dur, Pseq([4, 2, 1, 0.5], inf),
	// \db, Pseq([-4, -14], inf),
	\dur, Pseq([4], inf),
	\db, -10,
	\atk, Pseq([0.001, 0.001, 0.005, 0.008].scramble, inf),
	\rls, 0.1,
	\chaos, 1.8,
	\out, ~fbBus,
	\group, ~srcGroup
).play(quant: 4);
)

(
~keysSynthFb.free;
~keysSynthFb = Synth(
	\keys,
	[
		out: ~fbBus,
		hf: 100,
		lf: 10000,
		gain: 10,
		clip: 0.02
	]
);
)



~fbSynth.set(\decay, 3);
~fbSynth.set(\hf, 100);
~fbSynth.set(\lf, 18000);
~fbSynth.set(\freq, 7000);


