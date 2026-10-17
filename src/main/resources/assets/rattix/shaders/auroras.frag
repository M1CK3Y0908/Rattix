#ifdef GL_ES
precision mediump float;
#endif

#extension GL_OES_standard_derivatives : enable

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;

#define color gl_FragColor

void main(void)
{
    color = vec4(0.5, 1.0, 1.0, 1.0);
}