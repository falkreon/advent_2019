package blue.endless.advent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day7 {
	public static Day5.ShipComputer[] createAmplifierBank() {
		
		Day5.ShipComputer[] amplifierBank = new Day5.ShipComputer[5]; // A,B,C,D,E will be 0,1,2,3,4 respectively
		Day5.ShipComputer last = null;
		for(int i=0; i<amplifierBank.length; i++) {
			Day5.ShipComputer cur = new Day5.ShipComputer();
			if (last!=null) cur.state.input = last.state.output; //pipe input in from previous amp
			amplifierBank[i] = cur;
			last = cur;
		}
		
		return amplifierBank;
	}
	
	public static int[] decodeMemory(String s) {
		String[] cells = s.split(",");
		int[] memory = new int[cells.length];
		for(int i=0; i<cells.length; i++) {
			memory[i] = Integer.parseInt(cells[i].trim());
		}
		return memory;
	}
	
	public static int run(String program, int... settings) {
		return run(decodeMemory(program), settings);
	}
	
	public static int run(int[] program, int... settings) {
		return run(program, createAmplifierBank(), settings);
	}
	
	
	
	public static int run(int[] program, Day5.ShipComputer[] amplifierBank, int... settings) {
		if (settings.length<amplifierBank.length) settings = Arrays.copyOf(settings, amplifierBank.length);
		
		amplifierBank[0].state.input.add(0);
		for(int i=0; i<amplifierBank.length; i++) {
			amplifierBank[i].state.memory = Arrays.copyOf(program, program.length);
			amplifierBank[i].state.input.add(0, settings[i]);
			amplifierBank[i].run(false);
		}
		
		int thrusterOutput = amplifierBank[amplifierBank.length-1].state.output.get(0);
		
		System.out.println("Thruster output from "+Arrays.toString(settings)+": "+thrusterOutput);
		return thrusterOutput;
	}
	
	
	/* now that we can run singles and get the input back from them, we want to brute-force the list
	 * of combinations. I'm going to do this with an ugly nested loop.
	 */
	
	public static int bestThrust(String programString) {
		int[] program = decodeMemory(programString);
		int[] bestSettings = null;
		int bestThrust = Integer.MIN_VALUE;
		
		for(int a=0; a<=4; a++) {
			for(int b=0; b<=4; b++) {
				for(int c=0; c<=4; c++) {
					for(int d=0; d<=4; d++) {
						for(int e=0; e<=4; e++) {
							
							int[] settings = {a, b, c, d, e};
							if (hasDuplicates(settings)) continue;
							
							int thrust = run(program, settings);
							if (thrust>bestThrust) {
								bestThrust = thrust;
								bestSettings = settings;
							}
							
						}
					}
				}
			}
		}
		
		System.out.println("Best thrust was for settings "+Arrays.toString(bestSettings)+": "+bestThrust);
		return bestThrust;
	}
	
	/** This has to be the worst way to obtain this information. */
	public static boolean hasDuplicates(int[] set) {
		Set<Integer> elements = new HashSet<>();
		for(int i : set) {
			if (elements.contains(i)) return true;
			elements.add(i);
		}
		return false;
	}
	
	public static void runFromFile() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day7.dat"));
			bestThrust(file.get(0));
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	
	/**
	 * For part 2, I need to retool the intcode interpreter so that it can enter an input-blocking "wait"
	 * state, and can run one tick of code
	 */
	
	public static class ProgramState {
		public String prefix = "";
		public int programCounter = 0;
		public int[] memory = { 99 }; //No program loaded
		public int addressModes = 0;
		public boolean halt = false;
		public String error = "";
		public String lineDisassembly = "";
		public List<Integer> input = new ArrayList<>();
		public List<Integer> output = new ArrayList<>();
		public boolean wait = false; //True if blocking on input
		
		public void halt() {
			halt = true;
		}
		
		public boolean isHalted() {
			return halt;
		}
		
		public boolean waits() {
			return (wait && input.isEmpty());
		}
		
		private int addressMode(int argNumber) {
			int mode = addressModes;
			for(int i=0; i<argNumber; i++) {
				mode /= 10;
			}
			mode %= 10;
			return mode;
		}
		
		public int fetch(int argNumber) {
			int mode = addressMode(argNumber);
			
			switch(mode) {
			case 0: //mem
				int address = memory[programCounter+argNumber+1];
				if (address>=memory.length) return 0;
				if (address<0) return 0;
				return memory[address];
			case 1: //imm
				int constant = memory[programCounter+argNumber+1];
				return constant;
			default:
				error = "Unknown address mode "+mode;
				halt();
				return 0;
			}
		}
		
		public void write(int argNumber, int value) {
			int mode = addressMode(argNumber);
			
			switch(mode) {
			case 0: //mem
				int address = memory[programCounter+argNumber+1];
				if (address>=memory.length) return; //TODO: Do we have to grow the memory to accommodate new values?
				if (address<0) return;
				memory[address] = value;
				return;
			case 1: //imm
				error = "Cannot write to immediate value";
				halt();
				return;
			default:
				error = "Unknown address mode "+mode;
				halt();
				return;
			}
		}
		
		public String explain(int argNumber) {
			int mode = addressMode(argNumber);
			switch(mode) {
			case 0: //mem
				int address = memory[programCounter+argNumber+1];
				return "["+address+"]";
			case 1: //imm
				return ""+memory[programCounter+argNumber+1];
			default:
				return "?"+mode+"?"+memory[programCounter+argNumber+1];
			}
		}
	}
	
	public static class ShipComputer {
		public Map<Integer, Opcode> opcodes = new HashMap<>();
		public ProgramState state = new ProgramState();
		
		public ShipComputer() {
			opcodes.put(1, (state)->{ //ADD
				int a = state.fetch(0);
				int b = state.fetch(1);
				state.write(2, a+b);
				state.lineDisassembly = "ADD "+state.explain(0)+" "+state.explain(1)+" -> "+state.explain(2);
				state.programCounter += 4;
			});
			
			opcodes.put(2, (state)->{ //MUL
				int a = state.fetch(0);
				int b = state.fetch(1);
				state.write(2, a*b);
				state.lineDisassembly = "MUL "+state.explain(0)+" "+state.explain(1)+" -> "+state.explain(2);
				state.programCounter += 4;
			});
			
			opcodes.put(3, (state)->{
				state.lineDisassembly = "IN  "+state.explain(0);
				
				if (state.input.isEmpty()) {
					state.wait = true;
				} else {
					state.wait = false;
					int input = state.input.remove(0);
					state.write(0, input);
					state.programCounter += 2;
				}
			});
			
			opcodes.put(4, (state)->{
				state.lineDisassembly = "OUT "+state.explain(0);
				int out = state.fetch(0);
				state.output.add(out);
				
				state.programCounter += 2;
			});
			
			opcodes.put(5, (state)->{
				state.lineDisassembly = "JNZ "+state.explain(0)+" "+state.explain(1);
				int nz = state.fetch(0);
				int addr = state.fetch(1);
				if (nz!=0) {
					state.programCounter = addr;
				} else {
					state.programCounter += 3;
				}
			});
			
			opcodes.put(6, (state)->{
				state.lineDisassembly = "JZ  "+state.explain(0)+" "+state.explain(1);
				int z = state.fetch(0);
				int addr = state.fetch(1);
				if (z==0) {
					state.programCounter = addr;
				} else {
					state.programCounter += 3;
				}
			});
			
			opcodes.put(7, (state)->{
				state.lineDisassembly = "LES "+state.explain(0)+" "+state.explain(1)+" -> "+state.explain(2);
				int a = state.fetch(0);
				int b = state.fetch(1);
				
				int result = a<b ? 1 : 0;
				
				state.write(2, result);
				
				state.programCounter+=4;
			});
			
			opcodes.put(8, (state)->{
				state.lineDisassembly = "EQU "+state.explain(0)+" "+state.explain(1)+" -> "+state.explain(2);
				int a = state.fetch(0);
				int b = state.fetch(1);
				
				int result = a==b ? 1 : 0;
				
				state.write(2, result);
				
				state.programCounter+=4;
			});
			
			opcodes.put(99, (state)->{
				state.lineDisassembly = "HLT";
				state.programCounter++;
				state.halt();
			});
			
			opcodes.put(-1, (state)->{
				state.lineDisassembly = "HCF";
				state.programCounter = 0;
				state.input.clear();
				state.output.clear();
				state.memory = new int[]{ 99 };
				state.error = "EVERYTHING IS FINE";
				state.halt();
			});
		}
		
		public void setMemory(int[] memory) {
			state.memory = memory;
		}
		
		public void runUntilYield(boolean verbose) {
			if (state.isHalted() || state.waits()) return;
			while(!state.isHalted() && !state.waits()) step(verbose);
			
			if (!state.error.isEmpty()) System.out.println(state.prefix+"> "+"Error: "+state.error);
			if (verbose && !state.output.isEmpty()) System.out.println(state.prefix+"> "+"output: "+state.output);
			if (verbose) System.out.println(state.prefix+"> "+"Final memory state: "+Arrays.toString(state.memory));
		}
		
		public void step(boolean verbose) {
			if (state.isHalted()) return;
			if (state.wait && state.input.isEmpty()) return;
			
			int opcodeAndMode = state.memory[state.programCounter];
			int opcodeNum = opcodeAndMode % 100;
				
			Opcode opcode = opcodes.get(opcodeNum);
			if (opcode==null) {
				state.lineDisassembly = "ERROR";
				state.error = "Unknown opcode "+opcodeNum;
				state.halt();
			} else {
				state.addressModes = opcodeAndMode / 100;
				opcode.run(state);
			}
			
			if (verbose) System.out.println(state.prefix+"> "+state.lineDisassembly);
		}
	}
	
	public static interface Opcode {
		public void run(ProgramState state);
	}
	
	
	/* ****************************************************************************************** *
	 * Okay, now we can get back down to business.
	 * ****************************************************************************************** */
	
	public static ShipComputer[] createResonantBank() {
		
		ShipComputer[] amplifierBank = new ShipComputer[5]; // A,B,C,D,E will be 0,1,2,3,4 respectively
		ShipComputer last = null;
		for(int i=0; i<amplifierBank.length; i++) {
			ShipComputer cur = new ShipComputer();
			if (last!=null) cur.state.input = last.state.output; //pipe input in from previous amp
			amplifierBank[i] = cur;
			last = cur;
		}
		amplifierBank[0].state.input = last.state.output; //Complete the loop
		
		return amplifierBank;
	}
	
	public static int runResonantBank(int[] program, ShipComputer[] amplifierBank, int... settings) {
		if (settings.length<amplifierBank.length) settings = Arrays.copyOf(settings, amplifierBank.length);
		
		//Because the setting is the first input to be read, we can buffer those inputs before we begin.
		for(int i=0; i<amplifierBank.length; i++) {
			amplifierBank[i].state.input.add(settings[i]);
			amplifierBank[i].state.memory = Arrays.copyOf(program, program.length);
		}
		amplifierBank[0].state.input.add(0); //Initial thrust input
		
		//Run till success or failure. TODO: Solve the halting problem.
		int iterations = 0;
		while(!allHalted(amplifierBank) && !deadlocked(amplifierBank) && iterations<10000) {
			for(ShipComputer compy : amplifierBank) compy.runUntilYield(true);
			iterations++;
		}
		if (iterations>=10000) {
			System.out.println("Cycling endlessly: ");
			return -1;
		}
		
		List<Integer> finalOutput = amplifierBank[amplifierBank.length-1].state.output;
		if (finalOutput.size()<1) return -1;
		System.out.println("Final output for settings "+Arrays.toString(settings)+": "+finalOutput.get(0));
		return finalOutput.get(0);
	}
	
	/** Returns true if all computers in this bank have halted. */
	public static boolean allHalted(ShipComputer[] computers) {
		for(ShipComputer compy : computers) {
			if (!compy.state.isHalted()) return false;
		}
		return true;
	}
	
	/** Returns true if all computers in this bank are blocking on input, and cannot possibly run any further */
	public static boolean deadlocked(ShipComputer[] computers) {
		for(ShipComputer compy : computers) {
			if (!compy.state.waits()) return false;
		}
		return true;
	}
	
	public static int bestResonantThrust(String programString) {
		int[] program = decodeMemory(programString);
		int[] bestSettings = null;
		int bestThrust = Integer.MIN_VALUE;
		
		for(int a=5; a<=9; a++) {
			for(int b=5; b<=9; b++) {
				for(int c=5; c<=9; c++) {
					for(int d=5; d<=9; d++) {
						for(int e=5; e<=9; e++) {
							
							int[] settings = {a, b, c, d, e};
							if (hasDuplicates(settings)) continue;
							
							int thrust = runResonantBank(program, createResonantBank(), settings);
							if (thrust>bestThrust) {
								bestThrust = thrust;
								bestSettings = settings;
							}
							
						}
					}
				}
			}
		}
		
		System.out.println("Best thrust was for settings "+Arrays.toString(bestSettings)+": "+bestThrust);
		return bestThrust;
	}
	
	public static void runResonantFromFile() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day7.dat"));
			bestResonantThrust(file.get(0));
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
