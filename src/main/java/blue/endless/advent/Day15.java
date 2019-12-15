package blue.endless.advent;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.List;

public class Day15 {
	public static void runInteractive() {
		List<String> input = AdventOfCode.loadFile("day15.dat");
		Day9.IntcodeCore core = new Day9.IntcodeCore();
		long[] program = Day9.decode(input.get(0));
		core.setMemory(program);
		
		MiniTerminal term = new MiniTerminal(60, 60);
		
		Day10.Point2i droidStart = new Day10.Point2i(30, 30);
		Day10.Point2i droidPosition = new Day10.Point2i(30, 30);
		Day10.Point2i oxygenLocation = new Day10.Point2i(-1, -1);
		
		term.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent evt) {
				//System.out.println("EVT");
				if (core.state.waits()) {
					long lastDirection = 0L;
					switch(evt.getKeyCode()) {
					case KeyEvent.VK_UP:
						lastDirection = 1L;
						break;
					case KeyEvent.VK_DOWN:
						lastDirection = 2L;
						break;
					case KeyEvent.VK_LEFT:
						lastDirection = 3L;
						break;
					case KeyEvent.VK_RIGHT:
						lastDirection = 4L;
						break;
					default:
						return;
					}
					core.state.input.add(lastDirection);
					
					core.runUntilYield(true);
					
					long result = core.state.output.remove(0);
					
					if (droidPosition.equals(droidStart)) {
						term.putChar(droidPosition.x, droidPosition.y, 'S');
					} else if (oxygenLocation!=null && oxygenLocation.equals(droidPosition)) {
						term.putChar(droidPosition.x, droidPosition.y, '!');
					} else {
						term.putChar(droidPosition.x, droidPosition.y, '.');
					}
					
					if (result==0L) {
						int wallX = droidPosition.x;
						int wallY = droidPosition.y;
						switch((int)lastDirection) {
						case 1: wallY--; break;
						case 2: wallY++; break;
						case 3: wallX--; break;
						case 4: wallX++; break;
						}
						term.putChar(wallX, wallY, '#');
						
						//System.out.println("Bonk!");
					} else if (result==1L) {
						switch((int)lastDirection) {
						case 1: droidPosition.y--; break;
						case 2: droidPosition.y++; break;
						case 3: droidPosition.x--; break;
						case 4: droidPosition.x++; break;
						}
						
						//System.out.println("Okay.");
					} else if (result==2L) {
						switch((int)lastDirection) {
						case 1: droidPosition.y--; break;
						case 2: droidPosition.y++; break;
						case 3: droidPosition.x--; break;
						case 4: droidPosition.x++; break;
						}
						oxygenLocation.x = droidPosition.x;
						oxygenLocation.y = droidPosition.y;
						System.out.println("Okay. Oxygen system found!");
					}
					
					term.putChar(droidPosition.x, droidPosition.y, 'D');
					
					
					term.paintTerminal();
				}
			}

			@Override
			public void keyReleased(KeyEvent evt) {}

			@Override
			public void keyTyped(KeyEvent evt) {
				
			}
			
		});
		
		term.putChar(droidPosition.x, droidPosition.y, 'D');
		term.setVisible(true);
		
		
		core.runUntilYield(true);
	}
	
	public static void runPartTwo() {
		List<String> input = AdventOfCode.loadFile("day15b.dat");
		Field field = new Field(input);
		int iterations = 0;
		while(!isFullyOxygenated(field)) {
			field = spreadOxygen(field);
			iterations++;
			field.print();
		}
		
		System.out.println("Final iterations: "+iterations);
	}
	
	public static Field spreadOxygen(Field in) {
		Field out = in.clone();
		for(int y=0; y<in.height; y++) {
			for(int x=0; x<in.width; x++) {
				if (isVaccuum(in, x, y) && nearOxygen(in, x, y)) {
					out.put(x, y, 'O');
				}
			}
		}
		return out;
	}
	
	public static boolean isVaccuum(Field f, int x, int y) {
		return f.get(x, y)=='.';
	}
	
	public static boolean nearOxygen(Field f, int x, int y) {
		return
			f.get(x-1, y)=='O' ||
			f.get(x+1, y)=='O' ||
			f.get(x, y-1)=='O' ||
			f.get(x, y+1)=='O';
	}
	
	public static boolean isFullyOxygenated(Field f) {
		for(char ch: f.data) if (ch=='.') return false;
		return true;
	}
	
	public static class Field {
		public char[] data;
		public int width;
		public int height;
		
		private Field() {}
		
		public Field(List<String> input) {
			this.width = input.get(0).length();
			this.height = input.size();
			data = new char[width*height];
			for(int y=0; y<height; y++) {
				for(int x=0; x<height; x++) {
					char cur = input.get(y).charAt(x);
					if (cur==' ') cur='#';
					if (cur=='!') cur='O';
					
					data[y*width+x] = cur;
				}
			}
		}
		
		public void put(int x, int y, char data) {
			if (x<0 || x>=width || y<0 || y>=height) return;
			this.data[y*width+x] = data;
		}
		
		public char get(int x, int y) {
			if (x<0 || x>=width || y<0 || y>=height) return ' ';
			return data[y*width+x];
		}
		
		public void output(MiniTerminal term) {
			for(int y=0; y<height; y++) {
				for(int x=0; x<height; x++) {
					term.putChar(x, y, data[y*width+x]);
				}
			}
			term.paintTerminal();
		}
		
		public void print() {
			for(int y=0; y<height; y++) {
				for(int x=0; x<height; x++) {
					System.out.print(data[y*width+x]);
				}
				System.out.println();
			}
		}
		
		public Field clone() {
			Field field = new Field();
			field.width = this.width;
			field.height = this.height;
			field.data = Arrays.copyOf(data, data.length);
			return field;
		}
	}
}
