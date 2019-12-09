package blue.endless.advent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Day8 {
	
	public static int[] decodeImageData(String s) {
		int[] result = new int[s.length()];
		for(int i=0; i<s.length(); i++) {
			result[i] = Integer.parseInt(""+s.charAt(i));
		}
		return result;
	}
	
	public static int digitCount(SpaceImage im, int layer, int digit) {
		int zeroes = 0;
		int layerSize = im.layerSize();
		int baseAddr = layerSize*layer;
		for(int i=0; i<layerSize; i++) {
			if (im.data[baseAddr+i]==digit) zeroes++;
		}
		return zeroes;
	}
	
	/** Finds the layer index with the most zeroes. If there are no zeroes anywhere, returns -1 */
	public static int mostZeroes(SpaceImage im) {
		int bestZeroCount = -1;
		int bestZeroLayer = -1;
		for(int i=0; i<im.layers; i++) {
			int zeroes = digitCount(im, i, 0);
			if (zeroes>bestZeroCount) {
				bestZeroCount = zeroes;
				bestZeroLayer = i;
			}
		}
		
		return bestZeroLayer;
	}
	
	public static int fewestZeroes(SpaceImage im) {
		int bestZeroCount = Integer.MAX_VALUE;
		int bestZeroLayer = -1;
		for(int i=0; i<im.layers; i++) {
			int zeroes = digitCount(im, i, 0);
			if (zeroes<bestZeroCount) {
				bestZeroCount = zeroes;
				bestZeroLayer = i;
			}
		}
		
		return bestZeroLayer;
	}
	
	public static void runFromFile() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day8.dat"));
			SpaceImage im = new SpaceImage(file.get(0).trim(), 25, 6);
			int i = fewestZeroes(im);
			
			if (i==-1) throw new IllegalArgumentException("There are no zeroes in the file.");
			
			int j = digitCount(im, i, 1);
			int k = digitCount(im, i, 2);
			System.out.println("Layer "+i+"(zero-indexed) out of "+im.layers+": "+j+"*"+k+" = "+(j*k));
			
			for(String s : im.getLayerImage(i)) {
				System.out.println("|"+s+"|");
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void decodeFromFile() {
		try {
			List<String> file = Files.readAllLines(Paths.get("day8.dat"));
			SpaceImage im = new SpaceImage(file.get(0).trim(), 25, 6);
			
			
			for(String s : im.getDecodedImage()) {
				System.out.println("|"+s+"|");
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static class SpaceImage {
		public int[] data;
		public int width;
		public int height;
		public int layers;
		
		public SpaceImage(String im, int width, int height) {
			this(decodeImageData(im), width, height);
			
		}
		
		public SpaceImage(int[] im, int width, int height) {
			this.data = im;
			this.width = width;
			this.height = height;
			this.layers = data.length/(width*height);
		}
		
		private int addr(int x, int y, int plane) {
			return plane*width*height + y*width + x;
		}
		
		public int getPixel(int x, int y, int plane) {
			int addr = addr(x, y, plane);
			if (addr<0 || addr>=data.length) return 0;
			return data[addr];
		}
		
		public void setPixel(int x, int y, int plane, int pixel) {
			int addr = addr(x, y, plane);
			if (addr<0 || addr>=data.length) return;
			data[addr] = pixel;
		}
		
		public int layerSize() {
			return width*height;
		}
		
		public String[] getLayerImage(int layer) {
			String[] result = new String[height];
			for(int y=0; y<height; y++) {
				String row = "";
				for(int x=0; x<width; x++) {
					int addr = addr(x,y,layer);
					if (addr>=0 && addr<data.length) {
						row+= data[addr];
					}
				}
				result[y] = row;
			}
			return result;
		}
		
		public void printLayerImage(int layer) {
			for(String s : getLayerImage(layer)) {
				System.out.println("|"+s+"|");
			}
		}
		
		public String decodePixel(int x, int y) {
			for(int i=0; i<layers; i++) {
				int layerPixel = getPixel(x, y, i);
				if (layerPixel==2) {
					continue;
				} else if (layerPixel==0) {
					return " ";
				} else if (layerPixel==1) {
					return "#";
				}
			}
			return "?";
		}
		
		public String[] getDecodedImage() {
			String[] result = new String[height];
			for(int y=0; y<height; y++) {
				String row = "";
				for(int x=0; x<width; x++) {
					String decoded = decodePixel(x, y);
					row+=decoded;
				}
				result[y] = row;
			}
			return result;
		}
	}
}
