//////////////////////////////////////////////////////////
// Pitch & Amplitude Tracking -> MIDI NoteOn/NoteOff
//////////////////////////////////////////////////////////

(
// Configure MIDI
MIDIClient.init;  // initialize MIDI system
~midiOut = MIDIOut.new(2);

ServerOptions.inDevices;
Server.default.options.inDevice_("US-2x2");

s.waitForBoot {

	// Parameters
	~ampThreshold = 0.1;   // amplitude threshold
	~ampRelease = 0.05;
	~noteOnSent = false;    // track note state
	~currentNote = nil;     // last note sent

	SynthDef(\trackPitchAmp, {
		var in, amp, freq, hasFreq;
		in = SoundIn.ar(0);  // mono input from channel 0

		// Track amplitude
		amp = Amplitude.kr(in, 0.1, 1);

		// Track pitch
		# freq, hasFreq = Pitch.kr(in, ampThreshold: 0.1, median: 7);

		// Send data via Control Bus for monitoring
		SendReply.kr(Impulse.kr(100), '/track', [amp, freq, hasFreq]);
	}).add;

	// Start synth
	~tracker = Synth(\trackPitchAmp);

	// OSC listener
	OSCdef(\ampPitchTracker, { |msg|
		var amp = msg[3], freq = msg[4], hasFreq = msg[5];
		var note;

		if (hasFreq == 1) {
			note = freq.cpsmidi.round(1);  // convert freq to MIDI note
		} {
			note = nil;
		};

		// NoteOn condition: amp > threshold and no note playing yet
		if ((amp > ~ampThreshold) && ~noteOnSent.not && note.notNil) {
			~midiOut.noteOn(0, note.asInteger, 100);  // channel 0, velocity 100
			~noteOnSent = true;
			~currentNote = note.asInteger;
			("NoteOn: " ++ note).postln;
		};

		if ((amp > ~ampThreshold) && ~noteOnSent && note.notNil && ~currentNote.notNil) {
			if (~currentNote != note) {
				~midiOut.noteOff(0, ~currentNote);
				("NoteOff: " ++ ~currentNote).postln;
				~midiOut.noteOn(0, note.asInteger, 100);  // channel 0, velocity 100
				~noteOnSent = true;
				~currentNote = note.asInteger;
				("NoteOn: " ++ note).postln;
			}
		};

		// NoteOff condition: amp < threshold and note was playing
		if ((amp < (~ampThreshold - ~ampRelease)) && ~noteOnSent && ~currentNote.notNil) {
			~midiOut.noteOff(0, ~currentNote);
			~noteOnSent = false;
			~currentNote = nil;
			"NoteOff".postln;
		};

	}, '/track');
};
)
