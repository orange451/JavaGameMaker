package net.mantagames.jgm.networking;

import java.util.ArrayList;

public class PacketBuffer {
	public ArrayList<Integer> in = new ArrayList<Integer>();
	public ArrayList<Integer> out = new ArrayList<Integer>();
	public int inpointer = 0;

	public void log(String msg) {
		System.err.println(msg);
	}

	public void append(int read) {
		this.in.add(Integer.valueOf(read));
	}
	public void clear() {
		this.out.clear();
		this.inpointer = 0;
	}

	public int getNextIn() {
		int ret = 0;
		try {
			ret = ((Integer)this.in.get(inpointer)).intValue();
			this.inpointer += 1;
		} catch (Exception e) {
			log("Tried to read variable outside buffer!");
		}
		return ret;
	}
	
	public boolean hasNextIn() {
		if (inpointer >= this.in.size())
			return false;
		return true;
	}
}