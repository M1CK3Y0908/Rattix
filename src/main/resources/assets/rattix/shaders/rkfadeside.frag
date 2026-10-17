precision highp float;
uniform float time;
uniform vec2 resolution;
void main(void)
{
	vec2 r=resolution;
	vec2 u=gl_FragCoord.xy/r.y;
	vec2 v=u-r.xy/r.y/2.;
	v=vec2(v.x*abs(1./v.y),abs(1./v.y))+vec2(sin(time),time);
	float g=2.*max(abs((vec2(.5)-mod(v,vec2(1.))).x),abs((vec2(.5)-mod(v,vec2(1.))).y));
	
	vec3 c1 = vec3(mix(-1.,.9,u.y*2.),0.,.9) + vec3(1.2);
	float v1 = max(pow(g,sin(time*1.)*1.+5.),smoothstep(.93,.96,g)*2.)*(abs(u.y-.5)-.1)/.9;
	c1 *= v1;	//*2.0;
	
 	gl_FragColor=vec4(c1.xyz,1.0);
}