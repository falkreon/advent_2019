package blue.endless.advent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day9 {
	
	/** Okay, let's get down to it. We need a better incode computer */
	
	public static class ProgramState {
		public String prefix = "";
		public long programCounter = 0;
		public long baseAddress = 0;
		public long[] memory = { 99 }; //No program loaded
		public long addressModes = 0;
		public boolean halt = false;
		public String error = "";
		public String lineDisassembly = "";
		public List<Long> input = new ArrayList<>();
		public List<Long> output = new ArrayList<>();
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
			long mode = addressModes;
			for(int i=0; i<argNumber; i++) {
				mode /= 10;
			}
			mode %= 10;
			return (int)mode;
		}
		
		public long fetch(int argNumber) {
			int mode = addressMode(argNumber);
			
			switch(mode) {
			case 0: //mem
				long address = memory[(int)(programCounter+argNumber+1)];
				if (address>=memory.length) {
					error = "Requested address exceeded memory size setting ("+address+" > "+memory.length+")";
					halt();
					return 0L;
				}
				if (address<0) return 0;
				return memory[(int)address];
			case 1: //imm
				long constant = memory[(int)(programCounter+argNumber+1)];
				return constant;
			case 2: //rel
				long offset = memory[(int)(programCounter+argNumber+1)];
				long phys = baseAddress+offset;
				if (phys>=memory.length) {
					error = "Requested address exceeds memory size setting ("+baseAddress+" + "+offset+" > "+memory.length+")";
					halt();
					return 0L;
				}
				return memory[(int)phys];
			default:
				error = "Unknown address mode "+mode;
				halt();
				return 0;
			}
		}
		
		public void write(int argNumber, long value) {
			int mode = addressMode(argNumber);
			
			switch(mode) {
			case 0: //mem
				long address = memory[(int)(programCounter+argNumber+1)];
				if (address>=memory.length) {
					error = "Requested address exceeded memory size setting ("+address+" > "+memory.length+")";
					halt();
					return;
				}
				if (address<0) return;
				memory[(int)address] = value;
				return;
			case 1: //imm
				error = "Cannot write to immediate value";
				halt();
				return;
			case 2: //rel
				long offset = memory[(int)(programCounter+argNumber+1)];
				long phys = baseAddress+offset;
				if (phys>=memory.length) {
					error = "Requested address exceeds memory size setting ("+baseAddress+" + "+offset+" > "+memory.length+")";
					halt();
					return;
				}
				memory[(int)phys] = value;
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
				long address = memory[(int)(programCounter+argNumber+1)];
				return "["+address+"]";
			case 1: //imm
				return ""+memory[(int)(programCounter+argNumber+1)];
			case 2: //rel
				long offset = memory[(int)(programCounter+argNumber+1)];
				return "ADR:"+offset+"("+(baseAddress+offset)+")";
			default:
				return "?"+mode+"?"+memory[(int)(programCounter+argNumber+1)];
			}
		}
	}
	
	public static class IntcodeCore {
		public Map<Integer, Opcode> opcodes = new HashMap<>();
		public ProgramState state = new ProgramState();
		
		public IntcodeCore() {
			opcodes.put(1, (state)->{ //ADD
				long a = state.fetch(0);
				long b = state.fetch(1);
				state.write(2, a+b);
				state.lineDisassembly = "ADD "+state.explain(0)+" "+state.explain(1)+" -> "+state.explain(2);
				state.programCounter += 4;
			});
			
			opcodes.put(2, (state)->{ //MUL
				long a = state.fetch(0);
				long b = state.fetch(1);
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
					long input = state.input.remove(0);
					state.write(0, input);
					state.programCounter += 2;
				}
			});
			
			opcodes.put(4, (state)->{
				state.lineDisassembly = "OUT "+state.explain(0);
				long out = state.fetch(0);
				state.output.add(out);
				
				state.programCounter += 2;
			});
			
			opcodes.put(5, (state)->{
				state.lineDisassembly = "JNZ "+state.explain(0)+" "+state.explain(1);
				long nz = state.fetch(0);
				long addr = state.fetch(1);
				if (nz!=0) {
					state.programCounter = addr;
				} else {
					state.programCounter += 3;
				}
			});
			
			opcodes.put(6, (state)->{
				state.lineDisassembly = "JZ  "+state.explain(0)+" "+state.explain(1);
				long z = state.fetch(0);
				long addr = state.fetch(1);
				if (z==0) {
					state.programCounter = addr;
				} else {
					state.programCounter += 3;
				}
			});
			
			opcodes.put(7, (state)->{
				state.lineDisassembly = "LES "+state.explain(0)+" "+state.explain(1)+" -> "+state.explain(2);
				long a = state.fetch(0);
				long b = state.fetch(1);
				
				int result = a<b ? 1 : 0;
				
				state.write(2, result);
				
				state.programCounter += 4;
			});
			
			opcodes.put(8, (state)->{
				state.lineDisassembly = "EQU "+state.explain(0)+" "+state.explain(1)+" -> "+state.explain(2);
				long a = state.fetch(0);
				long b = state.fetch(1);
				
				int result = a==b ? 1 : 0;
				
				state.write(2, result);
				
				state.programCounter += 4;
			});
			
			opcodes.put(9, (state)->{
				state.lineDisassembly = "ADR "+state.explain(0);
				long a = state.fetch(0);
				state.baseAddress += a;
				state.programCounter += 2;
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
				state.memory = new long[]{ 99 };
				state.error = "EVERYTHING IS FINE";
				state.halt();
			});
		}
		
		public void setMemory(long[] memory) {
			state.memory = memory;
			if (memory.length<4096) {
				state.memory = Arrays.copyOf(memory, 4096); //Ensure room for at least 4096 cells, each capable of storing numbers from [ -9223372036854775808L .. 9223372036854775807L ] incluisive
			}
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
			
			long opcodeAndMode = state.memory[(int)state.programCounter];
			long opcodeNum = opcodeAndMode % 100;
				
			Opcode opcode = opcodes.get((int)opcodeNum);
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
	
	/**
	 * Now that that's done, we can load in the BOOST program and get our code.
	 */
	
	public static long[] decode(String s) {
		String[] parts = s.trim().split(",");
		long[] result = new long[parts.length];
		
		for(int i=0; i<parts.length; i++) {
			long l = Long.parseLong(parts[i]);
			result[i] = l;
		}
		
		return result;
	}
	
	public static void run(String program) {
		IntcodeCore core = new IntcodeCore();
		core.setMemory(decode(program));
		core.runUntilYield(true);
	}
	
	public static void runFromFile() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day9.dat"));
			IntcodeCore core = new IntcodeCore();
			core.state.input.add(1L); //"Test Mode"
			core.setMemory(decode(file.get(0)));
			core.runUntilYield(true);
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void runPartTwo() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day9.dat"));
			IntcodeCore core = new IntcodeCore();
			core.state.input.add(2L); //"Get Coordinates"
			core.setMemory(decode(file.get(0)));
			core.runUntilYield(true);
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
