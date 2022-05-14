package net.mantagames.jgm.engine;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glClearDepth;
import static org.lwjgl.opengl.GL11.glClearStencil;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import net.mantagames.jgm.engine.gl.Vector;
import net.mantagames.jgm.engine.gl.mesh.ModelMatrix;
import net.mantagames.jgm.engine.gl.mesh.VBOModel;
import net.mantagames.jgm.engine.skeleton.RoomInterface;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public abstract class Room extends GlobalObjectVariables implements RoomInterface {
	private int ticks;
	protected Runner runner;
	protected boolean game_start;
	protected boolean game_end;
	private Random random = new Random();

	public Room() {
		this.room_current = this;
		this.background_colour = c_ltgray;
	}

	protected void onCreate(Runner runner) {
		this.runner = runner;
		setGlobalObjectVariablesFromRunner(runner);
		this.event_room_start();
		this.viewLogic();
		this.checkIfGlobalObjectVariablesChanged(runner, this);
		runner.getWindow().setSize(view_wview, view_hview, runner.isFullscreen);
	}

	protected void endRoom() {
		this.event_room_end();
		for (int i = objects.size() - 1; i >= 0; i--) {
			if (i >= objects.size())
				continue;

			if (game_end)
				objects.get(i).event_game_end();

			objects.get(i).event_room_end();
		}
	}

	protected void tick() {
		ticks++;
		if (ticks == 1) {
			for (int i = objects.size() - 1; i >= 0; i--) {
				GameObject obj = objects.get(i);
				setGlobalObjectVariablesFromRunner(runner);
				copyRoomVariablesTo(obj);
				copyGlobalObjectVariablesTo(obj);

				if (game_start)
					obj.event_game_start();

				obj.event_room_start();
				if (i >= objects.size())
					continue;

				obj.event_create();
				obj.loaded = true;

				obj.lives = 4;
				System.out.println("A) " + obj + "    " + obj.lives);
				this.checkIfRoomVariablesChanged(obj);
				obj.show_message("Lives a little after create: " + obj.lives);
				System.out.println("B) " + obj + "    " + obj.lives);
				this.checkIfGlobalObjectVariablesChanged(runner, obj);
				obj.show_message("Lives end of create: " + obj.lives);
				obj.movementVectorLogic();
			}

			// First draw event call (always called before every other objects step event).
			if (runner.autoRedraw)
				draw();

			// Set game start back to false (in case it was changed).
			game_start = false;
		}

		reorderObjects();

		// Begin Step Event
		for (int i = objects.size() - 1; i >= 0; i--) {
			if (i >= objects.size())
				continue;
			setGlobalObjectVariablesFromRunner(runner);
			copyRoomVariablesTo(objects.get(i));
			copyGlobalObjectVariablesTo(objects.get(i));

			objects.get(i).pretick();
			if (i >= objects.size())
				continue;

			objects.get(i).event_begin_step();
			if (i >= objects.size())
				continue;
			this.checkIfRoomVariablesChanged(objects.get(i));
			this.checkIfGlobalObjectVariablesChanged(runner, objects.get(i));
			objects.get(i).movementVectorLogic();
		}

		reorderObjects();

		// Step Event
		for (int i = objects.size() - 1; i >= 0; i--) {
			if (i >= objects.size())
				continue;
			setGlobalObjectVariablesFromRunner(runner);
			copyRoomVariablesTo(objects.get(i));
			copyGlobalObjectVariablesTo(objects.get(i));

			objects.get(i).event_step();
			if (i >= objects.size())
				continue;
			checkIfRoomVariablesChanged(objects.get(i));
			checkIfGlobalObjectVariablesChanged(runner, objects.get(i));
			objects.get(i).movementVectorLogic();
		}

		reorderObjects();

		// Check some events
		synchronized (objects) {
			for (int i = objects.size() - 1; i >= 0; i--) {
				if (i >= objects.size())
					continue;
				GameObject check = objects.get(i);
				float checkX = check.x;
				float checkY = check.y;

				// Outside room event
				if (outsideRoom(check)) {
					check.event_outside_room();
				}

				// Collision Event
				ArrayList<GameObject> collidingWith = getObjectCollisions(check, checkX, checkY, false, true, true);
				for (int ii = 0; ii < collidingWith.size(); ii++) {
					check.event_collide(collidingWith.get(ii));

					if (check.destroyed) { // THis object was destroyed during the collision event
						if (collidingWith.get(ii).collision_check_can_collide_width(check.getClass())) {
							collidingWith.get(ii).event_collide(check);
						}
					}
				}
			}
		}

		// End Step Event
		for (int i = objects.size() - 1; i >= 0; i--) {
			if (i >= objects.size())
				continue;
			setGlobalObjectVariablesFromRunner(runner);
			copyRoomVariablesTo(objects.get(i));
			copyGlobalObjectVariablesTo(objects.get(i));

			objects.get(i).event_end_step();
			if (i >= objects.size())
				continue;
			checkIfRoomVariablesChanged(objects.get(i));
			checkIfGlobalObjectVariablesChanged(runner, objects.get(i));
		}

		// Post tick (used to move objects to their new location)
		for (int i = objects.size() - 1; i >= 0; i--) {
			if (i >= objects.size())
				continue;

			objects.get(i).posttick();
		}
	}

	protected void draw() {
		this.halign = fa_left;
		this.valign = fa_top;

		// Reset color & alpha
		this.current_shader = runner.getCurrentShader();
		this.current_alpha = 1;
		this.current_colour = c_white;
		this.current_font = Runner.default_font;

		// Reset the texture filter to none
		texture_set_filter(RunnerProperties.base_texture_filter);
		texture_set_anisotropy(1);
		this.fl_anisotropy_max = RenderProperties.filter_anisotropic_max;

		// Reset alpha threshold
		RenderProperties.alpha_threshold = 0;

		// Reclear depth so that objects can draw on-top of background
		Color bgCol = new Color(this.background_colour);
		glClearColor(bgCol.getRed()/255f, bgCol.getGreen()/255f, bgCol.getBlue()/255f, 1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		glClearDepth(1);

		// Send white back to the shader
		setColour(c_white);

		// Draw the background
		if (this.background_index != null) {
			background_x += background_hspeed;
			background_y += background_vspeed;
			background_index.draw(background_x, background_y, this.runner);
		}

		// Order the objects based on depth
		reorderObjects();

		// Do view logic
		viewLogic();

		// Render objects
		for (int i = objects.size() - 1; i >= 0; i--) {
			if (i >= objects.size())
				continue;
			GameObject object = objects.get(i);
			if (!object.loaded || !object.visible)
				continue;
			copyRoomVariablesTo(object);
			copyGlobalObjectVariablesTo(object);
			object.event_draw();
			if (i >= objects.size())
				continue;
			//checkIfRoomVariablesChanged(object);
			checkIfRoomVariablesChanged(object);
			checkIfGlobalObjectVariablesChanged(runner, object);
		}
		setRoomVariables();
	}

	protected void setColour(int colour) {
		Color mcolor = new Color(colour);
		int modelColorLocation = GL20.glGetUniformLocation(current_shader.getShaderProgram().getID(), "VECTOR_OBJECT_COLOUR");
		GL20.glUniform4f(modelColorLocation, mcolor.getRed()/255f, mcolor.getGreen()/255f, mcolor.getBlue()/255f, current_alpha);
	}

	public void setAlphaThreshold(float alpha) {
		RenderProperties.alpha_threshold = alpha / 255.0f;
	}

	public void setAlphaClipping(boolean clip) {
		RenderProperties.alpha_clip = clip;
	}


	/**
	 * This method returns all objects that are currently colloding with a given rectangle
	 * @param rect
	 * @return
	 */
	protected ArrayList<GameObject> getObjectsInsideRect(Rectangle rect) {
		ArrayList<GameObject> obj = new ArrayList<GameObject>();
		for (int ii = objects.size() - 1; ii >= 0; ii--) {
			if (ii >= objects.size())
				continue;

			GameObject check = objects.get(ii);
			if (check.sprite_index == null)
				continue;

			Rectangle myRect = check.sprite_index.getCollisionRectangle(check.x, check.y, check.image_xscale, check.image_yscale, check.image_angle);
			if (myRect.intersects(rect))
				obj.add(check);
		}

		return obj;
	}


	/**
	 * This method returns all objects (THAT CAN COLLIDE) that are pixel-perfectly colliding at a given position.
	 * @param checks
	 * @param check
	 * @param checkX
	 * @param checkY
	 * @param ignoreSolid
	 * @return
	 */
	protected ArrayList<GameObject> getPixelPerfectCollisions(ArrayList<GameObject> checks, GameObject check, float checkX, float checkY, boolean ignoreSolid) {
		ArrayList<GameObject> obj = new ArrayList<GameObject>();

		Rectangle rect = check.sprite_index.getCollisionRectangle(checkX, checkY, check.image_xscale, check.image_yscale, check.image_angle);

		if (check.colliderChecks.size() > 0 && check.sprite_index != null) {
			for (int ii = checks.size() - 1; ii >= 0; ii--) {
				if (ii >= checks.size())
					continue;

				GameObject potentialCollision = checks.get(ii);
				if (potentialCollision.equals(check))
					continue;

				// Check if our object is allowed to collide with the selected object
				if (potentialCollision.sprite_index != null && (potentialCollision.solid || ignoreSolid)) {

					float potentialCheckX = potentialCollision.x;
					float potentialCheckY = potentialCollision.y;

					// Crate the two potential collision rectangles
					Rectangle rect2 = potentialCollision.sprite_index.getCollisionRectangle(potentialCheckX, potentialCheckY, potentialCollision.image_xscale, potentialCollision.image_yscale, potentialCollision.image_angle);

					if (rect.intersects(rect2)) { // If they intersect, then there's a potential collision!
						BufferedImage image1 = check.sprite_index.getRotationalBufferedImage(rect, (int)check.image_index, check.image_angle, check.image_xscale, check.image_yscale);
						BufferedImage image2 = potentialCollision.sprite_index.getRotationalBufferedImage(rect2, (int)potentialCollision.image_index, potentialCollision.image_angle, potentialCollision.image_xscale, potentialCollision.image_yscale);
						if (check.sprite_index.isCollidingWith(image1, rect.x, rect.y, image2, rect2.x, rect2.y)) {
							// "check" is colliding with "potentialCollision"
							obj.add(potentialCollision);
						}
					}
				}
			}
		}

		return obj;
	}

	protected ArrayList<GameObject> getObjectCollisions(GameObject check, float checkX, float checkY, boolean ignoreCanCollide, boolean ignoreSolid, boolean nextStep) {
		ArrayList<GameObject> obj = new ArrayList<GameObject>();
		if ((check.colliderChecks == null && !ignoreCanCollide) || check.sprite_index == null)
			return obj;

		Rectangle USE_RECT;
		if (check.colliderChecks.size() > 0 && check.sprite_index != null) {
			for (int ii = objects.size() - 1; ii >= 0; ii--) {
				if (ii >= objects.size())
					continue;

				GameObject potentialCollision = objects.get(ii);
				if (potentialCollision.equals(check))
					continue;

				// If the potential collision or the current object are solid, then use the current objects solid mask (next step mask)
				if ((potentialCollision.solid || check.solid) && nextStep) {
					USE_RECT = check.sprite_index.getCollisionRectangle(checkX + check.hspeed, checkY + check.vspeed, check.image_xscale, check.image_yscale, check.image_angle);;
				}else {
					USE_RECT = check.sprite_index.getCollisionRectangle(checkX, checkY, check.image_xscale, check.image_yscale, check.image_angle);;
				}

				// Check if our object is allowed to collide with the selected object
				if (potentialCollision.sprite_index != null && (potentialCollision.solid || ignoreSolid) && (check.collision_check_can_collide_width(potentialCollision.getClass()) || ignoreCanCollide)) {

					float potentialCheckX = potentialCollision.x;
					float potentialCheckY = potentialCollision.y;

					// Crate the two potential collision rectangles
					Rectangle USE_RECT_POTENTIAL = potentialCollision.sprite_index.getCollisionRectangle(potentialCheckX, potentialCheckY, potentialCollision.image_xscale, potentialCollision.image_yscale, potentialCollision.image_angle);

					// If they intersect, then there's a potential collision!
					if (USE_RECT.intersects(USE_RECT_POTENTIAL)) {
						//System.out.println("  found a potential collision");
						BufferedImage image1 = check.sprite_index.getRotationalBufferedImage(USE_RECT, (int)check.image_index, check.image_angle, check.image_xscale, check.image_yscale);
						BufferedImage image2 = potentialCollision.sprite_index.getRotationalBufferedImage(USE_RECT_POTENTIAL, (int)potentialCollision.image_index, potentialCollision.image_angle, potentialCollision.image_xscale, potentialCollision.image_yscale);
						if (check.sprite_index.isCollidingWith(image1, USE_RECT.x, USE_RECT.y, image2, USE_RECT_POTENTIAL.x, USE_RECT_POTENTIAL.y)) {
							// "check" is colliding with "potentialCollision"
							//System.out.println("    found a pixel perfect collision");
							obj.add(potentialCollision);
						}
					}
				}
			}
		}

		return obj;
	}

	protected void changeRoom(Class<? extends Room> room) {
		for (int i = objects.size() - 1; i >= 0; i--) {
			if (i >= objects.size())
				continue;
			objects.get(i).event_room_end();
		}
		runner.setRoom(room);
	}

	protected void game_restart() {
		runner.newGame = true;
		changeRoom(runner.getRoom(0));
	}

	/**
	 * This sorts all the objects based on the depth variables
	 */
	private void reorderObjects() {
		Collections.sort(objects, new Comparator<GameObject>() {
			@Override
			public int compare(GameObject arg0, GameObject arg1) {
				return (int)((arg0.depth - arg1.depth) * 1000);
			}
		});
	}


	private void setRoomVariables() {
		Display.setTitle(room_caption);
	}

	private boolean outsideRoom(GameObject object) {
		if (object.sprite_index == null)
			return false;

		float wid = object.sprite_get_width(object.sprite_index);
		float hei = object.sprite_get_height(object.sprite_index);
		Vector origin = object.sprite_index.getImageOrigin();
		if (object.x < -wid + origin.getX() || object.x > room_width + wid - origin.getX() || object.y < -hei + origin.getY() || object.y > room_height + hei - origin.getY())
			return true;

		return false;
	}

	protected GameObject instance_create(float x, float y, Class<? extends GameObject> class1) {
		try {
			Constructor<? extends GameObject> ctor = class1.getConstructor();
			GameObject obj = ctor.newInstance();
			obj.x = x;
			obj.y = y;
			obj.xstart = x;
			obj.ystart = y;
			obj.xprevious = x;
			obj.yprevious = y;
			obj.loaded = true;
			if (ticks > 0) {
				this.copyRoomVariablesTo(obj);
				this.copyGlobalObjectVariablesTo(obj);
				obj.event_create();
				this.checkIfRoomVariablesChanged(obj);
				this.checkIfGlobalObjectVariablesChanged(runner, obj);
			}
			objects.add(obj);
			return obj;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void instance_destroy(GameObject object) {
		object.event_destroy();
		object.destroyed = true;
		objects.remove(object);
		object = null;
	}

	protected int instance_number(Class<? extends GameObject> class1) {
		int ret = 0;
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getClass().equals(class1) || class1.isAssignableFrom(objects.get(i).getClass())) {
				ret++;
			}
		}
		return ret;
	}

	protected void draw_set_halign(int align) {
		this.halign = align;
	}

	protected void draw_set_valign(int align) {
		this.valign = align;
	}

	public void event_room_end() {};

	public ArrayList<GameObject> getObjects() {
		ArrayList<GameObject> obj = new ArrayList<GameObject>();
		for (int i = 0; i < objects.size(); i++) {
			obj.add(objects.get(i));
		}
		return obj;
	}

	public void instance_destroy_all(Class<? extends GameObject> class1) {
		ArrayList<GameObject> objects = this.getObjects();
		for (int i = 0; i < objects.size(); i++) {
			if(class1.isAssignableFrom(objects.get(i).getClass())) {
				objects.get(i).instance_destroy();
			}
		}
	}

	public GameObject instance_first(Class<? extends GameObject> class1) {
		ArrayList<GameObject> objects = this.getObjects();
		for (int i = 0; i < objects.size(); i++) {
			if(class1.isAssignableFrom(objects.get(i).getClass())) {
				return objects.get(i);
			}
		}
		return null;
	}

	protected void drawVBO(VBOModel model, int color, float alpha, ModelMatrix matrix) {
		Color mcolor = new Color(color);
		GL20.glUniform4f(runner.getCurrentShader().vector_object_colour_location, mcolor.getRed()/255f, mcolor.getGreen()/255f, mcolor.getBlue()/255f, alpha);

		model.matrix_set_from_4x4(matrix.matrix_get());
		model.draw(null, runner.getCurrentShader());
	}

	public void set_automatic_draw(boolean value) {
		runner.autoRedraw = value;
	}
	public void window_set_fullscreen(boolean full) {
		runner.getWindow().setSize(runner.getWindow().getWidth(), runner.getWindow().getHeight(), full);
	}
	public boolean window_get_fullscreen() {
		return runner.getWindow().isFullscreen();
	}
	public void window_set_showborder(boolean show) {
		System.setProperty("org.lwjgl.opengl.Window.undecorated", Boolean.toString(!show));
		runner.getWindow().setSize(runner.getWindow().getWidth(), runner.getWindow().getHeight(), runner.getWindow().isFullscreen());
	}
	public void window_set_sizeable(boolean sizeable) {
		runner.getWindow().setResizeable(sizeable);
	}
	public void room_goto_previous() {
		int currentRoomPointer = runner.getRoomPointer(room_current.getClass());
		int toRoom = currentRoomPointer - 1;
		this.changeRoom(runner.getRoom(toRoom));
	}
	public void room_goto_next() {
		int currentRoomPointer = runner.getRoomPointer(room_current.getClass());
		int toRoom = currentRoomPointer + 1;
		room_current.changeRoom(runner.getRoom(toRoom));
	}

	public void draw_sprite_ext(Sprite sprite, float image_index2, float x, float y, float xscale, float yscale, float rot, int colour, float alpha, ModelMatrix matrix) {
		if (sprite == null)
			return;
		matrix.matrix_add_translation(x - view_xview, y - view_yview, 0);
		matrix.matrix_add_rotation_z(-rot);
		matrix.matrix_add_scaling(xscale, yscale, 1);
		sprite.draw(runner, colour, alpha, (int)image_index2, matrix);
		matrix.matrix_reset();
	}

	public void draw_surface_ext(Surface surface, float x, float y, float xscale, float yscale, float rot, int colour, float alpha, ModelMatrix matrix) {
		if (surface == null)
			return;
		matrix.matrix_add_translation(x - view_xview, y - view_yview, 0);
		matrix.matrix_add_scaling(surface.getWidth(), surface.getHeight(), 1);
		matrix.matrix_add_rotation_z(-rot);
		matrix.matrix_add_scaling(xscale, yscale, 1);

		Color mcolor = new Color(colour);
		GL20.glUniform4f(runner.getCurrentShader().vector_object_colour_location, mcolor.getRed()/255f, mcolor.getGreen()/255f, mcolor.getBlue()/255f, alpha);
		VBOModel model = GameConstants.quadInvert;
		model.matrix_set_from_4x4(matrix.matrix_get());
		model.draw(surface.getTexture(), runner.getCurrentShader());

		matrix.matrix_reset();
	}

	public void draw_sprite_mat(Sprite sprite, float image_index2, ModelMatrix matrix, int colour, float alpha) {
		if (sprite == null)
			return;
		sprite.draw(runner, colour, alpha, (int)image_index2, matrix);
	}

	public Shader shader_set(Shader shader) {
		return runner.setShader(shader);
	}
	public void shader_reset() {
		runner.setShader(null);
	}
	public void d3d_set_projection_ext(float x1, float y1, float z1, float x2, float y2, float z2, float fov, float aspect, float znear, float zfar) {
		runner.setPerspectiveProjection(x1, y1, z1, x2, y2, z2, fov, aspect, znear, zfar);
	}
	public void d3d_set_projection_ortho(float x, float y, float width, float height) {
		runner.setOrthographicProjection(x, y, width, height);
	}
	public void d3d_start() {
		RenderProperties.set3dMode(true);
	}
	public void d3d_end() {
		RenderProperties.set3dMode(false);
	}
	public boolean mouse_check_button(int button) {
		return runner.getMouse().isMouseButtonHeldDown(button);
	}
	public boolean keyboard_check_pressed(int key) {
		return runner.getKeyboard().isKeyPressed(key);
	}
	public boolean keyboard_check(int key) {
		return runner.getKeyboard().isKeyHeldDown(key);
	}
	public boolean keyboard_check_released(int key) {
		return runner.getKeyboard().isKeyReleased(key);
	}

	public void move_contact(GameObject check, float direction, int maxdist, boolean ignoreSolid) {
		Rectangle myRect = getCollisionRectangleContainingObjectInTwoLocations(check, direction, maxdist);
		float xlendir = check.lengthdir_x(1, direction);
		float ylendir = check.lengthdir_y(1, direction);

		ArrayList<GameObject> potential = room_current.getObjectsInsideRect(myRect);
		for (int i = 0; i < maxdist; i++) {
			float myx = check.x + xlendir;
			float myy = check.y + ylendir;

			ArrayList<GameObject> collidingWith = getPixelPerfectCollisions(potential, check, myx, myy, ignoreSolid);
			if (collidingWith.size() == 0) {
				check.x = myx;
				check.y = myy;
			}else{
				return;
			}
		}
	}

	public void move_outside(GameObject check, float direction, int maxdist, boolean ignoreSolid) {
		Rectangle myRect = getCollisionRectangleContainingObjectInTwoLocations(check, direction, maxdist);
		float xlendir = check.lengthdir_x(1, direction);
		float ylendir = check.lengthdir_y(1, direction);

		ArrayList<GameObject> potential = room_current.getObjectsInsideRect(myRect);
		for (int i = 0; i < maxdist; i++) {
			float myx = check.x + xlendir;
			float myy = check.y + ylendir;

			ArrayList<GameObject> collidingWith = getPixelPerfectCollisions(potential, check, myx, myy, ignoreSolid);
			if (collidingWith.size() != 0) {
				check.x = myx;
				check.y = myy;
			}else{
				return;
			}
		}
	}
	public float random(float n) {
		return (float) (random.nextDouble() * n);
	}
	public int irandom(int n) {
		return random.nextInt(n);
	}

	private Rectangle getCollisionRectangleContainingObjectInTwoLocations(GameObject check, float direction, int maxdist) {
		Rectangle myRect = check.sprite_index.getCollisionRectangle(check.x, check.y, check.image_xscale, check.image_yscale, check.image_angle);
		float xlendir = check.lengthdir_x(1, direction);
		float ylendir = check.lengthdir_y(1, direction);

		// Make the rectangle that will fit the current one, AND the maxdist
		float newx = myRect.x + (xlendir * maxdist);
		float newy = myRect.y + (ylendir * maxdist);
		float rectWid = myRect.width;
		float rectHei = myRect.height;
		if (newx < myRect.x) {
			rectWid += myRect.x - newx;
			myRect.x = (int) newx;
		}else if (newx > myRect.x) {
			rectWid += newx - myRect.x;
		}
		if (newy < myRect.y) {
			rectHei += myRect.y - newy;
			myRect.y = (int) newy;
		}else if (newy > myRect.y) {
			rectHei += newy - myRect.y;
		}
		myRect.setBounds(myRect.x, myRect.y, (int)rectWid, (int)rectHei);

		return myRect;
	}

	public void texture_set_filter(int filter) {
		int mag = filter;
		if (mag == fl_mipmap_linear)
			mag = fl_linear;
		if (mag == fl_mipmap_nearest)
			mag = fl_none;

		RenderProperties.filter_mag = mag;
		RenderProperties.filter_min = filter;
	}

	public void texture_set_anisotropy(int max) {
		RenderProperties.filter_anisotropy = max;
	}
}
