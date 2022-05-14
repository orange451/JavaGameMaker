package net.mantagames.jgm.engine.gl.shader;

import static org.lwjgl.opengl.EXTGeometryShader4.glProgramParameteriEXT;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.glBindFragDataLocation;

public class ShaderProgram {

    private int program;
    
    public ShaderProgram(){
        program = glCreateProgram();
    }

    public void attachShader(EngineShader shader){
        glAttachShader(program, shader.getID());
    }

    public void detachShader(EngineShader shader){
        glDetachShader(program, shader.getID());
    }
    
    public void bindFragDataLocation(int index, String name){
    	glBindFragDataLocation(program, index, name);
    }

    public void link(){
        glLinkProgram(program);
        String log = glGetProgramInfoLog(program, 65536);
        if(log.length() != 0){
            System.out.println("Program link log:\n" + log);
        }
    }

    public void bind(){
        glUseProgram(program);
    }

    public void delete(){
        glDeleteProgram(program);
    }

    public int getAttribLocation(String name){
        return getAttribLocation(name, true);
    }
    
    public int getAttribLocation(String name, boolean checkError){
    	int loc = glGetAttribLocation(program, name);
        if(checkError && loc == -1){
            System.err.println("SHADER ERROR: Attribute '" + name + "' does not exist in shader program " + this + "!");
        }
        return loc;
    }
    
    public int getUniformLocation(String name){
    	return getUniformLocation(name, true);
    }

    public int getUniformLocation(String name, boolean checkError){
        int loc = glGetUniformLocation(program, name);
        if(checkError && loc == -1){
            System.err.println("SHADER ERROR: Uniform '" + name + "' does not exist in shader program " + this + "!");
        }
        return loc;
    }

    public void programParameter(int parameter, int i){
        //Why is this method only core in GL 4.1 when geometry shaders are available in 3.2?!
        glProgramParameteriEXT(program, parameter, i);
    }
    
    public int getID(){
    	return program;
    }

    public static void useFixed(){
        glUseProgram(0);
    }
}