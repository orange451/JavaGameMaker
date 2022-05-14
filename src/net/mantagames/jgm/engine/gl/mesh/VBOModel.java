package net.mantagames.jgm.engine.gl.mesh;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import net.mantagames.jgm.engine.RenderProperties;
import net.mantagames.jgm.engine.Shader;
import net.mantagames.jgm.engine.gl.MathHelper;
import net.mantagames.jgm.engine.gl.Texture2D;
import net.mantagames.jgm.engine.gl.Vector;
import net.mantagames.jgm.engine.gl.util.ColorUtils;
import net.mantagames.jgm.engine.gl.util.MatrixUtils;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix3f;

public class VBOModel extends ModelMatrix {
	private int vaoId = -1;
	private int vboId = -1;
	private boolean modified;
	private Vertex[] vertices;
	FloatBuffer matrix4Buffer = BufferUtils.createFloatBuffer(16);
	FloatBuffer matrix3Buffer = BufferUtils.createFloatBuffer(9);
	
	public VBOModel() {
	}
	
	public VBOModel(int vertices) {
		this.blankModel(vertices);
	}
	
	public void blankModel(int amountVertices) {
		vertices = new Vertex[amountVertices];
	}
	
	public Vertex addVertex(float x, float y, float z) {
		return addVertex(x, y, z, 1, 1);
	}
	
	public Vertex addVertex(float x, float y, float z, float tx, float ty) {
		return addVertex(x, y, z, 1, 1, 1, tx, ty);
	}
	
	public Vertex addVertex(Vertex vert, int pointer) {
		vertices[pointer] = vert;
		return vertices[pointer];
	}
	
	public Vertex addVertex(int pointer, float x, float y, float z, float nx, float ny, float nz, float tx, float ty) {
		Vertex v = new Vertex().setXYZ(x, y, z).setST(tx, ty).setNormalXYZ(nx,  ny,  nz);
		return addVertex(v, pointer);
	}
	
	public Vertex addVertex(float x, float y, float z, float nx, float ny, float nz, float tx, float ty) {
		Vertex v = new Vertex().setXYZ(x, y, z).setST(tx, ty).setNormalXYZ(nx,  ny,  nz);
		return addVertex(v);
	}
	
