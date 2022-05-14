package net.mantagames.jgm.engine.user;

import java.util.ArrayList;


import org.lwjgl.input.Keyboard;


public class GameKeyboard {
	boolean[] keys = new boolean[256];
	private ArrayList<PressedKey> keysPressed = new ArrayList<PressedKey>();
	private PressedKey lastKeyPressed;
	
	public void pressKey(int key) {
		boolean found = false;
		for (int i = keysPressed.size() - 1; i >= 0; i--) {
			if (keysPressed.get(i).keyId == key) {
				found = true;
			}
		}
		if (!found) {
			PressedKey pkey = new PressedKey(key);
			keysPressed.add(pkey);
			lastKeyPressed = pkey;
		}
	}

	public void releaseKey(int key) {
		for (int i = keysPressed.size() - 1; i >= 0; i--) {
			if (keysPressed.get(i).keyId == key) {
				keysPressed.get(i).letGo();
			}
		}
	}
	
	public boolean isKeyHeldDown(int key) {
		for (int i = keysPressed.size() - 1; i >= 0; i--) {
			if (keysPressed.get(i).held && keysPressed.get(i).keyId == key) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isKeyPressed(int key) {
		for (int i = keysPressed.size() - 1; i >= 0; i--) {
			if (keysPressed.get(i).ticks == 1 && keysPressed.get(i).keyId == key) {
				return true;
			}
		}
		return false;
	}
	
	public void setKeysPressedTicks(int ticks) {
		for (int i = keysPressed.size() - 1; i >= 0; i--) {
			keysPressed.get(i).ticks = ticks;
		}
	}
	
	public boolean isKeyReleased(int key) {
		for (int i = keysPressed.size() - 1; i >= 0; i--) {
			if (keysPressed.get(i).keyId == key && keysPressed.get(i).released) {
				return true;
			}
		}
		return false;
	}
	
	private void mapKeys(){
		//Update keys
		for(int i=0;i<keys.length;i++){
			boolean isKeyDown = keys[i];
			keys[i] = Keyboard.isKeyDown(i);
			if (keys[i] == true && !isKeyDown) {
				this.pressKey(i);
			}else if (keys[i] == false && isKeyDown) {
				this.releaseKey(i);
			}
		}
	}

	public void tick() {
		mapKeys();
		for (int i = keysPressed.size() - 1; i >= 0; i--) {
			keysPressed.get(i).held = true;
			keysPressed.get(i).ticks++;
			if (keysPressed.get(i).hasKeyboardReleased())
				keysPressed.get(i).released = true;
		}
	}
	
	public void endTick() {
		for (int i = keysPressed.size() - 1; i >= 0; i--) {
			if (keysPressed.get(i).released) {
				keysPressed.remove(i);
			}
		}
	}

	public PressedKey getLastKeyPressed() {
		return lastKeyPressed;
	}

	public void clear() {
		Keyboard.poll();
		keysPressed.clear();
	}
}
