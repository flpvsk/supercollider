(
s.freeAll;
s.options.numInputBusChannels  = 0;
// Server.default.options.inDevice_("");
// Server.default.options.sampleRate_(48000);
// s.inDevice_ = nil;
s.boot;

// MIDI
MIDIClient.init;
MIDIClient.destinations;
MIDIClient.sources;
MIDIIn.connectAll;
MIDIFunc.trace(false);


)

(
m.free;
m = MIDIOut.newByName("IAC Driver", "Bus 1");

// MIDIOut.connect;
MIDIdef.freeAll;
MIDIdef.noteOn(\on, {
	arg val, note, chan, src;
	[ chan, note, val ].postln;
	Array.series(4, 22, 1).do({
		arg i;
		(i != note).if({
			[i, "switch off"].postln;
			m.noteOn(1, i + 12, val);
		});
	});

	m.noteOn(1, note, val)
}, noteNum: Array.series(5, 22, 1), srcID: MIDIClient.sources[1].uid);

MIDIdef.noteOff(\off, {
	arg val, note, chan, src;
	m.noteOff(1, note, val)
}, noteNum: Array.series(5, 22, 1), srcID: MIDIClient.sources[1].uid);

)


m.noteOn(1, 25, 100);
// m.noteOn(0, 25, 100);

// (
// r.free
// r = Routine({
// 	inf.do({
// 		m.noteOn(0, 60, 60);
// 		m.control(0, 1, 60);
// 		1.yield;
// 	})
// }).play
// )

