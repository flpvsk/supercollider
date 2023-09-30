s.boot

(
t = TempoClock(2.5);
)

(
var getNoteName = {
	arg n = 48;
	[
		'C', 'C#', 'D',
		'D#', 'E', 'F',
		'F#', 'G', 'G#',
		'A', 'A#', 'B'
	][n - (12 * (n / 12).floor)];
};


var getScale = {
	arg degrees = [ 2, 2, 1, 2, 2, 2, 1 ];
	{
		arg degree = 0, root = 52;
		var res = 0, idx = 0;
		degree.do({
			res = res + degrees.wrapAt(idx);
			idx = idx + 1;
		});
		root + res;
	};
};

r = Routine({
	var major = getScale.value();
	var minor = getScale.value([2, 1, 2, 2, 1, 2, 2]);
	var scales = [major, minor];
	var rootsCount = 4;
	var roots = Array.fill(rootsCount, { |n| major.value(6.rand) });
	var ptn = [0, 2, -3, 6].scramble;
	var rests = Array.fill(ptn.size , { [0.5, 0.5, 1,].choose });
	var chordRests = Array.fill(roots.size, { [0.5, 1].choose });

	inf.do({
		arg idx;
		var rootIdx = idx.wrap(0, rootsCount);
		var root = roots.wrapAt(idx);
		var iter = (idx / rootsCount).floor;
		var isOdd = (iter + 1) % 2;
		var scale = scales.choose;

		(
			"-- "
			++ getNoteName.value(root)
			++ " "
			++ (idx % roots.size)
			++ " --"
		).postln;

		ptn.do({
			arg degree, idx;
			var note = scale.value(degree: degree, root: root);
			var isRoot = (idx == 0).asInteger;
			var isEvenRoot = ((iter % 2) == 0 && (rootIdx == 0)).asInteger;
			getNoteName.value(note).postln;
			Synth(\beep, [
				freq: note.midicps,
				amp: 0.1 + (isRoot * 0.05),
				release: (
					(t.beatDur * rests[idx] * 1 + isRoot * rests[idx])
					// + t.beatDur * ((idx == 0).asInteger * 2)
				)
			]);
			rests[idx].yield;
		});
		chordRests[rootIdx].yield
	});
});

SynthDef(\beep, {
	arg freq = 440,
	  attack = 0.01,
	  release = 1,
	  envTime = 1,
	  amp = 0.2,
	  bus = 0,
	  chan = 2;

	var env = EnvGen.kr(
		envelope: Env.perc(
			attackTime: attack * envTime,
			level: amp,
			releaseTime: release * envTime,
		),
		doneAction: 2
	);


	var sig = SinOsc.ar(freq: freq, mul: env);

	Out.ar(bus, sig ! 2);
}).add;

r.play(clock: t, quant: 4);
)

r.stop;