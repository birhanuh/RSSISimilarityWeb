package net.obsearch.query;
   import java.util.List;
   import java.util.LinkedList;
   import java.util.HashSet;
   import java.util.Set;
   import net.obsearch.ob.OBByte;
   import net.obsearch.result.OBResultByte;
   import net.obsearch.result.OBPriorityQueueByte;
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
  
  
  public final class OBQueryByte<O extends OBByte> extends AbstractOBQuery<O> {
  
  
  		private OBResultByte<O> query;
      /**
       * Holds the results for the query.
       */
      protected OBPriorityQueueByte<O> result;
  
      /**
       * Minimum part of the rectangle of the query.
       */ 
  		protected byte[] min;
  
  		
  		/**
       * Maximum part of the rectangle of the query.
       */ 
  		protected byte[] max;
  
  		
  		/**
       * SMAPed vector
       */ 
  
  		protected byte[] smap;
  
      /**
       * Constructor.
       */
      public OBQueryByte(){
      }
  
  		public O getObject(){
  				return query.getObject();
  		}
  
      /**
       * Creates a new OBQueryByte object.
       * @param object
       *            The object that will be matched.
       * @param range
       *            The range to be used for the match.
       * @param result
       *            The priority queue were the results will be stored.
       */
      public OBQueryByte(O object, byte range, OBPriorityQueueByte<O> result){
  
          query = new OBResultByte<O>(object,-1,range);
          this.result = result;
      }
  
  		public OBQueryByte(O object, OBPriorityQueueByte<O> result){
  				this(object, Byte.MAX_VALUE, result);
  		}
  
  		
  
  		/**
  		 * Returns true if the given rectangle collides
       * with this query.
       * @param rectangle The rectangle to search.
       */
 		public boolean collides(byte[][] rectangle){
 				byte[] minOther = rectangle[0];
 				byte[] maxOther = rectangle[1];
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
      * Creates a new OBQueryByte object.
      * @param object
      *            The object that will be matched.
      * @param range
      *            The range to be used for the match.
      * @param result
      *            The priority queue were the results will be stored.
 		 * @param smap 
      *            SMAP vector representation of the given object.
      */
     public OBQueryByte(O object, byte range, OBPriorityQueueByte<O> result, byte[] smap){
 
         this(object,range,result);
 				this.smap = smap;
 				if(smap != null){
 						min = new byte[smap.length];
 				max = new byte[smap.length];
 				}
 				updateRectangle();
 				
     }
 
 		private void updateRectangle(){
 				if(smap != null){
 				int i = 0;
 				while (i < smap.length) {
 				    min[i] = (byte)Math.max(smap[i] - query.getDistance(), 0);
 						max[i] = (byte)Math.min(smap[i] + query.getDistance(), Byte.MAX_VALUE);
 				    i++;
 				}
 				}
 		}
 
 		/**
 		 * Return low of the query rectangle.
      */ 
 		public byte[] getLow(){
 				return min;
 		}
 
 		/**
 		 * Return low of the query rectangle.
      */ 
 		public byte[] getHigh(){
 				return max;
 		}
 
     /**
      * @return The current results of the matching.
      */
     public OBPriorityQueueByte<O> getResult(){
         return result;
     }
 
     /**
      * Set the results of the matching to a new object.
      * @param result
      *            The new result.
      */
     public void setResult(OBPriorityQueueByte<O> result){
         this.result = result;
     }
 
    /**
     * Returns true if we should calculate the real distance.
     * @param smapDistance The lower-resolution distance calculated
     * with SMAP.
     * 
     */
     public boolean isCandidate(byte smapDistance){
         return smapDistance <= query.getDistance() && result.isCandidate(smapDistance);
     }
 
 
 		public  boolean add(long id, O object) throws InstantiationException, IllegalAccessException , OBException{
 				return add(id, object,  query.getObject().distance(object));
 		}
 
 		public byte getDistance(){
 				return query.getDistance();
 		}
 
 		public List<AbstractOBResult<O>> getSortedElements(){
 				List<AbstractOBResult<O>> res = new LinkedList<AbstractOBResult<O>>();
 				for(OBResultByte<O> r : result.getSortedElements()){
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
     public boolean add(long id, O obj, byte d) throws InstantiationException, IllegalAccessException {
 				if(d <= currentRange()){
 				boolean res = result.add(id,obj,d);
 				byte temp = result.updateRange(query.getDistance());
 				if(temp != query.getDistance()){
 								 query.setDistance( temp );
 
 						updateRectangle();
         }
 				return res;
 				}else{
 						return false;
 				}
 		}
 
 		private byte currentRange(){
 				if(result.getSize() == result.getK()){
 						//						assert result.peek().getDistance() <= query.getDistance();
 						return (byte)Math.min(query.getDistance(), result.peek().getDistance());
 				}else{
 						return query.getDistance();
 				}
 		}
 
 		/**
 		 * Returns true if the originalRange has been modified.
 		 * @return true If the current range (getDistance()) is different than originalRange.
 		 */
 		public boolean updatedRange(byte originalRange){
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
 				for(OBResultByte<O> r : this.result.getSortedElements()){			
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
 				List<OBResultByte<O>> query = getResult().getSortedElements();
 				int i = 0;
 				int result = 0;
 				Set<OBResultByte<O>> s = new HashSet<OBResultByte<O>>();
 				for(OBResultByte<O> r : query){
 						// find the position in the db. 
 						int cx = 0;
 						for(AbstractOBResult<O> cr : dbin){
 								OBResultByte<O> c = (OBResultByte<O>)cr;
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
 		public  double ep(byte[] dbin){
 				return epAux(dbin) * (1 / (double)getResult().getSize());
 		}
 
 		
 				/**
 	 * Calculate the 1 + E  or (c for Adonis et al) for approx. nearest neighbor
 	 * This is the approximation and "real" is the real result.
 	 * @param q
 	 * @return
 	 * @throws RAException
 	 */
 		public double approx(byte[] dbin) throws OBException {
 
 				List<OBResultByte<O>> query = getResult().getSortedElements();
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
 		private  double epAux(byte[] dbin){
 				List<OBResultByte<O>> query = getResult().getSortedElements();
 				int i = 0;
 				int result = 0;
         // hold the visited elements
 				Set<Integer> s = new HashSet<Integer>();
 				for(OBResultByte<O> r : query){
 						// find the position in the db. 
 						int cx = 0;
 						for(byte cr : dbin){								
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
 		public  double compound(byte[] dbin){
 				List<OBResultByte<O>> query = getResult().getSortedElements();
 				int i = 0;
 				
 				double res = 0;
         // hold the visited elements
 				Set<Integer> s = new HashSet<Integer>();
 				for(OBResultByte<O> r : query){
 
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
     public double rde(byte[] dbin){
 				return rdeAux(dbin) / (double) getResult().getSize();
 		}
 
 		/*
 		 * Calculates the relative distance error without dividing by 1/k
 		 */
 		private  double rdeAux(byte[] dbin){
 				List<OBResultByte<O>> query = getResult().getSortedElements();
 				int i = 0;
 				double result = 0;
         // hold the visited elements
 				for(OBResultByte<O> r : query){
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
 		public  double tDR(byte[] dbin){
 				List<OBResultByte<O>> query = getResult().getSortedElements();
 				int i = 0;
 				double up = 0;
 				double down = 0;
         // hold the visited elements
 				for(OBResultByte<O> r : query){
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
 		public double precision(byte[] dbin){
 				List<OBResultByte<O>> query = getResult().getSortedElements();
 				int count = 0;
         // hold the visited elements
 				for(OBResultByte<O> r : query){
 						if(rank(r.getDistance(), dbin) < query.size()) {
 								count++;
 						}
 				}
 				return (double) count / (double) query.size();
 		}
 
 		/**
 		 * find the position of distance in the given dbin list
 		 */ 
 		private int rank(byte distance, byte[] dbin){
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
 		public byte peek() throws OBException{
 				OBAsserts.chkAssert(isFull(), "Queue is not full");
 				return result.peek().getDistance();
 		}
 		
 }
