precision mediump float;
uniform float time; // uh
uniform vec2  resolution; // resolution

void main(void){
	vec3 destColor = vec3(0.5, 0.2, 0.1);
	vec2 p = (gl_FragCoord.xy * 2.0 - resolution) / min(resolution.x, resolution.y); 	
	float a = atan(p.y / p.x) * 250.0; // Instead of * 2.0, try * 26 or * 128 and higher
	float l = 0.05 / abs(length(p) - 1.2 + atan(a + time * 12.0) * 0.1);
	destColor *= 1.9+ sin(a + time * 00.13) * 0.03;
	
	vec3 destColor2 = vec3(0.2, 0.2, 0.2);
	vec2 p2 = (gl_FragCoord.xy * 9.0 - resolution) / min(resolution.x, resolution.y); 
	float a2 = atan(p.y / p.x) * 3.0;
	float l2 = 0.05 / abs(length(p) + 0.1 - (tan(time/2.)+0.5) + sin(a + time * 13.5) * (0.1 * l));
	destColor2 *= ( 0.05 + tan(a + time * 00.05) * 0.03 ) * 4.0;
	
	vec3 destColor3 = vec3(0.2, 0.2, 0.2);
	vec2 p3 = (gl_FragCoord.xy * 2.0 - resolution) / min(resolution.x, resolution.y); 
	float a3 = atan(p.y / p.x) * 10.0;
	float l3 = 0.05 / abs(length(p) - 0.4 + sin(a + time * 1.5) * (0.1 * l2));
	destColor3 *= 0.15 + sin(a + time * 1000.0) * 0.1;
	
	gl_FragColor = vec4(l*destColor + l2*destColor2 + l3*destColor3, 1.0);
}