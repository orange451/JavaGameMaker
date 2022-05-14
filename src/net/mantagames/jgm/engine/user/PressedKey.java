package net.mantagames.jgm.engine.user;

public class PressedKey {
	public int keyId;
	public boolean held;
	public int ticks;
	public boolean released;
	private boolean keyboardReleased;
	
	public PressedKey(int keyName) {
		this.keyId = keyName;
		this.held = false;
	}
	
	public void letGo() {
		this.keyboardReleased = true;
	}

	public boolean hasKeyboardReleased() {
		return keyboardReleased;
	}
}
