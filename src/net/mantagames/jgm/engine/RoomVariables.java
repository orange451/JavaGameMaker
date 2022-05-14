package net.mantagames.jgm.engine;

import java.util.ArrayList;

import net.mantagames.jgm.engine.gl.font.GLFont;

public class RoomVariables extends GameConstants {
	protected ArrayList<GameObject> objects = new ArrayList<GameObject>();
	public String room_caption = "";
	public int room_width = 640;
	public int room_height = 480;
	public int view_wview = -1;
	public int view_hview = -1;
	public float view_xview;
	public float view_yview;
	public int view_hborder = 16;
	public int view_vborder = 16;
	public float view_hspeed = 1;
	public float view_vspeed = 1;
	public boolean view_lock = true;
	public GameObject view_object;
	public Room room_current;
	public int room_speed = 30;
	public int fps;
	public float background_hspeed;
	public float background_vspeed;
	public float background_x;
	public float background_y;
	public Background background_index;
	public int background_colour;
	public int fl_anisotropy_max;
	
	protected int current_colour = c_white;
	protected Shader current_shader;
	protected GLFont current_font = Runner.default_font;
	protected float current_alpha = 1;
	protected int halign;
	protected int valign;
}
