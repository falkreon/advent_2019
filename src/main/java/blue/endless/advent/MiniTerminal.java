package blue.endless.advent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

import javax.swing.JFrame;

public class MiniTerminal extends JFrame {
	private static final long serialVersionUID = -8646870692814593075L;
	
	private int cw = 20;
	private int ch = 20;
	
	private int xofs = 8;
	private int yofs = 8+ch;
	
	private int width = 45;
	private int height = 22;
	public char[] chars = new char[width*height];
	private int[] fg = new int[width*height];
	
	private int cursorX = 0;
	private int cursorY = 0;
	
	public boolean leftPressed = false;
	public boolean rightPressed = false;
	public boolean upPressed = false;
	public boolean downPressed = false;
	
	public MiniTerminal() {
		this(45,22);
	}
	
	public MiniTerminal(int width, int height) {
		this.width = width;
		this.height = height;
		this.chars = new char[width*height];
		this.fg = new int[width*height];
		Arrays.fill(fg, 0xFF_FFFFFF);
		Arrays.fill(chars, ' ');
		this.setMinimumSize(new Dimension(width*cw+xofs, height*ch+yofs+20));
		this.setMaximumSize(new Dimension(width*cw+xofs, height*ch+yofs+20));
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		
		this.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent evt) {
				switch(evt.getKeyCode()) {
				case KeyEvent.VK_LEFT: leftPressed = true; break;
				case KeyEvent.VK_RIGHT: rightPressed = true; break;
				case KeyEvent.VK_UP: upPressed = true; break;
				case KeyEvent.VK_DOWN: downPressed = true; break;
				default: break;
				}
			}

			@Override
			public void keyReleased(KeyEvent evt) {
				switch(evt.getKeyCode()) {
				case KeyEvent.VK_LEFT: leftPressed = false; break;
				case KeyEvent.VK_RIGHT: rightPressed = false; break;
				case KeyEvent.VK_UP: upPressed = false; break;
				case KeyEvent.VK_DOWN: downPressed = false; break;
				default: break;
				}
			}

			@Override
			public void keyTyped(KeyEvent evt) {}
		});
		this.createBufferStrategy(2);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public void paintTerminal() {
		Graphics2D g = (Graphics2D) this.getBufferStrategy().getDrawGraphics();
		
		Dimension tz = this.getSize();
		Dimension sz = this.getContentPane().getSize();
		
		int xofs = (tz.width-sz.width) + this.xofs;
		int yofs = (tz.height-sz.height) + this.yofs;
		
		Font.decode(Font.MONOSPACED);
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
		g.setBackground(Color.BLUE);
		//super.paint(g);
		
		
		g.clearRect(0, 0, width*cw + xofs, height*ch + yofs);
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				int addr = y*width+x;
				char cur = chars[addr];
				if (cur==' ') continue;
				int col = fg[addr];
				g.setColor(new Color(col));
				
				g.drawString(""+cur, x*cw+xofs, y*ch+yofs);
			}
		}
		g.dispose();
		this.getBufferStrategy().show();
	}
	
	@Override
	public void paint(Graphics graphics) {
		Graphics2D g = (Graphics2D)this.getBufferStrategy().getDrawGraphics();
		
		Dimension tz = this.getSize();
		Dimension sz = this.getContentPane().getSize();
		
		int xofs = (tz.width-sz.width) + this.xofs;
		int yofs = (tz.height-sz.height) + this.yofs;
		
		Font.decode(Font.MONOSPACED);
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
		g.setBackground(Color.BLUE);
		//super.paint(g);
		
		
		g.clearRect(0, 0, width*cw + xofs, height*ch + yofs);
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				int addr = y*width+x;
				char cur = chars[addr];
				if (cur==' ') continue;
				int col = fg[addr];
				g.setColor(new Color(col));
				
				g.drawString(""+cur, x*cw+xofs, y*ch+yofs);
			}
		}
	}
	
	public void putChar(int x, int y, char ch) {
		putChar(x, y, ch, 0xFF_FFFFFF);
	}
	
	public void putChar(int x, int y, char ch, int color) {
		if (x==-1 && y==0) {
			System.out.println("SCOREBOARD IS LEAKING: "+(int)ch);
		} else {
			if (x<0 || y<0 || x>=width || y>=height) return;
			int addr = y*width+x;
			chars[addr] = ch;
			//repaint();
		}
	}
	
	public char getChar(int x, int y) {
		if (x<0 || y<0 || x>=width || y>=height) return ' ';
		int addr = y*width+x;
		return chars[addr];
	}
	
	public void println(String s) {
		for(int i=0; i<s.length(); i++) {
			putChar(cursorX, cursorY, s.charAt(i));
			cursorX++;
			if (cursorX>=width) {
				cursorX = 0;
				cursorY++;
			}
		}
		if (cursorX!=0) {
			cursorX = 0;
			cursorY++;
		}
	}
	
	public void println() {
		cursorX = 0;
		cursorY++;
	}
	
	public void clear() {
		Arrays.fill(fg, 0xFF_FFFFFF);
		Arrays.fill(chars, ' ');
		repaint();
	}
	
	public void setCursorPos(int x, int y) {
		cursorX = x;
		cursorY = y;
	}
	
	public int charsWide() {
		return width;
	}
	
	public int charsHigh() {
		return height;
	}
}
