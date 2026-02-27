(
    SynthDef(\RmOctaver, { |out|
        var in, freq, hasFreq;
        in = SoundIn.ar(0);
        # freq, hasFreq = Pitch.kr(in);
        Out.ar(out, SinOsc.ar(freq: freq * 0.5) * in + in);
    }).add;
)

Synth.new(\RmOctaver);

ServerOptions.inDevices;
Server.default.options.inDevice_("US-2x2")

s.freeAll

MIDIClient.init;
MIDIClient.destinations;
m = MIDIOut.new(2);
m.noteOn(0, 60, 64);
m.noteOff(0, 60);

s.reboot;
{ var source = SoundIn.ar(0); var env = EnvDetect.ar(source * 100, 0.999); [source, env] }.scope

