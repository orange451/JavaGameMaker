package net.mantagames.jgm.engine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.opengl.GL14;

import net.mantagames.jgm.engine.gl.Texture2D;

public class Surface {
	private int fboId;
	private int textureId;
	private int depthRenderId;
	private int width;
	private int height;
	
	protected boolean loaded;
	
	public Surface(int width, int height) {
		this.width = width;
		this.height = height;
		
		this.fboId = glGenFramebuffers();
		this.textureId = glGenTextures();
		this.depthRenderId = glGenRenderbuffers();
		
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        //glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
        
        // initialize color texture
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16, width, height, 0, GL_RGBA, GL_FLOAT, (java.nio.ByteBuffer)null);
        //glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH24_STENCIL8, width, height, 0, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, (java.nio.ByteBuffer)null);
        //glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, 24, 0);
        //glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, width, height, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, (java.nio.ByteBuffer)null);
        
        
        // Bind texture to FBO
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
 
        
        // initialize depth renderbuffer
        glBindRenderbuffer(GL_RENDERBUFFER, depthRenderId);
        glRenderbufferStorage(GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,GL_RENDERBUFFER, depthRenderId);  
		
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
		this.loaded = true;
	}
	
	public void activate() {
        glViewport (0, 0, width, height);
        
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
	}

	public Texture2D getTexture() {
		return new Texture2D(textureId, width, height);
	}

	public float getWidth() {
		return this.width;
	}
	
	public float getHeight() {
		return this.height;
	}

}
