package net.mantagames.jgm.engine.dialogue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JTextArea;

import net.mantagames.jgm.engine.gl.util.TextureUtils;

public class Message extends ExtraWindow {
	private static final long serialVersionUID = 1L;
	private BufferedImage bg;
	
	public Message(String msg) {
		this.bg = TextureUtils.loadBufferedImage("net/mantagames/jgm/engine/res/dialogue.png");
		this.setFrameData(bg.getWidth(), bg.getHeight(), "", false);
		this.setLayout(null);
		
		// Create okay button
		int BUTTON_SIZE_X = 90;
		int BUTTON_SIZE_Y = 24;
		int BUTTON_LOCATION_X = (bg.getWidth()/2) - (BUTTON_SIZE_X/2);
		int BUTTON_LOCATION_Y = (bg.getHeight() - BUTTON_SIZE_Y - 8);
		JButton button1 = new JButton();
		button1.setText("Okay");
		button1.setLayout(null);
		button1.setBounds(BUTTON_LOCATION_X, BUTTON_LOCATION_Y, BUTTON_SIZE_X, BUTTON_SIZE_Y );
		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});  
		this.add(button1);
		
		// Create text box
		JTextArea textArea = new JTextArea();
		textArea.setLineWrap( true );
		textArea.setWrapStyleWord( true );
		textArea.setText(msg);
		textArea.setBounds(16, 16, bg.getWidth() - 32, BUTTON_LOCATION_Y - 32);
		textArea.setEditable(false);
		textArea.setOpaque(false);
		textArea.setForeground(Color.white);
		this.add(textArea);
		
		// Invisible background, so ours will draw!
		this.setOpaque(false);
		
		this.start();
	}
	
    public void paintComponent(Graphics g){
    	g.drawImage(bg, 0, 0, null);
    	super.paintComponent(g);
    }
}
