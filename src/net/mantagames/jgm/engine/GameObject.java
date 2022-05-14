package net.mantagames.jgm.engine;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;

import net.mantagames.jgm.engine.gl.Texture2D;
import net.mantagames.jgm.engine.gl.font.GLFont;
import net.mantagames.jgm.engine.gl.mesh.ModelLoaderGMMOD;
import net.mantagames.jgm.engine.gl.mesh.ModelLoaderOBJ;
import net.mantagames.jgm.engine.gl.mesh.ModelMatrix;
import net.mantagames.jgm.engine.gl.mesh.VBOModel;
import net.mantagames.jgm.engine.gl.util.TextureUtils;
import net.mantagames.jgm.engine.skeleton.GameObjectInterface;
import net.mantagames.jgm.networking.PacketBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;

public abstract class GameObject extends GlobalObjectVariables implements GameObjectInterface {
	public float x;
	public float y;
	public float xprevious;
	public float yprevious;
	public float friction;
	public float xstart;
	public float ystart;
	public float hspeed;
	public float vspeed;
	public float gravity;
	public float gravity_direction = 270;
	public float direction;
	public float speed;
	public float image_angle;
	public float image_xscale = 1;
	public float image_yscale = 1;
	public float image_alpha = 1;
	public float image_speed;
	public float image_index;
	public boolean solid;
	public boolean visible = true;
	public float depth;
	public int mouse_x;
	public int mouse_y;
	public int[] alarm;
	public Sprite<?> sprite_index;
	protected boolean loaded = false;
	
	protected boolean destroyed;
	private PacketBuffer buffer = new PacketBuffer();
	private float hspeedprevious;
	private float vspeedprevious;
	private float speedprevious;
	private float directionprevious;
	private ModelMatrix matrix;
	protected ArrayList<Class<? extends GameObject>> colliderChecks;
	
	public GameObject() {
		this.alarm = new int[12];
		this.matrix = new ModelMatrix();
		this.colliderChecks = new ArrayList<Class<? extends GameObject>>();
	}
	
	
	/**
	 * Displays a dialog box with the string as a message.
	 * @param message
	 */
	public void show_message(String message) {
		Runner.showMessage(message);
	}
	
	
	
	/**
	 * Asks the player in a dialog box for a string. str is the message. def is the default value shown.
	 * @param message Message for dialogue box
	 * @param def Default value
	 */
	public String get_string(String message, String def) {
		return Runner.getString(message, def);
	}
	
	
	
	/**
	 * Asks the player in a dialog box for an integer. str is the message. def is the default value shown.
	 * @param message Message for dialogue box
	 * @param def Default value
	 */
	public int get_integer(String message, String def) {
		return Runner.getInteger(message, def);
	}
	
	
	
	/**
	 * This function is good for probabilities where returning an integer (whole number) is not necessary. For example, random(100) will return a value from 0 to 99, but that value can be 22.56473! You can also use real numbers and not integers in this function like this - random(0.5), which will return a value between 0 and 0.4999999.
	 * @param n
	 * @return a random float number between 0 and n.
	 */
	public float random(float n) {
		return room_current.random(n);
	}
	
	
	
	/**
	 * This very useful function only returns integers (whole numbers). So, for example, to get a random number from 0 to 9 you can use irandom(9) and it will return a number from 0 to 9 inclusive...
	 * @param n
	 * @return a random integer between 0 and n.
	 */
	public int irandom(int n) {
		return room_current.irandom(n);
	}
	
	
	
	/**
	 * @param sprite
	 * @return the width of a given sprite.
	 */
	public int sprite_get_width(Sprite<?> sprite) {
		if (sprite == null)
			return 0;
		return sprite.getWidth();
	}
	
	
	
	/**
	 * @param sprite
	 * @return the height of a given sprite.
	 */
	public int sprite_get_height(Sprite<?> sprite) {
		if (sprite == null)
			return 0;
		return sprite.getHeight();
	}
	
	
	
	/**
	 * @param sprite
	 * @param index
	 * @return the texture of a given sprite.
	 */
	public Texture2D sprite_get_texture(Sprite<?> sprite, int index) {
		if (sprite == null)
			return null;
		return sprite.getTexture(index);
	}
	
	
	
	/**
	 * Returns the number of subimages of the sprite with the given index.
	 * @param sprite
	 * @return amount of subimages.
	 */
	public int sprite_get_number(Sprite<?> sprite) {
		if (sprite == null)
			return 0;
		return sprite.getAmountImages();
	}
	
	
	
	/**
	 * @param background
	 * @return the texture of a given background.
	 */
	public Texture2D background_get_texture(Background background) {
		if (background == null)
			return null;
		return background.getTexture();
	}
	
	
	
	/**
	 * With this function you can specify which objects the current object will check for collisions.
	 * @param object
	 */
	public void collision_check_add_object(Class<? extends GameObject> object) {
		this.colliderChecks.add(object);
	}
	
	
	
	/**
	 * With this function you can check whether or not the current object will attempt to check for collisions with a given object.
	 * @param object
	 * @return Boolean
	 */
	public boolean collision_check_can_collide_width(Class<? extends GameObject> object) {
		for (int i = 0; i < this.colliderChecks.size(); i++)
			if (colliderChecks.get(i).equals(object))
				return true;
		return false;
	}
	
	
	/**
	 * Returns whether the instance placed at position(x,y) is collision-free. Objects need to be solid. This is typically used as a check before actually moving to the new position.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean place_free(float x, float y) {
		return room_current.getObjectCollisions(this, x, y, true, false, false).size() == 0;
	}
	
	
	
	/**
	 * Returns whether the instance placed at position (x,y) meets nobody. So this function takes also non-solid instances into account.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean place_empty(float x, float y) {
		return room_current.getObjectCollisions(this, x, y, true, true, false).size() == 0;
	}
	
	
	
	/**
	 * This function will automatically "wrap" an instance that has left the room on either the horizontal or vertical (or both) axis.
	 * @param horizontal
	 * @param vertical
	 * @param border
	 */
	public void move_wrap(boolean horizontal, boolean vertical, float border) {
		if (horizontal) {
			if (x < -border)
				x = room_width + border;
			if (x > room_width + border)
				x = -border;
		}
		if (vertical) {
			if (y < -border)
				y = room_height + border;
			if (y > room_height + border)
				y = -border;
		}
	}
	
	
	
