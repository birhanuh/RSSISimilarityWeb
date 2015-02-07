 package net.obsearch.index.ghs.impl;
   
   import java.io.IOException;
   import java.nio.ByteBuffer;
   import java.util.ArrayList;
   import java.util.Arrays;
   import java.util.Collections;
   import java.util.Iterator;
   import java.util.List;
  import java.util.logging.Logger;
  
  import net.obsearch.index.ghs.CBitVector;
  import net.obsearch.index.CommonsFloat;
  import net.obsearch.AbstractOBResult;
  import net.obsearch.asserts.OBAsserts;
  import net.obsearch.constants.ByteConstants;
  import net.obsearch.exception.IllegalIdException;
  import net.obsearch.exception.NotFrozenException;
  import net.obsearch.exception.OBException;
  import net.obsearch.exception.OBStorageException;
  import net.obsearch.exception.OutOfRangeException;
  import net.obsearch.filter.Filter;
  import net.obsearch.filter.FilterNonEquals;
  import net.obsearch.index.IndexFloat;
  import net.obsearch.index.bucket.impl.BucketObjectFloat;
  import net.obsearch.index.bucket.sleek.SleekBucketFloat;
  import net.obsearch.index.ghs.AbstractSketch64;
  import net.obsearch.ob.OBFloat;
  import net.obsearch.pivots.IncrementalPairPivotSelector;
  import net.obsearch.pivots.IncrementalPivotSelector;
  import net.obsearch.query.AbstractOBQuery;
  import net.obsearch.query.OBQueryFloat;
  import net.obsearch.result.OBPriorityQueueFloat;
  import net.obsearch.result.OBResultInvertedByte;
  import net.obsearch.result.OBResultInvertedFloat;
  import net.obsearch.result.OBResultFloat;
  import net.obsearch.index.ghs.SketchProjection;
  
  public final class Sketch64Float<O extends OBFloat> extends AbstractSketch64<O, BucketObjectFloat<O>, OBQueryFloat<O>, SleekBucketFloat<O>>
  implements IndexFloat<O> {
  	
  	private boolean DEBUG = true;
  	
  	private static final transient Logger logger = Logger
  	.getLogger(Sketch64Float.class.getName());
  	
  	/**
  	 * Create a new Sketch64Float with m bytes. 
  	 * @param type Type of object that will be stored
  	 * @param pivotSelector Pivot selection strategy to be employed.
  	 * @param m The number of bits
  	 * @param bucketPivotCount Number of pivots per bucket
  	 * @throws OBStorageException
  	 * @throws OBException
  	 * @throws IOException
  	 */
  	public Sketch64Float(Class<O> type,
  			IncrementalPairPivotSelector<O> pivotSelector, int m
  			)												
  			throws OBStorageException, OBException, IOException {
  		
  		super(type, pivotSelector,  m, 0);
  		
  	}
  	
  	@Override
  	public BucketObjectFloat<O> getBucket(O object) throws OBException,
  			InstantiationException, IllegalAccessException {
  		BucketObjectFloat<O> res = new BucketObjectFloat<O>(null, -1L, object);
  		return res;
  	}
  
  	
  	/**
  	 * Compute the sketch for the given object.
  	 */
  	@Override
  	public SketchProjection getProjection(BucketObjectFloat<O> bucket) throws OBException {
  			//OBAsserts.chkAssert(m <= Byte.MAX_VALUE, "Cannot address more than Byte.MAX_VALUE");
  		int i = 0;
  		CBitVector res = new CBitVector(m);
  		double[] lowerBounds = new double[m];
  		while(i < m){
  				float distanceA = pivotGrid[i][0].distance(bucket.getObject());
  				float distanceB = pivotGrid[i][1].distance(bucket.getObject());
  			if(distanceA > distanceB ){
  					res.set(i);
  				distortionStats[i][1]++;
  			}else{
  				distortionStats[i][0]++;
  			}
  			// update the lower bounds:
  			lowerBounds[i] = Math.max(((double)Math.abs(distanceA - distanceB)) / 2, 0);
  			i++;
  		}
  
  		// calculate distances that are farther to the hyperplane
  		double[] lowerBoundsSorted = new double[m];
  		System.arraycopy(lowerBounds,0,lowerBoundsSorted,0,m);
 		Arrays.sort(lowerBoundsSorted);
 		byte[] ordering = new byte[m];
 		i = 0;
 		while(i < m){
 				
 				ordering[i] = (byte)Arrays.binarySearch(lowerBoundsSorted, lowerBounds[i]);
 				i++;
 		}
 		
 		SketchProjection result = new SketchProjection(ordering, res, -1, lowerBounds);
 		return result;
 	}
 
 	
 	@Override
 	protected SleekBucketFloat<O> instantiateBucketContainer(
 			byte[] data, byte[] address) throws InstantiationException, IllegalAccessException, OBException {
 		if(data == null){
 			return new SleekBucketFloat<O>( type, bucketPivotCount);
 		}else{
 			try{
 				return new SleekBucketFloat<O>( type, bucketPivotCount, data);
 			}catch(IOException e){
 				throw new OBException(e);
 			}
 		
 		}
 	}
 	
 	@Override
 	protected int primitiveDataTypeSize() {
 		return ByteConstants.Float.getSize();
 	}
 	
 	
 	
 	
 	//TODO: raise this method one level up and change all the interfaces.
 	@Override
 	public void searchOB(O object, float r, OBPriorityQueueFloat<O> result)
 			throws NotFrozenException, InstantiationException,
 			IllegalIdException, IllegalAccessException, OutOfRangeException,
 			OBException {
 		searchOB(object, r,null, result);
 		
 				
 	}
 	
 	@Override
 	protected AbstractOBQuery<O> getKQuery(O object, int k) throws OBException, InstantiationException, IllegalAccessException {
 		BucketObjectFloat<O> b = getBucket(object);
 		OBPriorityQueueFloat<O> result = new OBPriorityQueueFloat<O>(k);
 		OBQueryFloat<O> q = new OBQueryFloat<O>(object, Float.MAX_VALUE, result, b
 				.getSmapVector());
 		return q;
 	}
 
 
 	@Override
 	public void searchOB(O object, float r, Filter<O> filter,
 			OBPriorityQueueFloat<O> result) throws NotFrozenException,
 			InstantiationException, IllegalIdException, IllegalAccessException,
 			OutOfRangeException, OBException {
 		BucketObjectFloat<O> b = getBucket(object);
 
 		OBQueryFloat<O> q = new OBQueryFloat<O>(object, r, result, b
 				.getSmapVector());
 		SketchProjection query = this.getProjection(b);
 		
 		// we only use the k estimation once, at the beginning of the search process.
 		int kEstimation = estimateK(result.getK());
 		stats.addExtraStats("K-estimation", kEstimation);
 		long time = System.currentTimeMillis();
 		List<SketchProjection> sortedBuckets = searchBuckets(query, kEstimation);
 		getStats().addExtraStats("Buckets_search_time", System.currentTimeMillis() - time);
 		for(SketchProjection bucket: sortedBuckets){
 				SleekBucketFloat<O> container = this.bucketCache.get(bucket.getAddress());
         //SleekBucketFloat<O> container = this.instantiateBucketContainer(this.Buckets.getValue(bucket.getAddress()), bucket.getAddress());
 			stats.incBucketsRead();
 			container.search(q, b, filter, getStats());															
 		}
 	}
 	
 
 	
 	/**
 	 * This method returns a list of all the distances of the query against  the DB.
 	 * This helps to calculate EP values in a cheaper way. results that are equal to the original object are added
 	 * as Float.MAX_VALUE
 	 * @param query
 	 * @param filterSame if True we do not return objects o such that query.equals(o)
 	 * @return
 	 * @throws OBException 
 	 * @throws InstantiationException 
 	 * @throws IllegalAccessException 
 	 */
 	public float[] fullMatchLite(O query, boolean filterSame) throws OBException, IllegalAccessException, InstantiationException{
 			return CommonsFloat.fullMatchLite((OBFloat)query, filterSame, this);
 	}
 	
 	
 	/**
 	 * Get the kMax closest objects. Count how many different bucket ids are
 	 * there for each k and fill in accordingly the tables.
 	 * 
 	 * @param object
 	 * @throws OBException
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 */
 	protected void maxKEstimationAux(O object)
 			throws OBException, InstantiationException, IllegalAccessException {
 		// we calculate first the list of elements against the DB.
 		BucketObjectFloat<O> b = getBucket(object);
 		SketchProjection longAddr = getProjection(b);
 		byte[] addr = longAddr.getAddress();
 		
 		
 
 		float [] sortedList = fullMatchLite( object, true);
 		//`calculate the AVG distance between the sample and the DB.
 		for(float t : sortedList){
 				getStats().addExtraStats("GENERAL_DISTANCE", t);
 		}
 		// we now calculate the buckets and we sort them
 		// according to the distance of the query.
 		
 			loadMasks();
 
 		long time = System.currentTimeMillis();
 		OBAsserts.chkAssert(Buckets.size() <= Integer.MAX_VALUE, "Capacity exceeded");
 		List<SketchProjection> sortedBuckets = searchBuckets(longAddr, (int)Buckets.size());
 		
 		//List<OBResultFloat<O>> sortedBuckets2 = fullMatch(object);
 		logger.info("Time searching sketches: " + (System.currentTimeMillis() - time));
 
 		// now we have to calculate the EP for each k up to maxK
 		// and for each k we calculate how many buckets must be read in order
 		// to obtain ep less than
 		int i = 0;
 		// container
 		// used to
 		// read
 		// data.
 		FilterNonEquals<O> fne = new FilterNonEquals<O>();
 		while (i < getMaxK().length) {
 			double ep = Double.MAX_VALUE;
 			int goodK = 0;
 			// get the query for the
 			AbstractOBQuery<O> queryAbst = getKQuery(object, userK[i]);
 			OBQueryFloat<O> query = (OBQueryFloat<O>) queryAbst;
 			
 			for (SketchProjection result : sortedBuckets) {
 				
 					SleekBucketFloat<O> container = this.bucketCache.get(result.getAddress());
 					//SleekBucketFloat<O> container = this.instantiateBucketContainer(this.Buckets.getValue(result.getAddress()), result.getAddress());
 				// search the objects
 				assert container != null : "Problem while loading: " + result.getSketch();
 				container.search(query, b, fne, getStats());
 				// calculate the ep of the query and the DB.
 				if (query.isFull()) { // only if the query is full of items.
 					ep = query.approx(sortedList); //SORTED LIST SHOULD CONTAIN ALL THE VECTORS, e.g. 100'000 vectors where each have 100 dimensions
 					//double epOld = query.ep((List)sortedBuckets2);
 					//OBAsserts.chkAssert(Math.abs(ep - epOld)<= 1f / sortedList.length, "oops: new: " + ep + " old: " + epOld);					
 				}
 				goodK++;
 				if (ep <= this.getExpectedEP() && query.isFull()) {
 					// add the information to the stats:
 					// goodK buckets required to retrieve with k==i.
 						logger.info("Found result after reading: " + goodK + " buckets ");
 						logger.info("CARD" + result.getCompactRepresentation().cardinality() + " CARD_Q: " + longAddr.getCompactRepresentation().cardinality());
 					kEstimators[i].add(goodK);
 					// store the distance of the best object and the real-best object
 					float difference = (float)Math.abs(sortedList[0] - query.getResult().getSortedElements().get(0).getDistance());
 					break; // we are done.
 				}
 			}
 			i++;
 		}
 
 	}
 	
 	
 	protected double distance(O a, O b) throws OBException{
 		return a.distance(b);
 	}
 
 
 
 
 
 	@Override
 	protected int getCPSize() {
 			return Math.max((int)Math.ceil(m  /  ByteConstants.Byte.getBits() ), ByteConstants.Long.getSize());
 	}
 
 	@Override
 	protected Class<CBitVector> getPInstance() {
 			return CBitVector.class;
 	}
 
 	
 }
