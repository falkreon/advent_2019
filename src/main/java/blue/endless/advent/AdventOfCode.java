package blue.endless.advent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
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
		case "programalarm":
			//List<Integer> data = Arrays.asList(1,1,1,4,99,5,6,0,99);
			List<String> data;
			try {
				data = Files.readAllLines(Paths.get("intcode.txt"));
				List<Integer> tape = Day2.decode(data.get(0));
				
				//Restore the program to the "1202 program alarm" state
				tape.set(1, 12);
				tape.set(2,  2);
				
				Day2.execFully(tape);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			
			
		}
		
		
	}
	
	
	
	
}
