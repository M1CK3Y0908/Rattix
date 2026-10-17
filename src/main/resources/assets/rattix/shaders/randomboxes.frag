#extension GL_OES_standard_derivatives : enable

precision highp float;

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;

const float pi = acos(-1.);

float vibe(float x){
	return clamp(x, .9, 1.);
}

float box(vec3 p, vec3 b){
	vec3 q = abs(p) - b;
	return length(max(q, 0.)) - min(max(q.x, max(q.y, q.z)), 0.);
}

mat2 rot(float a){
	return mat2(cos(a), sin(a), -sin(a), cos(a));
}

float gTime = 5. * time;

float map(vec3 p){
	//gTime = 0.;

	vec3 q = p;
	//q.xy *= rot(time);
	vec3 q2 = q;
	float d = 1e5;
	q = mod(q, 2.) - 1.;
	float s = 0.;
	float st = fract(time)+1.;
	st /= .9;
//	q2 = mod(q2, 1.6*st) - .8*st;
	for(int i=0; i<1; i++){
//		float v = (abs(floor(sin(gTime)))+.3*abs(sin(gTime)+1.));
		float v = (abs(floor(sin(5.)))+.3);
		//v = 1.;
		d = min(box(q, vec3(.35) * v), d);

		float dl1 = box(q2, vec3(3., .01, .01));
		float dl2 = box(q2, vec3(.01, 3., .01));
		float dl3 = box(q2, vec3(.01, .01, 3.));

		d = min(d, min(dl1, min(dl2, dl3)));
	}
	return d;
}

void raymarch(vec3 ro, vec3 rd, inout vec3 col){
	float d, t = 0.;
	vec3 pos = ro;
	float ac = 0.;
	for(int i=0; i<100; i++){
		pos = ro + rd * d;
		t = map(pos);
//		t = max(t, 0.005);
		d += t;
		ac += 0.2*exp(-.7*d);
		if(t < 0.01){
			break;
		}
	}


	col += vec3(2.2, 1.0, 0.5) * ac * .2;
	col *= 3.;
}

void pvNoise(vec2 p, inout vec3 col){

	vec2 uv = cos(p*pi);

	uv = sin(uv)+sin(time);
	float y = uv.y;
	y = pow(1. - abs(y), 30. * vibe(sin(time*100.)));
	col += vec3(y) * vec3(1., 0., 0.);
}

void main( void ) {

	vec2 p = ( gl_FragCoord.xy * 2. - resolution.xy ) / min(resolution.x, resolution.y);
	//gTime = 0.;

	vec3 ro = vec3(time, .0, 1.0), rd = normalize(cross(normalize(vec3(0.)-ro),vec3(0.,1.,0.))*p.x + vec3(0.,1.,0.)*p.y + normalize(vec3(0.)-ro)*2.);
	vec3 col = vec3(0.);

	raymarch(ro, rd, col);

	gl_FragColor = vec4(col, 0.6);

}