	/**
	 * Moves the instance in the direction until a contact position with a solid object is reached. If there is no collision at the current position, the instance is placed just before a collision occurs. If there already is a collision the instance is not moved. You can specify the maximal distance to move (use a negative number for an arbitrary distance).
	 * @param direction
	 * @param maxdist
	 */
	public void move_contact_solid(float direction, int maxdist) {
		room_current.move_contact(this, direction, maxdist, false);
	}
	
	
	
	/**
	 * Same as move_contact_solid but this time you stop at a contact with any object, not just solid objects.
	 * @param direction
	 * @param maxdist
	 */
	public void move_contact_all(float direction, int maxdist) {
		room_current.move_contact(this, direction, maxdist, true);
	}
	
	
	
	/**
	 * Moves the instance in the direction until it no longer lies within a solid object. If there is no collision at the current position the instance is not moved. You can specify the maximal distance to move (use a negative number for an arbitrary distance).
	 * @param direction
	 * @param maxdist
	 */
	public void move_outside_solid(float direction, int maxdist) {
		room_current.move_outside(this, direction, maxdist, false);
	}
	
	
	
	/**
	 * Same as move_outside_solid but this time you move until outside any object, not just solid objects.
	 * @param direction
	 * @param maxdist
	 */
	public void move_outside_all(float direction, int maxdist) {
		room_current.move_outside(this, direction, maxdist, true);
	}
	
	
	
	/**
	 * With this function you can set the drawing target to the given shader and all further drawing will be done using that. You can end shader use with function shader_reset.
	 * @param shader
	 */
	public void shader_set(Shader shader) {
		current_shader = room_current.shader_set(shader);
	}
	
	
	
	/**
	 * With this function you can set the value (or values) of a shader constant. You must previously have gotten the "handle" of the constant using the function shader_get_uniform.
	 * @param handle
	 * @param value1
	 */
	public void shader_set_uniform_i(int handle, int ... value ) {
		if (handle == -1)
			return;
		int len = value.length;
		switch (len) {
			case 1: {
				GL20.glUniform1i(handle, value[0]);
				break;
			}
			case 2: {
				GL20.glUniform2i(handle, value[0], value[1]);
				break;
			}
			case 3: {
				GL20.glUniform3i(handle, value[0], value[1], value[2]);
				break;
			}
			case 4: {
				GL20.glUniform4i(handle, value[0], value[1], value[2], value[3]);
				break;
			}
		}
	}
	
	
	
	/**
	 * With this function you can set the value (or values) of a shader constant. You must previously have gotten the "handle" of the constant using the function shader_get_uniform.
	 * @param handle
	 * @param value1
	 */
	public void shader_set_uniform_f(int handle, float ... value ) {
		if (handle == -1)
			return;
		int len = value.length;
		switch (len) {
			case 1: {
				GL20.glUniform1f(handle, value[0]);
				break;
			}
			case 2: {
				GL20.glUniform2f(handle, value[0], value[1]);
				break;
			}
			case 3: {
				GL20.glUniform3f(handle, value[0], value[1], value[2]);
				break;
			}
			case 4: {
				GL20.glUniform4f(handle, value[0], value[1], value[2], value[3]);
				break;
			}
		}
	}
	
	
	
	/**
	 * With this function you can set the array value of a shader constant. You must previously have gotten the "handle" of the constant using the function shader_get_uniform.
	 * @param handle
	 * @param array
	 */
	public void shader_set_uniform_f_array(int handle, float[] array) {
		if (handle == -1)
			return;
		
		FloatBuffer buff = BufferUtils.createFloatBuffer(array.length);
        buff.put(array);
        buff.rewind();
        GL20.glUniform1(handle, buff);
	}
	
	
	
	/**
	 * With this function you can set the array value of a shader constant. You must previously have gotten the "handle" of the constant using the function shader_get_uniform.
	 * @param handle
	 * @param array
	 */
	public void shader_set_uniform_f_array(int handle, Vector2f[] array) {
		if (handle == -1)
			return;
		
		FloatBuffer buff = BufferUtils.createFloatBuffer(array.length * 2);
		for (int i = 0; i < array.length; i++) {
			buff.put(array[i].x);
			buff.put(array[i].y);
		}
        buff.rewind();
        GL20.glUniform2(handle, buff);
	}
	
	
	
	/**
	 * With this function you can set the value (or values) of a shader constant to the current transform matrix. You must previously have gotten the "handle" of the constant using the function shader_get_uniform.
	 * @param handle
	 * @param matrix
	 */
	public void shader_set_uniform_matrix(int handle, Matrix4f matrix) {
		if (handle == -1)
			return;
		
		FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
		matrix.store(matrixBuffer);
		matrixBuffer.flip();
		GL20.glUniformMatrix4(handle, false, matrixBuffer);
		matrixBuffer.clear();
	}
	
	
	
	/**
	 * With this function you can set the value (or values) of a shader constant to the current transform matrix. You must previously have gotten the "handle" of the constant using the function shader_get_uniform.
	 * @param handle
	 * @param matrix
	 */
	public void shader_set_uniform_matrix(int handle, Matrix3f matrix) {
		if (handle == -1)
			return;
		
		FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(9);
		matrix.store(matrixBuffer);
		matrixBuffer.flip();
		GL20.glUniformMatrix3(handle, false, matrixBuffer);
		matrixBuffer.clear();
	}
	
	
	
	/**
	 * Since you cannot change the value of a shader constant within the shader itself, you have to set it before calling the shader using one of the available uniform set functions. However, to be able to do that you must first call this function to get the "handle" of the shader constant that you will want to change.
	 * @param shader
	 * @param uniform
	 * @return Real
	 */
	public int shader_get_uniform(Shader tshad, String uniform) {
		if (tshad == null || tshad.getShaderProgram() == null)
			return -1;
		return GL20.glGetUniformLocation(tshad.getShaderProgram().getID(), uniform);
	}
	
	
	
	/**
	 * This function resets the draw target and should be called when you no longer wish to use the current shader (set using shader_set).
	 * @param shader
	 */
	public void shader_reset() {
		room_current.shader_reset();
	}
	
	
	
	/**
	 * This function sets the texture filter used by openGL. Use the fl_ constants.
	 * @param filter
	 */
	public void texture_set_filter(int filter) {
		room_current.texture_set_filter(filter);
	}
	
	
	
	/**
	 * This function sets the current texture anisotropy filter used by openGL. use fl_anisotropy_max for the max anisotropic level.
	 * @param max
	 */
	public void texture_set_anisotropy(int max) {
		room_current.texture_set_anisotropy(max);
	}
	
	
	/**
	 * This function sets a sampler value in a shader to the given texture.
	 * @param uniform
	 * @param texture
	 */
	public void texture_set_stage(int uniform, Texture2D texture, int unit) {
		if (texture == null)
			return;
		
		TextureUtils.setActiveUnit(unit);
		glBindTexture(GL_TEXTURE_2D, texture.getID());
		this.shader_set_uniform_i(uniform, unit);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	}
	
	
	
