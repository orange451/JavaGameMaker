out vec3 vNormal;
out vec3 vPosition;
out vec2 vTexCoords;
out vec4 vColour;

void main() {
	vPosition = vec3(MATRIX_VIEW * MATRIX_OBJECT_POSITION) * in_Position;
	vNormal = MATRIX_NORMAL * in_Normal;
	
	vTexCoords = in_TextureCoord;
	vColour = in_Colour * VECTOR_OBJECT_COLOUR;
	
	// Set the correct vertex position
	gl_Position = MATRIX_PROJECTION_VIEW * MATRIX_OBJECT_POSITION * vec4(in_Position, 1.0);
}