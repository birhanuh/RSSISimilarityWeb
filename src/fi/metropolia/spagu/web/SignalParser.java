package fi.metropolia.spagu.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;

import org.apache.log4j.Logger;

import draft.IntegerSimilarityTest;
import ext.Tools;

public class SignalParser {
	public String signalFolder = "/usr/local/WLANSpagu/bench/spagu/spagu_signals/";
	/**
	 * mapping from AP id to relative position in the vector. e.g. 00:15:2c:4a:37 --> 5
	 */
	private LinkedHashMap<String, Integer> accesspoints = new LinkedHashMap<String, Integer>();
	
	private int pointer;
	
	public SignalParser() {
		pointer = 0;
		
	}
	
	private File[] parseFiles() {
		File dir = new File(signalFolder);
		return dir.listFiles();
	}	
	
	/**
	 * this method parses through the sample data and generates the mapping from AP to integer pointer for vector position
	 * 
	 * 	
	 * @return
	 * @throws IOException 
	 */
	public LinkedHashMap<String, Integer> parseAccessPoints() throws IOException {
		
		for(File f: this.parseFiles()) {
			Scanner s = new Scanner(f);
//			System.out.println("FILE: " + f.getName());
			while(s.hasNextLine()) {
				String line = s.nextLine();
				if(line.startsWith(" WiFi Status:") || line.startsWith("WiFi Status:") || line.trim().length() == 0) {
					continue;
				} else {
					this.parseLineForAP(line); // headers
				}
				
			}
		}
//		System.out.println(accesspoints);

		Tools.doSerialize(accesspoints, "/usr/local/WLANSpagu/accesspoints.ser");
		
		return accesspoints;
	}
	
	@SuppressWarnings("unchecked")
	public boolean loadAccesspoints() {
		try {
			accesspoints = (LinkedHashMap<String, Integer>) Tools.readSerialized("/usr/local/WLANSpagu/accesspoints.ser");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;		
	}
	
	public void parseLineForAP(String line) {
//		System.out.println("--> LINE: "+line);
		this.parseAP(line);
	}
	
	public void parseAP(String line) {
		
		String ap = this.getAP(line);
		if(!accesspoints.containsKey(ap)) {
			accesspoints.put(ap, pointer);
			pointer++;
		} else {
			// do nothing, already there
		}
	}
	
	public String getAP(String line) {
		Scanner s = new Scanner(line);
		s.useDelimiter("/");
		
		return s.next().trim();
	}
	
	private Integer getSignal(String line) {
//		System.out.println("LINE: "+line);
		Scanner s = new Scanner(line);
		s.useDelimiter("/");
		s.next();s.next();s.next();
		String signal = s.next().trim();
//		System.out.println(signal);
		return Integer.parseInt(signal);
	}
	
	/**
	 * mapping from signal strength to room name
	 * @throws FileNotFoundException 
	 */
	public HashMap<ArrayList<Integer>, String> parseSignals() throws FileNotFoundException {
		
		HashMap<ArrayList<Integer>, String> result = new HashMap<ArrayList<Integer>, String>();
		
		for(File f: this.parseFiles()) {
				
			String filename = f.getName();
			String room = this.parseRoom2(filename);
			
//			System.out.println("ROOM: "+room);
			
			ArrayList<Integer> sample = null;
			
			sample = this.parseSignal(f);
			
			result.put(sample, room);
		}
//		System.out.println(result.toString());
//		System.exit(0);
		return result;
		
	}
	
	private ArrayList<Integer> generateEmptySample() {
		ArrayList<Integer> sample = new ArrayList<Integer>(accesspoints.keySet().size()); // arraylist of size of the #APs
		int i = 0;
		while(accesspoints.keySet().size() > i) {
			sample.add(-100);
			i++;
		}
		return sample;
	}
	
	public ArrayList<Integer> parseSignal(File f) throws FileNotFoundException {
		
		Scanner s = new Scanner(f);		
		
		ArrayList<Integer> sample = this.generateEmptySample(); // fill with empty values
		
		while(s.hasNextLine()) {
			String line = s.nextLine();
			if(line.startsWith(" WiFi Status:") || line.startsWith("WiFi Status:")|| line.trim().length() == 0) { // skip unnecessary lines
				continue;
			} else {
				String ap = this.getAP(line);
				int signal = this.getSignal(line);
//				System.out.println(accesspoints);
				Integer value = accesspoints.get(ap);
				if(value == null) {
					System.err.println("No value found for AP "+ap);
//					System.exit(0);
				}
				
				sample.set(value, signal); 
			
			}
			
		}
//		System.out.println("SAMPLE: "+sample.size());
		return sample;
	}

	public ArrayList<Integer> parseSignal(String str) throws FileNotFoundException {
		
		Scanner s = new Scanner(str);		
//		while(s.hasNextLine()) {
//			System.out.println("LINE:" +s.nextLine());
//		}
//		
//		System.exit(0);
		ArrayList<Integer> sample = this.generateEmptySample(); // fill with empty values
		
		while(s.hasNextLine()) {
			String line = s.nextLine();
			if(line.startsWith(" WiFi Status:") || line.startsWith("WiFi Status:") || line.trim().length() == 0) { // skip unnecessary lines
				continue;
			} else {
				String ap = this.getAP(line);
				int signal = this.getSignal(line);
				
//				System.out.println("AP: '"+ap+"', signal: "+signal);

				if(accesspoints.containsKey(ap)) { // SKIPPING APs that are unknown!
					sample.set(accesspoints.get(ap), signal);
				} else {
					System.out.println("NOTICE: UNKNOWN AP FOUND: "+ ap);
				}
			
			}
			
		}
		System.out.println("SAMPLE: "+sample.size());
		return sample;
	}
	
	private String parseRoom(String filename) {
		int suffix = filename.indexOf('_');
		String room = "";
		
		if(suffix != -1) {
			room = filename.substring(0, suffix);
		} else {
			room = filename.substring(0, filename.length()-4); //remove .txt
		}

		return room;
	}
	
	private String parseRoom2(String filename) {
		
		String room = "";
		room = filename.substring(0, filename.length()-4); //remove .txt
		
		return room;
	}

	public static void main(String[] args) throws IOException {
		
		SignalParser s = new SignalParser();
//		s.parseAccessPoints();
		
		
		HashMap<ArrayList<Integer>, String> result = s.parseSignals();
		System.out.println(result);
		System.out.println(result.size());
		
	}
	
}
