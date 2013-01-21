precision mediump float;

uniform sampler2D u_RGBTexture;
uniform sampler2D u_ATexture;

varying vec2 v_TexCoordinate;

void main() {
     vec4 rgbTexel = texture2D(u_RGBTexture, v_TexCoordinate);
     vec4 aTexel = texture2D(u_ATexture, v_TexCoordinate);
     vec4 texel = vec4(rgbTexel.rgb, aTexel.a);
     gl_FragColor = texel;
}