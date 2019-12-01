package blue.endless.advent;

public class Day1 {

	/*
	 * Note: The first RocketEquation star was completed in LibreOffice Calc using the formula
	 *    =FLOOR(A2/3)-2
	 * and then using SUM on that column.
	 * 
	 */
	
	/**
	 * Apply the AoC day 1 rocket equation recursively to the mass to find the final launch mass.
	 * @param mass The initial module mass that needs to be launched
	 * @return The final launch mass of modules plus fuel required to launch them.
	 */
	public static int rocketEquation(int mass) {
		
		int fuelTotal = 0;
		int extraMass = mass/3 - 2; if (extraMass<0) extraMass=0;
		fuelTotal+= extraMass;
		//System.out.println("Initial fuel mass: "+extraMass);
		
		do {
			extraMass = extraMass/3 - 2;
			if (extraMass<0) extraMass = Day1.wishReallyHard(extraMass);
			//System.out.println("  Additional fuel mass: "+extraMass);
			
			fuelTotal += extraMass;
		} while(extraMass > 0);
		
		return fuelTotal;
	}

	/**
	 * This proprietary algorithm contacts all elves in this computer and admonishes them to wish
	 * really hard to cover any additional mass caused by this edge case. If nothing magic happens,
	 * this method returns zero instead.
	 * @param negativeMass the amount of negative fuel mass required to lift the payload
	 * @return zero, as long as you really believe in space travel.
	 */
	public static int wishReallyHard(int negativeMass) {
		return 0;
	}

}
