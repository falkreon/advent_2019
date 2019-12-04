package blue.endless.advent;

import java.util.ArrayList;

public class Day4 {
	
	/** Finds the permutation count of a doubles scenario. */
	public static boolean fitsCriteria(int[] password) {
		int prev = 9;
		boolean hasDoubled = false;
		for(int i=0; i<password.length; i++) {
			if (i!=0 && prev>password[i]) return false; //password pieces cannot decrease
			if (i!=0 && prev==password[i]) {
				hasDoubled = true;
			}
			prev = password[i];
		}
		return hasDoubled;
	}
	
	/** Does this number have at least one *double* which isn't part of a larger repetition? */
	public static boolean hasPartTwoDouble(int[] password) {
		int doubleCount = 0;
		int curDouble = -1;
		for(int i=0; i<password.length; i++) {
			if (password[i]!=curDouble) {
				if (doubleCount==2) return true;
				
				doubleCount = 1;
				curDouble = password[i];
			} else {
				doubleCount++;
			}
		}
		return (doubleCount==2);
	}
	
	public static boolean isMonotonic(int[] password) {
		int prev = 9;
		for(int i=0; i<password.length; i++) {
			if (i!=0 && prev>password[i]) return false;
			prev = password[i];
		}
		return true;
	}
	
	public static boolean matchesPartTwo(int[] password) {
		//if (isMonotonic(password)) System.out.println("HasPartTwoDouble "+Arrays.toString(password)+": "+hasPartTwoDouble(password));
		
		return isMonotonic(password) && hasPartTwoDouble(password);
	}
	
	public static int permutations(int start, int end) {
		int permutations = 0;
		System.out.println("Permutations:");
		for(int i=start; i<=end; i++) {
			if (fitsCriteria(getDigits(i))) {
				System.out.println("    "+i);
				permutations++;
			}
		}
		
		return permutations;
	}
	
	public static int partTwoPermutations(int start, int end) {
		int permutations = 0;
		System.out.println("Permutations:");
		for(int i=start; i<=end; i++) {
			if (matchesPartTwo(getDigits(i))) {
				System.out.println("    "+i);
				permutations++;
			}
		}
		
		return permutations;
	}
	
	private static int[] getDigits(int password) {
		ArrayList<Integer> result = new ArrayList<>();
		while(password>0) {
			int digit = password % 10;
			result.add(0, digit);
			password = password / 10;
		}
		
		int[] arr = new int[result.size()];
		for(int i=0; i<result.size(); i++) arr[i] = result.get(i);
		return arr;
	}
}
