package net.mantagames.jgm.engine.gl.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;

public class FileUtils {
	private BufferedWriter bw;
	private FileWriter fw;
	
	private BufferedReader br;
	private FileReader fr;
	private InputStreamReader isr;
	
	public static FileUtils file_text_open_read(ClassLoader loader, String path) {
		try{
			URL url = loader.getResource(path);
			InputStreamReader isr = new InputStreamReader(url.openStream());
			BufferedReader br = new BufferedReader(isr);
			FileUtils fio = new FileUtils();
			fio.br = br;
			fio.isr = isr;
			return fio;
		}catch(Exception e) {
			return null;
		}
	}
	
	public static FileUtils file_text_open_read(String str) {
		try{
			FileReader fr = new FileReader(str);
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(fr);
			FileUtils fio = new FileUtils();
			fio.fr = fr;
			fio.br = br;
			return fio;
		}catch(Exception e) {
			return null;
		}
	}
	
	public static String file_text_read_line(FileUtils fio) {
		try{
			return fio.br.readLine();
		}catch(Exception e) {
			//
		}
		return null;
	}
	
	public static FileUtils file_text_open_write(String str) {
		try{
			FileWriter fw = new FileWriter(str);
			@SuppressWarnings("resource")
			BufferedWriter bw = new BufferedWriter(fw);
			FileUtils fio = new FileUtils();
			fio.fw = fw;
			fio.bw = bw;
			return fio;
		}catch(Exception e) {
			return null;
		}
	}
	
	public static boolean file_text_write_line(FileUtils fio, String str) {
		try{
			fio.bw.write(str);
			fio.bw.newLine();
		}catch(Exception e) {
			return false;
		}
		return true;
	}
	
	public static void file_text_close(FileUtils fio) {
		try{ fio.bw.close(); }catch(Exception e) { }
		try{ fio.fw.close(); }catch(Exception e) { }
		try{ fio.br.close(); }catch(Exception e) { }
		try{ fio.fr.close(); }catch(Exception e) { }
		try{ fio.isr.close(); }catch(Exception e) { }
		
		fio.bw = null;
		fio.fw = null;
		fio.br = null;
		fio.fr = null;
		fio.isr = null;
	}
}
