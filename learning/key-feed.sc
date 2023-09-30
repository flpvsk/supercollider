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
~reverbBus = Bus.audio(s, 2);
~keysBus = Bus.audio(s, 2);
~limiterBus = Bus.audio(s, 2);
~reverbFbBus = Bus.audio(s, 2);

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

		Out.ar(out, sig);
	}).add;


	SynthDef(\keys, {
		arg in = 2, out = 0;
		Out.ar(out, In.ar(in, 2));
	}).add;

	SynthDef(\reverb, {
		arg in = 2, out = 0,
		room = 0.9,
		damp = 0.1,
		amp = 0.1;

		Out.ar(out, FreeVerb2.ar(
			in: In.ar(in, 1),
			in2: In.ar(in + 1, 1),
			mix: 1,
			room: room,
			damp: damp,
			mul: amp
		));
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

		~reverbSynth = Synth(
			\reverb,
			[\in: ~reverbBus, \out: ~limiterBus],
			~fxGroup
		);

		"-- Nodes added --".postln;
	};

	ServerTree.removeAll;
	ServerTree.add(~addNodes);
	ServerTree.run;

	CmdPeriod.removeAll;
	CmdPeriod.add({ "-- Restart -- ".postln });
	// s.plotTree;
})
)

(
~keysSynth = Synth(
	\keys, [\in: 2, \out: ~keysBus],
	~srcGroup
);
)

(
SynthDef(\resonator, {
	arg in, out, amp, freq, delay, fb;
	var res = CombN.ar(
		In.ar(in, 2),
		maxdelaytime: 1,
		delaytime: (1 / ((62 + (88 * In.kr(delay))).floor.midiratio)),
		decaytime: In.kr(fb) * 10
	);

	res = LPF.ar(
		res,
		freq: LinExp.kr(In.kr(freq, 1), 0, 1, 20, 20000),
	);

	Out.ar(out, res * LinExp.kr(In.kr(amp)));
}).add
)

(
~resonator1.free;
~resonator2.free;

~resonator1 = Synth(
	\resonator,
	[
		\in: ~keysBus,
		\out: ~limiterBus,
		\amp: ~fadersBus.index + 2,
		\freq: ~fadersBus.index + 3,
		\delay: ~fadersBus.index + 4,
		\fb: ~fadersBus.index + 5
	],
	~patchGroup
);


~resonator2 = Synth(
	\resonator,
	[
		\in: ~keysBus,
		\out: ~reverbBus,
		\amp: ~fadersBus.index + 2,
		\freq: ~fadersBus.index + 3,
		\delay: ~fadersBus.index + 4,
		\fb: ~fadersBus.index + 5
	],
	~patchGroup
);

)


(
~reverbToFbSynth = Synth(
	\patch2aConst,
	[\in: ~reverbBus, \out: ~reverbFbBus],
	~fxGroup,
	\addToTail
);
~reverbFbToLimiterSynth = Synth(
  \patch2aConst,
	[\in: ~reverbFbBus, \out: ~limiterBus],
	~fxGroup,
	\addToTail
)
)

(
~reverbFb = Synth(
  \patchFB2a,
	[
		\in: ~reverbFbBus,
		\out: ~reverbFbBus,
		\amp: ~fadersBus.index + 2,
		\freq: ~fadersBus.index + 3,
		\delay: ~fadersBus.index + 4,
		\fb: ~fadersBus.index + 5
	],
	~reverbSynth,
	\addAfter
)
)

~reverbFb.free;


t = { Out.ar(~reverbBus, In.ar(~keysBus, 2)) }.play(~patchGroup);
t.free;
~keysSynth.free;
~keysBus.free;


(
Pbind(
	\instrument, \default,
	\midinote, Pseq([ 54, 58, 60 ]),
	\sustain, 0.5,
	\dur, 0.2,
	\amp, 0.3,
	\out, Array.series(~reverbBus.numChannels, ~reverbBus.index),
	\group, ~srcGroup,
).play;
)


// MIDIdef(\cc).disable;
// MIDIdef.freeAll;

(
s.newBusAllocators;

~testBus = Bus.control(s);
~testBus.value_(1);
// or ~testBus.setSynchronous(1);
~testBus.getSynchronous.postln;

SynthDef(\test, {
	arg bus = 0;
	In.kr(bus, 1).poll;
}).add;

~test = Synth(\test, [\bus: ~testBus]);

TempoClock.default.sched(0.1, {
	~testBus.getSynchronous.postln;
	~test.free;
	~testBus.free;
	"---".postln;
	"".postln;
});

)
