package blue.endless.advent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Day17 {
	
	public static List<String> stringifyOutput(List<Long> output) {
		List<String> lines = new ArrayList<>();
		String line = "";
		for(int i=0; i<output.size(); i++) {
			long cur = output.get(i);
			if (cur==10) {
				lines.add(line);
				line = "";
			} else if (cur==13) {
				//Skip newline
			} else {
				line += (char)cur;
			}
		}
		if (!line.isEmpty()) lines.add(line.trim());
		
		return lines;
	}
	
	public static List<String> stringifyOutput(Day9.IntcodeCore core) {
		return stringifyOutput(core.state.output);
	}
	
	public static void print(List<String> stringList) {
		for(String s : stringList) System.out.println(s);
	}
	
	public static char get(List<String> scaffolds, int x, int y) {
		if (x<0 || x>=scaffolds.get(0).length() || y<0 || y>=scaffolds.size()) return ' ';
		String line = scaffolds.get(y);
		if (line.length()<=x) return ' ';
		return scaffolds.get(y).charAt(x);
	}
	
	public static boolean isScaffold(List<String> scaffolds, int x, int y) {
		return get(scaffolds, x, y)=='#';
	}
	
	public static boolean isIntersection(List<String> scaffolds, int x, int y) {
		return
			isScaffold(scaffolds, x, y) &&
			isScaffold(scaffolds, x-1, y) &&
			isScaffold(scaffolds, x+1, y) &&
			isScaffold(scaffolds, x, y-1) &&
			isScaffold(scaffolds, x, y+1);
	}
	
	public static void run() {
		List<String> input = AdventOfCode.loadFile("day17.dat");
		
		Day9.IntcodeCore core = new Day9.IntcodeCore();
		core.setMemory(Day9.decode(input.get(0)));
		core.state.memory = Arrays.copyOf(core.state.memory, 8192); //Stretch memory further because the scaffold program is spendy
		
		core.runUntilYield(true);
		List<String> stringMap = stringifyOutput(core);
		print(stringMap);
		
		List<Day10.Point2i> intersections = new ArrayList<>();
		
		for(int y=0; y<stringMap.size(); y++) {
			for(int x=0; x<stringMap.get(0).length(); x++) {
				if (isIntersection(stringMap, x, y)) {
					System.out.println("    Intersection found at "+x+", "+y);
					intersections.add(new Day10.Point2i(x,y));
				}
			}
		}
		
		System.out.println("Finding sum...");
		
		int sum = 0;
		for(Day10.Point2i cur: intersections) sum += (cur.x*cur.y);
		System.out.println("Sum of alignment parameters: "+sum);
	}
	
	public static void runPartTwo() {
		List<String> input = AdventOfCode.loadFile("day17.dat");
		
		Day9.IntcodeCore core = new Day9.IntcodeCore();
		core.setMemory(Day9.decode(input.get(0)));
		core.state.memory = Arrays.copyOf(core.state.memory, 8192); //Stretch memory further because the scaffold program is spendy
		
		core.runUntilYield(true);
		List<String> stringMap = stringifyOutput(core);
	}
}
