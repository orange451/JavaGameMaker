package net.mantagames.jgm.engine.gl.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.mantagames.jgm.engine.Shader;
import net.mantagames.jgm.engine.gl.Texture2D;
import net.mantagames.jgm.engine.gl.mesh.ModelMatrix;
import net.mantagames.jgm.engine.gl.mesh.VBOModel;
import net.mantagames.jgm.engine.gl.mesh.Vertex;
import net.mantagames.jgm.engine.gl.util.TextureUtils;

import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector2f;

public class GLFont {
	private Font font;
	private int fontSize;
	private CharData[] charData;
	private Texture2D fontTexture;

	public static final boolean FONT_DEBUG = false;
	public static final int CHAR_PADDING = 1;
	
	public static final int ALIGN_LEFT   = 0;
	public static final int ALIGN_CENTER = 1;
	public static final int ALIGN_RIGHT  = 2;
	
	public static final int ALIGN_TOP     = 0;
	public static final int ALIGN_NIDDLE  = 1;
	public static final int ALIGN_BOTTOM  = 2;
	
	public int DRAW_STYLE = ALIGN_LEFT;
	
	public GLFont(Font font) {
		this.charData    = new CharData[256];
		this.font        = font;
		this.fontSize    = font.getSize();
		
		// Create the sprite-sheet
		long time = System.currentTimeMillis();
		System.out.println("Generating bitmap for font " + font.getName() + " with size " + fontSize);
		BufferedImage fontImage = generateFontBitmap();
		System.out.println("Font generated in " + (System.currentTimeMillis() - time) + " ms");
		
		// Load the font into openGL
		this.fontTexture = TextureUtils.loadTexture( fontImage );
		
		// Export to see how pretty it is
		this.exportFont(fontImage, "font.png");
	}
	
	public void exportFont(BufferedImage fontImage, String fileName) {
		try {
		    // retrieve image
		    File outputfile = new File(fileName);
		    ImageIO.write(fontImage, "png", outputfile);
		} catch (IOException e) {
		    //
		}
	}
	
	public void drawString(Shader shader, String text, float x, float y) {
		if (text == null || text.length() == 0)
			return;
		
		ModelMatrix matrix = new ModelMatrix();
		Color color = Color.white;
		float offsetY = 0;
		
		String[] lines = text.split("\\n");
		for (int j = 0; j < lines.length; j++) {
			String line = lines[j];
			
			float offsetX = 0;
			if (this.DRAW_STYLE == ALIGN_CENTER) {
				offsetX = -getStringWidth(line)/2f - 1;
			}else if (this.DRAW_STYLE == ALIGN_RIGHT) {
				offsetX = -getStringWidth(line.substring(2)) - 1;
			}
			
			for (int i = 0; i < line.length(); i++) {
				char ch = line.charAt(i);
				if (ch == FontColor.COLOR_CODE.charAt(0) && i < line.length() - 1) {
					FontColor chatColor = FontColor.getChatColor(line.substring(i, i + 2));
					color = new Color(chatColor.getRed(), chatColor.getGreen(), chatColor.getBlue());
					int modelColorLocation = GL20.glGetUniformLocation(shader.getShaderProgram().getID(), "VECTOR_OBJECT_COLOUR");
					GL20.glUniform4f(modelColorLocation, color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, 1);
					i++;
				} else {
					CharData data = charData[(int)ch];
					
					matrix.matrix_add_translation(x + offsetX - data.getXOffset(), y + offsetY - data.getYOffset() - 1, 0);
					data.getModel().matrix_set_from_4x4(matrix.matrix_get());
					data.getModel().draw(fontTexture, shader);
					matrix.matrix_reset();
					
					offsetX += data.getCharBounds().getWidth();
				}
			}
			
			offsetY += this.getSize();
		}
	}
	
	public float getStringWidth(String text) {
		text = FontColor.stripColor(text);
		int stringWid = 0;
		for (int i = 0; i < text.length(); i++) {
			stringWid += charData[(int)text.charAt(i)].getCharBounds().getWidth();
		}
		return stringWid;
	}
	
	public void drawStringUnderlined(Shader shader, String text, float x, float y, Vector2f direction) {
		drawString(shader, FontColor.BLACK + FontColor.stripColor(text), x + direction.x, y + direction.y);
		drawString(shader, text, x, y);
	}
	
