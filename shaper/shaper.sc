/*
(
s.freeAll;
s.newBusAllocators;
s.waitForBoot({
	~addNodes = {
		"-- Nodes Added --".postln;
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
	Stethoscope(s, numChannels: 2, view: ~scopeWin);
	~scopeWin.front;
	~scopeWin.alwaysOnTop_(true);
});

)
*/

(
~plotValues = {
	~s1.value.postln;
	~s2.value.postln;
};

~runShaper = {
	var thresh = ~s1.value;

	~s1.value.postln;
	~s2.value.postln;


	~synth.free;
	b.free;
	t.free;

	b = Buffer.alloc(s, 1024, 1);

	// or, for an arbitrary transfer function, create the data at 1/2 buffer size + 1
	t = Signal.fill(
		513,
		{
			arg i;
			var absV = abs(v);
			v = i.linlin(0.0, 512.0, -1.0, 1.0);
			if(
				absV > thresh,
				{
					sign(v) * (thresh - (~s2.value * (absV - thresh)))
				},
				{ v }
			)
		}
	);
	//t.plot;


	b.sendCollection(t.asWavetableNoWrap);

	~synth = {
		var sig = Shaper.ar(b, SinOsc.ar(440, 0, 0.4));
		sig ! 2
	}.scope;
};

Window.closeAll;
w = Window.new("GUI Introduction", Rect(200,200,320,320)).front;
// notice that FlowLayout refers to w.view, which is the container view
// automatically created with the window and occupying its entire space
w.view.decorator = FlowLayout(w.view.bounds);
~s1 = Slider(w, 150@20).action_(~runShaper);
~s2 = Slider(w, 150@20).action_(~runShaper);
)