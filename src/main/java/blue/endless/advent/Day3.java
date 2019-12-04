package blue.endless.advent;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * <p> At first, since the examples are gridded out in ascii, it looks like you could just plot the
 * lines out on a grid, and record any time you go to put a line at a grid cell where a line already
 * is. However, the examples on the page go up to x/y 83, and the actual data is just shy of 1000.
 * Given that the exercises are meant to run on a potato, and that the input lines are likely to be
 * capped at 255 (and therefore each bag of lines at 255/5 or so), the key information here is that
 * the line count is extremely well-bounded and the field size is not.
 * 
 * <p>That means today is all about line-line intersections. These intersections are really simple
 * for orthogonal lines, but if you need non-orthogonal you can look at the generalized case at
 * https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection#Given_two_points_on_each_line
 * or use {@link java.awt.geom.Line2D#intersectsLine(Line2D)}
 * 
 * 
 * <p>Parsing the examples a little further, it's easy to think that the distance to a given
 * intersection is the distance along the line network. It is not. It is just the manhattan distance
 * from the starting node. Another possible misreading is that you could look at the cartesian
 * product of all lines for intersections. But since lines can't cross themselves, we need to keep
 * the two line lists separate. The good news is that drastically reduces the search space too.
 * 
 */
public class Day3 {

	public static List<Line> parse(String s) {
		ArrayList<Line> result = new ArrayList<>();
		int x = 0;
		int y = 0;
		int d = 0;
		for(String str: s.split(",")) {
			str = str.trim();
			char dir = Character.toUpperCase(str.charAt(0));
			int length = Integer.parseInt(str.substring(1));
			
			switch(dir) {
			case 'U':
				result.add(new Line(x, y, d, x, y+length, d+length));
				y+=length;
				d+=length;
				break;
			case 'D':
				result.add(new Line(x, y, d, x, y-length, d+length));
				y-=length;
				d+=length;
				break;
			case 'R':
				result.add(new Line(x, y, d, x+length, y, d+length));
				x+=length;
				d+=length;
				break;
			case 'L':
				result.add(new Line(x, y, d, x-length, y, d+length));
				x-=length;
				d+=length;
				break;
			}
		}
		
		return result;
	}

	public static List<Point> getIntersections(List<Line> a, List<Line> b) {
		ArrayList<Point> points = new ArrayList<>();
		for(Line aLine : a) {
			for(Line bLine : b) {
				Point p = aLine.intersection(bLine);
				//System.out.println(""+aLine+" intersects? "+bLine+": "+p);
				
				if (p!=null && !p.isOrigin()) {
					points.add(p);
				}
			}
		}
		
		return points;
	}
	
	public static Point run(String s, String t) {
		List<Line> a = parse(s);
		List<Line> b = parse(t);
		
		System.out.println("Wire lines:");
		System.out.println("    "+a);
		System.out.println("    "+b);
		
		List<Point> intersections = getIntersections(a,b);
		System.out.println("Intersections: "+intersections);
		
		Point best = null;
		int bestDistance = Integer.MAX_VALUE;
		for(Point p : intersections) {
			int dist = Math.abs(p.x)+Math.abs(p.y);
			if (best==null || dist<bestDistance) {
				best = p;
				bestDistance = dist;
			}
		}
		
		System.out.println("Closest Intersection: "+best+" ("+bestDistance+")");
		return best;
	}
	
	public static void runFromFile() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day3.dat"));
			run(file.get(0), file.get(1));
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static Point runGraph(String s, String t) {
		List<Line> a = parse(s);
		List<Line> b = parse(t);
		
		System.out.println("Wire lines:");
		System.out.println("    "+a);
		System.out.println("    "+b);
		
		List<Point> intersections = getIntersections(a,b);
		System.out.println("Intersections: "+intersections);
		
		Point best = null;
		int bestDistance = Integer.MAX_VALUE;
		for(Point p : intersections) {
			int dist = p.d;
			if (best==null || dist<bestDistance) {
				best = p;
				bestDistance = p.d;
			}
		}
		
		System.out.println("Closest Intersection: "+best+" for a wire distance of "+bestDistance);
		return best;
	}
	
	public static void runGraph() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day3.dat"));
			runGraph(file.get(0), file.get(1));
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		//runGraph("R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51", "U98,R91,D20,R16,D67,R40,U7,R15,U6,R7");
	}
	
	/**
	 * Represents an orthogonal wiring line
	 */
	public static class Line {
		int x1;
		int y1;
		int x2;
		int y2;
		int d1;
		int d2;
		
		public Line(int x1, int y1, int x2, int y2) {
			if (!((x1==x2) ^ (y1==y2))) throw new IllegalArgumentException("Line must be horizontal or vertical");
			
			/* Flip lines if they're backwards */
			
			if (x1>x2) {
				this.x1 = x2;
				this.x2 = x1;
			} else {
				this.x1 = x1;
				this.x2 = x2;
			}
			
			if (y1>y2) {
				this.y1 = y2;
				this.y2 = y1;
			} else {
				this.y1 = y1;
				this.y2 = y2;
			}
		}
		
		public Line(int x1, int y1, int d1, int x2, int y2, int d2) {
			if (!((x1==x2) ^ (y1==y2))) throw new IllegalArgumentException("Line must be horizontal or vertical");
			
			/* Flip lines if they're backwards */
			
			if (x1>x2) {
				this.x1 = x2;
				this.x2 = x1;
				this.d1 = d2;
				this.d2 = d1;
			} else {
				this.x1 = x1;
				this.x2 = x2;
				if (x1!=x2) {
					this.d1 = d1;
					this.d2 = d2;
				}
			}
			
			if (y1>y2) {
				this.y1 = y2;
				this.y2 = y1;
				this.d1 = d2;
				this.d2 = d1;
			} else {
				this.y1 = y1;
				this.y2 = y2;
				if (y1!=y2) {
					this.d1 = d1;
					this.d2 = d2;
				}
			}
		}
		
		public boolean intersects(Line l) {
			return intersection(l)!=null;
		}
		
		@Nullable
		public Point intersection(Line l) {
			
			if (x1==x2 && y1!=y2) {
				
				if (l.x1<=this.x1 && l.x2>=this.x1) {
					if (l.y1>=this.y1 && l.y2<=this.y2) {
						Point result = new Point(this.x1, l.y1);
						//System.out.println("Distance to "+result+": "+l.distanceTo(result)+"+"+this.distanceTo(result));
						
						result.d = l.distanceTo(result) + this.distanceTo(result);
						return result;
					}
				}
				
				return null;
			} else if (x1!=x2 && y1==y2) {
				
				if (l.y1<=this.y1 && l.y2 >=this.y1) {
					if (l.x1>=this.x1 && l.x2<=this.x2) {
						Point result = new Point(l.x1, this.y1);
						//System.out.println("Distance to "+result+": "+l.distanceTo(result)+"+"+this.distanceTo(result));
						
						result.d = l.distanceTo(result) + this.distanceTo(result);
						return result;
					}
				}
				
				return null;
			} else {
				throw new IllegalStateException("Can't process diagonal lines");
			}
		}
		
		public int distanceTo(Point p) {
			int dir = (d2>d1) ? 1 : -1;
			
			if (x1==x2) {
				return (p.y - y1) * dir + d1;
			} else if (y1==y2) {
				return (p.x - x1) * dir + d1;
				
			} else throw new IllegalStateException("Line is not supposed to be diagonal");
		}
		
		@Override
		public String toString() {
			return "("+x1+", "+y1+" ("+d1+") -> "+x2+", "+y2+" ("+d2+"))";
		}
	}
	
	/**
	 * Integer point class, used here for tracking intersections.
	 * 
	 * In Haskell this would just be a data class. In Swift this could be a struct. Sadly, here,
	 * have some boilerplate. Let's hope for Amber soon ( http://cr.openjdk.java.net/~briangoetz/amber/datum.html )
	 */
	public static class Point {
		public int x;
		public int y;
		public int d = 0;
		
		public Point(int x, int y) {
			this.x = x; this.y = y;
		}
		
		public boolean isOrigin() {
			return x==0 && y==0;
		}
		
		@Override
		public String toString() {
			return "("+x+", "+y+")";
		}
	}
	
}
