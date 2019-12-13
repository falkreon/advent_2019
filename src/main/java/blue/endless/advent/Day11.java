package blue.endless.advent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Day11 {
	public static class IntcodeTurtle {
		Direction dir = Direction.UP;
		Day9.IntcodeCore core = new Day9.IntcodeCore();
		int x = 30;
		int y = 40;
		Hull hull = new Hull(200, 200);
		List<Day10.Point2i> paintOrder = new ArrayList<>();
		
		public IntcodeTurtle() {
			//Replace the computer's input instruction with one which reads the robot's camera
			core.opcodes.put(3, (state)->{
				flushOutputs();
				state.lineDisassembly = "IN  "+state.explain(0);
				
				long input = hull.readInt(x, y);
				state.write(0, input);
				state.programCounter += 2;
			});
		}
		
		public void loadProgram(String s) {
			long[] mem = Day9.decode(s);
			core.setMemory(mem);
		}
		
		public void flushOutputs() {
			if (core.state.output.isEmpty()) return;
			while (!core.state.output.isEmpty()) {
				if (core.state.output.size()==1) {
					//Don't clobber the prior error if it exists, but if it doesn't, give us *some* idea what happened.
					if (core.state.error.isEmpty()) core.state.error = "Machine yielded in invalid state: Painting robots always output two values!";
					core.state.halt();
					return;
				}
				long panelColor = core.state.output.remove(0);
				long direction = core.state.output.remove(0);
				
				hull.paint(x, y, (int)panelColor);
				paintOrder.add(new Day10.Point2i(x, y));
				
				if (direction==0) { //LEFT
					dir = dir.left();
					moveForward();
				} else if (direction==1) { //RIGHT
					dir = dir.right();
					moveForward();
				} else {
					core.state.error = "Invalid turn direction '"+direction+"'";
					core.state.halt();
					return;
				}
			}
		}
		
		public void run() {
			while(!core.state.isHalted()) {
				core.runUntilYield(true);
				flushOutputs();
			}
		}
		
		public void moveForward() {
			switch(dir) {
			case UP:
				y+= 1;
				break;
			case RIGHT:
				x+= 1;
				break;
			case DOWN:
				y-=1;
				break;
			case LEFT:
				x-=1;
				break;
			}
		}
	}
	
	public enum Direction {
		UP,
		DOWN,
		RIGHT,
		LEFT;
		
		public static Direction of(int id) {
			return values()[id];
		}
		
		public Direction left() {
			switch(this) {
			default:
			case UP: return LEFT;
			case RIGHT: return UP;
			case DOWN: return RIGHT;
			case LEFT: return DOWN;
			}
		}
		
		public Direction right() {
			switch(this) {
			default:
			case UP: return RIGHT;
			case RIGHT: return DOWN;
			case DOWN: return LEFT;
			case LEFT: return UP;
			}
		}
	}
	
	public static class Hull {
		boolean[] data;
		int width;
		int height;
		
		public Hull(int width, int height) {
			this.width = width;
			this.height = height;
			this.data = new boolean[width*height];
		}
		
		public boolean read(int x, int y) {
			if (x<0 || x>=width || y<0 || y>=height) return false;
			return data[width*y+x];
		}
		
		public int readInt(int x, int y) {
			return (read(x,y)) ? 1 : 0;
		}
		
		public void paint(int x, int y, boolean color) {
			if (x<0 || x>=width || y<0 || y>=height) return;
			data[width*y+x] = color;
		}
		
		public void paint(int x, int y, int color) {
			paint(x, y, (color==0) ? false : true);
		}
		
		public void print() {
			for(int y=0; y<height; y++) {
				String line = "";
				for(int x=0; x<width; x++) {
					line += read(x,y) ? "#" : ".";
				}
				System.out.println(line);
			}
		}
	}
	
	public static void runFromFile() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day11.dat"));
			
			IntcodeTurtle turtle = new IntcodeTurtle();
			turtle.loadProgram(file.get(0));
			turtle.run();
			turtle.flushOutputs();
			System.out.println("Cells painted: "+turtle.paintOrder);
			Set<Day10.Point2i> uniqueCells = new HashSet<>();
			uniqueCells.addAll(turtle.paintOrder);
			System.out.println("("+uniqueCells.size()+" cells total)");
			turtle.hull.print();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void runPartTwoFromFile() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day11.dat"));
			
			IntcodeTurtle turtle = new IntcodeTurtle();
			turtle.hull.paint(turtle.x, turtle.y, true); //Start on a white hull piece
			turtle.loadProgram(file.get(0));
			turtle.run();
			turtle.flushOutputs();
			System.out.println("Cells painted: "+turtle.paintOrder);
			Set<Day10.Point2i> uniqueCells = new HashSet<>();
			uniqueCells.addAll(turtle.paintOrder);
			System.out.println("("+uniqueCells.size()+" cells total)");
			turtle.hull.print();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