	public void drawStringOutlined(Shader shader, String text, float x, float y) {
		String text2 = FontColor.BLACK + FontColor.stripColor(text);
		drawString(shader, text2, x + 1, y );
		drawString(shader, text2, x - 1, y );
		drawString(shader, text2, x, y + 1 );
		drawString(shader, text2, x, y - 1 );
		drawString(shader, text, x, y);
	}
	
	private static int getNextPowerOf2(int dimension) {
		int val = dimension;
		int powof2 = 1;
		
		while( powof2 < val ) powof2 <<= 1;
		
		return powof2;
	}

	private BufferedImage generateFontBitmap() {
		int width  = getNextPowerOf2((16 + CHAR_PADDING) * fontSize);
		int height = getNextPowerOf2((16 + CHAR_PADDING) * fontSize);
		int cellSize = width/16;
		
		// Create the base font bitmap
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		
		// Clear the background color
		g.setColor(new Color(0,0,0,1));
		g.fillRect( 0, 0, width, height );

		// Start letter drawing
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.white);
		g.setFont(font);
		
		// Get font data from all characters and render to bitmap
		FontMetrics fontMetrics = g.getFontMetrics();
		for (int i = 0; i < charData.length; i++) {
			int cellX = (i % 16) * cellSize;
			int cellY = (i / 16) * cellSize;
			float centerX = cellX + (cellSize / 2f);
			float centerY = cellY + (cellSize / 2f);
			
			charData[i] = new CharData((char)i, fontMetrics, g,
													cellSize,
													cellX/(float)width,
													cellY/(float)height,
													cellSize/(float)width,
													cellSize/(float)width);
			
			Rectangle2D rect = charData[i].getCharBounds();
			float boundsWid = (float) rect.getWidth();
			float boundsHei = (float) rect.getHeight();
			
			if (FONT_DEBUG) {				
				// Draw the char bounds
				g.setColor(Color.blue);
				g.drawRect((int)(centerX - boundsWid/2d), (int)(centerY - boundsHei/2d), (int)boundsWid, (int)boundsHei);
				
				// Draw horizontal line
				g.setColor(Color.yellow);
				int drawY = (int) ((centerY - boundsHei/2d) + charData[i].getAscent());
				g.drawLine(cellX, drawY, cellX + cellSize, drawY);
				
				// Draw the cell
				g.setColor(Color.red);
				g.drawRect(cellX, cellY, cellSize - 1, cellSize - 1);
			}
			
			// Draw the character
			g.setColor(Color.white);
			g.drawString(charData[i].getCharacter(), centerX - (boundsWid/2f), (int)(centerY - boundsHei/2d) + fontMetrics.getAscent());
		}
		
		return image;
	}

	private class CharData {
		private String c;
		private int ascent;
		private Rectangle2D charBound;
		private VBOModel letter;
		private int xoff;
		private int yoff;
		private float cellSize;
		
		public CharData(char c, FontMetrics font, Graphics g, float cellSize, float tx, float ty, float tw, float th) {
			this.c = String.valueOf(c);
			this.ascent = font.getAscent();
			this.charBound = font.getStringBounds(this.c, g);
			this.cellSize = cellSize;
			
			this.letter = new VBOModel(6).createQuadExt(0, 0, 0,
														cellSize, cellSize, 0,
														tx,      ty,
														tx + tw, ty + th);
			
			float centerX = cellSize / 2f;
			float centerY = cellSize / 2f;
			float boundsWid = (float) charBound.getWidth();
			float boundsHei = (float) charBound.getHeight();
			float left = centerX - (boundsWid/2f);
			float top  = centerY - (boundsHei/2f);
			this.xoff = (int) left;
			this.yoff = (int) top;
		}
		
		public float getCellSize() {
			return this.cellSize;
		}

		public float getXOffset() {
			return this.xoff;
		}
		
		public float getYOffset() {
			return this.yoff;
		}

		public VBOModel getModel() {
			return this.letter;
		}
		
		public String getCharacter() {
			return this.c;
		}
		
		public Rectangle2D getCharBounds() {
			return charBound;
		}
		
		public int getAscent() {
			return this.ascent;
		}
	}

	public int getSize() {
		return this.fontSize;
	}

}
