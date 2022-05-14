package net.mantagames.jgm.engine;

import static org.lwjgl.opengl.ARBDebugOutput.glDebugMessageCallbackARB;

import java.awt.Rectangle;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ARBDebugOutputCallback;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.PixelFormat;

public class Window {
	private int width;
	private int height;
	private String title;
	private Rectangle viewport;
	private boolean fullscreen;
	private boolean resizeable;

	public Window(int width, int height, String title) {
		this.width = width;
		this.height = height;
		this.title = title;
		
		viewport = new Rectangle(0, 0, width, height);
	}

	public void createDisplay() {
		try {
			Display.setDisplayMode(new DisplayMode(width, height));

			// Setup the pixel format
			PixelFormat pixelFormat = new PixelFormat(24, 0, 24, 0, 0);
			
			
			// Setup the contex attributes
			ContextAttribs contextAttributes = new ContextAttribs(3, 2);
			contextAttributes = contextAttributes.withProfileCompatibility(false);
			contextAttributes = contextAttributes.withProfileCore(true);
			contextAttributes = contextAttributes.withDebug(false);
			
			
			// Create the display
			Display.create(pixelFormat,contextAttributes);

			if(GLContext.getCapabilities().GL_ARB_debug_output){
				glDebugMessageCallbackARB(new ARBDebugOutputCallback());
			}
			Display.setTitle(title);

			setSize(width, height, false);
		} catch (LWJGLException ex) {
			ex.printStackTrace();
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setSize(int width, int height, boolean fullscreen) {
		if (width != this.width || height != this.height || fullscreen != this.fullscreen)
			setDisplayMode(width, height, fullscreen);
	}
	
	public void setResizeable(boolean resizeable) {
		if (this.resizeable != resizeable) {
			setSize(width, height, fullscreen);
			this.resizeable = resizeable;
		}
	}
	
	public boolean isFullscreen() {
		return this.fullscreen;
	}
	
	private DisplayMode getNearestDisplayModeWidth(int width, int height, float ASPECT_RATIO) {
		try {
			DisplayMode[] modes = Display.getAvailableDisplayModes();
			DisplayMode currentDisplay = null;
			float lowestWid = 9999999;
			for (int i=0;i<modes.length;i++) {
				DisplayMode current = modes[i];
				int cwid = current.getWidth();
				int chei = current.getHeight();
				if (cwid/(float)chei == ASPECT_RATIO) {
					if (cwid >= width) {
						if (cwid - width < lowestWid) {
							lowestWid = cwid - width;
							currentDisplay = current;
						}
					}
				}
			}
			return currentDisplay;
		} catch (LWJGLException e) {
			//
		}
		return null;
	}
	
	private DisplayMode getNearestDisplayModeHeight(int width, int height, float ASPECT_RATIO) {
		try {
			DisplayMode[] modes = Display.getAvailableDisplayModes();
			DisplayMode currentDisplay = null;
			float lowestHei = 9999999;
			for (int i=0;i<modes.length;i++) {
				DisplayMode current = modes[i];
				int cwid = current.getWidth();
				int chei = current.getHeight();
				if (cwid/(float)chei == ASPECT_RATIO) {
					if (chei >= height) {
						if (chei - height < lowestHei) {
							lowestHei = chei - height;
							currentDisplay = current;
						}
					}
				}
			}
			return currentDisplay;
		} catch (LWJGLException e) {
			//
		}
		return null;
	}
	
	private DisplayMode getDisplayMode(int width, int height) {
		try {
			DisplayMode[] modes = Display.getAvailableDisplayModes();
			for (int i=0;i<modes.length;i++) {
				DisplayMode current = modes[i];
				if ((current.getWidth() == width) && (current.getHeight() == height)) {
					if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel()) && (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
						return current;
					}
				}
			}
		} catch (LWJGLException e) {
			//
		}
		return null;
	}

	private void setDisplayMode(int width, int height, boolean fullscreen) {
		this.fullscreen = fullscreen;
		float DESIRED_ASPECT_RATIO = width/(float)height;
		float SCREEN_ASPECT_RATIO = Display.getDesktopDisplayMode().getWidth() / (float) Display.getDesktopDisplayMode().getHeight();
		boolean changeWidth = false;
		boolean changeHeight = false;
		
		try {
			DisplayMode targetDisplayMode = null;

			if (fullscreen) {
				if (RunnerProperties.fullscreen_stretch) {
					if (RunnerProperties.fullscreen_keep_aspect_ratio) {
						if (SCREEN_ASPECT_RATIO != DESIRED_ASPECT_RATIO) {
							if (DESIRED_ASPECT_RATIO < SCREEN_ASPECT_RATIO) {
								targetDisplayMode = getNearestDisplayModeWidth((int) (height * SCREEN_ASPECT_RATIO), height, SCREEN_ASPECT_RATIO);
								changeWidth = true;
							} else {
								targetDisplayMode = getNearestDisplayModeHeight((int) (height * SCREEN_ASPECT_RATIO), height, SCREEN_ASPECT_RATIO);
								changeHeight = true;
							}
						}else{
							targetDisplayMode = getDisplayMode(width, height);
						}
					}else{
						targetDisplayMode = getDisplayMode(width, height);
					}
				}else{
					targetDisplayMode = Display.getDesktopDisplayMode();
				}
			} else {
				targetDisplayMode = new DisplayMode(width,height);
			}

			if (targetDisplayMode == null) {
				System.out.println("Failed to find value mode: "+width+"x"+height+" fs="+fullscreen);
				return;
			}

			this.width = width;
			this.height = height;

			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fullscreen);
			Display.setResizable(resizeable);

			// calculate new viewport
			viewport = new Rectangle(0, 0, width, height);
			if (fullscreen) {
				if (RunnerProperties.fullscreen_stretch && RunnerProperties.fullscreen_keep_aspect_ratio) {
					if (changeWidth) {
						int newHeight = targetDisplayMode.getHeight();
						int newWidth = (int) (width * (newHeight/(float)height));
						int newx = (targetDisplayMode.getWidth() - newWidth) / 2;
						int newy = 0;
						viewport = new Rectangle(newx, newy, newWidth, newHeight);
					}else if (changeHeight) {
						int newWidth = targetDisplayMode.getWidth();
						int newHeight = (int) (height * (newWidth/(float)width));
						int newx = 0;
						int newy = (targetDisplayMode.getHeight() - newHeight) / 2;
						viewport = new Rectangle(newx, newy, newWidth, newHeight);
					}
				}else if (!RunnerProperties.fullscreen_stretch) {
					int newWid = targetDisplayMode.getWidth();
					int newHei = targetDisplayMode.getHeight();
					int viewX = newWid/2 - (width/2);
					int viewY = newHei/2 - (height/2);
					viewport = new Rectangle(viewX, viewY, width, height);
				}else{
					int newWid = targetDisplayMode.getWidth();
					int newHei = targetDisplayMode.getHeight();
					viewport = new Rectangle(0, 0, newWid, newHei);
				}
			}
			
			GL11.glViewport((int) viewport.x, (int) viewport.y, (int) viewport.width, (int) viewport.height);
			
			Display.update();
		} catch (LWJGLException e) {
			System.out.println("Unable to setup mode "+width+"x"+height+" fullscreen="+fullscreen + e);
		}
	}
	
	public Rectangle getViewport() {
		return this.viewport;
	}
}
