package net.mantagames.jgm.engine;

import org.lwjgl.opengl.GL11;

public class RenderProperties {
	protected static int filter_min = GameConstants.fl_mipmap_nearest;
	protected static int filter_mag = GameConstants.fl_none;
	protected static int filter_anisotropy = 1;
	protected static int filter_anisotropic_max = 1;
	protected static float alpha_threshold = 0;
	protected static boolean alpha_clip = false;
	protected static boolean is3dModeOn;
	protected static boolean isCullingOn;
	
	public static int getMinFilter() {
		return filter_min;
	}
	
	public static int getMagFilter() {
		return filter_mag;
	}
	
	public static int getAnisotropyFilter() {
		return filter_anisotropy;
	}
	
	public static int getAnisotropyMax() {
		return filter_anisotropic_max;
	}
	
	public static float getAlphaThreshold() {
		return alpha_threshold;
	}
	
	public static boolean getAlphaClipping() {
		return alpha_clip;
	}

	public static boolean isCullingEnabled() {
		return isCullingOn;
	}
	
	public static void set3dMode(boolean b) {
		is3dModeOn = b;
		
		if (b) {
	        GL11.glShadeModel(GL11.GL_SMOOTH);
	        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); 
	        GL11.glClearDepth(1.0);  
		}
	}

	public static void setCulling(boolean enable) {
		isCullingOn = enable;
		if (enable) {
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glCullFace(GL11.GL_BACK);
		} else {
			GL11.glDisable(GL11.GL_CULL_FACE);
		}
	}
}
