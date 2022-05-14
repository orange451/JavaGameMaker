package net.mantagames.jgm.engine.gl.util;

import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.WritableVector3f;

public class MatrixUtils {
	
	private MatrixUtils(){}
	
	private static float coTangent(double d) {
		return (float)(1f / Math.tan(d));
	}
	
	public static Matrix4f createPerspectiveMatrix(Matrix4f m, float fov, float aspect, float znear, float zfar) {
		float fieldOfView = fov;
		float aspectRatio = aspect;
		float near_plane = znear;
		float far_plane = zfar;
		
		float y_scale = coTangent(Math.toRadians(fieldOfView / 2f));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = far_plane - near_plane;

		m.m00 = x_scale;
		m.m11 = y_scale;
		m.m22 = -((far_plane + near_plane) / frustum_length);
		m.m23 = -1;
		m.m32 = -((2 * near_plane * far_plane) / frustum_length);
		m.m33 = 0;
        
        return m;
    }
	
	public static Matrix4f createOrthographicMatrix(Matrix4f m, float left, float right, float top, float bot, float near, float far) {
    	float xDiff = right - left;
    	float yDiff = top - bot;
    	float zDiff = far - near;
    	
    	m.setIdentity();

    	m.m00 =  2 / xDiff;
    	m.m11 =  2 / yDiff;
    	m.m22 = -2 / zDiff;
    	
    	m.m30 = -(right + left) / xDiff;
    	m.m31 = -(top + bot) / yDiff;
    	m.m32 = -(far + near) / zDiff;
    	
    	return m;
	}
	
	public static void extract(Matrix4f matrix, Matrix3f result){
		result.m00 = matrix.m00;
		result.m01 = matrix.m01;
		result.m02 = matrix.m02;
        
		result.m10 = matrix.m10;
		result.m11 = matrix.m11;
		result.m12 = matrix.m12;
        
		result.m20 = matrix.m20;
        result.m21 = matrix.m21;
        result.m22 = matrix.m22;
	}
	
	public static Matrix3f createNormalMatrix(Matrix4f matrix, Matrix3f normalMatrix){
		
	    extract(matrix, normalMatrix);
	
	    normalMatrix.invert();
	
	    normalMatrix.transpose();
	
		return normalMatrix;
	}
	
	public static Matrix4f createLookAtMatrix(Matrix4f m, Vector3f eye, Vector3f at, Vector3f up){
	    Vector3f forward, side;
	    forward = Vector3f.sub(at, eye, null);
	    forward.normalise();
	    side = Vector3f.cross(forward, up, null);
	    side.normalise();
	    up = Vector3f.cross(side, forward, null);
	    
	    FloatBuffer buf = FloatBuffer.allocate(16);
	    buf.put(new float[]{
	    	side.x, up.x, -forward.x, 0,
            side.y, up.y, -forward.y, 0,
            side.z, up.z, -forward.z, 0,
            0, 0, 0, 1
	    });
	    buf.flip();
	    
	    m.load(buf);
	    eye.negate();
	    m.translate(eye);
	    
	    
	    return m;
	}
	
	public static void getScale(Matrix4f m, WritableVector3f result){
		result.set(
				(float)Math.sqrt(m.m00*m.m00 + m.m10*m.m10 + m.m20*m.m02),
				(float)Math.sqrt(m.m01*m.m01 + m.m11*m.m11 + m.m21*m.m21),
				(float)Math.sqrt(m.m02*m.m02 + m.m12*m.m12 + m.m22*m.m22)
		);
	}
	
	public static void store3x3(Matrix3f m, FloatBuffer buffer){
		buffer.
				put(m.m00).put(m.m10).put(m.m20).
				put(m.m01).put(m.m11).put(m.m21).
				put(m.m02).put(m.m12).put(m.m22);
	}
}