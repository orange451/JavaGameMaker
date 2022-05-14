package net.mantagames.jgm.engine.gl;

public class MathHelper {
	
	private static float degtorad(float degrees) {
		return degrees*3.14159f/180f;
	}
	private static float radtodeg(float rad) {
		return rad*180f/3.14159f;
	}
	
	public static float lengthdir_x(float length, float angle){
		return (float) (Math.cos(degtorad(angle))*length);
	}
	public static float lengthdir_y(float length, float angle){
		return (float) (-Math.sin(degtorad(angle))*length);
	}
	public static float point_distance(float x1,float y1,float x2,float y2){
		return (float) Math.sqrt(((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)));
	}
	public static float point_distance_3d(float x1,float y1, float z1,float x2,float y2, float z2){
		return (float) Math.sqrt(((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)) + ((z1-z2)*(z1-z2)));
	}
	public static float point_direction(float x1,float y1,float x2,float y2){
		return radtodeg((float) (Math.atan2(( y1 - y2),-(x1 - x2))));
	}
}