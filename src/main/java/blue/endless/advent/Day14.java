package blue.endless.advent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Day14 {
	public static Map<String, Reaction> parseReactions(String... input) {
		ArrayList<String> inputList = new ArrayList<String>(input.length);
		for(String s : input) inputList.add(s);
		return parseReactions(inputList);
	}
	
	public static Map<String, Reaction> parseReactions(List<String> input) {
		HashMap<String, Reaction> result = new HashMap<>();
		
		for(String s: input) {
			String[] yeildPieces = s.split(Pattern.quote("=>"));
			String[] reactantStrings = yeildPieces[0].split(Pattern.quote(","));
			ChemicalStack product = ChemicalStack.of(yeildPieces[1]);
			ArrayList<ChemicalStack> reactants = new ArrayList<>();
			for(String reactant: reactantStrings) reactants.add(ChemicalStack.of(reactant));
			result.put(product.name, new Reaction(product, reactants));
		}
		
		return result;
	}
	
	public static void walk(Map<String, Reaction> reactions, String desiredProduct, int amount) {
		Map<String, ChemicalStack> needed = new HashMap<>();
		Map<String, ChemicalStack> extra = new HashMap<>();
		needed.put(desiredProduct, new ChemicalStack(desiredProduct, amount));
		Map<Reaction, Integer> reactionQuantities = new HashMap<>(); //For better logging/output
		
		while(!isFullyReduced(needed)) {
			System.out.println("Needed: "+needed.values());
			
			//walk the graph
			Iterator<String> chemicalNames = needed.keySet().iterator();
			String cur = "ORE";
			while(cur.equals("ORE")) cur = chemicalNames.next();
			if (cur.equals("ORE")) {
				
				break;
			}
			
			ChemicalStack product = needed.remove(cur); //PLEASE DON'T CME
			ChemicalStack alreadyAvailable = extra.get(cur);
			if (alreadyAvailable!=null) {
				if (alreadyAvailable.amount>product.amount) {
					System.out.println(" Supplying ALL of "+product+" from existing stock of "+alreadyAvailable.amount);
					alreadyAvailable.amount -= product.amount;
					continue; //We've fully met this demand already without the need for an additional reaction
				} else {
					System.out.println(" Supplying SOME of "+product+" from existing stock of "+alreadyAvailable.amount);
					extra.remove(cur);
					product.amount -= alreadyAvailable.amount;
					if (product.amount==0) continue; //We've just barely fully met this demand.
				}
			}
			
			Reaction curReaction = reactions.get(cur);
			int count = 0;
			int total = 0;
			while(total<product.amount) {
				count++;
				total += curReaction.product.amount;
			}
			if (reactionQuantities.containsKey(curReaction)) {
				reactionQuantities.put(curReaction, reactionQuantities.get(curReaction)+count);
			} else {
				reactionQuantities.put(curReaction, count);
			}
			
			System.out.println("Employing "+curReaction+" x"+count+" producing "+total+" "+product.name);
			for(ChemicalStack stack: curReaction.reactants) {
				addNeeded(needed, stack.name, stack.amount*count);
			}
			if (total>product.amount) {
				addNeeded(extra, product.name, total-product.amount);
			}
		}
		System.out.println();
		System.out.println("Complete. Final requirements: "+needed.get("ORE"));
		System.out.println("Leftover chemicals: "+extra.values());
		
		System.out.println("Reaction quantities:");
		for(Map.Entry<Reaction, Integer> entry : reactionQuantities.entrySet()) {
			System.out.println("    "+entry.getValue()+"x    "+entry.getKey());
		}
	}
	
	public static boolean isFullyReduced(Map<String, ChemicalStack> map) {
		if (map.size()==0) {
			return true; //This is an error really
		} else if (map.size()>1) {
			return false;
		} else if (map.size()==1) {
			return map.containsKey("ORE");
		} else {
			throw new RuntimeException("...map size is "+map.size());
		}
	}
	
	public static void addNeeded(Map<String, ChemicalStack> needed, String name, int amount) {
		ChemicalStack existing = needed.get(name);
		if (existing==null) {
			existing = new ChemicalStack(name, 0);
			needed.put(name, existing);
		}
		existing.amount += amount;
	}
	
	public static class ChemicalStack {
		public String name;
		public int amount;
		
		public ChemicalStack(String name, int amount) {
			this.name = name;
			this.amount = amount;
		}
		
		@Override
		public String toString() {
			return ""+amount+" "+name;
		}
		
		public static ChemicalStack of(String s) {
			String[] pieces = s.trim().split(Pattern.quote(" "));
			return new ChemicalStack(pieces[1].trim(), Integer.parseInt(pieces[0].trim()));
		}
	}
	
	public static class Reaction {
		public ChemicalStack product;
		public List<ChemicalStack> reactants;
		
		public Reaction(ChemicalStack product, List<ChemicalStack> reactants) {
			this.product = product;
			this.reactants = reactants;
		}
		
		@Override
		public String toString() {
			return reactants+" => "+product;
		}
	}
	
	public static void run() {
		Map<String, Reaction> reactions = parseReactions(
			"2 VPVL, 7 FWMGM, 2 CXFTF, 11 MNCFX => 1 STKFG",
			"17 NVRVD, 3 JNWZP => 8 VPVL",
			"53 STKFG, 6 MNCFX, 46 VJHF, 81 HVMC, 68 CXFTF, 25 GNMV => 1 FUEL",
			"22 VJHF, 37 MNCFX => 5 FWMGM",
			"139 ORE => 4 NVRVD",
			"144 ORE => 7 JNWZP",
			"5 MNCFX, 7 RFSQX, 2 FWMGM, 2 VPVL, 19 CXFTF => 3 HVMC",
			"5 VJHF, 7 MNCFX, 9 VPVL, 37 CXFTF => 6 GNMV",
			"145 ORE => 6 MNCFX",
			"1 NVRVD => 8 CXFTF",
			"1 VJHF, 6 MNCFX => 4 RFSQX",
			"176 ORE => 6 VJHF"
			);
		
		walk(reactions, "FUEL", 1);
	}
	
	public static void run(List<String> input) {
		Map<String, Reaction> reactions = parseReactions(input);
		walk(reactions, "FUEL", 1);
	}
	
	public static final long ONE_TRILLION = 1000000000000L;
	public static final long MILLIS_PER_REPORT = 1000L;
	public static void walkOneTrillion(Map<String, Reaction> reactions, String desiredProduct) {
		Map<String, ChemicalStack> needed = new HashMap<>();
		Map<String, ChemicalStack> extra = new HashMap<>();
		
		long totalOreCost = 0;
		long totalMade = 0;
		long lastReport = System.currentTimeMillis();
		while(totalOreCost<ONE_TRILLION) {
			needed.clear();
			needed.put(desiredProduct, new ChemicalStack(desiredProduct, 1));
			
			while(!isFullyReduced(needed)) {
				//System.out.println("Needed: "+needed.values());
				
				//walk the graph
				Iterator<String> chemicalNames = needed.keySet().iterator();
				String cur = "ORE";
				while(cur.equals("ORE")) cur = chemicalNames.next();
				if (cur.equals("ORE")) {
					
					break;
				}
				
				ChemicalStack product = needed.remove(cur); //PLEASE DON'T CME
				ChemicalStack alreadyAvailable = extra.get(cur);
				if (alreadyAvailable!=null) {
					if (alreadyAvailable.amount>product.amount) {
						//System.out.println(" Supplying ALL of "+product+" from existing stock of "+alreadyAvailable.amount);
						alreadyAvailable.amount -= product.amount;
						continue; //We've fully met this demand already without the need for an additional reaction
					} else {
						//System.out.println(" Supplying SOME of "+product+" from existing stock of "+alreadyAvailable.amount);
						extra.remove(cur);
						product.amount -= alreadyAvailable.amount;
						if (product.amount==0) continue; //We've just barely fully met this demand.
					}
				}
				
				Reaction curReaction = reactions.get(cur);
				int count = 0;
				int total = 0;
				while(total<product.amount) {
					count++;
					total += curReaction.product.amount;
				}
				
				//System.out.println("Employing "+curReaction+" x"+count+" producing "+total+" "+product.name);
				for(ChemicalStack stack: curReaction.reactants) {
					addNeeded(needed, stack.name, stack.amount*count);
				}
				if (total>product.amount) {
					addNeeded(extra, product.name, total-product.amount);
				}
			}
			long curOreCost = needed.get("ORE").amount;
			if (curOreCost+totalOreCost>ONE_TRILLION) {
				break;
			} else {
				totalMade++;
				totalOreCost+=curOreCost;
			}
			
			long now = System.currentTimeMillis();
			if (now-lastReport>MILLIS_PER_REPORT) {
				lastReport = now;
				int percent = (int)(((double)totalOreCost / (double)ONE_TRILLION) * 100.0);
				System.out.println("  Made "+totalMade+" fuel costing "+totalOreCost+" ORE ("+percent+"%)");
			}
		}
		System.out.println();
		System.out.println("Complete. Total made: "+totalMade+" costing "+totalOreCost+" ORE");
	}
	
	
	public static void runPartTwo(List<String> input) {
		walkOneTrillion(parseReactions(input), "FUEL");
		
	}
}
