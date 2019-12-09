package blue.endless.advent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AdventOfCode {
	public static void main(String... args) {
		if (args.length==0) {
			System.out.println("usage: java -jar AdventOfCode.jar <day> [params]");
			System.exit(-1);
		}
		
		switch(args[0]) {
		case "1":
		case "rocketfuel":
			try {
				int total = 0;
				for(String s : Files.readAllLines(Paths.get("rocketfuel.txt"))) {
					total += Day1.rocketEquation(Integer.valueOf(s));
				}
				
				System.out.println(total);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//System.out.println(Day1.rocketEquation(100756));
			break;
		
		case "2":
		case "programalarm": {
			//List<Integer> data = Arrays.asList(1,1,1,4,99,5,6,0,99);
			List<String> data;
			try {
				data = Files.readAllLines(Paths.get("intcode.txt"));
				List<Integer> tape = Day2.decode(data.get(0));
				
				//Restore the program to the "1202 program alarm" state
				tape.set(1, 12);
				tape.set(2,  2);
				
				Day2.execFully(tape);
				
				System.out.println("Complete: "+tape);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
		case "2b": {
			List<String> data;
			try {
				data = Files.readAllLines(Paths.get("intcode.txt"));
				List<Integer> tape = Day2.decode(data.get(0));
				
				Day2.findAnswer(tape, 19690720);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
		
		case "wires":
		case "3": {
			Day3.runFromFile();
			break;
		}
		case "3b": {
			Day3.runGraph();
			break;
		}
		
		case "4": {
			int count = Day4.permutations(172851, 675869);
			System.out.println("Permutation count: "+count);
			break;
		}
		case "4b": {
			int count = Day4.partTwoPermutations(172851, 675869);
			System.out.println("Permutation count: "+count);
			break;
		}
		
		case "5": {
			Day5.runFromFile(1);
			break;
		}
		case "5b": {
			Day5.runFromFile(5);
			break;
		}
		
		case "6": {
			Day6.runFromFile();
			break;
		}
		case "6b": {
			Day6.runTransfer();
			break;
		}
		
		case "7": {
			Day7.runFromFile();
			break;
		}
		case "7b": {
			Day7.runResonantFromFile();
			break;
		}
		
		case "8": {
			Day8.runFromFile();
			break;
		}
		case "8b": {
			Day8.decodeFromFile();
			break;
		}
		
		case "9": {
			Day9.runFromFile();
			//Day9.run("109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99");
			break;
		}
		case "9b": {
			Day9.runPartTwo();
			break;
		}
		}
		
		
	}
	
	
	
	
}
