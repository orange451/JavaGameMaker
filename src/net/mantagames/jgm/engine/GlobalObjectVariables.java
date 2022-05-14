package net.mantagames.jgm.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;

public class GlobalObjectVariables extends RoomVariables {
	public float health;
	public float score;
	public int lives;

	public void copyGlobalObjectVariablesTo(GlobalObjectVariables object) {
		// Object Variables
		object.health = this.health;
		object.score = this.score;
		object.lives = this.lives;
	}

	public void setGlobalObjectVariablesFromRunner(Runner runner) {
		this.health = runner.getObjectVariables().health;
		this.lives = runner.getObjectVariables().lives;
		this.score = runner.getObjectVariables().score;
	}

	public void copyRoomVariablesTo(GlobalObjectVariables object) {
		// Room Variables
		object.room_caption = this.room_caption;
		object.room_current = this.room_current;
		object.room_height = this.room_height;
		object.room_width = this.room_width;
		object.room_speed = this.room_speed;
		object.background_index = this.background_index;
		object.background_colour = this.background_colour;
		object.background_hspeed = this.background_hspeed;
		object.background_vspeed = this.background_vspeed;
		object.background_x = this.background_x;
		object.background_y = this.background_y;
		object.current_colour = this.current_colour;
		object.current_alpha = this.current_alpha;
		object.current_font = this.current_font;
		object.current_shader = this.current_shader;
		object.fps = Runner.fps;

		// View variables
		object.view_hview = this.view_hview;
		object.view_wview = this.view_wview;
		object.view_xview = this.view_xview;
		object.view_yview = this.view_yview;
		object.view_object = this.view_object;
		object.view_vspeed = this.view_vspeed;
		object.view_hspeed = this.view_hspeed;
		object.view_hborder = this.view_hborder;
		object.view_vborder = this.view_vborder;

		object.fl_anisotropy_max = this.fl_anisotropy_max;

		// Text variables
		object.halign = this.halign;
		object.valign = this.valign;
	}

	public void checkIfGlobalObjectVariablesChanged(Runner runner, GlobalObjectVariables object) {
		if (object == null)
			return;

		GlobalObjectVariables tempVars = new GlobalObjectVariables();
		this.copyGlobalObjectVariablesTo(tempVars);
		
		System.out.println();

		// Object variables
		if (object.health != runner.getObjectVariables().health)
			tempVars.health = object.health;
		if (object.lives != runner.getObjectVariables().lives)
			tempVars.lives = object.lives;
		if (object.score != runner.getObjectVariables().score)
			tempVars.score = object.score;

		// Execute events
		if (object.health <= 0 && runner.getObjectVariables().health > 0) {
			for (int i = objects.size() - 1; i >=0 ; i--) {
				objects.get(i).event_out_of_health();
			}
		}
		if (object.lives <= 0 && runner.getObjectVariables().lives > 0) {
			for (int i = objects.size() - 1; i >=0 ; i--) {
				objects.get(i).event_out_of_lives();
			}
		}

		tempVars.copyGlobalObjectVariablesTo(runner.getObjectVariables());
	}

