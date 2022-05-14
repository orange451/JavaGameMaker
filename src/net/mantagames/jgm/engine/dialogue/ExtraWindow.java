package net.mantagames.jgm.engine.dialogue;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import net.mantagames.jgm.engine.Runner;

import org.lwjgl.opengl.Display;

public abstract class ExtraWindow extends JPanel implements Runnable {
	protected JFrame frame;
	private String title;
	private Dimension dimension = new Dimension(320, 240);
	private boolean open = true;
	private boolean showBorder;
	
	protected void start() {
		Thread t = new Thread(this);
		t.start();
		
		while (open) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setFrameData(int width, int height, String title, boolean showBorder) {
		this.dimension = new Dimension(width, height);
		this.title = title;
		this.showBorder = showBorder;
	}
	
	public void close() {
		open = false;
	}

	public String getTitle() {
		return this.title;
	}
	
	@Override
	public void run() {
		// Create the frame
		this.frame = new JFrame();
		this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.frame.setLayout(new BorderLayout());
		this.frame.getContentPane().add(this, "Center");
		this.frame.setUndecorated(!showBorder);
		this.frame.setAlwaysOnTop(true);
		this.setPreferredSize(dimension);
		this.setMinimumSize(dimension);
		this.setMaximumSize(dimension);
		this.setSize(dimension);
		this.frame.pack();
		this.frame.setResizable(false);
		this.frame.setVisible(true);
		
		// Center the frame
		int xx = (int) (Display.getX() + (Display.getWidth()/2) - (dimension.getWidth()/2));
		int yy = (int) (Display.getY() + (Display.getHeight()/2) - (dimension.getHeight()/2));
		this.frame.setLocation(xx, yy);
		
		// Add listener to resume the Runner
		this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	close();
		    }
		});
		
		repaint();
	}
}
