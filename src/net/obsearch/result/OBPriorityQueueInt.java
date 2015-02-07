package net.obsearch.result;
   
   import net.obsearch.AbstractOBPriorityQueue;
   import net.obsearch.ob.OBInt;
   import java.util.Iterator;
   
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
   * This is a class used to efficiently perform k-nn searches. This queue is
   * meant to be used with objects OBInt. 
   * @author Arnoldo Jose Muller Molina
   * @since 0.7
   */
  
  public final class OBPriorityQueueInt<O> extends AbstractOBPriorityQueue<OBResultInt<O>> {
  
      /**
       * Create the priority queue with k elements. This is how you set the k
       * for a query. Use a negative value for a boundless queue.
       */
      public OBPriorityQueueInt(int k){
          super(k);
      }
  
  		
  		
  		
      /**
       * Add the given object, object id and distance of type int to the
       * queue. This method tries to minimize the sizes of distances
       * of the priority queue.
       * @param id
       *            The id of the object to be used
       * @param obj
       *            The object to be added
       * @param distance
       *            The distance to be added
       * @throws IllegalAccessException
       *             If there is a problem when instantiating objects O
       * @throws InstantiationException
       *             If there is a problem when instantiating objects O
  		 * @return true if the range has changed.
       */
      public boolean add(long id, O obj, int distance) throws InstantiationException, IllegalAccessException {
          if (queue.size() == k) {
              // recycle objects.
  						
              if (queue.peek().getDistance() > distance) {// biggest object in
                  // the heap is
                  // bigger than d
                  OBResultInt<O> c = queue.poll();
                  c.setDistance(distance);
                  c.setObject(obj);
                  c.setId(id);
  								//								assert validateAddition(c);
                  queue.offer(c);
  								return true;
              }
  						
          } else { // if we are smaller than k we just create the object
              OBResultInt<O> c = new OBResultInt<O>();
              c.setDistance(distance);
              c.setObject(obj);
              c.setId(id);
  						// assert validateAddition(c);
              queue.offer(c);
          }
  				return false;
          //assert queue.size() <= k;
      }
  
  
  		/**
       * Add the given object, object id and distance of type int to the
       * queue. This method tries to <b>maximize</b> the sizes of distances
       * of the priority queue.
       * @param id
       *            The id of the object to be used
       * @param obj
       *            The object to be added
       * @param distance
       *            The distance to be added
      * @throws IllegalAccessException
      *             If there is a problem when instantiating objects O
      * @throws InstantiationException
      *             If there is a problem when instantiating objects O
      */
     public void addMax(long id, O obj, int distance) throws InstantiationException, IllegalAccessException {
         if (queue.size() == k) {
             // recycle objects.
 						
             if (queue.peek().getDistance() < distance) {// biggest object in
                 // the heap is
                 // bigger than d
                 OBResultInt<O> c = queue.poll();
                 c.setDistance(distance);
                 c.setObject(obj);
                 c.setId(id);
 								//								assert validateAddition(c);
                 queue.offer(c);
             }
         } else { // if we are smaller than k we just create the object
             OBResultInt<O> c = new OBResultInt<O>();
             c.setDistance(distance);
             c.setObject(obj);
             c.setId(id);
 						// assert validateAddition(c);
             queue.offer(c);
         }
         //assert queue.size() <= k;
     }
 
 		/**
 		 * Make sure no repeated elements are added
 		 */
 		private boolean validateAddition(OBResultInt<O> c){
 				Iterator<OBResultInt<O>> it = iterator();
 				while(it.hasNext()){
 						OBResultInt<O> t = it.next();
 						if(t.getId() == c.getId()){
 								return false;
 						}
 				}
 				return false;
 		}
 
     /**
      * If queue.size() == k, then if the user's range is greater than the
      * greatest element of the queue, we can reduce the range to the biggest
      * element of the priority queue, that is its queue.peek() element.
      * @param r
      *            The new range we want to calculate.
      * @return the new range or the old range if the above condition is not
      *         met
      */
     public int updateRange(int r) {
         // TODO: update the pyramid technique range so that we reduce the
         // searches in the
         // remaining pyramids. We could start actually matching random pyramids
         // and then hope we can get a very small r at the beginning
         // if so, the other pyramids will be cheaper to search.
         // in paralell mode we could take the first 2 * d queries and then match
         // one pyramid by one each of the queries waiting to get the sub result,
         // update the range
         // and then continue... this can potentially improve performance.
         if (this.getSize() == k) {
             int d = queue.peek().getDistance();
             if (d < r) {
                 return d; // return d
             }           
         }
         return r; // return d if we cannot safely reduce the range
     }
 
     /**
      * Returns true if the given distance can be a candidate for adding it into
      * the queue. The parameter d is an estimation of the real distance, and
      * this method is used to decide if we should calculate the real distance.
      * @param d
      *            The lower resolution distance.
      * @return True if we should calculate the real distance and attempt add the
      *         corresponding object into this queue.
      */
     public boolean isCandidate(int d){
         if(this.getSize() == k ){
             // d should be less than the biggest candiate
             // to be considered
             return d < queue.peek().getDistance();
         }
         // if the queue is smaller than k,
         // everybody is a candidate
         return true; 
     }
 
 
 
 }