	public void checkIfRoomVariablesChanged(GameObject object) {
		if (object == null)
			return;

		System.out.println(object + "'" + object.lives + "'");
		GlobalObjectVariables tempVars = new GlobalObjectVariables();
		this.copyRoomVariablesTo(tempVars);

		System.out.println(object.lives);
		// Room Variables
		if (!object.room_caption.equals(this.room_caption))
			tempVars.room_caption = object.room_caption;
		if (object.view_xview != this.view_xview)
			tempVars.view_xview = object.view_xview;
		if (object.view_yview != this.view_yview)
			tempVars.view_yview = object.view_yview;
		if (object.view_hspeed != this.view_hspeed)
			tempVars.view_hspeed = object.view_hspeed;
		if (object.view_vspeed != this.view_vspeed)
			tempVars.view_vspeed = object.view_vspeed;
		if (object.view_hborder != this.view_hborder)
			tempVars.view_hborder = object.view_hborder;
		if (object.view_vborder != this.view_vborder)
			tempVars.view_vborder = object.view_vborder;
		if (object.room_speed != this.room_speed)
			tempVars.room_speed = object.room_speed;
		if (object.current_colour != this.current_colour)
			tempVars.current_colour = object.current_colour;
		if (object.current_alpha != this.current_alpha)
			tempVars.current_alpha = object.current_alpha;
		if (object.current_font != this.current_font)
			tempVars.current_font = object.current_font;
		if (object.background_colour != this.background_colour)
			tempVars.background_colour = object.background_colour;
		if (object.background_hspeed != this.background_hspeed)
			tempVars.background_hspeed = object.background_hspeed;
		if (object.background_vspeed != this.background_vspeed)
			tempVars.background_vspeed = object.background_vspeed;
		if (object.background_x != this.background_x)
			tempVars.background_x = object.background_x;
		if (object.background_y != this.background_y)
			tempVars.background_y = object.background_y;
		if ((object.view_object != null && this.view_object != null) && ((object.view_object == null && this.view_object != null) || (object.view_object != null && this.view_object == null) || (!object.view_object.equals(this.view_object))))
			tempVars.view_object = object.view_object;

		if (this.background_index != null && object.background_index != null) {
			if (!object.background_index.equals(this.background_index))
				tempVars.background_index = object.background_index;
		}

		System.out.println(object.lives);
		tempVars.copyRoomVariablesTo(this);
		System.out.println(object.lives);
		System.out.println("  ");
	}


	protected void viewLogic() {
		// If you didn't specify the view size, it will auto set to the room size
		if (this.view_wview == -1)
			view_wview = room_width;
		if (this.view_hview == -1)
			view_hview = room_height;

		// Now do the actual view logic
		if (view_object != null) {
			float movey = view_vspeed;
			float movex = view_hspeed;
			if (view_vspeed == -1)
				movey = Math.abs(view_object.vspeed);
			if (view_hspeed == -1)
				movex = Math.abs(view_object.hspeed);

			if (view_object.y - view_yview < view_vborder)
				view_yview -= movey;
			if ((view_yview + view_hview) - view_object.y <= view_vborder)
				view_yview += movey;

			if (view_object.x - view_xview < view_hborder)
				view_xview -= movex;
			if ((view_xview + view_wview) - view_object.x < view_hborder)
				view_xview += movex;
		}

		if (view_lock) {
			if (view_xview < 0)
				view_xview = 0;
			if (view_yview < 0)
				view_yview = 0;
			if (view_wview < room_width && view_xview > room_width - view_wview)
				view_xview = room_width - view_wview;
			if (view_hview < room_height && view_yview > room_height - view_hview)
				view_yview = room_height - view_hview;
		}
	}

	public BufferedReader file_text_open_read(ClassLoader loader, String path) {
		try{
			URL url = loader.getResource(path);
			return new BufferedReader(new InputStreamReader(url.openStream()));
		}catch(Exception e) {
			return null;
		}
	}

	public BufferedReader file_text_open_read(String str) {
		try{
			return new BufferedReader(new FileReader(str));
		}catch(Exception e) {
			return null;
		}
	}

	public String file_text_read_line(BufferedReader out) {
		try{
			return out.readLine();
		}catch(Exception e) {
			//
		}
		return null;
	}

	public BufferedWriter file_text_open_write(String str) {
		try{
			return new BufferedWriter(new FileWriter(str));
		}catch(Exception e) {
			return null;
		}
	}

	public boolean file_text_write_line(BufferedWriter out, String str) {
		try{
			out.write(str);
			out.newLine();
		}catch(Exception e) {
			return false;
		}
		return true;
	}

	public void file_text_close(BufferedWriter out) {
		try{
			out.flush();
		}catch(Exception e) {

		}
		try{
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void file_text_close(BufferedReader out) {
		try{
			out.close();
		}catch(Exception e) {
			//
		}
	}
}
