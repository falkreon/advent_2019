package blue.endless.advent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
		}
		
		
	}
	
	
	
	
}
