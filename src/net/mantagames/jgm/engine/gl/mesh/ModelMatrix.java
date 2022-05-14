package net.mantagames.jgm.engine.gl.mesh;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class ModelMatrix {
	
	Matrix4f modelMatrix = new Matrix4f();
	
	public void matrix_reset() {
		modelMatrix = new Matrix4f();
	}
	
	public void matrix_add_rotation_x(float a) {
		Matrix4f.rotate((float)Math.toRadians(a), new Vector3f(1, 0, 0), modelMatrix, modelMatrix);
	}
	
	public void matrix_add_rotation_y(float a) {
		Matrix4f.rotate((float)Math.toRadians(a), new Vector3f(0, 1, 0), modelMatrix, modelMatrix);
	}
	
	public void matrix_add_rotation_z(float a) {
		Matrix4f.rotate((float)Math.toRadians(a), new Vector3f(0, 0, 1), modelMatrix, modelMatrix);
	}
	
	public void matrix_add_translation(float x, float y, float z) {
		Matrix4f.translate(new Vector3f(x, y, z), modelMatrix, modelMatrix);
	}
	
	public void matrix_add_scaling(float x, float y, float z) {
		Matrix4f.scale(new Vector3f(x, y, z), modelMatrix, modelMatrix);
	}
	
	public void matrix_set_from_3x3(Matrix3f mat3) {
		Matrix4f mat4 = getMatrix4FromMatrix3(mat3);
		modelMatrix.m00 = mat4.m00;
		modelMatrix.m10 = mat4.m10;
		modelMatrix.m20 = mat4.m20;
		modelMatrix.m01 = mat4.m01;
		modelMatrix.m11 = mat4.m11;
		modelMatrix.m21 = mat4.m21;
		modelMatrix.m02 = mat4.m02;
		modelMatrix.m12 = mat4.m12;
		modelMatrix.m22 = mat4.m22;
		modelMatrix.m33 = 1;
	}
	
	public void matrix_set_from_4x4(Matrix4f mat4) {
		modelMatrix.load(mat4);
	}
	
	public Matrix4f matrix_get() {
		return modelMatrix;
	}
	
	public Matrix4f getMatrix4FromMatrix3(Matrix3f mat) {
		Matrix4f ret = new Matrix4f();
		ret.m00 = mat.m00;
		ret.m10 = mat.m10;
		ret.m20 = mat.m20;
		ret.m01 = mat.m01;
		ret.m11 = mat.m11;
		ret.m21 = mat.m21;
		ret.m02 = mat.m02;
		ret.m12 = mat.m12;
		ret.m22 = mat.m22;
		ret.m33 = 1;
		return ret;
	}
	
	public void matrix_add_from_3x3(Matrix3f mat3) {
		Matrix4f mat = this.getMatrix4FromMatrix3(mat3);
		matrix_add_from_4x4(mat);
	}
	
	public void matrix_set_identity() {
		this.modelMatrix.setIdentity();
	}
	
	public void matrix_add_from_4x4(Matrix4f mat4) {
		Matrix4f.mul(modelMatrix, mat4, modelMatrix);
	}
}
