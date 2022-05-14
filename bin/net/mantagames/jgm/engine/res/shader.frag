in vec3 vNormal;
in vec2 vTexCoords;
in vec4 vColour;
in vec3 vPosition;

void main() {
	vec3 normal = normalize(vNormal);
	vec3 temp = vPosition;
	
	vec4 textureSample = texture(sampler, vTexCoords);
	if(textureSample.a <= uAlphaThreshold + 0.05){
		discard;
	}else if (uAlphaClipping == 1.0) {
		textureSample.a = 1.0;
	}
	out_Colour = textureSample * vColour;
}