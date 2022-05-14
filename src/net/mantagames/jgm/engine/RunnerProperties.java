package net.mantagames.jgm.engine;

public class RunnerProperties {
	public static boolean start_in_fullscreen           = false;
	public static boolean fullscreen_stretch            = true;
	public static boolean fullscreen_keep_aspect_ratio  = true;
	public static boolean draw_border_in_windowed_mode  = true;
	public static boolean let_esc_end_game              = true;
	public static boolean let_f4_switch_fullscreen      = true;
	public static boolean draw_loading_bar              = true;
	public static boolean draw_loading_text             = true;
	public static boolean let_player_resize_window      = false;
	public static boolean freeze_game_when_unfocused    = false;
	public static int base_texture_filter               = GameConstants.fl_none;
	
	public static String loading_background = "net/mantagames/jgm/engine/res/loading.png";
	public static String loading_bar_front  = "net/mantagames/jgm/engine/res/progress_front.png";
	public static String loading_bar_back   = "net/mantagames/jgm/engine/res/progress_back.png";
	public static String game_icon          = "net/mantagames/jgm/engine/res/favicon.png";
}
