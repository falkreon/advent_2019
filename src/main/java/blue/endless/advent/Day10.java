package blue.endless.advent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Day10 {
	
	/** Finds the cells where the ray passes through the *exact center* of the cell. */
	public static List<Point2i> raycast(int x1, int y1, int x2, int y2) {
		ArrayList<Point2i> result = new ArrayList<>();
		int dx = x2-x1;
		int dy = y2-y1;
		boolean horizontal = Math.abs(dx) > Math.abs(dy);
		int steps = (horizontal) ? Math.abs(dx) : Math.abs(dy);
		if (steps==0) return result; //This line is length 0. If we continue, we'll just get into DivideByZero errors for no reason
		
		double x = x1;
		double y = y1;
		
		double xStep = dx/(double)steps;
		double yStep = dy/(double)steps;
		
		/*
		 * This is basically Bresenham. I've written this a million times. It'll give me every
		 * cell's exact intercepts along the way.
		 */
		
		for(int i=0; i<steps; i++) {
			x += xStep;
			y += yStep;
			
			if (isInt(x) && isInt(y)) {
				result.add(new Point2i((int)x, (int)y));
			}
		}
		
		return result;
	}
	
	private static final double TOLERANCE = 1E-18;
	private static boolean isInt(double d) {
		return Math.abs(Math.floor(d) - d) < TOLERANCE;
	}
	
	public static double distance(Point2i point, int x, int y) {
		double dx = Math.abs(x-point.x);
		double dy = Math.abs(y-point.y);
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public static Point2i raycast(AsteroidField field, int x1, int y1, int x2, int y2) {
		
		//ATAN2 approach; this approach seems to be slightly incorrect for far-away points but gives answers equivalent to the AoC examples.
		double targetSlope = Math.atan2(y2-y1, x2-x1);
		Point2i closest = new Point2i(x2, y2);
		double closestDistance = distance(closest, x1, y1);
		for(Point2i cur : field.asteroids) {
			if (cur.x==x1 && cur.y==y1) continue;
			if (cur.equals(closest)) continue;
			
			double curSlope = Math.atan2(cur.y-y1, cur.x-x1);
			if (Math.abs(curSlope-targetSlope) < TOLERANCE) {
				double curDistance = distance(cur, x1, y1);
				if (curDistance<closestDistance) {
					closestDistance = curDistance;
					closest = cur;
				}
			}
		}
		
		return closest;
	}
	
	public static Point2i raycastBresenham(AsteroidField field, int x1, int y1, int x2, int y2) {
		
		//Bresenham approach; I'm way more confident in this approach's accuracy, but it gives me "incorrect" answers.
		List<Point2i> hits = raycast(x1, y1, x2, y2);
		if (hits.size()==0) return new Point2i(x2, y2);
		if (hits.size()==1) return hits.get(0);
		for(Point2i cur : hits) {
			if (cur.x==x1 && cur.y==y1) continue; //Should never happen
			if (field.hasAsteroid(cur.x, cur.y)) return cur;
		}
		return hits.get(hits.size()-1);
	}
	
	/** Returns true if a detector station at x1,y1 can detect an asteroid at x2,y2 */
	public static boolean canDetect(AsteroidField field, int x1, int y1, int x2, int y2) {
		Point2i hit = raycast(field, x1, y1, x2, y2);
		return hit.x==x2 && hit.y==y2;
	}
	
	public static boolean canDetectBresenham(AsteroidField field, int x1, int y1, int x2, int y2) {
		Point2i hit = raycastBresenham(field, x1, y1, x2, y2);
		return hit.x==x2 && hit.y==y2;
	}
	
	/** If you place your detector at this location, how many asteroids can you detect? */
	public static int detectorCount(AsteroidField field, int x, int y) {
		int count = 0;
		
		for(Point2i asteroid : field.asteroids) {
			if (asteroid.x==x && asteroid.y==y) continue; //This is the detector itself
			if (canDetect(field, x, y, asteroid.x, asteroid.y)) {
				count++;
			}
		}
		
		return count;
	}
	
	public static List<Point2i> getDetections(AsteroidField field, int x, int y) {
		List<Point2i> result = new ArrayList<>();
		
		for(Point2i asteroid : field.asteroids) {
			if (asteroid.x==x && asteroid.y==y) continue; //This is the detector itself
			if (canDetect(field, x, y, asteroid.x, asteroid.y)) {
				result.add(asteroid);
			}
		}
		
		return result;
	}
	
	public static List<Detection> getDetectionsAndAngles(AsteroidField field, int x, int y) {
		List<Detection> result = new ArrayList<>();
		
		for(Point2i asteroid : field.asteroids) {
			if (asteroid.x==x && asteroid.y==y) continue; //This is the detector itself
			if (canDetect(field, x, y, asteroid.x, asteroid.y)) {
				double theta = Math.atan2(asteroid.y-y, asteroid.x-x);
				if (theta<0) theta+=Math.PI*2; //-PI..PI -> 0..2PI
				
				theta += Math.PI/2; //Give them a quarter turn to point 0 up instead of right
				
				//Wrap that back into the range we need
				if (theta<0) theta+=Math.PI*2;
				if (theta>=Math.PI*2) theta-=Math.PI*2;
				
				result.add(new Detection(asteroid, theta));
			}
		}
		
		return result;
	}
	
	public static List<Point2i> getDetectionsBresenham(AsteroidField field, int x, int y) {
		List<Point2i> result = new ArrayList<>();
		
		for(Point2i asteroid : field.asteroids) {
			if (asteroid.x==x && asteroid.y==y) continue; //This is the detector itself
			if (canDetectBresenham(field, x, y, asteroid.x, asteroid.y)) {
				result.add(asteroid);
			}
		}
		
		return result;
	}
	
	public static void printDetectionMap(AsteroidField field, int x, int y) {
		List<Point2i> asteroids = getDetections(field, x, y);
		
		for(int yi=0; yi<field.height; yi++) {
			String line = "";
			for(int xi=0; xi<field.width; xi++) {
				if (xi==x && yi==y) {
					line += "D";
				} else {
					if (field.hasAsteroid(xi, yi)) {
						if (asteroids.contains(new Point2i(xi, yi))) {
							line += "#";
						} else {
							line += "~";
						}
					} else {
						line += ".";
					}
				}
			}
			System.out.println(line);
		}
	}
	
	public static void printDetectionMapDisputes(AsteroidField field, int x, int y) {
		List<Point2i> asteroids = getDetections(field, x, y);
		List<Point2i> bres = getDetectionsBresenham(field, x, y);
		
		for(int yi=0; yi<field.height; yi++) {
			String line = "";
			for(int xi=0; xi<field.width; xi++) {
				if (xi==x && yi==y) {
					line += "D";
				} else {
					if (field.hasAsteroid(xi, yi)) {
						if (asteroids.contains(new Point2i(xi, yi))) {
							line += "#";
						} else {
							if (bres.contains(new Point2i(xi,yi))) {
								line += "+";
							} else {
								line += "~";
							}
						}
					} else {
						line += ".";
					}
				}
			}
			System.out.println(line);
		}
	}
	
	public static Point2i bestDetector(AsteroidField field) {
		Point2i bestDetector = null;
		int bestDetections = 0;
		//System.out.println("Detector effectiveness");
		for(Point2i asteroid : field.asteroids) {
			int cur = detectorCount(field, asteroid.x, asteroid.y);
			//System.out.println("   "+asteroid+": "+cur);
			if (bestDetector==null || bestDetections<cur) {
				bestDetector = asteroid;
				bestDetections = cur;
			}
		}
		
		//System.out.println("Best detector is "+bestDetector+" with "+bestDetections+" detections.");
		
		return bestDetector;
	}
	
	public static class Point2i {
		int x = 0;
		int y = 0;
		
		public Point2i() {}
		public Point2i(int x, int y) { this.x=x; this.y=y; }
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof Point2i) {
				Point2i o = (Point2i)other;
				return o.x==x && o.y==y;
				
			} else return false;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(Integer.valueOf(this.x), Integer.valueOf(this.y));
		}
		
		@Override
		public String toString() {
			return "{ "+x+", "+y+" }";
		}
	}
	
	public static class Detection {
		public Point2i asteroid;
		public double theta;
		
		public Detection(Point2i asteroid, double theta) {
			this.asteroid = asteroid;
			this.theta = theta;
		}
		
		@Override
		public String toString() {
			return asteroid.toString()+" ("+theta+")";
		}
	}
	
	public static class AsteroidField {
		public int width;
		public int height;
		public boolean[] field;
		public List<Point2i> asteroids = new ArrayList<>();
		
		public AsteroidField(String... input) {
			height = input.length;
			width = input[0].length();
			field = new boolean[width*height];
			
			for(int y=0; y<height; y++) {
				String line = input[y];
				for(int x=0; x<width; x++) {
					if (line.length()<=x) break;
					boolean asteroid = (line.charAt(x)!='.');
					field[y*width+x] = asteroid;
					if (asteroid) asteroids.add(new Point2i(x,y));
				}
			}
		}
		
		public AsteroidField(List<String> input) {
			height = input.size();
			width = input.get(0).length();
			field = new boolean[width*height];
			
			
			for(int y=0; y<height; y++) {
				String line = input.get(y);
				for(int x=0; x<width; x++) {
					if (line.length()<=x) break;
					boolean asteroid = (line.charAt(x)!='.');
					field[y*width+x] = asteroid;
					if (asteroid) asteroids.add(new Point2i(x,y));
				}
			}
		}
		
		public boolean hasAsteroid(int x, int y) {
			if (x<0 || x>=width || y<0 || y>=height) return false;
			return field[y*width+x];
		}
	}
	
	public static void runFromFile() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day10.dat"));
			AsteroidField field = new AsteroidField(file);
			Point2i detector = bestDetector(field);
			System.out.println("Best detector: "+detector+" with "+detectorCount(field, detector.x, detector.y)+" detections");
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/** MUTATES THE FIELD */
	public static List<Point2i> vaporize(AsteroidField field, int x, int y) {
		ArrayList<Point2i> vaporizations = new ArrayList<Point2i>();
		
		while(field.asteroids.size()>1) {
			List<Detection> detections = getDetectionsAndAngles(field, x, y);
			if (detections.size()==0) {
				System.out.println("SOMETHING WENT WRONG");
				break;
			}
			detections.sort((a,b)->(int)Math.signum(a.theta-b.theta));
			
			for(Detection d: detections) {
				field.asteroids.remove(d.asteroid);
				vaporizations.add(d.asteroid);
			}
		}
		
		return vaporizations;
	}
	
	
	public static void runPartTwoFromFile() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day10.dat"));
			AsteroidField field = new AsteroidField(file);
			
			/*
			AsteroidField field = new AsteroidField(
					".#..##.###...#######",
					"##.############..##.",
					".#.######.########.#",
					".###.#######.####.#.",
					"#####.##.#.##.###.##",
					"..#####..#.#########",
					"####################",
					"#.####....###.#.#.##",
					"##.#################",
					"#####.##.###..####..",
					"..######..##.#######",
					"####.##.####...##..#",
					".#####..#.######.###",
					"##...#.##########...",
					"#.##########.#######",
					".####.#.###.###.#.##",
					"....##.##.###..#####",
					".#.#.###########.###",
					"#.#.#.#####.####.###",
					"###.##.####.##.#..##");
			
			List<Point2i> orderedVaporizations = vaporize(field, 11, 13);
			for(int i=0; i<orderedVaporizations.size(); i++) {
				System.out.println(""+(i+1)+": "+orderedVaporizations.get(i));
			}*/
			
			Point2i bestDetector = bestDetector(field);
			List<Point2i> vaporizations = vaporize(field, bestDetector.x, bestDetector.y);
			System.out.println(vaporizations);
			Point2i twoHundred = vaporizations.get(199);
			int answer = twoHundred.x*100 + twoHundred.y;
			System.out.println("200: "+twoHundred+" (puzzle answer is "+answer+")");
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * Dispute resolution: The best way I can think of to determine, for certain, if one or the other
	 * (atan2 or bresenham) approach is correct, is a third approach, wherein the slope to each asteroid
	 * is kept in rational form and compared against another slope by converting to least common divisor.
	 * 
	 * This will allow us to determine colinear asteroids with full (infinite) precision.
	 * 
	 * 
	 * 
	 */
	
	/** Returns the greatest common divisor (factor) of positive integers a and b
	 * <p>Source: https://en.wikipedia.org/wiki/Binary_GCD_algorithm
	 */
	public static int gcdUnsigned(int u, int v) {
		// simple cases (termination)
		if (u == v) return u;
		if (u == 0) return v;
		if (v == 0) return u;

		// look for factors of 2
		if ((u&1)==0) { // u is even
			if ((v&1)==1) { // v is odd
				return gcdUnsigned(u >> 1, v);
			} else { // both u and v are even
				return gcdUnsigned(u >> 1, v >> 1) << 1;
			}
		}
		if ((v&1)==0) {// u is odd, v is even
			return gcdUnsigned(u, v >> 1);
		}
		// reduce larger argument
		if (u > v)
			return gcdUnsigned((u - v) >> 1, v);

		return gcdUnsigned((v - u) >> 1, u);
	}
	
	/** Handles the one additional case for fractions which potentially have -1 as a factor */
	public static int gcd(int a, int b) {
		int gcd = gcdUnsigned(Math.abs(a), Math.abs(b));
		if (a<0 && b<0) { //-1 is also a common factor
			return -gcd;
		} else {
			return gcd;
		}
	}
	
	/**
	 * Returns the least common multiple of integer divisors a and b
	 * <p>Source: https://en.wikipedia.org/wiki/Least_common_multiple#Using_the_greatest_common_divisor
	 */
	public static int lcm(int a, int b) {
		return Math.abs(a*b) / gcd(a, b);
	}
	
	public static boolean areFractionsEqual(int n1, int d1, int n2, int d2) {
		int lcm = lcm(d1, d2);
		//LCM will never be zero, but if it is, that's exceptional enough to throw divideByZero here.
		
		int scale1 = lcm/d1;
		int scale2 = lcm/d2;
		
		int tn1 = n1*scale1;
		int tn2 = n2*scale2;
		
		return tn1==tn2; //When converted to the same bases, the numerators should be equal.
	}
	
	public static class PerfectDetection {
		public Point2i asteroid;
		
		/* Note: This is not rise-over-run! It's x/y */
		public int numerator;
		public int denominator;
		
		public PerfectDetection(Point2i asteroid, int dx, int dy) {
			this.asteroid = asteroid;
			this.numerator = dx;
			this.denominator = dy;
			reduceFraction();
		}
		
		//Takes this asteroid's fraction and reduces it
		public void reduceFraction() {
			int gcd = gcd(numerator, denominator);
			if (gcd==1 || gcd==-1) return; //Already reduced
			
			numerator /= gcd;
			denominator /= gcd;
		}
	}
	
	public List<PerfectDetection> allAsteroids(AsteroidField field, int x, int y) {
		ArrayList<PerfectDetection> result = new ArrayList<>();
		for(Point2i point: field.asteroids) {
			PerfectDetection cur = new PerfectDetection(point, point.x-x, point.y-y);
		}
		
		return null;
	}
	
	public static void runDisputeResolution() {
		
		String[] testInput = {
			".#..##.###...#######",
			"##.############..##.",
			".#.######.########.#",
			".###.#######.####.#.",
			"#####.##.#.##.###.##",
			"..#####..#.#########",
			"####################",
			"#.####....###.#.#.##",
			"##.#################",
			"#####.##.###..####..",
			"..######..##.#######",
			"####.##.####...##..#",
			".#####..#.######.###",
			"##...#.##########...",
			"#.##########.#######",
			".####.#.###.###.#.##",
			"....##.##.###..#####",
			".#.#.###########.###",
			"#.#.#.#####.####.###",
			"###.##.####.##.#..##"
			};
		AsteroidField field = new AsteroidField(testInput);
		
		System.out.println(gcd(-3, -6));
	}
}
