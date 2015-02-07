package net.obsearch.query;
   import java.util.List;
   import java.util.LinkedList;
   import java.util.HashSet;
   import java.util.Set;
   import net.obsearch.ob.OBLong;
   import net.obsearch.result.OBResultLong;
   import net.obsearch.result.OBPriorityQueueLong;
   import net.obsearch.exception.OBException;
  import net.obsearch.AbstractOBResult;
  import net.obsearch.asserts.OBAsserts;
  /*
      OBSearch: a distributed similarity search engine
      This project is to similarity search what 'bit-torrent' is to downloads.
      Copyright (C)  2007 Arnoldo Jose Muller Molina
  
    	This program is free software: you can redistribute it and/or modify
      it under the terms of the GNU General Public License as published by
      the Free Software Foundation, either version 3 of the License, or
      (at your option) any later version.
  
      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.
  
      You should have received a copy of the GNU General Public License
      along with this program.  If not, see <http://www.gnu.org/licenses/>.
   */
  /**
   * Object used to store a query request.
   * @author Arnoldo Jose Muller Molina
   * @since 0.7
   */
  
  
  public final class OBQueryLong<O extends OBLong> extends AbstractOBQuery<O> {
  
  
  		private OBResultLong<O> query;
      /**
       * Holds the results for the query.
       */
      protected OBPriorityQueueLong<O> result;
  
      /**
       * Minimum part of the rectangle of the query.
       */ 
  		protected long[] min;
  
  		
  		/**
       * Maximum part of the rectangle of the query.
       */ 
  		protected long[] max;
  
  		
  		/**
       * SMAPed vector
       */ 
  
  		protected long[] smap;
  
      /**
       * Constructor.
       */
      public OBQueryLong(){
      }
  
  		public O getObject(){
  				return query.getObject();
  		}
  
      /**
       * Creates a new OBQueryLong object.
       * @param object
       *            The object that will be matched.
       * @param range
       *            The range to be used for the match.
       * @param result
       *            The priority queue were the results will be stored.
       */
      public OBQueryLong(O object, long range, OBPriorityQueueLong<O> result){
  
          query = new OBResultLong<O>(object,-1,range);
          this.result = result;
      }
  
  		public OBQueryLong(O object, OBPriorityQueueLong<O> result){
  				this(object, Long.MAX_VALUE, result);
  		}
  
  		
  
  		/**
  		 * Returns true if the given rectangle collides
       * with this query.
       * @param rectangle The rectangle to search.
       */
 		public boolean collides(long[][] rectangle){
 				long[] minOther = rectangle[0];
 				long[] maxOther = rectangle[1];
 				boolean res = true;
 				assert minOther.length == smap.length;
 				int i = 0;
 				while (i < minOther.length) {
 				    if(max[i] < minOther[i] || min[i] > maxOther[i]){
 								res =false;
 								break;
 						}
 				    i++;
 				}
 				return res;
 		}
 
 		/**
      * Creates a new OBQueryLong object.
      * @param object
      *            The object that will be matched.
      * @param range
      *            The range to be used for the match.
      * @param result
      *            The priority queue were the results will be stored.
 		 * @param smap 
      *            SMAP vector representation of the given object.
      */
     public OBQueryLong(O object, long range, OBPriorityQueueLong<O> result, long[] smap){
 
         this(object,range,result);
 				this.smap = smap;
 				if(smap != null){
 						min = new long[smap.length];
 				max = new long[smap.length];
 				}
 				updateRectangle();
 				
     }
 
 		private void updateRectangle(){
 				if(smap != null){
 				int i = 0;
 				while (i < smap.length) {
 				    min[i] = (long)Math.max(smap[i] - query.getDistance(), 0);
 						max[i] = (long)Math.min(smap[i] + query.getDistance(), Long.MAX_VALUE);
 				    i++;
 				}
 				}
 		}
 
 		/**
 		 * Return low of the query rectangle.
      */ 
 		public long[] getLow(){
 				return min;
 		}
 
 		/**
 		 * Return low of the query rectangle.
      */ 
 		public long[] getHigh(){
 				return max;
 		}
 
     /**
      * @return The current results of the matching.
      */
     public OBPriorityQueueLong<O> getResult(){
         return result;
     }
 
     /**
      * Set the results of the matching to a new object.
      * @param result
      *            The new result.
      */
     public void setResult(OBPriorityQueueLong<O> result){
         this.result = result;
     }
 
    /**
     * Returns true if we should calculate the real distance.
     * @param smapDistance The lower-resolution distance calculated
     * with SMAP.
     * 
     */
     public boolean isCandidate(long smapDistance){
         return smapDistance <= query.getDistance() && result.isCandidate(smapDistance);
     }
 
 
 		public  boolean add(long id, O object) throws InstantiationException, IllegalAccessException , OBException{
 				return add(id, object,  query.getObject().distance(object));
 		}
 
 		public long getDistance(){
 				return query.getDistance();
 		}
 
 		public List<AbstractOBResult<O>> getSortedElements(){
 				List<AbstractOBResult<O>> res = new LinkedList<AbstractOBResult<O>>();
 				for(OBResultLong<O> r : result.getSortedElements()){
 						res.add(r);
 				}
 				return res;	
 		}
 
    /**
     * Add the given object, object id and distance of type float to the
     * queue. Updates the range of the query as needed if the range
     * shrinks after this insertion.
     * @param id
     *            The id of the object to be used
     * @param obj
     *            The object to be added
     * @param d
     *            The distance to be added
     * @throws IllegalAccessException
     *             If there is a problem when instantiating objects O
     * @throws InstantiationException
     *             If there is a problem when instantiating objects O
     */
     public boolean add(long id, O obj, long d) throws InstantiationException, IllegalAccessException {
 				if(d <= currentRange()){
 				boolean res = result.add(id,obj,d);
 				long temp = result.updateRange(query.getDistance());
 				if(temp != query.getDistance()){
   					query.setDistance( (long)(temp-(long)1));
             if(query.getDistance() < 0){
 								query.setDistance( (long)0);
 						}
 
 
 						updateRectangle();
         }
 				return res;
 				}else{
 						return false;
 				}
 		}
 
 		private long currentRange(){
 				if(result.getSize() == result.getK()){
 						//						assert result.peek().getDistance() <= query.getDistance();
 						return (long)Math.min(query.getDistance(), result.peek().getDistance());
 				}else{
 						return query.getDistance();
 				}
 		}
 
 		/**
 		 * Returns true if the originalRange has been modified.
 		 * @return true If the current range (getDistance()) is different than originalRange.
 		 */
 		public boolean updatedRange(long originalRange){
 				return query.getDistance() != originalRange;
 		}
 
 		/**
 		 * @return true if the underlying priority queue's size is equal to k
      */
 		public boolean isFull(){
 				return result.isFull();
 		}
 
 
 		public  double recall(List<AbstractOBResult<O>> perfectQuery){
 				int hits = 0;
 				Set<AbstractOBResult<O>> s = new HashSet<AbstractOBResult<O>>();
 				for(OBResultLong<O> r : this.result.getSortedElements()){			
 						int i = 0;
 						for(AbstractOBResult<O> d : perfectQuery){
 								if(! s.contains(d) && d.compareTo(r) == 0){
 										s.add(d);
 										hits++;
 										break;
 								}
 								i++;
 						}
 				}
 				return (double)hits / (double)perfectQuery.size();
 		}
 
 
 		public  double ep(List<AbstractOBResult<O>> dbin){
 				List<OBResultLong<O>> query = getResult().getSortedElements();
 				int i = 0;
 				int result = 0;
 				Set<OBResultLong<O>> s = new HashSet<OBResultLong<O>>();
 				for(OBResultLong<O> r : query){
 						// find the position in the db. 
 						int cx = 0;
 						for(AbstractOBResult<O> cr : dbin){
 								OBResultLong<O> c = (OBResultLong<O>)cr;
 								if(! s.contains(c) &&c.compareTo(r) == 0){
 										s.add(c);
 										result += cx - i;
 										break;
 								}
 								cx++;
 						}
 						i++;
 				}
 				if(query.size() == 0){
 						return 0;
 				}else{
 						double res = ((double)result)/ ((double)(query.size() * dbin.size()));
 						return res;
 				}
 		}
 
 
 		/**
      * Calculate the EP value for a sorted list of distances.
      */
 		public  double ep(long[] dbin){
 				return epAux(dbin) * (1 / (double)getResult().getSize());
 		}
 
 		
 				/**
 	 * Calculate the 1 + E  or (c for Adonis et al) for approx. nearest neighbor
 	 * This is the approximation and "real" is the real result.
 	 * @param q
 	 * @return
 	 * @throws RAException
 	 */
 		public double approx(long[] dbin) throws OBException {
 
 				List<OBResultLong<O>> query = getResult().getSortedElements();
 		// get the last guys
 		if(! this.isFull()){
 				return 1;
 		}
 		
 		int last = query.size() - 1;
 		return Math.abs((query.get(last).getDistance()) / dbin[last]);
 	}
 
 		/**
      * Calculate the EP value for a sorted list of distances.
 		 * Does not multiply by 1/k
      */
 		private  double epAux(long[] dbin){
 				List<OBResultLong<O>> query = getResult().getSortedElements();
 				int i = 0;
 				int result = 0;
         // hold the visited elements
 				Set<Integer> s = new HashSet<Integer>();
 				for(OBResultLong<O> r : query){
 						// find the position in the db. 
 						int cx = 0;
 						for(long cr : dbin){								
 								if(! s.contains(cx) && cr == r.getDistance()){
 										s.add(cx);
 										result += cx - i;
 										break;
 								}
 								cx++;
 						}
 						i++;
 				}
 				if(query.size() == 0){
 						return 0;
 				}else{
 						double res = ((double)result)/ ((double)(dbin.length));
 						return res;
 				}
 		}
 
 
 		/**
 		 * Calculates ep without multiplying by 1/k and 
 		 */ 
 		public  double compound(long[] dbin){
 				List<OBResultLong<O>> query = getResult().getSortedElements();
 				int i = 0;
 				
 				double res = 0;
         // hold the visited elements
 				Set<Integer> s = new HashSet<Integer>();
 				for(OBResultLong<O> r : query){
 
 						double ep = 1 - (((double)rank(r.getDistance(), dbin) - (i))/(double)(dbin.length));
 						// fix rounding error
 						if(r.getDistance() == 0){
 								assert dbin[i] == 0;
 								res += 1;
 						}else{
 								res += ((double)dbin[i] / (double)r.getDistance()) * ep;
 						}
 						i++;
 				}
 				return res / (double) query.size();
 		}
 
 		/*
 		 * Calculates the relative distance error.
 		 */
     public double rde(long[] dbin){
 				return rdeAux(dbin) / (double) getResult().getSize();
 		}
 
 		/*
 		 * Calculates the relative distance error without dividing by 1/k
 		 */
 		private  double rdeAux(long[] dbin){
 				List<OBResultLong<O>> query = getResult().getSortedElements();
 				int i = 0;
 				double result = 0;
         // hold the visited elements
 				for(OBResultLong<O> r : query){
 						// find the position in the db. 
 						if(dbin[i] != 0){
 								result +=  ( (double) r.getDistance() / (double)dbin[i]) - 1; 
 						}
 						i++;
 				}
 				return result;
 		}
 
 
 		/**
 		 * Total distance ratio
 		 */
 		public  double tDR(long[] dbin){
 				List<OBResultLong<O>> query = getResult().getSortedElements();
 				int i = 0;
 				double up = 0;
 				double down = 0;
         // hold the visited elements
 				for(OBResultLong<O> r : query){
 						// find the position in the db. 
 						up += (double)dbin[i];
 						down += (double) r.getDistance();
 						i++;
 				}
 				return up / down;
 		}
 
 		/** 
      * Calculates the precision
 		 */
 		public double precision(long[] dbin){
 				List<OBResultLong<O>> query = getResult().getSortedElements();
 				int count = 0;
         // hold the visited elements
 				for(OBResultLong<O> r : query){
 						if(rank(r.getDistance(), dbin) < query.size()) {
 								count++;
 						}
 				}
 				return (double) count / (double) query.size();
 		}
 
 		/**
 		 * find the position of distance in the given dbin list
 		 */ 
 		private int rank(long distance, long[] dbin){
 				int i = 0;
 				while(i < dbin.length){
 						if(distance == dbin[i]){
 								break;
 						}
 						i++;
 				}
 				return i;
 		}
 
 		/**
 		 * peek to the largest value if the queue is full.
 		 */
 		public long peek() throws OBException{
 				OBAsserts.chkAssert(isFull(), "Queue is not full");
 				return result.peek().getDistance();
 		}
 		
 }
