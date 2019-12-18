package blue.endless.advent;

import java.util.Arrays;

public class Day16 {
	public static void run() {
		long[] input = decode(
				"5979067790332293069735877097945699671297385945170972051507448714124650741959003959"+
				"8329735611909754526681279087091321241889537569965210074382210124927546962637736867"+
				"7426602277965664668716805800052881001926708871740840775742582063075576825498367955"+
				"9841062404254926180168911355988100862975204821386279655615668180216384321154644322"+
				"8186862314896620419832148583664829023116082772951046466358463667825025457939806789"+
				"4696838660092412294877087324359095446504280692631805222639092119862315812283304564"+
				"4145192777712538859019717065396284208318691472161156045145992841881525444377346083"+
				"2555717155899456905676980728095392900218760297612453568324542692109397431554"
				);
		long[] basePattern = { 0L, 1L, 0L, -1L };
		
		transformCycle(basePattern, input, 100);
	}
	
	public static long[] decode(String s) {
		long[] result = new long[s.length()];
		for(int i=0; i<s.length(); i++) result[i] = Integer.valueOf(""+s.charAt(i));
		return result;
	}
	
	public static long generateFFTPattern(long[] basePattern, int digitNumber, int termNumber) {
		int index = (termNumber+1) / digitNumber;
		if (index<0) index+= basePattern.length;
		return basePattern[index%basePattern.length];
	}
	
	public static long getDigitTerm(long[] basePattern, long[] input, int inputPosition, int outputPosition) {
		long fft = generateFFTPattern(basePattern, inputPosition, outputPosition);
		long digit = input[outputPosition%input.length];
		return fft*digit;
	}
	
	public static long transformDigit(long[] basePattern, long[] input, int inputPosition) {
		long finalDigit = 0L;
		
		for(int i=inputPosition-1; i<input.length; i++) {
			long cur = getDigitTerm(basePattern, input, inputPosition, i);
			finalDigit+=cur;
		}
		String transform = ""+finalDigit;
		return Long.parseLong(transform.substring(transform.length()-1));
	}
	
	public static long[] transform(long[] basePattern, long[] input) {
		long lastReport = System.currentTimeMillis();
		
		long[] output = new long[input.length];
		for(int i=0; i<input.length; i++) {
			output[i] = transformDigit(basePattern, input, i+1);
			
			long now = System.currentTimeMillis();
			if (now-lastReport>5_000L) {
				System.out.println("Finished "+i+" / "+input.length+" digits");
				lastReport = now;
			}
		}
		return output;
	}
	
	public static String stringify(long[] l) {
		String result = "";
		for(int i=0; i<l.length; i++) result+=l[i];
		return result;
	}
	
	public static long[] transformCycle(long[] basePattern, long[] input, int phases) {
		long[] prev = input;
		for(int i=0; i<phases; i++) {
			long[] output = transform(basePattern, prev);
			System.out.println("After "+(i+1)+" phases: "+stringify(output));
			prev = output;
		}
		return prev;
	}
	
	/* Let's just use all this memory to solve the problem */
	public static int TEN_THOUSAND = 10_000;
	public static long[] stretch(long[] input, int times) {
		long[] stretched = new long[input.length*times];
		for(int i=0; i<times; i++) {
			int base = input.length*i;
			System.arraycopy(input, 0, stretched, base, input.length);
		}
		return stretched;
	}
	
	
	public static class FFTStep {
		FFTStep previous = null;
		
		public long[] basePattern;
		public long[] input;
		public long[] output;
		public int firstInput; //first index into input which is available
		public int firstOutput; //first index into output which is complete and can be fetched by later steps
		public int digit;
		public int firstTerm;
		public long combinedDigit;
		public String termsDebug = "";
		
		public FFTStep(long[] basePattern, long[] input) {
			this.basePattern = basePattern;
			this.input = input;
			this.output = new long[input.length];
			this.firstInput = 0; //All input is buffered
			this.firstOutput = input.length; //No indices are available
			this.firstTerm = input.length; //No terms are computed
			this.digit = input.length;
		}
		
		public void fetch() {
			if (previous==null) return;
			if (previous.firstOutput<previous.output.length && previous.firstOutput < firstInput) {
				System.arraycopy(previous.output, previous.firstOutput, input, previous.firstOutput, firstInput-previous.firstOutput);
				firstInput = previous.firstOutput;
			}
		}
		
		/** Returns true if it could run */
		public boolean computeTerm() {
			if (firstTerm==0) return false;
			if (this.digit==0) return false;
			if (firstTerm<firstInput) {
				fetch();
				if (firstTerm<firstInput) return false;
			}
			
			firstTerm--;
			long fft = generateFFTPattern(basePattern, this.digit, firstTerm);
			long digit = input[firstTerm%input.length];
			//System.out.println(""+digit+"*"+fft+" = "+(digit*fft));
			this.combinedDigit +=fft*digit;
			
			if (firstTerm==0 || firstTerm<this.digit) {
				this.digit--;
				output[this.digit] = Math.abs(combinedDigit) % 10L;
				combinedDigit = 0L;
				firstTerm = input.length;
			}
			
			if (termsDebug!="") termsDebug += " + ";
			termsDebug += digit+"*"+fft;
			return true;
		}
		
		public void computeDigit() {
			int lastDigit = this.digit;
			computeTerm();
			while(this.digit==lastDigit) { 
				computeTerm();
			}
		}
	}
	
	
	public static void runPartTwo() {
		String inputString = "03036732577212944063491565474664";
		long[] basePattern = {0L, 1L, 0L, -1L};
		long[] input = decode(inputString);
		input = stretch(input, TEN_THOUSAND);
		
		int offset = Integer.valueOf(inputString.substring(0,7));
		System.out.println("Offset: "+offset);
		
		
		FFTStep step = new FFTStep(basePattern, input);
		while(step.digit>offset) {
			step.computeDigit();
			//System.out.println(step.termsDebug);
		}
		System.out.println(Arrays.toString(step.output));
		
	}
}
