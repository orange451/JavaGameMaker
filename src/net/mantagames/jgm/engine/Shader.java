package net.mantagames.jgm.engine;

import static org.lwjgl.opengl.GL20.glUniformMatrix3;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;

import java.nio.FloatBuffer;

import net.mantagames.jgm.engine.gl.shader.EngineShader;
import net.mantagames.jgm.engine.gl.shader.ShaderProgram;
import net.mantagames.jgm.engine.gl.util.FileUtils;
import net.mantagames.jgm.engine.gl.util.MatrixUtils;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Shader implements GameResourceLoader {
	protected String[] fragmentShaders;
	protected String[] vertexShaders;
	
	public boolean loaded;
	
	private ShaderProgram program;
	private FloatBuffer matrixBuffer;
	
	public int matrix_view_location;
	public int matrix_projection_location;
	public int matrix_projection_view_location;
	public int matrix_normal_location;
	public int matrix_object_position_location;
	public int matrix_object_normal_location;
	public int vector_object_colour_location;
	
	protected Matrix4f matrix_view;
	protected Matrix4f matrix_projection;
	protected Matrix4f matrix_projection_view;
	protected Matrix4f matrix_object_position;
	protected Vector4f vector_object_colour;
	
	public void addFragmentShader(String source) {
		// Build the array
		if (fragmentShaders == null) {
			fragmentShaders = new String[1];
		} else {
			String[] temparray = fragmentShaders.clone();
			fragmentShaders = new String[temparray.length + 1];
			for (int i = 0; i < temparray.length; i++) {
				fragmentShaders[i] = temparray[i];
			}
		}
		
		// Add the new source
		//if (fragmentShaders.length == 1)
			//source = this.getBaseFragmentShader() + source;
		fragmentShaders[fragmentShaders.length-1] = source;
		
		//System.out.println(source);
	}
	
	public static String loadShaderFromFile(String filePath) {
		FileUtils file = FileUtils.file_text_open_read(Runner.class.getClassLoader(), filePath);
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
	
	public void addVertexShader(String source) {
		// Build the array
		if (vertexShaders == null) {
			vertexShaders = new String[1];
		} else {
			String[] temparray = vertexShaders.clone();
			vertexShaders = new String[temparray.length + 1];
			for (int i = 0; i < temparray.length; i++) {
				vertexShaders[i] = temparray[i];
			}
		}
		
		//// Add the new source
		if (vertexShaders.length == 1)
			//source = this.getBaseVertexShader() + source;
		vertexShaders[vertexShaders.length-1] = source;
		
		//System.out.println(source);
	}
	
	protected String getBaseVertexShader() {
		return    "#version 330\n"
				+ "\n"
				+ "uniform mat4 MATRIX_VIEW;\n"
				+ "uniform mat4 MATRIX_PROJECTION;\n"
				+ "uniform mat4 MATRIX_PROJECTION_VIEW;\n"
				+ "uniform mat3 MATRIX_NORMAL;\n"
				+ "uniform mat4 MATRIX_OBJECT_POSITION;\n"
				+ "uniform mat3 MATRIX_OBJECT_NORMAL;\n"
				+ "uniform vec4 VECTOR_OBJECT_COLOUR;\n"
				+ "\n"
				+ "layout (location = 0) in vec3 in_Position;\n"
				+ "layout (location = 1) in vec3 in_Normal;\n"
				+ "layout (location = 2) in vec2 in_TextureCoord;\n"
				+ "layout (location = 3) in vec4 in_Colour;\n\n";
	}
	
	protected String getBaseFragmentShader() {
		return    "#version 150\n"
				+ "\n"
				+ "uniform sampler2D sampler;\n"
				+ "uniform float uAlphaThreshold;\n"
				+ "uniform float uAlphaClipping;\n"
				+ "\n"
				+ "out vec4 out_Colour;\n";
	}
	
	private void uploadMatrix(Matrix4f matrix, int location) {
		if (matrixBuffer == null || matrixBuffer.capacity() != 16)
			this.matrixBuffer = BufferUtils.createFloatBuffer(16);
		
		matrix.store(matrixBuffer);
		matrixBuffer.flip();
		glUniformMatrix4(location, false, matrixBuffer);
		matrixBuffer.clear();
	}
	
	private void uploadMatrix(Matrix3f matrix, int location) {
		if (matrixBuffer == null || matrixBuffer.capacity() != 9)
			this.matrixBuffer = BufferUtils.createFloatBuffer(9);
		
		matrix.store(matrixBuffer);
		matrixBuffer.flip();
		glUniformMatrix3(location, false, matrixBuffer);
		matrixBuffer.clear();
	}
	
	protected void setOrthographicProjection(float x, float y, float width, float height) {
		this.matrix_projection = new Matrix4f();
		this.matrix_projection_view = new Matrix4f();
		this.matrix_object_position = new Matrix4f();
		this.matrix_view = new Matrix4f();
		
		Matrix4f ortho = new Matrix4f();
		MatrixUtils.createOrthographicMatrix(ortho, x, width, y, height, 0, 128);
		
		uploadMatrix(ortho, this.matrix_projection_view_location);
		uploadMatrix(ortho, this.matrix_projection_location);
		uploadMatrix(ortho, this.matrix_view_location);
		//uploadMatrix(new Matrix4f(), runner.shader.normalMatrixLocation);
	}
	
	protected void setPerspectiveProjection(float x1, float y1, float z1, float x2, float y2, float z2, float fov, float aspect, float znear, float zfar) {
		Matrix3f normalMatrix = new Matrix3f();
		this.matrix_projection = new Matrix4f();
		this.matrix_projection_view = new Matrix4f();
		this.matrix_view = new Matrix4f();
		
		// Calculate projection matrix
		MatrixUtils.createPerspectiveMatrix(this.matrix_projection, fov, aspect, znear, zfar);
		
		// Calculate view matrix
		MatrixUtils.createLookAtMatrix(this.matrix_view, new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2), new Vector3f(0, 0, 1));
		
		// Calculate projection-view matrix
		Matrix4f.mul(this.matrix_projection, this.matrix_view, this.matrix_projection_view);
		
		// Calculate normal matrix
		MatrixUtils.createNormalMatrix(this.matrix_view, normalMatrix);
		
		uploadMatrix(this.matrix_projection, this.matrix_projection_location);
		uploadMatrix(this.matrix_view, this.matrix_view_location);
		uploadMatrix(this.matrix_projection_view, this.matrix_projection_view_location);
		uploadMatrix(normalMatrix, this.matrix_normal_location);
	}

	@Override
	public void loadResources() {
		if (this.loaded)
			return;
		
		this.loaded = true;
		this.program = new ShaderProgram();
		
		//program.attachShader(new Shader(Shader.VERTEX_SHADER, this.getBaseVertexShader()));
		//program.attachShader(new Shader(Shader.FRAGMENT_SHADER, this.getBaseFragmentShader()));
		
		for (int i = 0; i < vertexShaders.length; i++) {
			String vert = this.getBaseVertexShader() + vertexShaders[i];
			program.attachShader(new EngineShader(EngineShader.VERTEX_SHADER, vert));
		}
		for (int i = 0; i < fragmentShaders.length; i++) {
			String frag = this.getBaseFragmentShader() + fragmentShaders[i];
			program.attachShader(new EngineShader(EngineShader.FRAGMENT_SHADER, frag));
		}
		program.link();
		
		matrix_view_location = program.getUniformLocation("MATRIX_VIEW");
		matrix_projection_location = program.getUniformLocation("MATRIX_PROJECTION");
		matrix_projection_view_location = program.getUniformLocation("MATRIX_PROJECTION_VIEW");
		matrix_normal_location = program.getUniformLocation("MATRIX_NORMAL");
		matrix_object_position_location = program.getUniformLocation("MATRIX_OBJECT_POSITION");
		matrix_object_normal_location = program.getUniformLocation("MATRIX_OBJECT_NORMAL");
		vector_object_colour_location = program.getUniformLocation("VECTOR_OBJECT_COLOUR");
	}

	public void bind() {
		if (this.program != null)
			this.program.bind();
	}

	public ShaderProgram getShaderProgram() {
		return this.program;
	}
	
	public Matrix4f getViewMatrix() {
		return this.matrix_view;
	}
	
	public Matrix4f getProjectionMatrix() {
		return this.matrix_projection;
	}
}
