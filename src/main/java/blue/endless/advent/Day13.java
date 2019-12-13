package blue.endless.advent;

import java.util.List;

public class Day13 {
	public static void run(List<String> data) {
		Day9.IntcodeCore core = new Day9.IntcodeCore();
		core.setMemory(Day9.decode(data.get(0)));
		GraphicsCard gpu = new GraphicsCard();
		core.opcodes.put(4, (state)->{
			state.lineDisassembly = "OUT "+state.explain(0);
			long out = state.fetch(0);
			gpu.consume(out);
			
			state.programCounter += 2;
		});
		
		MiniTerminal term = new MiniTerminal();
		gpu.term = term;
		term.setVisible(true);
		
		core.runUntilYield(true);
		
		if (core.state.isHalted()) {
			int count = 0;
			for(char ch: term.chars) {
				if (ch=='#') count++;
			}
			System.out.println("Total walls on halt: "+count);
			
			
		} else {
			System.out.println("Core is yielded, waiting for input O_o");
		}
	}
	
	public static int getRelativeX(MiniTerminal term) {
		int ballX = -1;
		int paddleX = -1;
		
		for(int y=0; y<term.charsHigh(); y++) {
			for(int x=0; x<term.charsWide(); x++) {
				char cur = term.getChar(x, y);
				if (cur=='o') {
					ballX = x;
				} else if (cur=='='){
					paddleX = x;
				}
			}
		}
		
		if (ballX<paddleX) return -1;
		if (ballX>paddleX) return 1;
		return 0;
	}
	
	public static void runInteractive(List<String> data) {
		Day9.IntcodeCore core = new Day9.IntcodeCore();
		core.setMemory(Day9.decode(data.get(0)));
		core.state.memory[0] = 2L;
		GraphicsCard gpu = new GraphicsCard();
		core.opcodes.put(4, (state)->{
			state.lineDisassembly = "OUT "+state.explain(0);
			long out = state.fetch(0);
			gpu.consume(out);
			
			state.programCounter += 2;
		});
		
		MiniTerminal term = new MiniTerminal();
		gpu.term = term;
		term.setVisible(true);
		
		while(!core.state.isHalted()) {
			core.runUntilYield(false);
			if (!core.state.isHalted()) {
				term.paintTerminal();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {}
				
				core.state.input.add((long)getRelativeX(term));
				
				//Turns out interactive mode isn't super helpful.
				/*
				if (term.leftPressed && term.rightPressed) {
					core.state.input.add(0L);
				} else if (term.leftPressed) {
					core.state.input.add(-1L);
				} else if (term.rightPressed) {
					core.state.input.add(1L);
				} else {
					core.state.input.add(0L);
				}*/
			}
		}
		System.out.println("Halted. Final score: "+gpu.score);
		
	}
	
	private static class GraphicsCard {
		int x = -99;
		int y = -99;
		int tile = -99;
		MiniTerminal term;
		long score = -999;
		
		public void consume(long i) {
			if (x==-99) {
				x = (int)i;
			} else if (y==-99) {
				y = (int)i;
			} else {
				if (x==-1 && y==0) { //Segment Display
					System.out.println("Score: "+i);
					this.score = i;
					//term.setCursorPos(35, 20);
					//term.println(""+i);
				} else {             //Primary Display
					if (term!=null) {
						char ch = '?';
						tile = (int)i;
						switch(tile) {
						case 0: ch = '.'; break;
						case 1: ch = '@'; break;
						case 2: ch = '#'; break;
						case 3: ch = '='; break;
						case 4: ch = 'o'; break;
						}
						term.putChar(x, y, ch);
					}
				}
				x = -99;
				y = -99;
				tile = -99;
			}
		}
	}
}