	public Vertex addVertex(Vertex vert) {
		
		if (vertices == null)
			blankModel(1);
		
		modified = true;
		int pointer = -1;
		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i] == null) {
				pointer = i;
				break;
			}
		}
		
		if (pointer == -1) { //Uh oh! the model is full!
			pointer = vertices.length;
			
			//move all the vertices to a temp list
			Vertex[] v = new Vertex[vertices.length];
			for (int i = 0; i < vertices.length; i++)
				v[i] = vertices[i].clone();
			
			//move all the temp vertices into the main list
			vertices = new Vertex[vertices.length + 1];
			for (int i = 0; i < v.length; i++)
				vertices[i] = v[i];
		}
		
		//add vertex to list
		return addVertex(vert, pointer);
	}
	
	public void rotateModel(float xRot, float yRot, float zRot) {
		for (int i = 0; i < vertices.length; i++) {
			Vertex v = vertices[i];
			float[] vertex = {v.getXYZ()[0], v.getXYZ()[1], v.getXYZ()[2]};
			vertex = applyRotations(vertex, xRot, yRot, zRot);
			v.setXYZ(vertex[0], vertex[1], vertex[2]);
			
			float[] normal = {v.getNormalXYZ()[0], v.getNormalXYZ()[1], v.getNormalXYZ()[2]};
			normal = applyRotations(normal, xRot, yRot, zRot);
			v.setNormalXYZ(normal[0], normal[1], normal[2]);
		}
		modified = true;
	}
	
	public void scaleModel(float d, float e, float f) {
		for (int i = 0; i < vertices.length; i++) {
			Vertex v = vertices[i];
			float[] vertex = {v.getXYZ()[0], v.getXYZ()[1], v.getXYZ()[2]};
			v.setXYZ(vertex[0] * d, vertex[1] * e, vertex[2] * f);
		}
		this.modified = true;
	}
	
	public void translateModel(float a, float b, float c) {
		for (int i = 0; i < vertices.length; i++) {
			Vertex v = vertices[i];
			float[] vertex = {v.getXYZ()[0], v.getXYZ()[1], v.getXYZ()[2]};
			v.setXYZ(vertex[0] + a, vertex[1] + b, vertex[2] + c);
		}
		this.modified = true;
	}
	
	private static float[] applyRotations(float[] vertex, float xa, float ya, float za) {
		float x1 = vertex[0];
		float y1 = vertex[1];
		float z1 = vertex[2];
		if (xa!=0){
		    float pd = MathHelper.point_distance_3d(x1, y1, z1, x1, 0, 0);
		    float pa = MathHelper.point_direction(0, 0, z1, y1);
		    z1=0+MathHelper.lengthdir_x(pd,pa-xa);
		    y1=0+MathHelper.lengthdir_y(pd,pa-xa);
		}
		if (ya!=0){
			float pd = MathHelper.point_distance_3d(x1, y1, z1, 0, y1, 0);
			float pa = MathHelper.point_direction(0, 0, z1, x1);
		    z1=0+MathHelper.lengthdir_x(pd,pa+ya);
		    x1=0+MathHelper.lengthdir_y(pd,pa+ya);
		}
		if (za!=0){
			float pd = MathHelper.point_distance(x1,y1,0,0);
			float pa = MathHelper.point_direction(0, 0, x1,y1);
		    x1=0+MathHelper.lengthdir_x(pd,pa+za);
		    y1=0+MathHelper.lengthdir_y(pd,pa+za);
		}
		
		return new float[] {x1, y1, z1};
	}
	
	public void setVerticesFromModel(VBOModel model) {
		this.vertices = model.vertices;
		this.modified = true;
	}
	
	public void addVerticesFromModel(VBOModel model) {
		Vertex[] verts = new Vertex[getSize() + model.vertices.length];
		for (int i = 0; i < getSize(); i++) {
			verts[i] = vertices[i];
		}
		for (int i = 0; i < model.vertices.length; i++) {
			verts[i + getSize()] = model.vertices[i];
		}
		
		this.vertices = verts;
		this.modified = true;
	}
	
	public void flip() {
		this.invertNormals();
		
		//Create a temp copy of the vertices
		Vertex[] v = new Vertex[vertices.length];
		for (int i = 0; i < v.length; i++) {
			v[i] = vertices[i];
		}
		
		// Invert the vertices
		for (int i = 0; i < v.length; i++)
			vertices[(v.length - 1) - i] = v[i];
		
		modified = true;
	}
	
	public void invertNormals() {
		for (int i = 0; i < vertices.length; i++) {
			vertices[i].setNormalXYZ(vertices[i].getNormalXYZ()[0] * -1, vertices[i].getNormalXYZ()[1] * -1, vertices[i].getNormalXYZ()[2] * -1);
		}
		modified = true;
	}
	
	public void clip() {
		int pointer = -1;
		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i] == null) {
				pointer = i;
				break;
			}
		}
		
		if (pointer > -1 && pointer < vertices.length - 1) { //can be clipped
			//move all the vertices to a temp list
			Vertex[] v = new Vertex[pointer];
			for (int i = 0; i < pointer; i++)
				v[i] = vertices[i];
			
			//move all the temp vertices into the main list
			vertices = new Vertex[pointer];
			for (int i = 0; i < v.length; i++)
				vertices[i] = v[i];
		}
		
		modified = true;
	}
	
	public VBOModel createQuad(float x1, float y1, float x2, float y2) {	
		createQuadExt(x1, y1, 0, x2, y2, 0, 0, 0, 1, 1);
		return this;
	}
	
	public VBOModel createQuadExt(float x1, float y1, float z1, float x2, float y2, float z2, float tx1, float ty1, float tx2, float ty2) {
		addVertex(x1, y1,  z1, tx1, ty1);
		addVertex(x1, y2,  z1, tx1, ty2);
		addVertex(x2, y2,  z2, tx2, ty2);
		
		addVertex(x2, y2,  z2, tx2, ty2);
		addVertex(x2, y1,  z2, tx2, ty1);
		addVertex(x1, y1,  z1, tx1, ty1);
		
		return this;
	}
	
	public VBOModel createWallExt(float x1, float y1, float z1, float x2, float y2, float z2, float tx1, float ty1, float tx2, float ty2) {
		addVertex(x1, y1, z1, tx1, ty1);
		addVertex(x2, y2, z1, tx2, ty1);
		addVertex(x2, y2, z2, tx2, ty2);
		
		addVertex(x2, y2, z2, tx2, ty2);
		addVertex(x1, y1, z2, tx1, ty2);
		addVertex(x1, y1, z1, tx1, ty1);
		
		return this;
	}
	
	protected void sendToGPU() {
		modified = false;
		
		// Put each 'Vertex' in one FloatBuffer
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length * Vertex.elementCount);
		for (int i = 0; i < vertices.length; i++) {
			verticesBuffer.put(vertices[i].getElements());
		}
		verticesBuffer.flip();
		
		if (vboId == -1)
			vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
		
		if (vaoId == -1)
			vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);

		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);
		
		// Put the position coordinates in attribute list 0
		boolean normalize = false;
		GL20.glVertexAttribPointer(0, Vertex.positionElementCount, GL11.GL_FLOAT, normalize, Vertex.stride, Vertex.positionByteOffset);
		GL20.glVertexAttribPointer(1, Vertex.normalElementCount, GL11.GL_FLOAT,  normalize, Vertex.stride, Vertex.normalByteOffset);
		GL20.glVertexAttribPointer(2, Vertex.textureElementCount, GL11.GL_FLOAT,  normalize, Vertex.stride, Vertex.textureByteOffset);
		GL20.glVertexAttribPointer(3, Vertex.colorElementCount, GL11.GL_FLOAT, normalize, Vertex.stride, Vertex.colorByteOffset);
		//GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		// Deselect (bind to 0) the VAO
		GL30.glBindVertexArray(0);
		
		// Create a new VBO for the indices and select it (bind) - INDICES
		/*vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);*/
	}
	
	public void draw(Texture2D texture, Shader shader) {
		while (modified)
			this.sendToGPU();
		
		if (vertices == null) {
			System.err.println("Attempting to draw model with no vertices");
			return;
		}
		
		if (texture != null) {
			texture.bind();
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, RenderProperties.getMagFilter());
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, RenderProperties.getMinFilter());
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, RenderProperties.getAnisotropyFilter());
		}
		
		int alphaThreshLocation = GL20.glGetUniformLocation(shader.getShaderProgram().getID(), "uAlphaThreshold");
		GL20.glUniform1f(alphaThreshLocation, RenderProperties.getAlphaThreshold());
		
		int alphaClipLocation = GL20.glGetUniformLocation(shader.getShaderProgram().getID(), "uAlphaClipping");
		GL20.glUniform1f(alphaClipLocation, RenderProperties.getAlphaClipping()?1:0);

		
		// Send model matrix to shader
		modelMatrix.store(matrix4Buffer);
		matrix4Buffer.flip();
		GL20.glUniformMatrix4(shader.matrix_object_position_location, false, matrix4Buffer);
		
		// Send normal matrix to shader
		Matrix3f normalMatrix = new Matrix3f();
		MatrixUtils.createNormalMatrix(modelMatrix, normalMatrix);
		normalMatrix.store(matrix3Buffer);
		matrix3Buffer.flip();
		GL20.glUniformMatrix3(shader.matrix_object_normal_location, false, matrix3Buffer);
		
		glBindVertexArray(vaoId);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertices.length);
		glBindVertexArray(0);
	}

	public void draw(Texture2D texture, Shader shader, float x, float y, float z) {		
		matrix_add_translation(x, y, z);
		draw(texture, shader);
		matrix_reset();
	}

	public int getSize() {
		if (vertices == null)
			return 0;
		return this.vertices.length;
	}

	public Vertex getVertex(int i) {
		return vertices[i];
	}

	public void destroy() {
		if (vertices != null) {
			for (int i = 0; i < vertices.length; i++) {
				vertices[i].destroy();
			}
		}
		vertices = null;
		GL15.glDeleteBuffers(vboId);
		GL30.glDeleteVertexArrays(vaoId);
	}

	public VBOModel clone() {
		VBOModel ret = new VBOModel();
		ret.blankModel(this.vertices.length);
		for (int i = 0; i < vertices.length; i++)
			ret.addVertex(vertices[i]);
		return ret;
	}

	public void setColor(Color col) {
		Vector v = ColorUtils.getVectorFromColor(col);
		v.normalize();
		float r = v.getX();
		float g = v.getY();
		float b = v.getZ();
		
		setColor(r, g, b);
	}
	
	public void setColor(float r, float g, float b) {
		for (int i = 0; i < vertices.length; i++) {
			vertices[i].setRGBA(r, g, b, 1);
		}
		
		modified = true;
	}
	
	public void setAlpha(float alpha) {
		for (int i = 0; i < vertices.length; i++) {
			float[] rgba = vertices[i].getRGBA();
			vertices[i].setRGBA(rgba[0], rgba[1], rgba[2], alpha);
		}
		
		modified = true;
	}

	public void loadFromString(ArrayList<String> loadStrings, boolean flipX) {
		int pointer = 0;
		if (loadStrings.get(0).equals("100")) {
			String header = loadStrings.get(1);
			String[] blueprint = header.split(" ");
			int amtPolys = Integer.parseInt(blueprint[0]);
			if (amtPolys >= 5) {
				blankModel(amtPolys);
				String line;
				for (int i = 2; i < loadStrings.size(); i++) {
					String[] dat = loadStrings.get(i).split(" "); //read the line of the D3D model, and split it into an array
					
					float polyType = Integer.parseInt(dat[0]);
					if (polyType < 8)
						continue;
					float vx = Float.parseFloat(dat[1]);
					float vy = Float.parseFloat(dat[2]);
					float vz = Float.parseFloat(dat[3]);
					
					float nx = Float.parseFloat(dat[4]);
					float ny = Float.parseFloat(dat[5]);
					float nz = Float.parseFloat(dat[6]);
					
					float tx = Float.parseFloat(dat[7]);
					float ty = Float.parseFloat(dat[8]);
					
					float c = (int) Color.white.getRGB();
					float a = 1;

					if (polyType == 9) { //Polygon type 9
						float col = Float.parseFloat(dat[9]);
						float alp = Float.parseFloat(dat[10]);
						
						if (alp > 0) {
							c = (int) col;
							a = alp;
						}
					}
					
					if (flipX) {
						addVertex(pointer, -vx, vy, vz, nx, -ny, -nz, tx, ty);
					}else{
						addVertex(pointer, vx, vy, vz, nx, ny, nz, tx, ty);
					}
					pointer++;
				}
				// Clip the model as it might has some blank vertices
				clip();
				
				if (flipX) {
					// Rotate the model, to correct location
					rotateModel(0, 0, 90);
					
					// Flip the model, as the x component was inverted
					flip();
				}
				
				//flatShade();
			}
		}
	}

	public void flatShade() {
		modified = true;
		for (int i = 0; i < vertices.length; i+=3) {
			if (vertices.length - i >= 3) {
				float[] normal = getTriangleNormal(vertices[i].getXYZ()[0], vertices[i].getXYZ()[1], vertices[i].getXYZ()[2], vertices[i + 1].getXYZ()[0], vertices[i + 1].getXYZ()[1], vertices[i + 1].getXYZ()[2], vertices[i + 2].getXYZ()[0], vertices[i + 2].getXYZ()[1], vertices[i + 2].getXYZ()[2]);
				vertices[i].setNormalXYZ(normal[0], normal[1], normal[2]);
				vertices[i + 1].setNormalXYZ(normal[0], normal[1], normal[2]);
				vertices[i + 2].setNormalXYZ(normal[0], normal[1], normal[2]);
			}
		}
	}
	
	public static float[] getTriangleNormal(float argument0, float argument1, float argument2, float argument3, float argument4, float argument5, float argument6, float argument7, float argument8) {
		/*
	    Arguments:
	        0 x point0
	        1 y
	        2 z
	        3 x point1
	        4 y
	        5 z
	        6 x point2
	        7 y
	        8 z

	    Returns:
	        vector3f of the triangles' normal
		 */

		float ax,ay,az,bx,by,bz,m,rx,ry,rz;

		//point0 -> point1
		ax = argument3-argument0;
		ay = argument4-argument1;
		az = argument5-argument2;

		//point0 -> point2
		bx = argument6-argument0;
		by = argument7-argument1;
		bz = argument8-argument2;

		//cross product
		rx = ay*bz-by*az;
		ry = az*bx-bz*ax;
		rz = ax*by-bx*ay;

		//magnitude
		m = (float) Math.sqrt(rx*rx+ry*ry+rz*rz);

		//normalize
		rx /= m;
		ry /= m;
		rz /= m;

		return new float[] {rx, ry, rz};
	}
}
