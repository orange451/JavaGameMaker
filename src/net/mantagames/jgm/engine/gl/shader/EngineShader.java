package net.mantagames.jgm.engine.gl.shader;

import net.mantagames.jgm.engine.gl.util.FileUtils;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

public class EngineShader {

    public static final int VERTEX_SHADER = GL20.GL_VERTEX_SHADER;
    public static final int FRAGMENT_SHADER = GL20.GL_FRAGMENT_SHADER;
    public static final int GEOMETRY_SHADER = GL32.GL_GEOMETRY_SHADER;

    private int shaderID;

    public EngineShader(int type, FileUtils file) {
        initialize(type, loadFileSource(file));
    }

    public EngineShader(int type, String source) {
        initialize(type, source);
    }
    
    private void initialize(int type, String source) {
        shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, source);
        GL20.glCompileShader(shaderID);

        String errorLog = GL20.glGetShaderInfoLog(shaderID, 65536);
        if(errorLog.length() != 0){
            System.out.println("\nCompiling shader " + source + "\nShader compile log: \n" + errorLog);
        }
    }

    private static String loadFileSource(FileUtils file){
    	if (file == null)
    		return "";
        StringBuilder source = new StringBuilder();
        String line;
        
        while ((line = FileUtils.file_text_read_line(file)) != null) {
        	source.append(line).append('\n');
        }
        
        FileUtils.file_text_close(file);
        return source.toString();
    }

    public void delete(){
        GL20.glDeleteShader(shaderID);
    }

    public int getID(){
        return shaderID;
    }
}
