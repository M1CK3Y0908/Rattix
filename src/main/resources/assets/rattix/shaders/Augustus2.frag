precision highp float;

// todo: uniform?
const int OCTAVES = 4;

// currently, i use glsl for displaying shaders. when i get shaders working in the mod,
// these uniforms need to be replaced with the uniforms provided by the game.
uniform vec2 resolution;
uniform float time;

float noise(vec2 pos) {
    vec2 pos_floor = floor(pos);
    vec2 pos_fract = fract(pos);

    float x = fract(sin(pos_floor.x) + cos(pos_floor.y));
    float y = fract(sin(pos_floor.x + 1.0) + cos(pos_floor.y));
    float z = fract(sin(pos_floor.x) + cos(pos_floor.y + 1.0));
    float w = fract(sin(pos_floor.x + 1.0) + cos(pos_floor.y + 1.0));

    vec2 p = pow(pos_fract, vec2(3.0));

    return mix(x, y, p.x) + (z - x) * p.y * (1.0 - p.x) + (w - y) * p.x * p.y;
}

float fbm(vec2 position) {
    float value = 0.0;
    float noise_multipier = 0.5;

    mat2 rotation = mat2(cos(0.15), sin(0.15), -sin(0.25), cos(0.5));

    for (int i = 0; i < OCTAVES; ++i) {
        value += noise_multipier * noise(position);
        position = rotation * position * 2.0;
        noise_multipier *= 0.5;
    }

    return value;
}

float snow(vec2 uv, float scale, float density, float intensity) {
    float gradient = smoothstep(1.0, 0.0, - uv.y * scale / 10.0);
    float scaled_time = time * 0.5 / scale;

    uv.y += scaled_time * 2.0;
    uv.x += cos(uv.y + time * 0.5) / scale - scaled_time;
    uv *= scale * density;

    vec2 uv_floor = floor(uv);
    vec2 uv_fract = fract(uv);

    mat2 scattering_matrix = mat2(7, 3, 6, 8);
    vec2 p_DEOBF = 0.5 + 0.35 * sin(360.0 * fract(sin((uv_floor + scale) * scattering_matrix))) - uv_fract;
    float brightness = min(length(p_DEOBF), 3.0);
    brightness = smoothstep(0.0, brightness, sin(uv_fract.x + uv_fract.y) / 100.0);

    return brightness * gradient * intensity;
}

vec3 clouds(vec2 uv, float wind_speed, vec3 color) {
    vec2 p = vec2(fbm(uv + wind_speed * time), fbm(uv + 1.0));
    vec2 q = vec2(fbm(uv * p + 0.15 * time));
    float f = fbm(p + q);

    float grayscale = clamp(pow(f, 2.0) * 2.0, 0.0, 1.0);
    return grayscale * color;
}

void main(void) {
    float shader_size = mix(min(resolution.x, resolution.y), max(resolution.x, resolution.y), 0.5);
    vec2 shader_uv = (gl_FragCoord.xy * 2.0 - resolution.xy) / shader_size;

    vec3 cloud_color = vec3(ivec3(255, 25, 148)) / 255.0;
    vec3 color = clouds(shader_uv, 0.5, cloud_color);

    color += snow(shader_uv, 30.0, 2.0, 0.3);
    color += snow(shader_uv, 20.0, 1.0, 0.5);
    color += snow(shader_uv, 15.0, 1.0, 0.8);

    color += snow(shader_uv, 10.0, 1.0, 1.0);
    color += snow(shader_uv, 9.0, 1.0, 1.0);
    color += snow(shader_uv, 8.0, 1.0, 1.0);
    color += snow(shader_uv, 7.0, 1.0, 1.0);
    color += snow(shader_uv, 6.0, 1.0, 1.0);
    color += snow(shader_uv, 5.0, 1.0, 1.0);
    color += snow(shader_uv, 4.0, 1.0, 1.0);
    color += snow(shader_uv, 3.0, 1.0, 1.0);

    gl_FragColor = vec4(color, 1.0);
}
