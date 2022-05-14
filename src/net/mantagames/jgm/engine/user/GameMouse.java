package net.mantagames.jgm.engine.user;

import java.util.ArrayList;

import org.lwjgl.input.Mouse;


public class GameMouse {
	boolean[] buttons = new boolean[3];
	
	private ArrayList<MouseButton> mousebuttons = new ArrayList<MouseButton>();
	
	public void pressMouse(int mouseId) {
		boolean found = false;
		for (int i = mousebuttons.size() - 1; i >= 0; i--) {
			if (mousebuttons.get(i).id == mouseId) {
				found = true;
			}
		}
		if (!found) {
			mousebuttons.add(new MouseButton(mouseId));
		}
	}

	public void releaseMouse(int mouseId) {
		for (int i = mousebuttons.size() - 1; i >= 0; i--) {
			if (mousebuttons.get(i).id == mouseId) {
				mousebuttons.get(i).letGo();
			}
		}
	}
	
	public boolean isMouseButtonHeldDown(int mouseId) {
		for (int i = mousebuttons.size() - 1; i >= 0; i--) {
			if (mousebuttons.get(i).held && mousebuttons.get(i).id == mouseId) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isMouseButtonPressed(int mouseId) {
		for (int i = mousebuttons.size() - 1; i >= 0; i--) {
			if (mousebuttons.get(i).ticks == 1 && mousebuttons.get(i).id == mouseId) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isMouseButtonReleased(int mouseId) {
		for (int i = mousebuttons.size() - 1; i >= 0; i--) {
			if (mousebuttons.get(i).id == mouseId && mousebuttons.get(i).released) {
				return true;
			}
		}
		return false;
	}
	
	private void mapButtons(){
		//Update buttons
		for(int i=0;i<buttons.length;i++){
			boolean isButtonDown = buttons[i];
			buttons[i] = Mouse.isButtonDown(i);
			if (buttons[i] == true && !isButtonDown) {
				this.pressMouse(i);
			}else if (buttons[i] == false && isButtonDown) {
				this.releaseMouse(i);
			}
		}
	}
	
	public void tick() {
		mapButtons();
		for (int i = mousebuttons.size() - 1; i >= 0; i--) {
			mousebuttons.get(i).held = true;
			mousebuttons.get(i).ticks++;
			if (mousebuttons.get(i).hasMouseReleased())
				mousebuttons.get(i).released = true;
		}
	}
	
	public void endTick() {
		for (int i = mousebuttons.size() - 1; i >= 0; i--) {
			if (mousebuttons.get(i).released) {
				mousebuttons.remove(i);
			}
		}
	}
}
