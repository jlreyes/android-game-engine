uniform mat4 u_MVPMatrix;

attribute vec2 a_Position;
attribute vec2 a_TexCoordinate;

varying vec2 v_TexCoordinate;

void main() {
    v_TexCoordinate = vec2(a_TexCoordinate.x, 1.0 - a_TexCoordinate.y);
    vec4 position = vec4(0.0 - a_Position.x, a_Position.y, 0.0, 1.0);
    gl_Position = u_MVPMatrix * position;
}