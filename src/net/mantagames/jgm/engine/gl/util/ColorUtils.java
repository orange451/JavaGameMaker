package net.mantagames.jgm.engine.gl.util;

import java.awt.Color;

import net.mantagames.jgm.engine.gl.Vector;

public class ColorUtils {
	public static Color blend (Color color1, Color color2, double ratio) {
		float r  = (float) ratio;
		float ir = (float) 1.0 - r;

		float rgb1[] = new float[3];
		float rgb2[] = new float[3];    

		color1.getColorComponents (rgb1);
		color2.getColorComponents (rgb2);
		
		float nr = (rgb1[0] * r) + (rgb2[0] * ir);
		float ng = (rgb1[1] * r) + (rgb2[1] * ir);
		float nb = (rgb1[2] * r) + (rgb2[2] * ir);
		
		if (nr < 0)
			nr = 0;
		if (ng < 0)
			ng = 0;
		if (nb < 0)
			nb = 0;
		if (nr > 1)
			nr = 1;
		if (ng > 1)
			ng = 1;
		if (nb > 1)
			nb = 1;
		
		Color color = new Color (nr, ng, nb);

		return color;
	}
	
	public static Vector getVectorFromColor(Color c) {
		float cc[] = new float[3];    
		c.getColorComponents (cc);
		return new Vector(cc[0], cc[1], cc[2]);
	}	
}
