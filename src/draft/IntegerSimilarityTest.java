package draft;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import net.obsearch.ambient.bdb.AmbientBDBJe;
import net.obsearch.example.vectors.VectorsDemo;
import net.obsearch.exception.NotFrozenException;
import net.obsearch.exception.OBException;
import net.obsearch.exception.OBStorageException;
import net.obsearch.exception.PivotsUnavailableException;
import net.obsearch.index.ghs.impl.Sketch64Int;
import net.obsearch.index.utils.Directory;
import net.obsearch.pivots.AcceptAll;
import net.obsearch.pivots.rf04.RF04PivotSelectorInt;
import net.obsearch.result.OBPriorityQueueInt;
import net.obsearch.result.OBResultInt;


public class IntegerSimilarityTest {
	
	/**
	 * Logging provided by Java
	 */
	static Logger logger = Logger.getLogger(IntegerSimilarityTest.class.getName());
	
	//random generator
	final static Random r = new Random();
	
	/**
	 * Query count.
	 */
	final static int QUERY_SIZE = 5; // size of result set for a query? TODO
	
	/**
	 * Database size.
	 */
	final static int DB_SIZE = 15;
	
	/**
	 * Dimension of the vectors.
	 */
	final static int VEC_SIZE = 1;
	
	final static File INDEX_FOLDER = new File("." + File.separator +"dummyIndex");
	
	public static void init() throws IOException {

		InputStream is = VectorsDemo.class.getResourceAsStream("obsearch.properties");
		Properties props = new Properties();
		props.load(is);
		
	}
	
	public static ManhattanIntVector generateIntVector() {

		int[] data = new int[VEC_SIZE];
		int i = 0;

		while (i < data.length) {
			data[i] = r.nextInt(1000);
			i++;
		}

		return new ManhattanIntVector(data);
	}
	
	public static void main(String args[]) throws FileNotFoundException, OBStorageException, NotFrozenException, IllegalAccessException, InstantiationException, OBException, IOException, PivotsUnavailableException {
			
		init();
			
		// Delete the directory of the index just in case.
		Directory.deleteDirectory(INDEX_FOLDER);
			
		// Create the pivot selection strategy
		RF04PivotSelectorInt<ManhattanIntVector> sel = new RF04PivotSelectorInt<ManhattanIntVector>(new AcceptAll<ManhattanIntVector>());
		sel.setDataSample(100);
							
		// make the bit set as short so that m objects can fit in the buckets.
		// create an index.
		// Choose pivot sizes that are multiples of 64 to optimize the space
		
		//TYPE OF THE INDEX, ABSTRACT
		Sketch64Int<ManhattanIntVector> index = new Sketch64Int<ManhattanIntVector>(ManhattanIntVector.class, sel, 64);
	    
	    // error expected 
	    index.setExpectedError(VEC_SIZE);
	    // small if you are planning to insert a lot of objects!
	    index.setSampleSize(100); 
	    // Probability of returning an error within 1.40 times the real distance
	    // (measured in standard deviations) (3 means a prob. of 0.99)
	    index.setKAlpha(3);
	    
	    // select the ks that the user will call. 
	    // This example will only be called with k=1
	    index.setMaxK(new int[]{1,5,10});	  
	    // little optimization that can help if your objects are of the same size.
	    index.setFixedRecord(true);
    	index.setFixedRecord(VEC_SIZE);
		// Create the ambient that will store the index's data. (NOTE: folder name is hardcoded)
    	
    	//CONCRETE, PHYSICAL INDEX
    	AmbientBDBJe<ManhattanIntVector, Sketch64Int<ManhattanIntVector>> a = new AmbientBDBJe<ManhattanIntVector, Sketch64Int<ManhattanIntVector>>(index, INDEX_FOLDER);
   	
		// Add some random objects to the index:	
		logger.info("Adding " + DB_SIZE + " objects...");
		int i = 0;		
		while(i < DB_SIZE){
			ManhattanIntVector randomItem;
			if(i < 2) {
				int[] fixed = new int[1];
				fixed[0] = 5;
				randomItem = new ManhattanIntVector(fixed);
			} else {
				randomItem = generateIntVector();
			}
//			logger.info("Inserted "+i+": "+randomItem.toString());
			index.insert(randomItem);
			/* progress information about the loading
			if(i % 100000 == 0){
				logger.info("Loading: " + i);
			}
			*/
			i++;
		}
		
		// prepare the index
		logger.info("Preparing the index...");
		a.freeze();
		logger.info("Index stats: " + index.getStats());
		
		//query range: from how wide area the items will be retrieved
		int range = 10000;
		// now we can match some objects!		
		logger.info("Querying the index...");
		i = 0;
		index.resetStats(); // reset the stats counter
		long start = System.currentTimeMillis();
		List<OBPriorityQueueInt<ManhattanIntVector>> queryResults = new ArrayList<OBPriorityQueueInt<ManhattanIntVector>>(QUERY_SIZE);
		List<ManhattanIntVector> queries = new ArrayList<ManhattanIntVector>(QUERY_SIZE);
		
		String results;
		System.out.println("DATASET: "+a.getIndex().getAllObjects().toString());
		while(i < QUERY_SIZE){
			ManhattanIntVector q = generateIntVector();
			
			results = "";			
			// query the index with k=1			
			OBPriorityQueueInt<ManhattanIntVector> queue = new OBPriorityQueueInt<ManhattanIntVector>(5);			
			// perform a query with a large range and k = 1 
			index.searchOB(q, range , queue);
			queryResults.add(queue);
			for(OBResultInt<ManhattanIntVector> f : queue.getSortedElements()){
				// check that the id makes sense
				assert index.getObject(f.getId()).equals(f.getObject());
				//logger.info("Distance: " + f.getId() + " " + f.getDistance());
				assert f.getDistance() <= range;
				System.out.println("Object in resultset, id: '" + f.getId() +"', value: '"+f.getObject().toString());
				results += f.getObject().toString()+", ";
			}
			System.out.println("Query: "+q.toString()+", results: "+results);
			queries.add(q);
			
			i++;
		}
		// print the results of the set of queries. 
		long elapsed = System.currentTimeMillis() - start;
		//logger.info("Time per query: " + elapsed / QUERY_SIZE + " millisec.");
		
		//logger.info("Stats follow: (total distances / pivot vectors computed during the experiment)");
		/*
		logger.info(index.getStats().toString());

		// now we validate the result of the search
		//logger.info("Doing Error validation");
		StaticBin1D ep = new StaticBin1D();
		

		Iterator<OBPriorityQueueInt<ManhattanIntVector>> it1 = queryResults.iterator();
		Iterator<ManhattanIntVector> it2 = queries.iterator();
		StaticBin1D seqTime = new StaticBin1D();
		i = 0;
		while(it1.hasNext()){
			OBPriorityQueueInt<ManhattanIntVector> qu = it1.next();
			ManhattanIntVector q = it2.next();
			long time = System.currentTimeMillis();
			int[] sortedList = index.fullMatchLite(q, false);
			long el = System.currentTimeMillis() - time;
			seqTime.add(el);
			//logger.info("Elapsed: " + el + " "  + i);
			OBQueryInt<ManhattanIntVector> queryObj = new OBQueryInt<ManhattanIntVector>(q, range, qu, null);
			ep.add(queryObj.approx(sortedList));
			i++;
		}
		
		logger.info(ep.toString());
		logger.info("Time per seq query: ");
		logger.info(seqTime.toString());
		*/
	}

}

	

