(
b = NetAddr.new("127.0.0.1", 8000);

b.sendMsg("/u_shape_mask", 0.6);
b.sendMsg("/u_fb_mask", 0.3);
b.sendMsg("/u_fb_mix", 0.3);
b.sendMsg("/u_fb_scale", 1.2, 0.8);
b.sendMsg("/u_fb_displace", 0.001, 0.2);
)

(
r.stop;
r.free;

b.sendMsg("/u_dx", 0.001);
b.sendMsg("/u_dy", -0.01);
b.sendMsg("/u_dotsize", 0.8);
b.sendMsg("/u_opacity", 0.999);
b.sendMsg("/u_blend", 0.0);
b.sendMsg("/u_h", 0.6);
b.sendMsg("/u_s", 0.6);
b.sendMsg("/u_l", 1.0);



r = Routine({
	inf.do({
		arg j;
		~mask = 0.rrand(0.5);

		b.sendMsg("/u_h", 0.rrand(1.0));
		b.sendMsg("/u_s", 0.2.exprand(1.0));
		b.sendMsg("/u_l", 0.8.rrand(1.0));
		b.sendMsg("/u_dotsize", 0.3.rrand(1));
		b.sendMsg("/u_noise_intensity", 0.rrand(0.8));
		b.sendMsg("/u_noise_density", 0.rrand(0.9));

		b.sendMsg("/u_shape_mask", ~mask);
		b.sendMsg("/u_fb_mask", ~mask - 0.rrand(~mask));
		b.sendMsg("/u_fb_mix", 0.rrand(0.5));
		b.sendMsg("/u_fb_scale", 0.99.rrand(1.1), 0.99.rrand(1.1));
		b.sendMsg("/u_fb_displace", 0.rrand(0.02), 0.rrand(0.02));

		10.do({
			arg i;

			b.sendMsg(
				"/u_dx",
				sin(i * 0.01) * 0.1,
			);

			b.sendMsg(
				"/u_dy",
				sin(i * j * 0.0001) * 0.1,
			);

			1.yield;
		})
	})
}).play
)
