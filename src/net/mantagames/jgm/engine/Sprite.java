package net.mantagames.jgm.engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL20;

import net.mantagames.jgm.engine.gl.Texture2D;
import net.mantagames.jgm.engine.gl.Vector;
import net.mantagames.jgm.engine.gl.mesh.ModelMatrix;
import net.mantagames.jgm.engine.gl.mesh.VBOModel;
import net.mantagames.jgm.engine.gl.util.TextureUtils;

public class Sprite<E> {
	private Texture2D[] textures;
	private BufferedImage[] bufferedImage;
	private VBOModel model;
	private Vector image_origin;
	private int width;
	private int height;
	private SpritePrecision precision = SpritePrecision.PIXEL;
	
	enum SpritePrecision {
		PIXEL, RADIUS, RECTANGLE;
	}
	
	public Sprite(Texture2D texture) {
		this();
		this.addTexture(texture);
	}
	
	public Sprite() {
		this.image_origin = new Vector(0, 0, 0);
	}
	
	public SpritePrecision getSpritePrecision() {
		return this.precision;
	}

	public void setImageOrigin(Vector v) {
		this.image_origin = v;
		this.reconstructSprite();
	}
	
	public void addTexture(String filePath) {
		Texture2D texture = TextureUtils.loadTexture(filePath);
		this.addTexture(texture);
	}
	
	public void addTexture(Texture2D texture) {
		if (textures == null) {
			this.width = texture.getWidth();
			this.height = texture.getHeight();
			textures = new Texture2D[1];
			bufferedImage = new BufferedImage[1];
			this.reconstructSprite();
			textures[0] = texture;
			this.bufferedImage[0] = texture.getImage();
		}else{
			Texture2D[] temp = textures;
			textures = new Texture2D[temp.length + 1];
			for (int i = 0; i < temp.length; i++)
				textures[i] = temp[i];
			textures[temp.length] = texture;
			
			BufferedImage[] temp2 = bufferedImage;
			bufferedImage = new BufferedImage[temp2.length + 1];
			for (int i = 0; i < temp2.length; i++)
				bufferedImage[i] = temp2[i];
			bufferedImage[temp2.length] = texture.getImage();
		}
	}
	
	public static class SpriteMaskPixel {
		public int x;
		public int y;
		
