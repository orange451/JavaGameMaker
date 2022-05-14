package net.mantagames.jgm.engine;

import org.lwjgl.opengl.GL20;

import net.mantagames.jgm.engine.gl.Texture2D;
import net.mantagames.jgm.engine.gl.mesh.VBOModel;

public class Background {
	private Texture2D texture;
	private VBOModel model;
	private int width;
	private int height;
	protected float hspeed;
	protected float vspeed;
	
	public Background(Texture2D texture) {
		this.setTexture(texture);
	}
	
	public Background() {
	}

	public Background setTexture(Texture2D texture) {
		this.width = texture.getWidth();
		this.height = texture.getHeight();
		this.texture = texture;
		this.model = new VBOModel();
		this.reconstructBackground();
		return this;
	}
	
	private void reconstructBackground() {
		if (this.model != null) {
			this.model.destroy();
		}
		model.createQuadExt(0, 0, 0, width, height, 0, 0, 0, 1, 1);
	}
	
	private int getStartX(float x) {
		float startX = 0;
		if (x > 0) {
			startX = x;
			while (startX > 0)
				startX -= width;
		}else if (x < -width) {
			startX = x;
			while (startX < -width)
				startX += width;
		}
		
		return (int) startX;
	}
	
	private int getStartY(float y) {
		float startY = 0;
		if (y > 0) {
			startY = y;
			while (startY > 0)
				startY -= height;
		}else if (y < -height) {
			startY = y;
			while (startY < -height)
				startY += height;
		}
		return (int) startY;
	}

	protected void draw(float x, float y, Runner runner) {
		if (model == null || RenderProperties.is3dModeOn)
			return;
		
		GL20.glUniform4f(runner.getCurrentShader().vector_object_colour_location, 1, 1, 1, 1);
		int startX = getStartX(x);
		int startY = getStartY(y);
		for (int i = startX; i <= Runner.getCurrentRoom().room_width; i += width) {
			for (int ii = startY; ii <= Runner.getCurrentRoom().room_height; ii += height) {
				float drawX = i - Runner.getCurrentRoom().view_xview;
				float drawY = ii - Runner.getCurrentRoom().view_yview;
				
				if (drawX + width < 0 || drawX > Runner.getCurrentRoom().view_wview)
					continue;
				if (drawY + height < 0 || drawY > Runner.getCurrentRoom().view_hview)
					continue;
				model.draw(texture, runner.getCurrentShader(), drawX, drawY, 0);
			}	
		}
	}
	
	protected int getWidth() {
		return width;
	}
	
	protected int getHeight() {
		return height;
	}

	protected Texture2D getTexture() {
		return texture;
	}
}
