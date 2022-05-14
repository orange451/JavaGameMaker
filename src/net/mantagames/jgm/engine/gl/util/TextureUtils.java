package net.mantagames.jgm.engine.gl.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.ImageIcon;

import net.mantagames.jgm.engine.gl.PixelHandler;
import net.mantagames.jgm.engine.gl.Texture2D;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class TextureUtils {
	private static HashMap<String, Texture2D> textures = new HashMap<String, Texture2D>();

    public static final int TEXTURE_UNITS = GL11.glGetInteger(GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);

    public static void setActiveUnit(int i) {
        if (i < 0 || i >= TEXTURE_UNITS) {
            throw new IllegalArgumentException("Texture unit " + i + " out of bounds. GL_MAX_TEXTURE_UNITS: " + TEXTURE_UNITS);
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
    }
    
	@SuppressWarnings("deprecation")
	public static BufferedImage loadBufferedImage(String ref) {
		URL url = TextureUtils.class.getClassLoader().getResource(ref);

		if (url == null) {
			try {
				url = new File(ref).toURL();
			} catch (MalformedURLException e) {
				return null;
			}
		}
		
		Image img = new ImageIcon(url).getImage();
		
		int wid = img.getWidth(null);
		int hei = img.getHeight(null);
		
		BufferedImage bufferedImage = new BufferedImage(wid, hei, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bufferedImage.getGraphics();
		g.drawImage(img, 0, 0, wid, hei, null);
		g.dispose();

		return bufferedImage;
	}
    
	public static Texture2D loadTexture(String filename) {
		if (textures.containsKey(filename))
			return textures.get(filename);
		
		BufferedImage img = TextureUtils.loadBufferedImage(filename);
		Texture2D texture = loadTexture(img);
		textures.put(filename, texture);
		
		return texture;
	}
	
	public static Texture2D loadTexture(BufferedImage img) {
		ByteBuffer data = convertImage(img, TextureUtils.PIXEL_HANDLER_RGBA);//convertImageData(img);
		int texId = GL11.glGenTextures();
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		
		Texture2D texture = new Texture2D(texId, img.getWidth(), img.getHeight());
		texture.bind();
		
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, img.getWidth(), img.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		
		texture.setBufferedImage(img);
		
		return texture;
	}
	
    public static final PixelHandler PIXEL_HANDLER_RGBA = new PixelHandler() {

        @Override
        public int getBytesPerPixel() {
            return 4;
        }
        
        @Override
        public void handlePixel(ByteBuffer b, int pixel) {
            b.put((byte) ((pixel >> 16) & 0xFF));     // Red component
            b.put((byte) ((pixel >> 8) & 0xFF));      // Green component
            b.put((byte) (pixel & 0xFF));
            b.put((byte) ((pixel >> 24) & 0xFF));     // Alpha component
        }
    };
    
    public static ByteBuffer convertImage(BufferedImage image, PixelHandler handler) {
        return convertImage(image, 0, 0, image.getWidth(), image.getHeight(), handler, null);
    }
    
    public static ByteBuffer convertImage(BufferedImage image, PixelHandler handler, ByteBuffer buffer) {
        return convertImage(image, 0, 0, image.getWidth(), image.getHeight(), handler, buffer);
    }
    
    public static ByteBuffer convertImage(BufferedImage image, int x, int y, int width, int height, PixelHandler handler) {
    	return convertImage(image, x, y, width, height, handler, null);
    }

    public static ByteBuffer convertImage(BufferedImage image, int x, int y, int width, int height, PixelHandler handler, ByteBuffer buffer) {
        int[] pixels = new int[width * height];
        image.getRGB(x, y, width, height, pixels, 0, width);
        
        if(buffer == null){
            buffer = BufferUtils.createByteBuffer(width * height * handler.getBytesPerPixel());
        }
        
        for(int yy = 0; yy < height; yy++){
            for(int xx = 0; xx < width; xx++){
                handler.handlePixel(buffer, pixels[yy * width + xx]);
            }
        }
        
        buffer.flip();
        return buffer;
    }
}