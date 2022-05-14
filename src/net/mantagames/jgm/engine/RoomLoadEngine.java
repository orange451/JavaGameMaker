package net.mantagames.jgm.engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

import org.lwjgl.opengl.Display;

import net.mantagames.jgm.engine.gl.font.FontColor;
import net.mantagames.jgm.engine.gl.font.GLFont;
import net.mantagames.jgm.engine.gl.mesh.ModelMatrix;
import net.mantagames.jgm.engine.gl.util.TextureUtils;

public class RoomLoadEngine extends Room {
	private Background loadingImage;
	private Sprite progress_back;
	private Sprite progress_front;
	private boolean loading;
	private float percent;
	private int ticks;
	private String loadText = "";
	
	public static int loadBar = 0;
	public static int loadMax = 2; // sound engine + default font
	protected static RoomLoadEngine loadRoom;
	
	private Runner runner;
	
	@Override
	public void onCreate(Runner runner) {
		this.runner = runner;
		//this.runner.autoRedraw = false;
		this.loadingImage = new Background(TextureUtils.loadTexture(RunnerProperties.loading_background));
		this.progress_back = new Sprite(TextureUtils.loadTexture(RunnerProperties.loading_bar_back));
		this.progress_front = new Sprite(TextureUtils.loadTexture(RunnerProperties.loading_bar_front));
		this.view_wview = loadingImage.getWidth();
		this.view_hview = loadingImage.getHeight();
		this.room_width = view_wview;
		this.room_height = view_hview;
		
		RoomLoadEngine.loadRoom = this;
		
		super.onCreate(runner);
	}
	
	@Override
	public void tick() {
		super.tick();
		ticks++;
		if (loading && ticks > 1) {
			// Make the loading window decorated based on the users definition.
			if (RunnerProperties.draw_border_in_windowed_mode)
				System.setProperty("org.lwjgl.opengl.Window.undecorated", "false");
			
			// Allow the room to be resized if the player changed the resize property.
			if (RunnerProperties.let_player_resize_window)
				runner.getWindow().setResizeable(RunnerProperties.let_player_resize_window);
			
			// Set autoredraw back to true.
			runner.autoRedraw = true;
			
			// Change the room to the first room defined by user.
			this.changeRoom(runner.getRoom(0));
		}
	}
	
	public void draw() {
		super.draw();
		
		// Draw background
		this.loadingImage.draw(0, 0, runner);
		float xx = 16;
		float yy = room_height - xx - progress_back.getHeight();
		float width = room_width - (xx * 2);
		
		
		// Draw loading bar
		if (RunnerProperties.draw_loading_bar) {
			ModelMatrix matrix = new ModelMatrix();
			matrix.matrix_add_translation(xx, yy, 0);
			matrix.matrix_add_scaling(width, 1, 1);
			progress_back.draw(runner, c_white, 1, 0, matrix);
			matrix.matrix_add_translation(0, progress_back.getHeight() - progress_front.getHeight(), 0);
			matrix.matrix_add_scaling(percent, 1, 1);
			progress_front.draw(runner, c_white, 1, 0, matrix);
			matrix.matrix_reset();
		}
		
		
		// Draw loading text
		if (Runner.default_font != null && RunnerProperties.draw_loading_text) {
			Runner.default_font.drawString(this.current_shader, FontColor.BLACK + "Loading " + loadText, xx, yy + 4);
		}
		
		
		// Move the Bar
		percent = loadBar/(float)loadMax;
		if (!loading) {
			loading = true;
			draw();
			
			Display.update();
			loadText = "Internal Resources";
			Runner.default_font = new GLFont(new Font("Arial", Font.PLAIN, 12));
			loadBar++;
			draw();
			
			Display.update();
			sleep(200);
			loadText = "Sound System";
			runner.loadRunnerResources();
			loadBar++;
			draw();
			
			Display.update();
			sleep(200);
			loadText = "External Resources";
			runner.executeResources();
			draw();
			
			Display.update();
		}
	}
	
	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void event_room_start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void event_room_end() {
		// TODO Auto-generated method stub
		
	}
}
