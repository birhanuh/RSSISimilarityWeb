
package fi.metropolia.spagu.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import net.obsearch.ambient.bdb.AmbientBDBJe;
import net.obsearch.example.vectors.VectorsDemo;
import net.obsearch.exception.IllegalIdException;
import net.obsearch.exception.NotFrozenException;
import net.obsearch.exception.OBException;
import net.obsearch.exception.OBStorageException;
import net.obsearch.exception.PivotsUnavailableException;
import net.obsearch.index.ghs.impl.Sketch64Int;
import net.obsearch.pivots.AcceptAll;
import net.obsearch.pivots.rf04.RF04PivotSelectorInt;
import net.obsearch.result.OBPriorityQueueInt;
import net.obsearch.result.OBResultInt;

import net.obsearch.index.bucket.impl.*;
import net.obsearch.index.IndexInt;
import net.obsearch.index.ghs.impl.*;
import net.obsearch.exception.*;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import draft.ManhattanIntVector;



public class WlanSimilarity {
	
//	static Logger logger = Logger.getLogger(WlanSimilarity.class.getName());
	
	static ResponseParser responseParser = new ResponseParser();
	
	//random generator
	final static Random r = new Random();
	
	/**
	 * Query count.
	 */
	final static int QUERY_SIZE = 5; // size of result set for a query? TODO
		
	/**
	 * Dimension of the vectors.
	 */
	public static int VEC_SIZE = 115; //FIXME HARDCODED
	
