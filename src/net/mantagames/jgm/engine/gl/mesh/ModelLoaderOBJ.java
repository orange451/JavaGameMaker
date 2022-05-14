package net.mantagames.jgm.engine.gl.mesh;

import java.io.BufferedReader;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import net.mantagames.jgm.engine.GlobalObjectVariables;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class ModelLoaderOBJ extends GlobalObjectVariables {	
	public VBOModel importStaticModel(String path, String materialFile) {
		VBOModel model = new VBOModel();

        BufferedReader reader = file_text_open_read(ModelLoaderOBJ.class.getClassLoader(), path);
		if (reader == null) { // Then try to look for it outside the jar!
			String str = new File(path).getAbsolutePath();
			reader = file_text_open_read(str);
		}
		
		if (reader == null)
			return null;
		
        ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
        ArrayList<Vector3f> normals  = new ArrayList<Vector3f>();
        ArrayList<Vector2f> textures  = new ArrayList<Vector2f>();
        ArrayList<Vertex> vertices_final = new ArrayList<Vertex>();
        
        ObjMaterial currentMaterial = new ObjMaterial("base");
        currentMaterial.setDiffuse(1, 1, 1);
        
        ArrayList<ObjMaterial> materials = parseOBJMaterial(materialFile);
        
        String line;
        while ((line = file_text_read_line(reader)) != null) {
        	if (line == null || line.length() <= 0)
        		continue;
        	String[] lineSplit = line.split(" ");
            String prefix = lineSplit[0];
            
            if (prefix.equals("#")) {
                continue;
            } else if (prefix.equals("v")) {
                vertices.add(parseOBJVertex(line));
            } else if (prefix.equals("vn")) {
                normals.add(parseOBJNormal(line));
            } else if (prefix.equals("vt")) {
                textures.add(parseOBJTexture(line));
            } else if (prefix.equals("f")) {
            	parseOBJFace(vertices_final, vertices, normals, textures, line, currentMaterial);
            } else if (prefix.equals("usemtl")) {
            	String tempName = lineSplit[1];
            	ObjMaterial temp = null;
            	for (int i = 0; i < materials.size(); i++) {
            		if (materials.get(i).name.equals(tempName)) {
            			temp = materials.get(i);
            		}
            	}
            	
            	if (temp != null) {
            		currentMaterial = temp;
            	}
            }
        }

        System.out.println("Loaded vertices");
        model.blankModel(vertices_final.size());
        System.out.println("Cached model");
        for (int i = 0; i < vertices_final.size(); i++) {
        	model.addVertex(vertices_final.get(i), i);
        }
        System.out.println("Transferred vertices into model");
        
        file_text_close(reader);
        reader = null;

		model.sendToGPU();
		
		return model;
	}

	public static class ObjMaterial {
		private Vector3f diffuse;
		public String name;
		
		public ObjMaterial(String name) {
			this.name = name;
		}
		
		public ObjMaterial setDiffuse(float r, float g, float b) {
			diffuse = new Vector3f(r, g, b);
			return ObjMaterial.this;
		}
		
		public Vector3f getDiffuse() {
			return diffuse;
		}
	}
	
	private ArrayList<ObjMaterial> parseOBJMaterial(String materialFile) {
		ArrayList<ObjMaterial> materials = new ArrayList<ObjMaterial>();
		if (materialFile != null && materialFile.length() > 0) {
	        BufferedReader reader = file_text_open_read(ModelLoaderOBJ.class.getClassLoader(), materialFile);
			if (reader == null) { // Then try to look for it outside the jar!
				String str = new File(materialFile).getAbsolutePath();
				reader = file_text_open_read(str);
			}
				
			ObjMaterial buildingMaterial = null;
			
			String line;
	        while ((line = file_text_read_line(reader)) != null) {
	        	if (buildingMaterial != null) {
		        	if (line == null || line.length() == 0) {
		        		materials.add(buildingMaterial);
		        		buildingMaterial = null;
		        	}else{
			        	if (line.trim().startsWith("Kd")) {
			        		String[] temp = line.substring(line.indexOf("Kd") + 3).split(" ");
			        		float r = (float) Double.parseDouble(temp[0]);
			        		float g = (float) Double.parseDouble(temp[1]);
			        		float b = (float) Double.parseDouble(temp[2]);
			        		
			        		buildingMaterial.setDiffuse(r, g, b);
			        	}
		        	}
	        	}else{
		        	if (line.startsWith("newmtl")) {
		        		buildingMaterial = new ObjMaterial(line.substring(7));
		        	}
	        	}
	        }
			
	        file_text_close(reader);
	        reader = null;
		}
		return materials;
	}
	
    private void parseOBJFace(ArrayList<Vertex> vert, ArrayList<Vector3f> vertices, ArrayList<Vector3f> normals, ArrayList<Vector2f> textures, String line, ObjMaterial currentMaterial) {
        String[] faceIndices = line.split(" ");
        int[] textureIndicesArray = null;
        int[] vertexIndicesArray = null;
        int[] normalIndicesArray = null;
        
        vertexIndicesArray = new int[3];
        vertexIndicesArray[0] = Integer.parseInt(faceIndices[1].split("/")[0]) - 1;
        vertexIndicesArray[1] = Integer.parseInt(faceIndices[2].split("/")[0]) - 1;
        vertexIndicesArray[2] = Integer.parseInt(faceIndices[3].split("/")[0]) - 1;
        
        if (textures.size() > 0) {
	        textureIndicesArray = new int[3];
	        textureIndicesArray[0] = Integer.parseInt(faceIndices[1].split("/")[1]) - 1;
	        textureIndicesArray[1] = Integer.parseInt(faceIndices[2].split("/")[1]) - 1;
	        textureIndicesArray[2] = Integer.parseInt(faceIndices[3].split("/")[1]) - 1;
        }
        
        normalIndicesArray = new int[3];
        normalIndicesArray[0] = Integer.parseInt(faceIndices[1].split("/")[2]) - 1;
        normalIndicesArray[1] = Integer.parseInt(faceIndices[2].split("/")[2]) - 1;
        normalIndicesArray[2] = Integer.parseInt(faceIndices[3].split("/")[2]) - 1;
        
        for (int i = 0; i < 3; i++) {
        	int vertexindex = vertexIndicesArray[i];
        	int normalindex = normalIndicesArray[i];
        	//System.out.println(index + " || " + line);
	        Vertex v = new Vertex();
	        v.setXYZ(vertices.get(vertexindex).x, vertices.get(vertexindex).y, vertices.get(vertexindex).z);
	        v.setNormalXYZ(normals.get(normalindex).x, normals.get(normalindex).y, normals.get(normalindex).z);
	        if (textureIndicesArray != null) {
	        	int textureindex = textureIndicesArray[i];
	        	v.setST(textures.get(textureindex).x, 1.0f - textures.get(textureindex).y);
	        }
	        vert.add(v);
	        
	        Vector3f diffuse = currentMaterial.getDiffuse();
	        v.setRGBA(diffuse.x, diffuse.y, diffuse.z, v.getRGBA()[3]);
        }
        //System.out.println("--");
    }
	
	private static Vector3f parseOBJVertex(String line) {
        String[] xyz = line.split(" ");
        float x = Float.parseFloat(xyz[1]);
        float y = Float.parseFloat(xyz[2]);
        float z = Float.parseFloat(xyz[3]);
        return new Vector3f(x, y, z);
	}
	
	private static Vector2f parseOBJTexture(String line) {
        String[] xy = line.split(" ");
        float x = Float.parseFloat(xy[1]);
        float y = Float.parseFloat(xy[2]);
        return new Vector2f(x, y);
	}
	
    private static Vector3f parseOBJNormal(String line) {
        String[] xyz = line.split(" ");
        float x = Float.parseFloat(xyz[1]);
        float y = Float.parseFloat(xyz[2]);
        float z = Float.parseFloat(xyz[3]);
        return new Vector3f(x, y, z);
    }
}
