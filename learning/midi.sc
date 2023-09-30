(
s.boot;
)

(
MIDIClient.init;
MIDIIn.connectAll;
)

// MIDIFunc.trace(false);

(
var musicThingFaders = Array.series(41 - 34 + 1, 34, 1);
var musicThingAcc = Array.series(49 - 42 + 1, 42, 1);
MIDIdef.cc(\cc, {
	arg val, num, chan, src;
	[val, num, chan, src].postln;
}, musicThingFaders, 0).permanent_(true);
)

// MIDIdef(\cc).disable;
// MIDIdef.freeAll;