	/**
	 * This function sets up the initial projection matrix as well as the view matrix for a 3D game.
	 * @param x1 (camera x)
	 * @param y1 (camera y)
	 * @param z1 (camera z)
	 * @param x2 (location x)
	 * @param y2 (location x)
	 * @param z2 (location x)
	 * @param fov (field of view)
	 * @param aspect (aspect ratio)
	 * @param znear
	 * @param zfar
	 */
	public void d3d_set_projection_ext(float x1, float y1, float z1, float x2, float y2, float z2, float fov, float aspect, float znear, float zfar) {
		room_current.d3d_set_projection_ext(x1, y1, z1, x2, y2, z2, fov, aspect, znear, zfar);
	}
	
	
	/**
	 * This function sets the current projection to orthographic mode for 2D drawing.
	 * @param x (view x)
	 * @param y (view y)
	 * @param width (width of view)
	 * @param height (height of view)
	 */
	public void d3d_set_projection_ortho(float x, float y, float width, float height) {
		room_current.d3d_set_projection_ortho(x, y, width, height);
	}
	
	
	
	/**
	 * A 3D triangle has a front and a back side. The front side is said to be the side where the vertices are defined in counter-clockwise order. Now, normally both sides are drawn, but if you make a closed shape then this is a waste of processing power because the back side of the triangle can never be seen. It's in these cases that you can switch on backface culling. This saves about half the amount of drawing time but it means you have the task of defining your polygons in the correct way to prevent issues.
	 * @param enable
	 */
	public void d3d_set_culling(boolean enable) {
		RenderProperties.setCulling(enable);
	}
	
	
	
	/**
	 * With this function you can tell the engine that all further drawing should be done in 3D mode. This function can be called in any event, but will affect all drawing done after, so if your game is not completely done in 3D you may need to switch it off using d3d_end to draw certain parts (and then use this function again to switch 3D mode back on).
	 */
	public void d3d_start() {
		room_current.d3d_start();  
	}
	
	
	
	/**
	 * This function should be called whenever you wish to tell the engine to stop drawing in 3D mode, and resume normal drawing.
	 */
	public void d3d_end() {
		room_current.d3d_end();
	}
	
	
	
	/**
	 * When enabled this function performs depth tests on all surfaces and removes those that are hidden in the current projection.
	 * @param enable
	 */
	public void d3d_set_hidden(boolean enable) {
		if (enable) {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
		} else {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		}
	}
	
	
	/**
	 * When enables this function causes all geometry to be drawn with a wireframe.
	 * @param enable
	 */
	public void d3d_set_wireframe(boolean enable) {
		if (enable)
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		else
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}
	
	
	
	/**
	 * Loads an obj model into the specified VBO Model.
	 * @param model
	 * @param modelFile
	 * @param materialFile
	 */
	public void d3d_model_load_obj(VBOModel model, String modelFile, String materialFile) {
		model.addVerticesFromModel(new ModelLoaderOBJ().importStaticModel(modelFile, materialFile));
	}
	
	
	
	/**
	 * Loads a Game Maker (gmmod/d3d) model into the specified VBO Model.
	 * @param model
	 * @param modelFile
	 * @param materialFile
	 */
	public void d3d_model_load(VBOModel model, String modelFile) {
		d3d_model_load(model, modelFile, 0, 0, 0);
	}
	
	
	
	
	/**
	 * Loads a Game Maker (gmmod/d3d) model into the specified VBO Model.
	 * @param model
	 * @param modelFile
	 * @param materialFile
	 */
	public void d3d_model_load(VBOModel model, String modelFile, float x, float y, float z) {
		VBOModel mod = new ModelLoaderGMMOD().importStaticModel(modelFile);
		if (x != 0 || y != 0  || z != 0) {
			mod.translateModel(x, y, z);
		}
		model.addVerticesFromModel(mod);
	}
	
	
	
	/**
	 * Merges two VBOModels together
	 * @param model
	 * @param modelFile
	 * @param materialFile
	 */
	public void d3d_model_merge(VBOModel target, VBOModel merge) {
		target.addVerticesFromModel(target);
	}
	
	
	
	/**
	 * Adds a (slanted) floor to the given VBOModel. hrepeat indicates how often the texture must be repeated along the horizontal edge of each face. vrepeat does the same for the vertical edge. 
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @param texture
	 */
	public void d3d_model_floor(VBOModel model, float x1, float y1, float z1, float x2, float y2, float z2, float hrepeat, float vrepeat) {
		model.createQuadExt(x1, y2, z1, x2, y1, z2, 0, 0, hrepeat, vrepeat);
		model.flatShade();
	}
	
	
	
	/**
	 * Adds a wall model to the given VBOModel. hrepeat indicates how often the texture must be repeated along the horizontal edge of each face. vrepeat does the same for the vertical edge. 
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @param texture
	 */
	public void d3d_model_wall(VBOModel model, float x1, float y1, float z1, float x2, float y2, float z2, float hrepeat, float vrepeat) {
		model.createWallExt(x1, y1, z1, x2, y2, z2, 0, 0, hrepeat, vrepeat);
		model.flatShade();
	}
	
	
	
	/**
	 * Draws a model in the current color with the given corners using the indicated texture.
	 * @param model
	 * @param x
	 * @param y
	 * @param z
	 * @param texture
	 */
	public void d3d_model_draw(VBOModel model, float x, float y, float z, Texture2D texture) {
		if (model == null)
			return;
		
		ModelMatrix mat = new ModelMatrix();
		mat.matrix_set_from_4x4(this.matrix.matrix_get());
		model.matrix_set_from_4x4(mat.matrix_get());
		model.draw(texture, current_shader);
		model.matrix_reset();
		mat.matrix_reset();
	}
	
	
	
	
	public void matrix_reset() {
		matrix.matrix_reset();
	}
	
	
	
	
	public void matrix_add_rotation_x(float a) {
		matrix.matrix_add_rotation_x(a);
	}
	
	
	
	
	public void matrix_add_rotation_y(float a) {
		matrix.matrix_add_rotation_y(a);
	}

	
	
	
	public void matrix_add_rotation_z(float a) {
		matrix.matrix_add_rotation_z(a);
	}
	
	
	
	
	public void matrix_add_translation(float x, float y, float z) {
		matrix.matrix_add_translation(x, y, z);
	}
	
	
	
	
	public void matrix_add_scaling(float x, float y, float z) {
		matrix.matrix_add_scaling(x, y, z);
	}
	
	
	
	
	public void matrix_set_from_3x3(Matrix3f mat3) {
		matrix.matrix_set_from_3x3(mat3);
	}
	
	
	
	
	public void matrix_set_from_4x4(Matrix4f mat4) {
		matrix.matrix_set_from_4x4(mat4);
	}
	
	
	
	
	public Matrix4f matrix_get() {
		return matrix.matrix_get();
	}
	
	
	
	
	public void matrix_set_identity() {
		matrix.matrix_set_identity();
	}
	
	
	
	
	
