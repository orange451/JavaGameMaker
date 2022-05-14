package net.mantagames.jgm.engine.gl.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.Display;

public class IconUtil {
	public static ByteBuffer loadIcon(BufferedImage imageFromFile, int width, int height) {
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    image.getGraphics().drawImage(imageFromFile, 0, 0, width, height, null);

	    // convert image to byte array
	    byte[] imageBytes = new byte[width * height * 4];
	    for (int i = 0; i < height; i++) {
	        for (int j = 0; j < width; j++) {
	            int pixel = image.getRGB(j, i);
	            for (int k = 0; k < 3; k++) // red, green, blue
	                imageBytes[(i*16+j)*4 + k] = (byte)(((pixel>>(2-k)*8))&255);
	            imageBytes[(i*16+j)*4 + 3] = (byte)(((pixel>>(3)*8))&255); // alpha
	        }
	    }
	    return ByteBuffer.wrap(imageBytes);
	}
	
	public static void setIcon(BufferedImage imageFromFile) {
	    try {
	    	System.out.println("SETTING ICON");
	        Class util = Class.forName("com.apple.eawt.Application");
	        Method getApplication = util.getMethod("getApplication", new Class[0]);
	        Object application = getApplication.invoke(util);
	        Class params[] = new Class[1];
	        params[0] = Image.class;
	        Method setDockIconImage = util.getMethod("setDockIconImage", params);
	        setDockIconImage.invoke(application, imageFromFile);
	    } catch (Exception e) {
	    	//e.printStackTrace();
	    }
	    
		ByteBuffer[] icons = new ByteBuffer[3];
	    icons[0] = loadIcon(imageFromFile, 16, 16);
	    icons[1] = loadIcon(imageFromFile, 32, 32);
	    icons[2] = loadIcon(imageFromFile, 64, 64);
	    Display.setIcon(icons);
	}
	
	public static void setIcon(String filePath) {
		BufferedImage imageFromFile = TextureUtils.loadBufferedImage(filePath);
		setIcon(imageFromFile);
	}
}