	final static File INDEX_FOLDER = new File("/usr/local/WLANSpagu/bench/spagu/sim_db" + File.separator +"wlanDB");
	
	
	/**
	 * LOAD OBSEARCH PARAMS
	 * @throws IOException
	 */
	public static void init() throws IOException {

		File f = new File("obsearch.properties");
		//debug
		if(!f.exists()) {
			System.err.println("Init failed, file does not exist "+f.getAbsolutePath());
			System.exit(0);
		}
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(f));
		} catch (IOException e) {
			System.err.println("error loading properties");
		}
		
	}
	/**
	 * FOR INDEXING VECTORS
	 * @return
	 */
	public static ManhattanIntVector generateIntVector() {

		int[] data = new int[VEC_SIZE];
		int i = 0;

		while (i < data.length) {
			data[i] = r.nextInt(1000);
			i++;
		}

		return new ManhattanIntVector(data);
	}
	
	private static SignalParser signalParser;
	private static Sketch64Int<ManhattanIntVector> index;
	private AmbientBDBJe<ManhattanIntVector, Sketch64Int<ManhattanIntVector>> a;
	private HashMap<ArrayList<Integer>, String> samples; // FIXME: do not expect that indexing is done every time from the files, remove this and invent better solution...
	private static HashMap<String, String> samplesStr;
	private static boolean accesspointsLoaded = false;
	
	
	public WlanSimilarity() {
		
		try {
			// Create the pivot selection strategy
			RF04PivotSelectorInt<ManhattanIntVector> sel = new RF04PivotSelectorInt<ManhattanIntVector>(new AcceptAll<ManhattanIntVector>());
			sel.setDataSample(100);

			//TYPE OF THE INDEX, ABSTRACT
			index = new Sketch64Int<ManhattanIntVector>(ManhattanIntVector.class, sel, 64);


			// error expected 
			index.setExpectedError(VEC_SIZE);
			// small if you are planning to insert a lot of objects!
			index.setSampleSize(100); 
			// Probability of returning an error within 1.40 times the real distance
			// (measured in standard deviations) (3 means a prob. of 0.99)
			index.setKAlpha(3);

			// select the ks that the user will call. 
			// This example will only be called with k=1
			index.setMaxK(new int[]{1,3,5,10});	  
			// little optimization that can help if your objects are of the same size.
			index.setFixedRecord(true);
			index.setFixedRecord(VEC_SIZE);
			// Create the ambient that will store the index's data. (NOTE: folder name is hardcoded)

			//CONCRETE, PHYSICAL INDEX
			a = new AmbientBDBJe<ManhattanIntVector, Sketch64Int<ManhattanIntVector>>(index, INDEX_FOLDER);
			
			signalParser = new SignalParser();
			
			if(signalParser.loadAccesspoints()) {
				accesspointsLoaded = true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OBStorageException e) {
			e.printStackTrace();
		} catch (NotFrozenException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (OBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Lets keep this separate, we call this when we need to re-index the data
	 */
	public void doIndex() {
		
		try {
			signalParser.parseAccessPoints();
//			HashMap<ArrayList<Integer>,String> samples = s.parseSignals(); // FIXME
			samples = signalParser.parseSignals();
			samplesStr = this.generateStrSamples(samples);
			
			WlanSimilarity.VEC_SIZE = samples.size();
			
			
			// Delete the directory of the index just in case.
//			Directory.deleteDirectory(INDEX_FOLDER);
											
			// make the bit set as short so that m objects can fit in the buckets.
			// create an index.
			// Choose pivot sizes that are multiples of 64 to optimize the space
		
	    	int DB_SIZE = samples.size();

			// Add some random objects to the index:	
	//		logger.info("Adding " + DB_SIZE + " objects...");

			for(ArrayList<Integer> sample: samples.keySet()) { // for each vector

				int [] temp = new int[sample.size()];
				int pointer = 0;
				for(Integer val : sample) {
					temp[pointer] = val;
					pointer++;
				}
//				System.out.println("LENGTH: " +temp.length);

				ManhattanIntVector item = new ManhattanIntVector(temp, samples.get(sample));
				index.insert(item);
			}
			
			// prepare the index
	//		logger.info("Preparing the index...");
			a.freeze();
	//		logger.info("Index stats: " + index.getStats());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OBStorageException e) {
			e.printStackTrace();
		} catch (OBException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (PivotsUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private HashMap<String, String> generateStrSamples(
			HashMap<ArrayList<Integer>, String> samples2) {
		
		HashMap<String,String> result = new HashMap<String,String>();
		for(ArrayList<Integer> i: samples2.keySet()) {
			System.out.println(i == null);
			result.put(i.toString(), samples2.get(i));
		}
		return result;
	}
	
	public List<String> getRoom(File str) throws FileNotFoundException, IllegalIdException, IllegalAccessException, InstantiationException, OBException {
		ArrayList<Integer> signal = signalParser.parseSignal(str);
		int [] temp = new int[signal.size()];
		
		int pointer = 0;
		for(Integer val: signal) { // for each vector
			temp[pointer] = val;
			pointer++;
		}
		System.out.println("QUERY VECTOR:\n" +signal);
		return getSimilar(temp);
		
	}
	
	public List<String> getRooms(String str) throws FileNotFoundException, IllegalIdException, IllegalAccessException, InstantiationException, OBException {
		
		if(!accesspointsLoaded) {
			System.err.println("ACCESS POINTS NOT LOADED!");
			return null;
		}
		
		ArrayList<Integer> signal = signalParser.parseSignal(str);
		
		int [] temp = new int[signal.size()];
		
		int pointer = 0;
		for(Integer val: signal) { // for each vector
			temp[pointer] = val;
			pointer++;
		}
		
		System.out.println("QUERY VECTOR :" + signal);
		System.out.println("QUERY VECTOR :" + temp.length + "\n");
	
        List<String> roomnamesWithDistances = getSimilar(temp);
        
		
		return roomnamesWithDistances;
		
	}
	
	
	/**
	 * Input: HashMap<String, Integer> --> where String is AP, integer is signal strength
	 * 
	 * FIXME: input param is quite raw
	 * @return
	 * @throws OBException 
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 * @throws IllegalIdException 
	 */
	
	public static List<String> getSimilar(int [] vector) throws IllegalIdException, IllegalAccessException, InstantiationException, OBException {
		List<String> rooms = new ArrayList<String>();
//		System.out.println("QUERY WITH VECTOR "+vector);
		//query range: from how wide area the items will be retrieved
		int range = 10000;
		// now we can match some objects!		
	//	logger.info("Querying the index...");
		
		final int RESULTSETSIZE = 1;
		
		RF04PivotSelectorInt<ManhattanIntVector> sel = new RF04PivotSelectorInt<ManhattanIntVector>(new AcceptAll<ManhattanIntVector>());
		sel.setDataSample(100);

		index.resetStats(); // reset the stats counter
		
		long start = System.currentTimeMillis();
		List<OBPriorityQueueInt<ManhattanIntVector>> queryResults = new ArrayList<OBPriorityQueueInt<ManhattanIntVector>>(QUERY_SIZE);
		List<ManhattanIntVector> resultset = new ArrayList<ManhattanIntVector>(RESULTSETSIZE);
		
//		String results;
//		System.out.println("DATASET: "+a.getIndex().getAllObjects().toString());
		
		int i = 0;
		while(i < RESULTSETSIZE){ //HERE WE NOW LOOP THROUGH 10 BEST MATCHES, ONLY FIRST ONE IS RETRIEVED
			ManhattanIntVector q = new ManhattanIntVector(vector);
//			System.out.println("VSIZE: " + q.vector.length);
			
//			results = "";			
			// query the index with k=1			
			OBPriorityQueueInt<ManhattanIntVector> queue = new OBPriorityQueueInt<ManhattanIntVector>(10); // WAS: 5			
			// perform a query with a large range and k = 1 
			index.searchOB(q, range , queue);
			queryResults.add(queue);
			for(OBResultInt<ManhattanIntVector> f : queue.getSortedElements()){
				// check that the id makes sense
				assert index.getObject(f.getId()).equals(f.getObject());
				//logger.info("Distance: " + f.getId() + " " + f.getDistance());
				assert f.getDistance() <= range;
//				System.out.println("Object in resultset, id: '" + f.getId() +"', value: '"+f.getObject().toString());
//				results += f.getObject().toString()+", ";
				resultset.add(f.getObject());
			}
//			System.out.println("Query: "+q.toString()+", results: "+results);
			
			i++;
		}
		// print the results of the set of queries. 
		long elapsed = System.currentTimeMillis() - start;
		//logger.info("Time per query: " + elapsed / QUERY_SIZE + " millisec.");
		
		//logger.info("Stats follow: (total distances / pivot vectors computed during the experiment)");
		System.out.println("SIZE: " + resultset.size());
		//RETURN FIRST RESULT
		System.out.println("FIRST VECTOR: "+ resultset.get(0));
		System.out.println("SECOND VECTOR: "+ resultset.get(1) + "\n");
		System.out.println("FIRST ROOM: " + samplesStr.get(resultset.get(0).toString()) + "\n");	// get(0) value in the first index of the arraylist	
		
		//RETURN THE FIRST FIVE RESULT
	
//		rooms.add(resultset.get(0).toString());
//		rooms.add(resultset.get(1).toString());
//		rooms.add(resultset.get(2).toString());
//		rooms.add(resultset.get(3).toString());
		
		// NAMES LOOPED EXAMPLE: ten best 
		for(ManhattanIntVector res: resultset) { 
//			System.out.println(count+": "+samplesStr.get(res.toString()));
//			count++;
			rooms.add(samplesStr.get(res.toString())+", distance: "+ WlanSimilarity.getDistance(vector, res.getVector())); // this will return the room names (values of samplesStr) 
		} 
		
		
//		System.out.println("NAME?:" + resultset.get(0).name);
//		this.getNameOfTheResult(resultset.get(0));
		
		return rooms;	// get(0) value in the first index of the arraylist	
	}
	
	public void getNameOfTheResult(ManhattanIntVector manhattanIntVector) {
		for(ArrayList<Integer> key : samples.keySet()) {
			boolean found = true;
			for(int i = 0; i < manhattanIntVector.vector.length; i++) {
				if(manhattanIntVector.vector[i] != key.get(i)) {
					found = false;
					break;
				}
			}
			if(found) {
				System.out.println("ROOM is: "+samples.get(key));
				break;
			}
		}
	}
	
	
//	private ArrayList<Integer> generateEmptySample() {
//		ArrayList<Integer> sample = new ArrayList<Integer>(.size()); // arraylist of size of the #APs
//		int i = 0;
//		while(accesspoints.keySet().size() > i) {
//			sample.add(+1);
//			i++;
//		}
//		return sample;
//	}
	
	public static double getDistance(int[] x, int[] y) {
		double distSquare = 0.0;
		
		for (int i = 0; i < x.length; i++) {
			
			distSquare +=  Math.pow((x[i] - y[i]), 2);
	
		}
		
		return  Math.sqrt(distSquare);
		
	}
	/**
	 * arraylist size may vary, e.g. 3, 5 or 10
	 * @param range defines the range from within we will include the values, e.g. value 10 is indicating that we take +-10 values range compared to top 1 result
	 * range 10 for top 1 value 126 means range between 116-136
	 * @param samples
	 */
	public void calculateAverage(ArrayList<Sample> samples, int range) {
		
	}
	
	/**
	public static void main(String args[]) throws FileNotFoundException, OBStorageException, NotFrozenException, IllegalAccessException, InstantiationException, OBException, IOException, PivotsUnavailableException {
		
		String test = "00:1b:d5:c0:90/<c1:metropolia-student>,<c3:metropolia-guest>,<c4:eduroam>/2472/-75/2011.11.16 AD at 17:02:27 +0200 \n"
				 + "02:2c:bc:d9:04/<d5:HP4FF46C>/2457/-90/2011.11.03 AD at 17:12:13 +0200 \n"
				 + "02:29:39:fe:bd/<fb:HPEE8293>/2457/-85/2011.11.16 AD at 17:02:27 +0200 \n"
				 + "00:1b:d5:c0:96/<11:metropolia-student>,<13:metropolia-guest>,<14:eduroam>/2412/-75/2011.11.16 AD at 17:02:27 +0200 \n"
				 + "00:27:0d:8b:7e/<b3:metropolia-guest>/2472/-88/2011.11.16 AD at 17:02:27 +0200 \n"
				 + "00:1c:57:41:de/<23:metropolia-guest>,<24:eduroam>/2412/-84/2011.11.16 AD at 17:02:27 +0200 \n";
		
		Logger.getLogger("net.obsearch.*").setLevel(Level.OFF);
		
		init();
				
		WlanSimilarity sim = new WlanSimilarity();
		sim.doIndex();
		File f = new File("test.file");
		System.out.println(f.getAbsolutePath());
		System.out.println(test);
		
//		System.out.println(sim.getRoom(new File("test.file")));
		
//		System.out.println(sim.getRoom(f));

		int i = 1;
		for(String s: getRooms(test)) {
			System.out.println(i+": "+ s);
			i++;
		}	
		
		List<String> mostReapetedNames = responseParser.getMostReapetedNames(getRooms(test));
		
		int j = 1;
		for(String name : mostReapetedNames) {
			System.out.println(j + ":" + name);
			i++;
		}
					
	}		*/
	

}

	


