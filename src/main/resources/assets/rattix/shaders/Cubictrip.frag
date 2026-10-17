#extension GL_OES_standard_derivatives : enable

precision highp float;

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;

// Cole Peterson (Plento)

#define rot(a) mat2(cos(a), -sin(a), sin(a), cos(a))

float rbox( vec3 p, vec3 b, float r ){
  vec3 q = abs(p) - b;
  return length(max(q,0.0)) + min(max(q.x,max(q.y,q.z)),0.0) - r;
}

// Dave Hoshkin 
float hash13(vec3 p3){
	p3  = fract(p3 * .1031);
    p3 += dot(p3, p3.zyx + 31.32);
    return fract((p3.x + p3.y) * p3.z);
}

vec3 hash33(vec3 p3){
	p3 = fract(p3 * vec3(.1031, .1030, .0973));
    p3 += dot(p3, p3.yxz+33.33);
    return fract((p3.xxy + p3.yxx)*p3.zyx);

}


#define b vec3(3.25, 3.25, 3.25)

vec3 getCell(vec3 p){
    return floor(p / b);
}

vec3 getCellCoord(vec3 p){

	return mod(p, b) - b*.5;	
}

float map(vec3 p){
    //vec3 id = getCell(p);
    p = getCellCoord(p);
    
    //float rnd = 1.*hash13(id*663.) - 1.;
    
    //p.xz *= rot(rnd*time*.3 + rnd);
    //p.xy *= rot(rnd*time*.3 + rnd);
    //p.yz *= rot(p.x*(5.+rnd*10.));

    return rbox(p, vec3(0.5, .5, .5), .1);
}

vec3 normal( in vec3 pos ){
    vec2 e = vec2(0.001, -0.001);
    return normalize(
        e.xyy * map(pos + e.xyy) + 
        e.yyx * map(pos + e.yyx) + 
        e.yxy * map(pos + e.yxy) + 
        e.xxx * map(pos + e.xxx));
}

vec3 color(vec3 ro, vec3 rd, vec3 n, float t){
    vec3 p = ro + rd*t;
    vec3 lp = ro + vec3(.0, .0, 1.7);
    
    //if(iMouse.z>0.) lp.z += m.y*14.;
    
    vec3 ld = normalize(lp-p);
    float dd = length(p - lp);
    float dif = max(dot(n, ld), .1);
    float fal = 1.5 / dd;
    float spec = pow(max(dot( reflect(-ld, n), -rd), 0.), 31.);

    vec3 id = getCell(p);
    vec3 objCol = vec3(hash33(id*555.).x/1.33, 0.0, hash33(id*555.).y*1.67);
    
    
    objCol *= (dif + .2);
    objCol += spec * 0.6;
    objCol *= fal;
    
    return objCol;
}


void main( void ){
    vec2 uv = vec2(gl_FragCoord.xy - 0.5*resolution.xy)/resolution.y;
    vec3 rd = normalize(vec3(uv, 0.8));
    vec3 ro = vec3(0., 7.0, 4.+time);
    //rd.xy*=rot(-time*.1 + .5);
    //ro.zy += time;
    ro.x += mouse.x * 9.;
    ro.y += mouse.y * 9.;
    
    int nHits = 0;
    float d = 0.0, t = 0.0, ns = 0.;
    vec3 p, n, col = vec3(0);
    
    for(int i = 0; i < 120; i++){
    	d = map(ro + rd*t); 
        
        if(nHits >= 13 || t >= 40.) break;
        
        if(abs(d) < .001){
            p = ro + rd*t;
            n = normal(p);
            
            if(d > 0. && nHits == 0) rd = refract(rd, n, 1./1.053);  // use the inverse of refraction index here ;)
            
            col += color(ro, rd, n, t) * 1.25;
            
            nHits++;
            t += .1;
        }
        t += abs(d) * 0.8;
        
        if(nHits == 0) ns++;
    }
    
    //col /= float(nHits)*.6;
    //col *= smoothstep(.5, .3, ns * .01);
    //col = 1.-exp(-col);
    float fog = 0.005;
    col *= 1.25 / (1.0 + d * d * fog);
    gl_FragColor = vec4(col, 1.0);
}










