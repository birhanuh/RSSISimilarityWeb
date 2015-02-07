package ext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {
		
	
	public static void writeFile(String file, String text) {
		try{
			// Create file 
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(text);
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public static void writeFile(String file, String text, String encoding) {
		try{
			// Create file
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), encoding));
			out.write(text);
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
		
	public static String readFile(File file) throws FileNotFoundException {
		StringBuffer s = new StringBuffer();
		Scanner scanner = new Scanner(file,"ISO-8859-1");
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			s.append(line+"\n");
		}
			
		return s.toString();         
	}
	
	public static String readFile(File file, String enc) throws FileNotFoundException {
		StringBuffer s = new StringBuffer();
		Scanner scanner = new Scanner(file,enc);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			s.append(line+"\n");
		}
			
		return s.toString();         
	}

	
	public static void doSerialize(Serializable obj, String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(obj);
	    oos.close();
	}
	
	public static Object readSerialized(String filename) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(filename);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Object obj = ois.readObject();
		ois.close();
	    
		return obj;
	}
	
	public static void orderRowsInFile(String inputFile, String outputFile) throws FileNotFoundException {
		String content = Tools.readFile(new File(inputFile),"ISO-8859-1");

		Scanner s = new Scanner(content);
		TreeSet<String> temp = new TreeSet<String>();
		while(s.hasNextLine()) {
			temp.add(s.nextLine());
		}
		StringBuffer result = new StringBuffer();
		
		for(String line: temp) {
			result.append(line+"\n");	
		}
		
		Tools.writeFile(outputFile, result.toString());
	}

	
	
}