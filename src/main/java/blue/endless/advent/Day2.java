package blue.endless.advent;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Don't be fooled by the Apollo window dressing, what today's exercises are about is building a Turing
 * machine. The addition and multiplication instructions are extra for this; you could build those
 * operations by comparing symbols on the tape. The four-integer word is also extra. But this is
 * easier to build and understand programs for, so we'll go with it.
 */
public class Day2 {
	
	/**
	 * Executes one iteration of the Turing machine
	 * @param tape The intcode list of the program/data
	 * @param offset The current offset into the list. Must be a multiple of 4.
	 * @return true to continue executing, false if the halt instruction has been encountered
	 * @throws RuntimeException if an unknown opcode is encountered. Both HLT and HCF are considered known opcodes.
	 */
	public static boolean exec(List<Integer> tape, int offset) throws RuntimeException {
		int op = tape.get(offset);
		switch(op) {
		case 1: { // Add
			/*
			 * Adds indirect operands A and B together, writing the result to address C
			 */
			int aAddr = tape.get(offset+1);
			int bAddr = tape.get(offset+2);
			int dstAddr = tape.get(offset+3);
			int a = tape.get(aAddr);
			int b = tape.get(bAddr);
			growTape(tape, dstAddr);
			tape.set(dstAddr, a+b);
			return true;
		}
		
		case 2: { // Multiply
			/*
			 * Multiplies indirect operands A and B together, writing the result to address C
			 */
			int aAddr = tape.get(offset+1);
			int bAddr = tape.get(offset+2);
			int dstAddr = tape.get(offset+3);
			int a = tape.get(aAddr);
			int b = tape.get(bAddr);
			growTape(tape, dstAddr);
			tape.set(dstAddr, a*b);
			return true;
		}
		
		case 99: // Halt
			return false;
			
		case -1: // Halt and Catch Fire
			/*
			 * Burn the tape from start to finish with more halt-and-catch-fire instructions, requiring
			 * an Apollo-themed code exercise to build a new ship computer.
			 */
			for(int i=0; i<tape.size(); i++) {
				tape.set(i, -1);
			}
			return false;
			
		default: //Invalid Opcode
			throw new RuntimeException("Unknown opcode "+op);
		}
	}
	
	public static void execFully(List<Integer> tape) throws RuntimeException {
		int offset = 0;
		while(exec(tape, offset)) {
			System.out.println("Executing: "+tape);
			offset += 4;
		}
		
		System.out.println("Complete: "+tape); // Java has really nice List.toString implementations
	}
	
	public static void growTape(List<Integer> tape, int neededPos) {
		while(tape.size()<neededPos+1) {
			tape.add(0);
		}
	}
	
	/**
	 * Convenience method to take comma-separated integers loaded from a file and parse it into a
	 * List&lt;Integer&gt; that can be used as intcode by the Turing machine.
	 * @throws NumberFormatException if one of the values in the list cannot be parsed into an Integer.
	 */
	public static List<Integer> decode(String s) throws NumberFormatException {
		ArrayList<Integer> result = new ArrayList<>();
		for(String t : s.split(",")) {
			result.add(Integer.valueOf(t.trim()));
		}
		
		return result;
	}
}
