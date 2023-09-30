s.boot;

(
b = NetAddr.new("127.0.0.1", 8000);
b.sendMsg("/color/bg/hsb", 0.5, 0.4, 0.5);
b.sendMsg("/color/fg/hsb", 0.1, 0.5, 0.8);
b.sendMsg("/logoFeedback/scale", 0.4);
b.sendMsg("/fadingLines/speed", 0.2);
b.sendMsg("/fadingLines/spread", 0.1.rrand(1), 0.1.rrand(1));
)

(
r.stop;
r.free;


r = Routine({
	inf.do({
		arg j;
		b.sendMsg("/color/bg/hsb", 0.5, 0.4, 0.5);

		b.sendMsg(
			"/color/fg/hsb",
			sin(j * 0.1).linlin(-1, 1, 0, 1),
			sin(j * 0.1).linlin(-1, 1, 0.2, 1),
			sin(j * 0.1).linlin(-1, 1, 0.2, 1)
		);
		b.sendMsg("/logoFeedback/scale", 0.1.rrand(1));
		b.sendMsg("/fadingLines/spread", 0.1.rrand(1), 0.1.rrand(1));
		b.sendMsg("/fadingLines/speed", 0.1.rrand(1));

		100.do({
			arg i;
			b.sendMsg(
				"/logoFeedback/offset",
				sin(i * 0.001).linlin(-1, 1, 0, 1),
				sin(1.2 * i * 0.001 + pi * 0.25).linlin(-1, 1, 0, 1)
			);
			0.1.yield;
		})
	})
}).play
)
