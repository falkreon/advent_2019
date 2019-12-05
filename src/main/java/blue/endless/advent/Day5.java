package blue.endless.advent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Today I completely rewrote the ship computer. I didn't have to, but it's clear we're doing
 * intcodes for the longhaul, and this will be much easier to alter as new requirements come in.
 */
public class Day5 {
	public static void runFromFile(int input) {
		try {
			List<String> file = Files.readAllLines(Paths.get("day5.dat"));
			decodeAndRun(file.get(0), input);
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void decodeAndRun(String intcode, int... inputs) {
		String[] cells = intcode.split(",");
		int[] memory = new int[cells.length];
		for(int i=0; i<cells.length; i++) {
			memory[i] = Integer.parseInt(cells[i].trim());
		}
		
		run(memory, inputs);
	}
	
	public static void run(int[] memory, int... inputs) {
		
		ShipComputer computer = new ShipComputer();
		computer.setMemory(memory);
		
		if (inputs.length>0) for(int i: inputs) computer.state.input.add(i);
		
		computer.run();
	}
	
	
	public static class ProgramState {
		public int programCounter = 0;
		public int[] memory = { 99 }; //No program loaded
		public int addressModes = 0;
		public boolean halt = false;
		public String error = "";
		public String lineDisassembly = "";
		public List<Integer> input = new ArrayList<>();
		public List<Integer> output = new ArrayList<>();
		
		public void halt() {
			halt = true;
		}
		
		public boolean isHalted() {
			return halt;
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
				state.lineDisassembly = "IN "+state.explain(0);
				
				if (state.input.isEmpty()) {
					state.error = "No input to read.";
					state.halt();
				} else {
					int input = state.input.remove(0);
					state.write(0, input);
				}
				
				state.programCounter += 2;
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
				state.lineDisassembly = "JZ "+state.explain(0)+" "+state.explain(1);
				int z = state.fetch(0);
				int addr = state.fetch(1);
				if (z==0) {
					state.programCounter = addr;
				} else {
					state.programCounter += 3;
				}
			});
			
			opcodes.put(7, (state)->{
				state.lineDisassembly = "LESS "+state.explain(0)+" "+state.explain(1)+" -> "+state.explain(2);
				int a = state.fetch(0);
				int b = state.fetch(1);
				
				int result = a<b ? 1 : 0;
				
				state.write(2, result);
				
				state.programCounter+=4;
			});
			
			opcodes.put(8, (state)->{
				state.lineDisassembly = "EQUAL "+state.explain(0)+" "+state.explain(1)+" -> "+state.explain(2);
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
		}
		
		public void setMemory(int[] memory) {
			state.memory = memory;
		}
		
		public void run() {
			while(!state.isHalted()) {
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
				
				System.out.println(state.lineDisassembly);
			}
			
			if (!state.error.isEmpty()) System.out.println("Error: "+state.error);
			if (!state.output.isEmpty()) System.out.println("Final output: "+state.output);
			System.out.println("Final memory state: "+Arrays.toString(state.memory));
		}
	}
	
	public static interface Opcode {
		public void run(ProgramState state);
	}
}
