 package net.mantagames.jgm.engine;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Date;

import net.mantagames.jgm.engine.dialogue.Input;
import net.mantagames.jgm.engine.dialogue.Message;
import net.mantagames.jgm.engine.gl.Texture2D;
import net.mantagames.jgm.engine.gl.font.GLFont;
import net.mantagames.jgm.engine.gl.util.IconUtil;
import net.mantagames.jgm.engine.gl.util.TextureUtils;
import net.mantagames.jgm.engine.user.GameKeyboard;
import net.mantagames.jgm.engine.user.GameMouse;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class Runner implements Runnable {
	private static Runner runner;
	public static int fps;
	private GlobalObjectVariables objectVariables;
	private Window window;
	private GameKeyboard keyboard;
	private GameMouse mouse;

	private Room current_room;
	private Shader current_shader;
	private Shader shader;
	private Thread mainThread;

	public static GLFont default_font;
	public static Texture2D default_texture;
	public static SoundSystem soundSystem;
	protected static ArrayList<GameResourceLoader> resourceLoader = new ArrayList<GameResourceLoader>();
	protected static ArrayList<Class<? extends Room>> rooms = new ArrayList<Class<? extends Room>>();
	protected boolean isFullscreen;
	protected boolean autoRedraw = true;
	protected boolean newGame = true;

	private static RenderProperties renderProperties;

	public Runner() {
		Runner.runner = this;
		System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
		Runner.renderProperties = new RenderProperties();

		this.mainThread = new Thread(this);
	}

	protected void update() {
		// Begin user input tracking
		keyboard.tick();
		mouse.tick();

		if (keyboard.isKeyPressed(Keyboard.KEY_F4) && RunnerProperties.let_f4_switch_fullscreen) {
			runner.isFullscreen = !runner.isFullscreen;
			this.window.setSize(window.getWidth(), window.getHeight(), isFullscreen);
		}

		if (keyboard.isKeyPressed(Keyboard.KEY_ESCAPE) && RunnerProperties.let_esc_end_game) {
			gameEnd();
		}

		// Bind UI shader and update matrices
		setNormalRenderShader();

		// Update the room
		current_room.tick();
		if (autoRedraw)
			redrawScreen();

		// End user input tracking
		keyboard.endTick();
		mouse.endTick();
	}

	public void redrawScreen() {
		// Clear transparent pixels
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		// OpenGL stuff
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glClearColor(0, 0, 0, 1);
		GL11.glClearDepth(1.0f);

		Rectangle viewport = window.getViewport();
		GL11.glViewport((int) viewport.x, (int) viewport.y, (int) viewport.width, (int) viewport.height);

		if (current_room == null)
			return;
		this.setShader(this.shader);
		current_room.draw();

		Display.update();
	}

	private void setNormalRenderShader() {
		if (current_room == null)
			return;

		runner.setShader(null);
	}

	public void start() {
		this.mainThread.start();
	}

	public Window getWindow() {
		return runner.window;
	}

	public GlobalObjectVariables getObjectVariables() {
		return runner.objectVariables;
	}

	public Shader getCurrentShader() {
		return runner.current_shader;
	}

	public void setRoom(Class<? extends Room> room) {
		try {

			runner.keyboard.endTick();
			runner.mouse.endTick();

			runner.current_room.event_room_end();
			runner.current_room = room.newInstance();
			if (newGame) {
				runner.current_room.game_start = true;
				newGame = false;
			}
			keyboard.setKeysPressedTicks(10);
			runner.current_room.onCreate(this);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void executeResources() {
		for (int i = 0; i < Runner.resourceLoader.size(); i++) {
			resourceLoader.get(i).loadResources();
			RoomLoadEngine.loadBar++;
			RoomLoadEngine.loadRoom.draw();
			Display.update();
		}
	}

	public void loadGameResources() { }; // gets its implementation when user creates the Runner

	protected void loadRunnerResources() {
		try{
			SoundSystemConfig.addLibrary( LibraryLWJGLOpenAL.class );
			SoundSystemConfig.setCodec( "wav", CodecWav.class );
		}catch(Exception e) {
			e.printStackTrace();
		}
		soundSystem = new SoundSystem();
		SoundSystemConfig.setSoundFilesPackage("");
	}

	public static Room getCurrentRoom() {
		return runner.current_room;
	}

	protected GameKeyboard getKeyboard() {
		return runner.keyboard;
	}

	protected GameMouse getMouse() {
		return runner.mouse;
	}

	public static void showMessage(String msg) {
		System.out.println(msg);
		runner.getKeyboard().clear();
		boolean to = runner.getWindow().isFullscreen();
		if (to)
			runner.getWindow().setSize(runner.getWindow().getWidth(), runner.getWindow().getHeight(), false);
		runner.current_room.draw();
		Display.update();
		new Message(msg);

		if (to)
			runner.getWindow().setSize(runner.getWindow().getWidth(), runner.getWindow().getHeight(), true);
	}

	public static void gameEnd() {
		runner.current_room.game_end = true;
		runner.current_room.endRoom();
		System.exit(0);
	}

	public static String getString(String msg, String def) {
		runner.getKeyboard().clear();
		boolean to = runner.getWindow().isFullscreen();
		if (to)
			runner.getWindow().setSize(runner.getWindow().getWidth(), runner.getWindow().getHeight(), false);
		runner.current_room.draw();
		Display.update();
		Input input = new Input(msg, def);

		if (to)
			runner.getWindow().setSize(runner.getWindow().getWidth(), runner.getWindow().getHeight(), true);
		return input.getValue();
	}

	public static int getInteger(String msg, String def) {
		runner.getKeyboard().clear();
		boolean to = runner.getWindow().isFullscreen();
		if (to)
			runner.getWindow().setSize(runner.getWindow().getWidth(), runner.getWindow().getHeight(), false);
		runner.current_room.draw();
		Display.update();
		Input input = new Input(msg, def);

		if (to)
			runner.getWindow().setSize(runner.getWindow().getWidth(), runner.getWindow().getHeight(), true);

		int num = 0;
		try {
			num = Integer.parseInt(input.getValue());
		} catch(NumberFormatException e) {
		}
		return num;
	}

	public Class<? extends Room> getRoom(int i) {
		return rooms.get(i);
	}

	protected int getRoomPointer(Class<? extends Room> class1) {
		for (int i = 0; i < rooms.size(); i++) {
			if (rooms.get(i).equals(class1)) {
				return i;
			}
		}
		return 0;
	}

	protected void setOrthographicProjection(float x, float y, float width, float height) {
		if (runner != null && runner.shader != null) {
			runner.current_shader.setOrthographicProjection(x, y, width, height);
		}
	}

	protected Shader setShader(Shader mshader) {
		if (mshader != null) {
			if (!mshader.loaded)
				mshader.loadResources();

			activateShader(mshader);
			return mshader;
		}else{
			activateShader(runner.shader);
		}
		return null;
	}

	private void activateShader(Shader mshader) {
		runner.current_shader = mshader;
		runner.current_shader.bind();
		setOrthographicProjection(0, 0, current_room.view_wview, current_room.view_hview);

		runner.current_room.current_shader = mshader;
		ArrayList<GameObject> objects = runner.current_room.getObjects();
		for (int i = objects.size() - 1; i >= 0; i--) {
			objects.get(i).current_shader = mshader;
		}
	}

	public static void setDefaultShader(Shader shader) {
		runner.shader = shader;
	}

	protected void setPerspectiveProjection(float x1, float y1, float z1, float x2, float y2, float z2, float fov, float aspect, float znear, float zfar) {
		runner.current_shader.setPerspectiveProjection(x1, y1, z1, x2, y2, z2, fov, aspect, znear, zfar);
	}

	public void addInternalResourceLoader(GameResourceLoader res) {
		Runner.resourceLoader.add(res);
		RoomLoadEngine.loadMax++;
	}

	public void addRoom(Class<? extends Room> class1) {
		Runner.rooms.add(class1);
	}

	public Thread getMainThread() {
		return this.mainThread;
	}

	@Override
	public void run() {
		// Create the viewing window
		this.window = new Window(1, 1, "Game Engine");
		this.window.createDisplay();

		// Max anisotropy
		FloatBuffer max = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max);
		RenderProperties.filter_anisotropic_max = (int) max.get(0);

		// Create first default texture
		BufferedImage white = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		white.getGraphics().fillRect(0, 0, 1, 1);
		Runner.default_texture = TextureUtils.loadTexture(white);

		IconUtil.setIcon(RunnerProperties.game_icon);

		runner.isFullscreen = RunnerProperties.start_in_fullscreen;

		// Setup user devices
		this.keyboard = new GameKeyboard();
		this.mouse = new GameMouse();

		// Setup game stuff
		this.objectVariables = new GlobalObjectVariables();
		this.shader = new Shader();
		this.shader.addFragmentShader(Shader.loadShaderFromFile("net/mantagames/jgm/engine/res/shader.frag"));
		this.shader.addVertexShader(Shader.loadShaderFromFile("net/mantagames/jgm/engine/res/shader.vert"));

		// Setup the room
		this.current_room = new RoomLoadEngine();
		this.current_room.onCreate(this);

		// Create the resource loaders
		this.loadGameResources();
		this.shader.loadResources();
		this.setShader(shader);

		// Start gameloop
		double nanoSecond = 1000000000.0;
		long startTime = System.nanoTime();
		long framerate_timestamp = new Date().getTime();
		int actualFPS = 0;

		// Start the main loop
		while(!Display.isCloseRequested()) {
			// Freeze runner when out-of-focus (and enabled in properties)
			while (!Display.isActive() && RunnerProperties.freeze_game_when_unfocused) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			double maxfps = current_room.room_speed;
			long waitTime = (long) (nanoSecond / maxfps);
			long now = System.nanoTime();

			// Calculate FPS
			Date d = new Date();
			long this_framerate_timestamp = d.getTime();
			if ((this_framerate_timestamp - framerate_timestamp) >= 1000) {
				Runner.fps = actualFPS;

				actualFPS = 0;
				framerate_timestamp = this_framerate_timestamp;
			}

			// Handle the game logic
			if (now - startTime > waitTime) {
				startTime = now;
				update();
				actualFPS++;
			}
			Thread.yield();
		}
		Display.destroy();
		System.exit(0);
	}

	public static RenderProperties getRenderProperties() {
		return renderProperties;
	}
}
