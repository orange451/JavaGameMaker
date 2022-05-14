package net.mantagames.jgm.engine;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.mantagames.jgm.engine.gl.font.GLFont;
import net.mantagames.jgm.engine.gl.mesh.VBOModel;


public class GameConstants {
	// These are the exact color values from Game Maker. Because Game Maker colors switch the R and B channels, we need to convert them.
	public static int c_black   = convertGameMakerColorToJavaColor(0);
	public static int c_red     = convertGameMakerColorToJavaColor(255);
	public static int c_aqua    = convertGameMakerColorToJavaColor(16776960);
	public static int c_blue    = convertGameMakerColorToJavaColor(16711680);
	public static int c_gray    = convertGameMakerColorToJavaColor(8421504);
	public static int c_lime    = convertGameMakerColorToJavaColor(65280);
	public static int c_green   = convertGameMakerColorToJavaColor(32768);
	public static int c_maroon  = convertGameMakerColorToJavaColor(128);
	public static int c_navy    = convertGameMakerColorToJavaColor(8388608);
	public static int c_olive   = convertGameMakerColorToJavaColor(32896);
	public static int c_orange  = convertGameMakerColorToJavaColor(4235519);
	public static int c_purple  = convertGameMakerColorToJavaColor(8388736);
	public static int c_yellow  = convertGameMakerColorToJavaColor(65535);
	public static int c_teal    = convertGameMakerColorToJavaColor(8421376);
	public static int c_dkgray  = convertGameMakerColorToJavaColor(4210752);
	public static int c_white   = convertGameMakerColorToJavaColor(16777215);
	public static int c_silver  = convertGameMakerColorToJavaColor(12632256);
	public static int c_ltgray  = convertGameMakerColorToJavaColor(12632256);
	public static int c_fuchsia = convertGameMakerColorToJavaColor(16711935);
	
	public static int fa_left   = GLFont.ALIGN_LEFT;
	public static int fa_center = GLFont.ALIGN_CENTER;
	public static int fa_right  = GLFont.ALIGN_RIGHT;
	
	public static int fa_top    = GLFont.ALIGN_TOP;
	public static int fa_middle = GLFont.ALIGN_CENTER;
	public static int fa_bottom = GLFont.ALIGN_BOTTOM;
	
	public static int mb_left   = 0;
	public static int mb_middle = 2;
	public static int mb_right  = 1;
	
	public static int fl_mipmap_nearest = GL11.GL_NEAREST_MIPMAP_NEAREST;
	public static int fl_mipmap_linear  = GL11.GL_LINEAR_MIPMAP_LINEAR;
	public static int fl_none           = GL11.GL_NEAREST;
	public static int fl_linear         = GL11.GL_LINEAR;
	
	public static final VBOModel quad = new VBOModel();
	public static final VBOModel quadInvert = new VBOModel();
	
	// This method flips the R and B channels, to correct for gamemakers invert.
	private static int convertGameMakerColorToJavaColor(int color) {
		Color c = new Color(color);
		Color c2 = new Color(c.getBlue(), c.getGreen(), c.getRed());
		
		return c2.getRGB();
	}
	
	static {
		quad.createQuad(0, 0, 1, 1);
		quadInvert.createQuadExt(0, 0, 0, 1, 1, 0, 0, 1, 1, 0);
	}
}
