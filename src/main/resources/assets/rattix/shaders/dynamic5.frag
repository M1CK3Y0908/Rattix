
precision highp float;

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;

void main( void ) {
	// mother fucking energon!
	vec2 p = 2.0*( gl_FragCoord.xy / resolution.xy ) -1.0;
	vec3 col = vec3(0);
	float t = time;
	col += vec3(1)*1.0/(1.0+60.0*abs(p.y+sin(p.x*4.0+t)*0.3)+abs(sin(p.x*2.3+t*1.4)*20.)); 
	t+=1.56;
	col += vec3(1)*1.0/(1.0+60.0*abs(p.y+sin(p.x*5.0+t)*0.3)+abs(sin(p.x*2.3+t*1.4)*20.)); 
	t+=3.134;
	col += vec3(1)*1.0/(1.0+60.0*abs(p.y+sin(p.x*6.0+t)*0.3)+abs(sin(p.x*2.3+t*1.4)*20.)); 
	//col += vec3(1)*1.0/(1.0+50.0*abs(p.y+sin(p.x*10.0+3.1415)*0.3)); 
	col.g -= col.r;	
	gl_FragColor = vec4(col, 1.0);

}