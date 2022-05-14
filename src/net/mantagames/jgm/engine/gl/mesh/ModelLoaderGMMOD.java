package net.mantagames.jgm.engine.gl.mesh;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;

import net.mantagames.jgm.engine.GlobalObjectVariables;

public class ModelLoaderGMMOD extends GlobalObjectVariables {
	public VBOModel importStaticModel(String path) {
		VBOModel model = new VBOModel();
		BufferedReader br = file_text_open_read(ModelLoaderOBJ.class.getClassLoader(), path);
		if (br == null) { // Then try to look for it outside the jar!
			String str = new File(path).getAbsolutePath();
			br = file_text_open_read(str);
		}
		if (br == null)
			return null;
		
		ArrayList<String> loadStrings = new ArrayList<String>();
		String strLine;
		while ((strLine = file_text_read_line(br)) != null) {
			loadStrings.add(strLine);
		}
		
		model.loadFromString(loadStrings, true);
		
		file_text_close(br);
		br = null;
		model.sendToGPU();
		
		return model;
	}
}
