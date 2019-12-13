package blue.endless.advent;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class Day12 {
	public static List<Moon> parseLines(String... input) {
		ArrayList<Moon> result = new ArrayList<>();
		for(String s: input) result.add(Moon.of(s));
		return result;
	}
	
	public static List<Moon> parseLines(List<String> input) {
		ArrayList<Moon> result = new ArrayList<>();
		for(String s: input) result.add(Moon.of(s));
		return result;
	}
	
	public static void step(List<Moon> moons) {
		for(Moon moon: moons) moon.applyGravity(moons); //Doesn't CME because the list structure stays the same
		for(Moon moon: moons) moon.applyVelocity();
		
		//for(Moon moon: moons) System.out.println(moon);
	}
	
	public static void run(List<Moon> moons, int steps) {
		//System.out.println("After 0 steps:");
		//for(Moon moon: moons) System.out.println(moon);
		
		for(int i=1; i<=steps; i++) {
			//System.out.println();
			//System.out.println("After "+i+" steps:");
			step(moons);
		}
	}
	
	public static int totalEnergy(List<Moon> moons) {
		return moons.stream().mapToInt(Moon::getEnergy).sum();
	}
	
	public static void runFromFile() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day12.dat"));
			List<Moon> moons = parseLines(file);
			run(moons, 1000);
			System.out.println("Total energy after 1000 steps: "+totalEnergy(moons));
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	
	public static class Moon {
		int x;
		int y;
		int z;
		
		int vx;
		int vy;
		int vz;
		
		public Moon(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public void applyGravity(List<Moon> moons) {
			for(Moon other: moons) {
				if (other==this) continue;
				vx += (int)Math.signum(other.x-x);
				vy += (int)Math.signum(other.y-y);
				vz += (int)Math.signum(other.z-z);
			}
		}
		
		public void applyVelocity() {
			x += vx;
			y += vy;
			z += vz;
		}
		
		public int getEnergy() {
			int potentialEnergy = Math.abs(x) + Math.abs(y) + Math.abs(z);
			int kineticEnergy = Math.abs(vx) + Math.abs(vy) + Math.abs(vz);
			return potentialEnergy*kineticEnergy;
		}
		
		public String i(int i) {
			String result = ""+i;
			while(result.length()<3) result = " "+result;
			return result;
		}
		
		@Override
		public boolean equals(Object other) {
			return (other instanceof Moon) && equalTo((Moon)other);
		}
		
		private boolean equalTo(Moon other) {
			return
				x==other.x &&
				y==other.y &&
				z==other.z &&
				vx==other.vx &&
				vy==other.vy &&
				vz==other.vz;
		}
		
		@Override
		public String toString() {
			return "pos=<x="+i(x)+", y="+i(y)+", z="+i(z)+">, vel=<x="+i(vx)+", y="+i(vy)+", z="+i(vz)+">";
		}
		
		public Moon clone() {
			Moon clone = new Moon(x,y,z);
			clone.vx = vx;
			clone.vy = vy;
			clone.vz = vz;
			return clone;
		}
		
		public static Moon of(String s) {
			s = s.trim();
			if (s.startsWith("<")) s = s.substring(1);
			if (s.endsWith(">")) s = s.substring(0, s.length()-1);
			String[] parts = s.split(",");
			//Lots of potential format edge cases here, but the leaderboards are calling
			String xs = parts[0].split("=")[1];
			String ys = parts[1].split("=")[1];
			String zs = parts[2].split("=")[1];
			
			Moon result = new Moon(Integer.parseInt(xs.trim()), Integer.parseInt(ys.trim()), Integer.parseInt(zs.trim()));
			return result;
		}
	}
	
	public static List<Moon> cloneMoons(List<Moon> moons) {
		ArrayList<Moon> result = new ArrayList<>();
		for(Moon moon: moons) result.add(moon.clone());
		return result;
	}
	
	public static long getXPeriod(List<Moon> prototype) {
		BiPredicate<Moon,Moon> matchesX = (a,b) -> a.x==b.x && a.vx==b.vx;
		
		List<Moon> moons = cloneMoons(prototype);
		
		step(moons);
		long i = 1L;
		while (!allMatch(matchesX, moons, prototype)) {
			step(moons);
			i++;
		}
		return i;
	}
	
	public static long getYPeriod(List<Moon> prototype) {
		BiPredicate<Moon,Moon> matchesY = (a,b) -> a.y==b.y && a.vy==b.vy;
		
		List<Moon> moons = cloneMoons(prototype);
		
		step(moons);
		long i = 1L;
		while (!allMatch(matchesY, moons, prototype)) {
			step(moons);
			i++;
		}
		return i;
	}
	
	public static long getZPeriod(List<Moon> prototype) {
		BiPredicate<Moon,Moon> matchesZ = (a,b) -> a.z==b.z && a.vz==b.vz;
		
		List<Moon> moons = cloneMoons(prototype);
		
		step(moons);
		long i = 1L;
		while (!allMatch(matchesZ, moons, prototype)) {
			step(moons);
			i++;
		}
		return i;
	}
	
	public static boolean allMatch(BiPredicate<Moon, Moon> pred, List<Moon> a, List<Moon> b) {
		if (a.size()!=b.size()) return false;
		
		for(int i=0; i<a.size(); i++) {
			if (!pred.test(a.get(i), b.get(i))) return false;
		}
		
		return true;
	}
	
	public static long getPeriod(List<Moon> prototype, int subjectIndex) {
		List<Moon> moons = cloneMoons(prototype);
		Moon subject = moons.get(subjectIndex);
		Moon initialState = subject.clone();
		
		step(moons);
		long i = 1L;
		long xPeriod = -1L;
		long yPeriod = -1L;
		long zPeriod = -1L;
		//TODO: Flip this all around so we find the total solution for one axis instead of the period for the moon
		while(xPeriod<0 || yPeriod<0 || zPeriod<0) {
			step(moons);
			i++;
			
			if (xPeriod<0 && subject.x==initialState.x && subject.vx==initialState.vx) {
				xPeriod = i;
				System.out.println("xPeriod: "+xPeriod);
			}
			if (yPeriod<0 && subject.y==initialState.y && subject.vy==initialState.vy) {
				yPeriod = i;
				System.out.println("yPeriod: "+yPeriod);
			}
			if (zPeriod<0 && subject.z==initialState.z && subject.vz==initialState.vz) {
				zPeriod = i;
				System.out.println("zPeriod: "+zPeriod);
			}
		}
		
		return lcm(xPeriod, lcm(yPeriod, zPeriod));
	}
	
	/** Returns the greatest common divisor (factor) of positive integers a and b
	 * <p>Source: https://en.wikipedia.org/wiki/Binary_GCD_algorithm
	 */
	public static long gcdUnsigned(long u, long v) {
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
	public static long gcd(long a, long b) {
		long gcd = gcdUnsigned(Math.abs(a), Math.abs(b));
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
	public static long lcm(long a, long b) {
		return Math.abs(a*b) / gcd(a, b);
	}
	
	public static void runPartTwoFromFile() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day12.dat"));
			List<Moon> moons = parseLines(file);
			/*List<Moon> moons = parseLines(
					"<x=-8, y=-10, z=0>",
					"<x=5, y=5, z=10>",
					"<x=2, y=-7, z=3>",
					"<x=9, y=-8, z=-3>");
			*/
			long lcm = -1;
			
			long x = getXPeriod(moons);
			System.out.println("X Period: "+x);
			long y = getYPeriod(moons);
			System.out.println("Y Period: "+y);
			lcm = lcm(x,y);
			System.out.println("Prelim lcm: "+lcm);
			long z = getZPeriod(moons);
			System.out.println("Z Period: "+z);
			lcm = lcm(lcm, z);
			System.out.println("Total lcm: "+lcm);
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