		public SpriteMaskPixel(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	protected static SpriteMaskPixel[][] getSpriteMask(BufferedImage image, float x, float y) {
		SpriteMaskPixel[][] mask = new SpriteMaskPixel[image.getWidth()][image.getHeight()];
		int pixel, a;
		for(int i = 0; i < image.getWidth(); i++){ // for every (x,y) component in the given box, 
			for( int j = 0; j < image.getHeight(); j++){
				pixel = image.getRGB(i, j); // get the RGB value of the pixel
				a= (pixel >> 24) & 0xff;

				if(a != 0){  // if the alpha is not 0, it must be something other than transparent
					mask[i][j] = new SpriteMaskPixel((int)x + i, (int)y + j);
				}
			}
		}
		
		return mask;
	}
	
	/*
	protected static HashSet<String> getSpriteMask(BufferedImage image, float x, float y) {
		HashSet<String> mask = new HashSet<String>();
		int pixel, a;
		for(int i = 0; i < image.getWidth(); i++){ // for every (x,y) component in the given box, 
			for( int j = 0; j < image.getHeight(); j++){
				pixel = image.getRGB(i, j); // get the RGB value of the pixel
				a= (pixel >> 24) & 0xff;

				if(a != 0){  // if the alpha is not 0, it must be something other than transparent
					mask.add((x+i)+","+(y+j)); // add the absolute x and absolute y coordinates to our set
				}
			}
		}
		
		return mask;
	}
	 */

	private void reconstructSprite() {
		if (this.model != null) {
			this.model.destroy();
		}else{
			this.model = new VBOModel();
		}
		model.createQuadExt(-image_origin.getX(), -image_origin.getY(),0,  width-image_origin.getX(), height-image_origin.getY(), 0, 0, 0, 1, 1);
	}

	protected void draw(Runner runner, int color, float alpha, int image_index, ModelMatrix matrix) {
		if (model == null || textures == null)
			return;
		
		// Force the image_index to the proper number;
		while (image_index >= textures.length)
			image_index -= textures.length;
		while (image_index < 0)
			image_index += textures.length;
		
		Color mcolor = new Color(color);
		GL20.glUniform4f(runner.getCurrentShader().vector_object_colour_location, mcolor.getRed()/255f, mcolor.getGreen()/255f, mcolor.getBlue()/255f, alpha);
		
		model.matrix_set_from_4x4(matrix.matrix_get());
		model.draw(textures[image_index], runner.getCurrentShader());
		
		mcolor = null;
		
	}

	public Sprite centerOrigin() {
		this.setImageOrigin(new Vector(width/2, height/2, 0));
		return this;
	}

	protected int getWidth() {
		return width;
	}
	
	protected int getHeight() {
		return height;
	}

	protected Texture2D getTexture(int image_index) {
		return textures[image_index];
	}

	protected int getAmountImages() {
		if (textures == null)
			return 0;
		return textures.length;
	}
    
    public BufferedImage getImage(int index) {
    	return this.bufferedImage[index];
    }

	public Vector getImageOrigin() {
		return this.image_origin;
	}
	
	/**
	 * Returns a bufferedImage with an exact mask of the sprite with rotation. use getCollisionRectangle() first.
	 * @param collisionRectangle
	 * @param index
	 * @param rotation
	 * @return
	 */
	public BufferedImage getRotationalBufferedImage(Rectangle collisionRectangle, int index, float rotation, float xscale, float yscale) {
		BufferedImage ret = new BufferedImage(collisionRectangle.width, collisionRectangle.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) ret.getGraphics();
		
		if (rotation == 0) {
			g.drawImage(this.bufferedImage[index], 0, 0, (int) (width * xscale), (int) (height * yscale), null);
		} else {
			float originX = (collisionRectangle.width/2);
			float originY = (collisionRectangle.height/2);
			
			float x = - image_origin.getX();
			float y = - image_origin.getY();
			
	        AffineTransform at = new AffineTransform();
	        at.translate(originX, originY);
	        at.scale(xscale, yscale);
	        at.rotate(Math.toRadians(-rotation));
	        at.translate(x, y);
	        g.drawImage(this.bufferedImage[index], at, null);
		}
        /*if (rotation != 0) {
        	try{
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, ret.getWidth(), ret.getHeight());
                g.setColor(Color.WHITE);
                g.drawImage(this.bufferedImage[index], at, null);
                
            	File outputfile = new File("image.png");
    			ImageIO.write(ret, "png", outputfile);
            	System.out.println("SAVED: " + outputfile.getAbsolutePath());
        	}catch(Exception e) {
        		e.printStackTrace();
        	}
        }*/
		return ret;
	}
	
	/**
	 * Use getRotationalBufferedImage() to get the bufferedImages
	 * @param sprite1
	 * @param x1
	 * @param y1
	 * @param sprite2
	 * @param x2
	 * @param y2
	 * @return
	 */
	public boolean isCollidingWith(BufferedImage sprite1, float x1, float y1, BufferedImage sprite2, float x2, float y2) {
		SpriteMaskPixel[][] mask1 = Sprite.getSpriteMask(sprite1, x1, y1);
		SpriteMaskPixel[][] mask2 = Sprite.getSpriteMask(sprite2, x2, y2);
		
		int width1  = sprite1.getWidth();
		int height1 = sprite1.getHeight();
		int width2  = sprite2.getWidth();
		int height2 = sprite2.getHeight();
		int minx = (int) Math.max(x1, x2);
		int maxx = (int) Math.min(x1 + width1, x2 + width2);
		int miny = (int) Math.max(y1,  y2);
		int maxy = (int) Math.min(y1 + height1, y2 + height2);
		
		for (int i = minx; i < maxx; i++) {
			for (int j = miny; j < maxy; j++) {
				int relativex1 = (int) (i - x1);
				int relativey1 = (int) (j - y1);
				int relativex2 = (int) (i - x2);
				int relativey2 = (int) (j - y2);
				
				// There exists a pixel in both sprite masks
				if (mask1[relativex1][relativey1] != null && mask2[relativex2][relativey2] != null) {
					return true;
				}
			}
		}
		
		return false;
		
		//HashSet<String> mask1 = Sprite.getSpriteMask(sprite1, x1, y1);
		//HashSet<String> mask2 = Sprite.getSpriteMask(sprite2, x2, y2);
		
		/*
		Iterator<String> it = mask1.iterator();
		while (it.hasNext()) {
			if (mask2.contains(it.next())) {
				return true;
			}
		}
		return false;
		*/
		
		/*mask1.retainAll(mask2);
		if(mask1.size() > 0){
			return true;
		}
		return false;
		*/
	}

	/**
	 * Returns a rectangle that the sprite will ALWAYS be inside no matter what rotation
	 * @param x
	 * @param y
	 * @param image_angle 
	 * @return
	 */
	public Rectangle getCollisionRectangle(float x, float y, float xscale, float yscale, float image_angle) {
		if (image_angle == 0) {
			Rectangle ret = new Rectangle((int)(x - (this.image_origin.getX()) * xscale), (int)(y - (this.image_origin.getY()) * yscale), (int)(width * xscale), (int)(height * yscale));
			return ret;
		}else{
			float dist1 = image_origin.clone().getMagnitude();
			float dist2 = image_origin.clone().subtract(width, 0, 0).getMagnitude();
			float dist3 = image_origin.clone().subtract(0, height, 0).getMagnitude();
			float dist4 = image_origin.clone().subtract(width, height, 0).getMagnitude();
			float scale = Math.max(xscale, yscale);
			float dist = Math.max(dist1, Math.max(dist2, Math.max(dist3, dist4))) * scale;
			
			int rectX = (int) (x - dist);
			int rectY = (int) (y - dist);
			int size = (int) (dist * 2);// + (Math.max(width, height));
			Rectangle ret = new Rectangle(rectX, rectY, size, size);
			return ret;
		}
	}
	
	/**
	 * Loads a sprite-strip into the sprite. PNG ONLY.
	 * @param filePath
	 */
	public void create_sprite_from_strip(String filePath) {
		if (filePath.toLowerCase().contains(".png")) {
			if (filePath.toLowerCase().contains("_strip")) {
				BufferedImage totalImage = TextureUtils.loadBufferedImage(filePath);
				int stripPos = filePath.indexOf("_strip");
				String iDontReallyKnowFuckThisVariableOkay = filePath.substring(stripPos, filePath.length()).toLowerCase();
				String tempNumber = iDontReallyKnowFuckThisVariableOkay.replace("_strip", "").replace(".png", "");
				int numberOfFrames = Integer.parseInt(tempNumber);
				int width = totalImage.getWidth() / numberOfFrames;
				int height = totalImage.getHeight();
				for (int i = 0; i < numberOfFrames; i++) {
					BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					newImage.getGraphics().drawImage(totalImage, -i * width, 0, null);
					Texture2D glTexture = TextureUtils.loadTexture(newImage);
					addTexture(glTexture); // Fuck yah! We did it!
				}
			}else{
				System.err.println("create_sprite_from_strip() needs \"_strip#\" at the end of the filename.");
			}
		}else{
			System.err.println("create_sprite_from_strip() can only load PNG images");
		}
	}
}
