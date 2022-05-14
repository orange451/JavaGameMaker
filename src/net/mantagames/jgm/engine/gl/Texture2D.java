package net.mantagames.jgm.engine.gl;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;

import java.awt.image.BufferedImage;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Texture2D {
    private int width, height;
    private int id;
    private BufferedImage image;
    private int unit;

    public Texture2D(int id, int width, int height){
    	this.id = id;
    	this.width = width;
    	this.height = height;
    	this.unit = GL13.GL_TEXTURE0;
    }
    
    public void bind() {
    	GL13.glActiveTexture(GL13.GL_TEXTURE0);
    	GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    public void unbind(){
        glBindTexture(0, 0);
    }
    
    public void unload(){
    	glDeleteTextures(id);
    	id = glGenTextures();
    	image.flush();
    	image = null;
    }
    
    public void delete(){
    	glDeleteTextures(id);
    }

    public int getID(){
        return id;
    }
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public void setBufferedImage(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getImage() {
		return image;
	}
}