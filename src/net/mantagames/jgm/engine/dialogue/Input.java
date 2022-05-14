package net.mantagames.jgm.engine.dialogue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JTextArea;

import net.mantagames.jgm.engine.gl.util.TextureUtils;

public class Input extends ExtraWindow {
	private static final long serialVersionUID = 1L;
	private BufferedImage bg;
	private BufferedImage inputbg;
	private String retValue;
	
	public Input(String msg, String def) {
		this.bg = TextureUtils.loadBufferedImage("net/mantagames/jgm/engine/res/dialogue.png");
		this.inputbg = TextureUtils.loadBufferedImage("net/mantagames/jgm/engine/res/dialogue_input.png");
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
		
		// Create input field
		int INPUT_SIZE_X = inputbg.getWidth();
		int INPUT_SIZE_Y = inputbg.getHeight();
		int INPUT_LOCATION_X = (bg.getWidth()/2) - (INPUT_SIZE_X/2);
		int INPUT_LOCATION_Y = (BUTTON_LOCATION_Y - INPUT_SIZE_Y - 32);
		final JTextArea input = new JTextArea();
		input.setText(def);
		input.setLayout(null);
		input.setBounds(INPUT_LOCATION_X, INPUT_LOCATION_Y, INPUT_SIZE_X, INPUT_SIZE_Y);
		input.setEditable(true);
		//input.setOpaque(false);
		input.setBackground(Color.DARK_GRAY);
		input.setForeground(Color.white);
		input.setVisible(true);
		input.addFocusListener(new FocusListener() {
            @Override public void focusLost(final FocusEvent pE) {}
            @Override public void focusGained(final FocusEvent pE) {
                input.selectAll();
            }
        });
		this.add(input);
		input.validate();
		input.requestFocus();
		
		// Create text box
		JTextArea textArea = new JTextArea();
		textArea.setLineWrap( true );
		textArea.setWrapStyleWord( true );
		textArea.setText(msg);
		textArea.setBounds(16, 16, bg.getWidth() - 32, INPUT_LOCATION_Y - 20);
		textArea.setEditable(false);
		textArea.setOpaque(false);
		textArea.setForeground(Color.white);
		this.add(textArea);
		
		
		// Add button last to track data
		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				retValue = input.getText();
				try{
					Thread.sleep(1);
				}catch(Exception ee) {
				}
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});  
		this.add(button1);
		
		// Invisible background, so ours will draw!
		this.setOpaque(false);
		
		this.start();
	}
	
    public void paintComponent(Graphics g){
    	g.drawImage(bg, 0, 0, null);
    	super.paintComponent(g);
    }

	public String getValue() {
		return this.retValue;
	}
}
