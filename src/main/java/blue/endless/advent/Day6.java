package blue.endless.advent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Yay, directed graphs! */
public class Day6 {
	
	public static void runFromFile() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day6.dat"));
			
			Map<String, Node> input = parse(file);
			getOrbitCount(input);
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void runTransfer() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day6.dat"));
			
			Map<String, Node> nodes = parse(file);
			Node you = nodes.get("YOU");
			Node san = nodes.get("SAN");
			String a = you.orbiting;
			String b = san.orbiting;
			String ancestor = getCommonAncestor(a, b, nodes);
			
			int transferSteps = stepsTo(a, ancestor, nodes) + stepsTo(b, ancestor, nodes);
			System.out.println("Steps to transfer from "+a+" to "+b+": "+transferSteps);
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static Map<String, Node> parse(String... input) {
		List<String> tmp = new ArrayList<>(input.length);
		for(String s : input) tmp.add(s);
		return parse(tmp);
	}
	
	public static Map<String, Node> parse(List<String> input) {
		HashMap<String, Node> result = new HashMap<>();
		Node com = new Node("COM");
		result.put("COM", com);
		for(String s : input) {
			Node created = parseNode(s, result);
			if (result.containsKey(created.name)) {
				throw new IllegalStateException("Duplicate definitions of the same orbiting body; a body can only orbit one other body directly.");
			} else {
				result.put(created.name, created);
			}
		}
		
		return result;
	}
	
	public static Node parseNode(String s, Map<String, Node> nodes) {
		String[] parts = s.split("\\)");
		if (parts.length<2) throw new IllegalArgumentException("Orbit specifications must be in the form A)B - found "+s);
		String anchorName = parts[0];
		String resultName = parts[1];
		Node result = new Node(resultName);
		result.orbiting = anchorName;
		
		return result;
	}
	
	public static void getOrbitCount(Map<String, Node> nodes) {
		int globalSteps = 0;
		for(Node node : nodes.values()) {
			String followDiagram = node.name;
			Node follow = node;
			int localSteps = 0;
			while(follow.orbiting!=null) {
				localSteps++;
				follow = nodes.get(follow.orbiting);
				followDiagram += ")"+follow.name;
			}
			globalSteps += localSteps;
			followDiagram += " = "+localSteps+" orbits";
		}
		
		System.out.println("Total orbits in system: "+globalSteps);
	}
	
	public static List<String> getAncestryChain(String nodeName, Map<String, Node> nodes) {
		ArrayList<String> result = new ArrayList<>();
		
		Node follow = nodes.get(nodeName);
		
		if (follow==null) throw new IllegalArgumentException("Unknown node "+nodeName);
		while (follow.orbiting!=null) {
			follow = nodes.get(follow.orbiting);
			result.add(follow.name);
		}
		
		return result;
	}
	
	public static String getCommonAncestor(String a, String b, Map<String, Node> nodes) {
		List<String> aa = getAncestryChain(a, nodes);
		List<String> ba = getAncestryChain(b, nodes);
		
		for(String s : aa) { //walk up a's ancestry tree from leaf to stem
			for(String t : ba) {
				if (s.equals(t)) { //this ancestor of A matches *any* ancestor of B, so we have our common ancestor
					return s;
				}
			}
		}
		
		throw new IllegalArgumentException("these orbits have no common ancestor.");
	}
	
	public static int stepsTo(String node, String ancestor, Map<String, Node> nodes) {
		Node follow = nodes.get(node);
		int steps = 0;
		if (follow==null) throw new IllegalArgumentException("Unknown node "+node);
		while (follow.orbiting!=null) {
			follow = nodes.get(follow.orbiting);
			steps++;
			if (follow.name.equals(ancestor)) {
				return steps;
			}
		}
		throw new IllegalArgumentException("Node "+node+" does not have ancestor "+ancestor);
	}
	
	public static class Node {
		public String name = "";
		public String orbiting = null;
		public List<String> children = new ArrayList<String>();
		
		public Node(String name) {
			this.name = name;
		}
	}
}
