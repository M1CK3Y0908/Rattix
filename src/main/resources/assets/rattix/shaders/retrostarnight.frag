precision highp float;

uniform vec2 resolution;
uniform float time;
uniform vec2 mouse;
uniform sampler2D backbuffer;

float grid(vec2 uv)
{
    vec2 size = vec2(uv.y, uv.y * uv.y * 0.2) * 0.01;
    uv += vec2(0.0, time * 0.3925 * (1.05));
    uv = abs(fract(uv) - 0.5);
 	vec2 lines = smoothstep(size, vec2(0.0), uv);
 	lines += smoothstep(size * 5.0, vec2(0.0), uv) * 0.3;
    return clamp(lines.x + lines.y, 0.0, 3.0);
}

float random (float st) {
    return fract(sin(dot(vec2(st,st+1.), vec2(12.9898,78.233)))* 43758.5453123);
}

float point(vec2 uv,float i)
{   
    vec2 p;
    p.x = uv.x+(random(i)-0.5)*2.0*(resolution.x/resolution.y);
    p.y = uv.y+(random(i+1.)-1.0);
    
	float t = (0.0023-(random(i+2.)*0.0003)-((1.-uv.y)*0.0032))/ length(p);
    t = pow(t, 5.0);
    
    
    return t;
}

float dot2(in vec2 v ) { return dot(v,v); }

void main(void) {
    vec2 uv = (2.0 * gl_FragCoord.xy - resolution.xy)/resolution.y;
    
    // Grid
    vec3 col = vec3(0.0, 0.0, 0.0);
    if (uv.y < -0.)
    {
        uv.y = 2.0 / (abs(uv.y + 0.0) + 0.05);
        uv.x *= uv.y * 1.0;
        float gridVal = grid(uv);
        col = mix(col, vec3(0.0, 0.5, 1.0), gridVal);
    }
    else
    {
        float c=0.0;
        for (float i = 0.;i <= 500.;i++) {
            c+=point(uv,i);
        }        
        col = (vec3(0.1,0.55,1.0))*c*60.0;
    }

    gl_FragColor = vec4(col,1.0);
}