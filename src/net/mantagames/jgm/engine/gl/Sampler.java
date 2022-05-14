package net.mantagames.jgm.engine.gl;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LOD;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MIN_LOD;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_FUNC;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL33.glBindSampler;
import static org.lwjgl.opengl.GL33.glGenSamplers;
import static org.lwjgl.opengl.GL33.glSamplerParameterf;
import static org.lwjgl.opengl.GL33.glSamplerParameteri;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GLContext;

public class Sampler {

    private static boolean checkCapabilities = true;
    private static boolean anisotropySupported;
    private static int maxAnisotropy;
    

    private int id;

    /**
     * Creates a new OpenGL sampler object.
     * 
     * @throws RuntimeException if sampler objects are not supported
     */
    public Sampler(){
    	if(checkCapabilities){
    		ContextCapabilities cc = GLContext.getCapabilities();
    		if(!cc.OpenGL33 && !cc.GL_ARB_sampler_objects){
    			throw new RuntimeException("Sampler objects are not supported!");
    		}
    		
    		anisotropySupported = cc.GL_EXT_texture_filter_anisotropic;
    		if(anisotropySupported){
    			maxAnisotropy = glGetInteger(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
    		}
    		checkCapabilities = false;
    	}
        id = glGenSamplers();
    }

    public void setMinMagFilter(int minFilter, int magFilter){
        glSamplerParameteri(id, GL_TEXTURE_MIN_FILTER, minFilter);
        glSamplerParameteri(id, GL_TEXTURE_MAG_FILTER, magFilter);
    }

    public void setWrapModes(int filter){
        setWrapModes(filter, filter, filter);
    }

    public void setWrapModes(int s, int t){
        glSamplerParameteri(id, GL_TEXTURE_WRAP_S, s);
        glSamplerParameteri(id, GL_TEXTURE_WRAP_T, t);
    }

    public void setWrapModes(int s, int t, int r){
        glSamplerParameteri(id, GL_TEXTURE_WRAP_S, s);
        glSamplerParameteri(id, GL_TEXTURE_WRAP_T, t);
        glSamplerParameteri(id, GL_TEXTURE_WRAP_R, r);
    }
    
    public void setMinLod(float min){
        glSamplerParameterf(id, GL_TEXTURE_MIN_LOD, min);
    }
    
    public void setMaxLod(float max){
        glSamplerParameterf(id, GL_TEXTURE_MAX_LOD, max);
    }

    public void setMinMaxLOD(float min, float max){
    	setMinLod(min);
    	setMaxLod(max);
    }

    public void setLODBias(float bias){
        glSamplerParameterf(id, GL_TEXTURE_LOD_BIAS, bias);
    }

    /**
     * Sets the maximum level of anisotropy, if supported. The value is clamped between 0 and the implementation specific maximum value, usually 16.
     * 
     * @param anisotropy The requested maximum anisotropy
     */
    public void setAnisotropy(float anisotropy){
        if(anisotropySupported){
            glSamplerParameterf(id, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, Math.min(maxAnisotropy, Math.max(anisotropy, 1)));
        }
    }
    
    /**
     * Used for hardware shadow mapping.
     */
    public void setCompareFunc(int func){
    	glSamplerParameteri(id, GL_TEXTURE_COMPARE_FUNC, func);
    }

    /**
     * Used for hardware shadow mapping.
     */
    public void setCompareMode(int mode){
    	glSamplerParameteri(id, GL_TEXTURE_COMPARE_MODE, mode);
    }
    
    public int getID(){
    	return id;
    }

    /**
     * Binds this sampler to the given texture unit. The settings in the bound texture are ignored.
     * @param textureUnit
     */
    public void bind(int textureUnit){
        glBindSampler(textureUnit, id);
    }

    /**
     * Unbinds any sampler from the given texture unit and uses the settings in the bound texture instead.
     * (This is an illegal operation in OpenGL 3.3 without backwards compatibility.)
     * @param textureUnit
     */
    public static void unbind(int textureUnit){
        glBindSampler(textureUnit, 0);
    }
}