	public Matrix4f getMatrix4FromMatrix3(Matrix3f mat) {
		return matrix.getMatrix4FromMatrix3(mat);
	}
	
	
	
	
	public void matrix_add_from_3x3(Matrix3f mat3) {
		this.matrix.matrix_add_from_3x3(mat3);
	}
	
	
	
	
	public void matrix_add_from_4x4(Matrix4f mat4) {
		this.matrix.matrix_add_from_4x4(mat4);
	}
	
	
	/**
	 * This function is used to create a surface. Surfaces are used as dynamic textures.
	 * @param width
	 * @param height
	 * @return
	 */
	public Surface surface_create(int width, int height) {
		return new Surface(width, height);
		/*if (!GLContext.getCapabilities().GL_ARB_framebuffer_object) {
            System.out.println("FBO not supported!");
            return null;
        } else {            
        	return new Surface(width, height);
        }*/
	}
	
	
	
	/**
	 * With this function you set all further drawing to the target surface rather than the screen.
	 * @param surface
	 */
	public void surface_set_target(Surface surface) {
		if (surface == null)
			return;
		surface.activate();
	}
	
	
	
	/**
	 * With this function you reset all further drawing from the target surface back to the screen.
	 */
	public void surface_reset_target() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		Rectangle rect = this.room_current.runner.getWindow().getViewport();
		glViewport (rect.x, rect.y, rect.width, rect.height);
	}
	
	
	
	public boolean surface_exists(Surface surface) {
		if (surface == null || !surface.loaded)
			return false;
		
		return true;
	}
	
	
	
	/**
	 * This function returns a special pointer for the surface texture. This value can then be used in other draw functions, particularly in general 3D and some of the 2D primitive functions.
	 * @param surface
	 * @return
	 */
	public Texture2D surface_get_texture(Surface surface) {
		if (surface == null)
			return null;
		
		return surface.getTexture();
	}
	
	
	
	/**
	 * Plays the indicated sound once.
	 * @param sound
	 */
	public void sound_play(Sound sound) {
		sound.play(false);
	}
	
	
	
	/**
	 * Plays the indicated sound once, looping continuously.
	 * @param sound
	 */
	public void sound_loop(Sound sound) {
		sound.play(true);
	}
	
	
	
	/**
	 * Stops the indicated sound.
	 * @param sound
	 */
	public void sound_stop(Sound sound) {
		sound.stop();
	}
	
	
	
	/**
	 * Changes the volume for the indicated sound (0 = low, 1 = high).
	 * @param sound
	 */
	public void sound_set_volume(Sound sound, float volume) {
		sound.setVolume(volume);
	}
	
	
	
	/**
	 * This function returns the combined RGB values of a color.
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	public int make_color_rgb(int red, int green, int blue) {
		return new Color(red, green, blue).getRGB();
	}
	
	
	
	/**
	 * This function sets the current alpha value used for drawing.
	 * @param alpha
	 */
	public void draw_set_alpha(float alpha) {
		this.current_alpha = alpha;
		
		Color mcolor = new Color(current_colour);
		int modelColorLocation = GL20.glGetUniformLocation(current_shader.getShaderProgram().getID(), "VECTOR_OBJECT_COLOUR");
		GL20.glUniform4f(modelColorLocation, mcolor.getRed()/255f, mcolor.getGreen()/255f, mcolor.getBlue()/255f, current_alpha);
		mcolor = null;
	}
	
	
	
	
	/**
	 * @return current alpha value used for drawing.
	 */
	public float draw_get_alpha() {
		return this.current_alpha;
	}
	
	
	
	/**
	 * Draws the string at position (x,y), using the drawing color and alpha.
	 * @param x
	 * @param y
	 * @param string
	 */
	public void draw_text(float x, float y, String string) {
		Color mcolor = new Color(current_colour);
		int modelColorLocation = GL20.glGetUniformLocation(current_shader.getShaderProgram().getID(), "VECTOR_OBJECT_COLOUR");
		GL20.glUniform4f(modelColorLocation, mcolor.getRed()/255f, mcolor.getGreen()/255f, mcolor.getBlue()/255f, 1);
		
		current_font.DRAW_STYLE = halign;
		current_font.drawString(current_shader, string, x - view_xview, y - view_yview);
		
		mcolor = null;
	}
	
	
	
	
	/**
	 * Sets the font used when drawing strings.
	 * @param font
	 */
	public void draw_set_font(GLFont font) {
		this.current_font = font;
	}
	
	
	
	/**
	 * @return current alpha value used for drawing.
	 */
	public int draw_get_colour() {
		return this.current_colour;
	}
	
	
	
	
	/**
	 * This function draws the sprite assigned to the instance exactly as it would be drawn if the draw event held no code or actions.
	 */
	public void draw_self() {
		draw_sprite_ext(sprite_index, image_index, (int)x, (int)y, image_xscale, image_yscale, image_angle, draw_get_colour(), image_alpha);
	}
	
	
	
	/**
	 * With this function you can set the base draw color for the game. This value will affect all further drawing where appropriate, including fonts, models, and 3D. If any of those assets are drawn with their own color value changed, this value will be ignored.
	 * @param RGB Color
	 */
	public void draw_set_color(int color) {
		this.current_colour = color;
		this.room_current.setColour(color);
	}
	
	
	/**
	 * This function will set the reference value for the alpha testing. This is the "cut-off" threshold at which pixels with alpha will not be drawn. (0 - 255).
	 * @param alpha
	 */
	public void draw_set_alpha_threshold(float alpha) {
		this.room_current.setAlphaThreshold(alpha);
	}
	
	
	
	public void draw_set_alpha_clipping(boolean clip) {
		this.room_current.setAlphaClipping(clip);
	}
	
	
	
	/**
	 * This function can be used to clear the entire screen with a given colour and the alpha component of the destination is set to the value you have set.
	 * @param color
	 * @param alpha
	 */
	public void draw_clear_alpha(int color, float alpha) {
		Color mcolor = new Color(color);
		glClearColor (mcolor.getRed()/255f, mcolor.getGreen()/255f, mcolor.getBlue()/255f, alpha);
        glClear (GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	
	
	/**
	 * This functions draws a rectangle on the screen at the given coordinates.
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param outline
	 */
	public void draw_rectangle(float x1, float y1, float x2, float y2, boolean outline) {
		Runner.default_texture.bind();
		
		this.matrix.matrix_add_translation(x1 - view_xview, y1 - view_yview, 0);
		
		VBOModel temp = new VBOModel();
		temp.createQuad(0, 0, x2 - x1, y2 - y1);
		drawVBO(temp, current_colour, current_alpha);
		temp.destroy();
		
		this.matrix.matrix_reset();
	}
	
	
	
	/**
	 * This function draws a pixel on the screen at the given coordinates.
	 * @param x
	 * @param y
	 * @param color
	 */
	public void draw_point_color(int x, int y, int color) {
		Color mcolor = new Color(color);
		GL20.glUniform4f(room_current.runner.getCurrentShader().vector_object_colour_location, mcolor.getRed()/255f, mcolor.getGreen()/255f, mcolor.getBlue()/255f, current_alpha);
		
		GameConstants.quad.matrix_reset();
		GameConstants.quad.matrix_add_translation(x, y, 0);
		GameConstants.quad.draw(Runner.default_texture, room_current.runner.getCurrentShader());
		GameConstants.quad.matrix_reset();
	}
	
	
	
	
	/**
	 * 
	 * @param surface
	 * @param x
	 * @param y
	 */
	public void draw_surface(Surface surface, float x, float y) {
		this.draw_surface_ext(surface, x, y, 1, 1, 0, current_colour, current_alpha);
	}
	
	
	
	
	/**
	 * 
	 * @param surface
	 * @param x
	 * @param y
	 * @param xscale
	 * @param yscale
	 * @param angle
	 * @param color
	 * @param alpha
	 */
	public void draw_surface_ext(Surface surface, float x, float y, float xscale, float yscale, float angle, int color, float alpha) {
		room_current.draw_surface_ext(surface, x, y, xscale, yscale, angle, color, alpha, matrix);
	}
	
	
	
	/**
	 * This functions draws a sprite on the screen at the given coordinates.
	 * @param sprite
	 * @param image_index
	 * @param x
	 * @param y
	 */
	public void draw_sprite(Sprite<?> sprite, float image_index, float x, float y) {
		this.draw_sprite_ext(sprite, image_index, x, y, image_xscale, image_yscale, image_angle, current_colour, current_alpha);
	}
	
	
	
	
	/**
	 * This function will draw the given sprite as in the function draw_sprite but with additional options to change the scale, blending, rotation and alpha of the sprite being drawn. Changing these values does not modify the resource in any way (only how it is drawn), and you can use any of the available sprite variables instead of direct values for all the arguments in the function.
	 * @param sprite
	 * @param image_index
	 * @param x
	 * @param y
	 * @param xscale
	 * @param yscale
	 * @param rot
	 * @param colour
	 * @param alpha
	 */
	public void draw_sprite_ext(Sprite<?> sprite, float image_index, float x, float y, float xscale, float yscale, float rot, int colour, float alpha) {
		room_current.draw_sprite_ext(sprite, image_index, x, y, xscale, yscale, rot, colour, alpha, matrix);
	}
	
	
	
	
	/**
	 * This function will draw the given sprite with the given colour and alpha, however it will use the given matrix for all transformations.
	 * @param sprite
	 * @param image_index
	 * @param colour
	 * @param alpha
	 * @param matrix
	 */
	public void draw_sprite_mat(Sprite<?> sprite, float image_index, int colour, float alpha, ModelMatrix matrix) {
		if (sprite == null)
			return;
		room_current.draw_sprite_mat(sprite, image_index, matrix, colour, alpha);
	}
	
	
	
	private void drawVBO(VBOModel model, int color, float alpha) {
		room_current.drawVBO(model, color, alpha, matrix);
	}
	
	
	
	/**
	 * This function sets the horizontal text alignment.
	 * @param align
	 */
	public void draw_set_halign(int align) {
		this.halign = align;
		room_current.draw_set_halign(align);
	}
	
	
	
	/**
	 * This function sets the vertical text alignment.
	 * @param align
	 */
	public void draw_set_valign(int align) {
		this.valign = align;
		room_current.draw_set_valign(align);
	}
	
	
	
	/**
	 * This function is useful for knowing how many of a given instance exist.
	 * @param class1
	 * @return the amount of objects instanced from a specific class in the current room.
	 */
	public int instance_number(Class<? extends GameObject> class1) {
		return room_current.instance_number(class1);
	}
	
	
	
	/**
	 * This functions destroys an object from the room.
	 */
	public void instance_destroy() {
		room_current.instance_destroy(this);
	}
	
	
	
	/**
	 * This functions destroys all object from the room that are a child of class1.
	 */
	public void instance_destroy_all(Class<? extends GameObject> class1) {
		room_current.instance_destroy_all(class1);
	}
	
	
	
	/**
	 * This function finds the first instance of or child to the given class.
	 */
	public GameObject instance_first(Class<? extends GameObject> class1) {
		return room_current.instance_first(class1);
	}
	
	
	
	/**
	 * creates an instance of an object at the specified x/y coordinates.
	 * @param class1
	 * @return the class created as a Java object.
	 */
	public GameObject instance_create(float x, float y, Class<? extends GameObject> class1) {
		return room_current.instance_create(x, y, class1);
	}
	
	
	
	/**
	 * Restarts the game.
	 */
	public void game_restart() {
		room_current.game_restart();
	}
	
	
	
	/**
	 * Ends the game.
	 */
	public void game_end() {
		Runner.gameEnd();
	}
	
	
	
	/**
	 * This function is used to restart the current room.
	 */
	public void room_restart() {
		room_current.changeRoom(room_current.getClass());
	}
	
	
	
	/**
	 * This function is used to jump to a specific room.
	 * @param room
	 */
	public void room_goto(Class<? extends Room> room) {
		room_current.changeRoom(room);
	}
	
	
	
	/**
	 * This function sets the games room to the room directly following the current room.
	 */
	public void room_goto_next() {
		room_current.room_goto_next();
	}
	
	
	
	
	/**
	 * This function sets the games room to the room directly behind the current room.
	 */
	public void room_goto_previous() {
		room_current.room_goto_previous();
	}
	
	
	
	/**
	 * @param button
	 * @return Returns whether the mouse button is currently down (use as values mb_left, mb_middle, or mb_right).
	 */
	public boolean mouse_check_button(int button) {
		return room_current.mouse_check_button(button);
	}
	
	
	
	/**
	 * @param key
	 * @return whether or not a key is being pressed.
	 */
	public boolean keyboard_check_pressed(int key) {
		return room_current.keyboard_check_pressed(key);
	}
	
	
	
	/**
	 * @param key
	 * @return whether or not a key is being held down.
	 */
	public boolean keyboard_check(int key) {
		return room_current.keyboard_check(key);
	}
	
	
	
	/**
	 * @param key
	 * @return whether or not a key has just been released
	 */
	public boolean keyboard_check_released(int key) {
		return room_current.keyboard_check_released(key);
	}
	
	
	
	/**
	 * Indicates whether to automatically redraw the room (true, default) or not (false).
	 * @param value
	 */
	public void set_automatic_draw(boolean value) {
		room_current.set_automatic_draw(value);
	}
	
	
	/**
	 * Sets the position of the mouse (based on the windows location).
	 * @param mouse_x
	 * @param mouse_y
	 */
	public void window_mouse_set(int mouse_x, int mouse_y) {
		Mouse.setCursorPosition(mouse_x, mouse_y);
		this.mouse_x = mouse_x;
		this.mouse_y = mouse_y;
	}
	
	
	
	/**
	 * Sets whether the window is shown in full screen mode.
	 * @param full
	 */
	public void window_set_fullscreen(boolean full) {
		room_current.window_set_fullscreen(full);
	}
	
	
	
	
	/**
	 * Returns whether the window is shown in full screen mode.
	 * @return fullscreen mode
	 */
	public boolean window_get_fullscreen() {
		return room_current.window_get_fullscreen();
	}
	
	
	
	
	/**
	 * Sets whether the window is shown in full screen mode
	 * @param show
	 */
	public void window_set_showborder(boolean show) {
		room_current.window_set_showborder(show);
	}
	
	
	
	
	/**
	 * Sets whether the window is sizeable by the player. (The player can only size it when the border is shown and the window is not in full screen mode.)
	 * @param sizeable
	 */
	public void window_set_sizeable(boolean sizeable) {
		room_current.window_set_sizeable(sizeable);
	}
	
	
	
	
	/**
	 * Returns whether the border around the window is shown in windowed mode.
	 * @param showborder
	 */
	public boolean window_get_showborder() {
		return !Boolean.parseBoolean(System.getProperty("org.lwjgl.opengl.Window.undecorated"));
	}
	
	
	
	
	/**
	 * Gives the horizontal component of a vector with the given length and direction.
	 * @param length
	 * @param angle
	 * @return Returns the horizontal component of the given vector.
	 */
	public float lengthdir_x(float length, float angle){
		return (float) (Math.cos(Math.toRadians(angle))*length);
	}
	
	
	
	
	/**
	 * Gives the vertical component of a vector with the given length and direction.
	 * @param length
	 * @param angle
	 * @return Returns the vertical component of the given vector.
	 */
	public float lengthdir_y(float length, float angle){
		return (float) (-Math.sin(Math.toRadians(angle))*length);
	}
	
	
	
	/**
	 * This function is used to calculate the distance between two points
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return distance between two points.
	 */
	public float point_distance(float x1,float y1,float x2,float y2){
		return (float) Math.sqrt(((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)));
	}
	
	
	
	/**
	 * This function is used similarly to point_distance, but contains an additional point for 3D space.
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @return distance between three points.
	 */
	public float point_distance_3d(float x1,float y1, float z1,float x2,float y2, float z2){
		return (float) Math.sqrt(((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)) + ((z1-z2)*(z1-z2)));
	}
	
	
	
	/**
	 * This function returns the direction of a vector formed by the specified components [x1,y1] and [x2,y2] in relation to the fixed x/y coordinates of the room.
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return direction in degrees between two points.
	 */
	public float point_direction(float x1,float y1,float x2,float y2) {
		float ret = (float) (Math.toDegrees((float) (Math.atan2(( y1 - y2),-(x1 - x2)))));
		if (ret < 0)
			ret += 360;
		if (ret > 360)
			ret -= 360;
		return ret;
	}
	
	
	
	/**
	 * Sets the motion with the given speed in the given direction.
	 * @param direction
	 * @param speed
	 */
	public void motion_set(float direction, float speed) {
		// Set the new direction/speed
		this.direction = direction;
		this.speed = speed;
		
		// Update the hspeed/vspeed from speed/direction
		this.movementVectorLogic();
	}
	
	
	
	/**
	 * Adds the motion to the current motion (as a vector addition).
	 * @param direction
	 * @param speed
	 */
	public void motion_add(float direction, float speed) {
		if (speed == 0)
			return;
		
		// Update hspeed/vspeed incase changed
		this.movementVectorLogic();
		
		// Calculate new hspeed/vspeed
		this.hspeed += (float) (Math.cos(Math.toRadians(direction))) * speed;
		this.vspeed += (float) (-Math.sin(Math.toRadians(direction))) * speed;
		
		// Update the hspeed/vspeed from speed/direction
		this.movementVectorLogic();
	}
	
	
	
	/**
	 * This function returns the linearized interpolation between two values by a given percent: 0 = start, 1 = end.
	 * @param start
	 * @param end
	 * @param percent
	 * @return
	 */
	public float lerp(float start, float end, float percent) {
		return (start + percent * (end - start));
	}
	
	
	
	protected void pretick() {
		// Fix direction
		if (this.direction < 0)
			this.direction += 360;
		if (this.direction > 360)
			this.direction -= 360;
		
		Mouse.poll();
		while (Mouse.next()) {
			mouse_x = (int) (view_xview + Mouse.getX());
			mouse_y = (int) (view_yview + (Display.getHeight() - Mouse.getY()));
		}
		
		// Alarm logic
		for (int i = 0; i < alarm.length; i++)
			alarm[i]--;
		checkAlarms();
	}
	
	
	
	protected void posttick() { // This is called after the end step event
		// Apply gravity to the object
		motion_add(gravity_direction, gravity);
		
		xprevious = x;
		yprevious = y;
		x += hspeed;
		y += vspeed;
		
		// update friction (do this after movement, so that velocity the next step will overwrite)
		if (speed > 0) {
			if (speed - friction > 0)
				motion_set(direction, speed - friction);
			else
				motion_set(direction, 0);
		}
		
		// Update the sprite animation
		if (sprite_index != null) {
			image_index += image_speed;
			if (image_index >= sprite_index.getAmountImages()) {
				image_index = 0;
				event_animation_end();
			}
		}
	}
	
	
	
	protected void movementVectorLogic() { // This method controls how speed/direction communicates with hspeed/vspeed
		if (speed != speedprevious || direction != directionprevious) { // User changed speed/direction variables, update hspeed/vspeed
			hspeed = (float) (Math.cos(Math.toRadians(direction))) * speed;
			vspeed = (float) (-Math.sin(Math.toRadians(direction))) * speed;
		}else if (hspeedprevious != hspeed || vspeedprevious != vspeed) { // User changed the hspeed/vspeed variables, update speed/direction
			speed = (float) Math.sqrt((hspeed * hspeed) + (vspeed * vspeed));
			direction = point_direction(0, 0, hspeed, vspeed);
		}
		
		// Reset previous variables
		hspeedprevious = hspeed;
		vspeedprevious = vspeed;
		speedprevious = speed;
		directionprevious = direction;
	}
	
	
	
	/**
	 * This method connects to an ip/port using TCP.
	 * @param ip
	 * @param port
	 * @param blocking
	 * @return a new Socket connection
	 */
	public SocketChannel tcpconnect(String ip, int port, int blocking) {
		try {
			SocketChannel sChannel = SocketChannel.open();
		    sChannel.configureBlocking(blocking == 1);
		    sChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
		    sChannel.connect(new InetSocketAddress(ip, port));
		    
		    // Wait for socket to connect.
			while(!sChannel.finishConnect()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					//
				}
			}
		    
		    return sChannel;
		} catch (IOException e) {
			System.out.println("Could not connect to server: " + ip);
		}
		return null;
	}
	
	
	
	/**
	 * This method creates a listening socket on a port
	 * @param port
	 * @param maxconnections
	 * @param blocking
	 * @return
	 */
	public ServerSocketChannel tcplisten(int port, int maxconnections, int blocking) {
		try {
			// Create a new selector
		    Selector socketSelector = SelectorProvider.provider().openSelector();

		    // Create a new non-blocking server socket channel
		    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(blocking == 1);
			
		    // Bind the server socket to the specified address and port
		    InetSocketAddress isa = new InetSocketAddress(port);
		    serverSocketChannel.socket().bind(isa);

		    // Register the server socket channel, indicating an interest in 
		    // accepting new connections
		    serverSocketChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
			
			return serverSocketChannel;
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + port);
		}return null;
	}

	
	
	
	/**
	 * This method creates a new client connection (see tcplisten).
	 * @param soc
	 * @param blocking
	 * @return
	 */
	public SocketChannel tcpaccept(ServerSocketChannel soc, int blocking) {
		try {
			SocketChannel socket = soc.accept();
			socket.configureBlocking(blocking == 1);
			socket.setOption(StandardSocketOptions.TCP_NODELAY, true);
			
			return socket;
		} catch (Exception localException) {
			
		}
		return null;
	}
	
	
	
	/**
	 * This method returns the size in bytes incoming from a socket. It also fills this the current data buffer with the incoming data.
	 * @param client
	 * @return
	 */
	public int receivemessage(SocketChannel socket) {
		if (socket == null || !socket.isConnected())
			return -1;
		
		ByteBuffer buf = ByteBuffer.allocateDirect(1024);
		
		int amountBytes = -1;
		try { amountBytes = socket.read(buf); } catch (IOException e) {}
		
		if (amountBytes != -1) {
			buf.flip();
			
			this.buffer.in.clear();
			this.buffer.inpointer = 0;
			for (int i = 0; i < amountBytes; i++) {
				this.buffer.append(buf.get(i) & 0xFF);
			}
		}
		
		return amountBytes;
		/*Socket client = socket.socket();
		
		int amountBytes = 0;
		try {
			InputStream stream = client.getInputStream();
			amountBytes = stream.available();
			this.in = new DataInputStream(stream);
		} catch (Exception localException) {
			//
		}
		int msg = 0;
		if (amountBytes > 0) {
			try {
				msg = this.in.read();
				this.buffer.in.clear();
				this.buffer.inpointer = 0;
				for (int i = 0; i < msg + 1; i++) {
					int read = this.in.read();
					if (i > 0)
						this.buffer.append(read);
				}
			}
			catch (Exception localException1) {
			}
		}
		this.in = null;
		return msg;*/
	}

	
	
	/**
	 * This method sends this objects buffer data over a socket
	 * @param client
	 */
	public void sendmessage(SocketChannel socket) {
		/*try {
			Socket client = socket.socket();
			socket.
			if (client != null) {
				OutputStream outst = client.getOutputStream();

				BufferedOutputStream bos = new BufferedOutputStream(outst);
				if (bos != null) {
					if (client instanceof EmptySocket) {
						byte[] bytes = new byte[2 + this.buffer.out.size()];
						bytes[0] = (byte) this.buffer.out.size();
						bytes[1] = 0;
						for (int i = 0; i < this.buffer.out.size(); i++) {
							int bytesend = ((Integer)this.buffer.out.get(i)).intValue();
							bytes[2 + i] = (byte) bytesend;
						}
						
						((EmptySocket)client).fakeReceive(bytes);

						this.buffer.in.clear();
						bos.flush();
						return;
					}
					bos.write(this.buffer.out.size());
					bos.write(0);
					for (int i = 0; i < this.buffer.out.size(); i++) {
						int bytesend = ((Integer)this.buffer.out.get(i)).intValue();
						bos.write(bytesend);
					}

					this.buffer.in.clear();
					bos.flush();
				}
			} else {
				this.buffer.in.clear();
			}
		} catch (Exception e) {
			//
		}*/
		
		
		try {
			if (socket != null) {
				//ByteBuffer buffer = ByteBuffer.wrap(this.buffer.out.size() + 2);  
				ByteBuffer packetBuffer = ByteBuffer.allocate(this.buffer.out.size());
				packetBuffer.clear();
				for (int i = 0; i < this.buffer.out.size(); i++) {
					int send = this.buffer.out.get(i);
					packetBuffer.put((byte)send);
				}
				
				packetBuffer.flip();
				while(packetBuffer.hasRemaining()) {
					socket.write(packetBuffer);
				}
			}
			this.buffer.in.clear();
		} catch (Exception e) {
			//
		}
	}
	
	
	/**
	 * This function writes a byte of data to this objects data buffer.
	 * @param i
	 */
	public void writebyte(int i) {
		this.buffer.out.add(Integer.valueOf(i));
	}

	
	
	/**
	 * This function writes a 4-byte integer to this objects data buffer.
	 * @param i
	 */
	public void writeint(int i) {
		int val4 = i >> 24;
		int val3 = i >> 16 & 0xFF;
		int val2 = i >> 8 & 0xFF;
		int val1 = i & 0xFF;
		writebyte(val1);
		writebyte(val2);
		writebyte(val3);
		writebyte(val4);
	}

	
	
	/**
	 * This function writes a 2-byte short to this objects data buffer.
	 * @param i
	 */
	public void writeshort(int i) {
		int val2 = i >> 8 & 0xFF;
		int val1 = i & 0xFF;
		writebyte(val1);
		writebyte(val2);
	}
	
	
	
	/**
	 * This function writes a string to this objects data buffer. The end of the string is marked with a byte value of 0.
	 * @param str
	 */
	public void writestring(String str) {
		for (int i = 0; i < str.length(); i++) {
			int write = str.charAt(i);
			if (write > 0)
				writebyte(write);
		}
		writebyte(0);
	}

	
	
	/**
	 * This function clears the current data buffer used for socket connections.
	 */
	public void buffer_clear() {
		this.buffer.clear();
	}

	public void log(String str) {
		System.out.println(str);
	}

	
	
	/**
	 * This function reads a byte of data from this objects data buffer.
	 * @return
	 */
	public int readbyte() {
		return this.buffer.getNextIn();
	}
	
	
	
	/**
	 * This function reads a byte of data from this objects data buffer.
	 * @return
	 */
	public boolean buffer_has_next() {
		return this.buffer.hasNextIn();
	}

	
	
	/**
	 * This function prints this objects data buffer to the console.
	 */
	public void buffer_dump() {
		log("CURRENT INCOMING BUFFER SIZE: " + this.buffer.in.size() + "  byte");
		log("BUFFER DATA: ");
		for (int i = 0; i < this.buffer.in.size(); i++) {
			log("    " + this.buffer.in.get(i));
		}
		log("CURRENT OUTGOING BUFFER SIZE: " + this.buffer.out.size() + "  byte");
		log("BUFFER DATA: ");
		for (int i = 0; i < this.buffer.out.size(); i++)
			log("    " + this.buffer.out.get(i));
	}

	
	
	/**
	 * This function reads a string from this objects data buffer.
	 * @return
	 */
	public String readstring() {
		String str = "";
		boolean found = false;
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		for (int i = 0; i < this.buffer.in.size(); i++) {
			if (!found) {
				byte set = (byte)readbyte();
				bytes.add(Byte.valueOf(set));
				if (set == 0) {
					found = true;
				}
			}
		}
		if (!found) {
			return "";
		}
		byte[] byteArray = new byte[bytes.size()];
		for (int i = 0; i < bytes.size() - 1; i++) {
			byteArray[i] = ((Byte)bytes.get(i)).byteValue();
		}
		str = new String(byteArray);
		str = str.substring(0,str.length()-1);
		bytes.clear();
		return str;
	}

	
	
	/**
	 * This function reads a 4-byte integer from this objects data buffer.
	 * @return
	 */
	public int readint() {
		int[] array = new int[4];
		array[0] = this.buffer.getNextIn();
		array[1] = this.buffer.getNextIn();
		array[2] = this.buffer.getNextIn();
		array[3] = this.buffer.getNextIn();
		return toInt(array);
	}
	
	
	
	/**
	 * This function reads a 2-byte short from this objects data buffer.
	 * @return
	 */
	public int readshort() {
		int[] array = new int[2];
		array[0] = this.buffer.getNextIn();
		array[1] = this.buffer.getNextIn();
		return toInt(array);
	}
	
	private int toInt(int[] bytes) {
		int ret = 0;
		for (int i = 0; i < bytes.length; i++) {
			ret <<= 8;
			ret |= bytes[((bytes.length-1) - i)];
		}
		return ret;
	}
	

	@Override
	public void event_draw() {
		draw_self();
	}
	
	public void event_outside_room() {};
	public void event_game_start() {};
	public void event_game_end() {};
	public void event_room_start() {};
	public void event_room_end() {};
	public void event_animation_end() {};
	public void event_out_of_lives() {};
	public void event_out_of_health() {};
	public void event_alarm_0() {};
	public void event_alarm_1() {};
	public void event_alarm_2() {};
	public void event_alarm_3() {};
	public void event_alarm_4() {};
	public void event_alarm_5() {};
	public void event_alarm_6() {};
	public void event_alarm_7() {};
	public void event_alarm_8() {};
	public void event_alarm_9() {};
	public void event_alarm_10() {};
	public void event_alarm_11() {};
	public void event_user_0() {};
	public void event_user_1() {};
	public void event_user_2() {};
	public void event_user_3() {};
	public void event_user_4() {};
	public void event_user_5() {};
	public void event_user_6() {};
	public void event_user_7() {};
	public void event_user_8() {};
	public void event_user_9() {};
	public void event_user_10() {};
	public void event_user_11() {};
	
	/**
	 * Calls the user event corresponding to the parameter.
	 * @param event
	 */
	public void event_user(int event) {
		if (event == 0)
			event_user_0();
		else if (event == 1)
			event_user_1();
		else if (event == 2)
			event_user_2();
		else if (event == 3)
			event_user_3();
		else if (event == 4)
			event_user_4();
		else if (event == 5)
			event_user_5();
		else if (event == 6)
			event_user_6();
		else if (event == 7)
			event_user_7();
		else if (event == 8)
			event_user_8();
		else if (event == 9)
			event_user_9();
		else if (event == 10)
			event_user_10();
		else if (event == 11)
			event_user_11();
	}
	
	private void checkAlarms() {
		if (alarm[0] == 0)
			event_alarm_0();
		if (alarm[1] == 0)
			event_alarm_1();
		if (alarm[2] == 0)
			event_alarm_2();
		if (alarm[3] == 0)
			event_alarm_3();
		if (alarm[4] == 0)
			event_alarm_4();
		if (alarm[5] == 0)
			event_alarm_5();
		if (alarm[6] == 0)
			event_alarm_6();
		if (alarm[7] == 0)
			event_alarm_7();
		if (alarm[8] == 0)
			event_alarm_8();
		if (alarm[9] == 0)
			event_alarm_9();
		if (alarm[10] == 0)
			event_alarm_10();
		if (alarm[11] == 0)
			event_alarm_11();
	}
}
