package net.mantagames.jgm.engine.user;

public class MouseButton {
	public boolean held;
	public int ticks;
	public boolean released;
	private boolean mouseReleased;
	public int id;
	
	public MouseButton(int mouseId) {
		this.held = false;
		this.id = mouseId;
	}
	
	public void letGo() {
		this.mouseReleased = true;
	}

	public boolean hasMouseReleased() {
		return mouseReleased;
	}
}
