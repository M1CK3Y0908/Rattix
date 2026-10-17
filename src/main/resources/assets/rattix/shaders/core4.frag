precision highp float;
uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;

const float det=.005;
const float maxdist=50.;
float objcol, flo;
vec3 ldir=vec3(8.,-1.5,-1.);
const vec3 lcol=vec3(.7,.3,.2);
const vec3 ilcol=vec3(.6,.5,1.);


mat2 rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c,s,-s,c);
}

vec3 fractal(vec2 p) {
    float m=100., l=100.;
    vec2 c=vec2(100.);
    p*=.2;
    for (int i=0; i<5; i++) {
        p=abs(p+.75)-abs(p-.75)-p;
        p+=vec2(0.,2.);
        p=p*2.5/clamp(dot(p,p),.2,1.)-1.5;
        if (i>0) l=min(l,min(abs(p.x),abs(p.y)));
        m=min(m,length(p));
        c=min(c,abs(p));
    }
    l=exp(-6.*l)*pow(abs(.5-fract(m*.3+time))*2.,6.);
    c=normalize(exp(-1.*c));
    return l*vec3(c.x,length(p)*.015,c.y)*3.5;
}

vec3 rotate(vec3 p) {
    p.xz*=rot(time);
    p.yz*=rot(time*.05);
    return p;
}

float de_light(vec3 p) {
    return length(p)-1.5;
}


float de(vec3 p) {
    float op=smoothstep(.5,.7,sin(time*.5));
    //float op=1.;
    float r=-op*1.3;
    float w=cos(length(p.xz*2.)-time*5.)*.15*smoothstep(0.,3.,length(p.xz));
    w*=exp(-.1*length(p.xz));
    float f=p.y+4.+w-r;
    p=rotate(p);
    float c1=abs(p.x)-op;
    float c2=abs(p.y)-op;
    float c3=abs(p.z)-op;
    float c=min(min(c3,min(c1,c2)),length(p)-2.8+r);
    float s=length(p)-4.+r;
    float d=max(s,-c);
    p=fract(p*5.);
    float grid=min(abs(p.z),min(abs(p.x),abs(p.y)))*.5;
    d=min(d,f);
    objcol=max(step(c,d+.1),grid);
    flo=step(f,d);
    objcol=max(flo,objcol);
    return d;
}

vec3 normal(vec3 p) {
    vec3 e=vec3(0.,det*4.,0.);
    return normalize(vec3(de(p+e.yxx),de(p+e.xyx),de(p+e.xxy))-de(p));
}

vec3 triplanar(vec3 p, vec3 n) {
    p=rotate(p);
    return fractal(p.yz) * abs(n.x) + fractal(p.xz) * abs(n.y) + fractal(p.xy) * abs(n.z);
}

float shadow(vec3 p, vec3 dir) {
    float td=.0, sh=1., d=.2;
    for (int i=0; i<60; i++) {
        p-=d*dir;
        d=de(p);
        float dl=de_light(p);
        td+=min(d,dl);
        sh=min(sh,10.*d/td);
        if (sh<.9 || dl<1.) break;
    }
    return clamp(sh,.5,1.);
}


vec3 shade(vec3 p, vec3 n, vec3 dir) {
    float f=flo;
    float ocol=objcol;
    float sh=shadow(p,ldir);
    float ish=shadow(p,normalize(p));
    sh=max(sh,ish);
    float amb=max(0.,dot(dir,-n))*.1;
    float dif=max(0.,dot(ldir,-n))*sh;
    vec3 ref=reflect(ldir,-n);
    float spe=pow(max(0.,dot(dir,-ref)),50.);
    vec3 fcol=triplanar(p,n)*(1.-ocol);
    vec3 col=ocol*vec3(1.5)-f;
    vec3 ildir=normalize(p);
    float idif=max(0.,dot(ildir,-n))*exp(-.05*length(p));    
    vec3 lref=reflect(ildir,-n);
    float lspe=pow(max(0.,dot(dir,-lref)),30.)*ish;
    return max((amb+dif*lcol)*col+spe*lcol,idif*ilcol*ish+lspe)+fcol;
}


vec3 march(vec3 from, vec3 dir) {
    float d, dl, td=0., tdl=0., ref=0.;
    vec3 p=from, refrom=from, pl=p, col=vec3(0.), savecol=col, odir=dir;
    vec3 back=lcol*max(0.,1.-dir.y*3.)*.1;
    for (int i=0; i<80; i++) {
        p+=dir*d;
        pl+=dir*dl;
        d=de(p);
        td+=d;
        if (d<det && flo<.5) break;
        if (td>maxdist) break;
        if (d<det && flo>.5) {
            p-=det*dir*2.;
            vec3 n=normal(p);
            savecol=mix(shade(p,n,dir),back,td/maxdist);
            dir=reflect(dir,n);
            ref=.7;
            refrom=p;
        }
    }
    if (d<det) {
        p-=det*dir*2.;
        vec3 n=normal(p);
        col=shade(p,n,dir);
    } else {
        back+=pow(max(0.,dot(dir,normalize(-ldir))),100.)*lcol;
        back+=pow(max(0.,dot(dir,normalize(-ldir))),200.)*.5;
        col=back;
        td=maxdist;
    }
    float li1=pow(max(0.,dot(dir,-normalize(refrom))),120.);
    float li2=pow(max(0.,dot(odir,-normalize(from))),120.);
    float li=max(li1,li2);
    li*=step(length(from),distance(p,from));
    col=mix(col,savecol,ref)+li*ilcol*2.;
    
    float lmax=maxdist*.5;
    float st=lmax/float(70);
    li=0.;
    for (int i=0; i<70; i++) {
        p=from+odir*tdl;
        tdl+=st;
        if (tdl>td) break;
        li+=shadow(p, normalize(p))*exp(-.25*length(p));
    }
    
    return col+li*.035*ilcol;
}



void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    ldir=normalize(ldir);
    vec2 uv=(fragCoord-resolution.xy*.5)/resolution.y;
    vec3 dir=normalize(vec3(uv,1.+sin(10.+time*.5)*.3));
    vec3 from=vec3(0.,0.,-25.);
    from.xz*=rot(time*.2);
    dir.xz*=rot(time*.2);
    dir.x+=sin(time*.5)*.3;
    dir=normalize(dir);
    vec3 col = march(from, dir);
    fragColor = vec4(col,1.0);
}

void main(void)
{
    mainImage(gl_FragColor, gl_FragCoord.xy);
    gl_FragColor.a = 1.;
}
