/*
 * Original shader from: https://www.shadertoy.com/view/7lfXRB
 */

#ifdef GL_ES
precision mediump float;
#endif

// glslsandbox uniforms
uniform float time;
uniform vec2 resolution;

// shadertoy emulation
#define iTime time
#define iResolution resolution

// --------[ Original ShaderToy begins here ]---------- //
precision highp float;

float rand(vec2 coords)
{
	return fract(sin(dot(coords, vec2(56.3456,78.3456)) * 5.0) * 10000.0);
}

float noise(vec2 coords)
{
	vec2 i = floor(coords);
	vec2 f = fract(coords);

	float a = rand(i);
	float b = rand(i + vec2(1.0, 0.0));
	float c = rand(i + vec2(0.0, 1.0));
	float d = rand(i + vec2(1.0, 1.0));

	vec2 cubic = f * f * (3.0 - 2.0 * f);

	return mix(a, b, cubic.x) + (c - a) * cubic.y * (1.0 - cubic.x) + (d - b) * cubic.x * cubic.y;
}

float fbm(vec2 coords)
{
	float value = 0.0;
	float scale = 0.5;

	for (int i = 0; i < 10; i++)
	{
		value += noise(coords) * scale;
		coords *= 4.0;
		scale *= 0.5;
	}

	return value;
}


void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 uv = fragCoord.xy / iResolution.y * 2.0;
 
	float final = 0.0;
    
    for (int i =1; i < 6; i++)
    {
        vec2 motion = vec2(fbm(uv + iTime * 0.05 + vec2(i)));

        final += fbm(uv + motion);

    }
    
    final /= 5.0;
	fragColor = vec4(mix(vec3(-0.3), vec3(0.45, 0.4, 0.6) + vec3(0.6), final), 1);
}
// --------[ Original ShaderToy ends here ]---------- //

void main(void)
{
    mainImage(gl_FragColor, gl_FragCoord.xy);
}