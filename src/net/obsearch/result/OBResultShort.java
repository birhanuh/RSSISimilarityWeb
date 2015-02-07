package net.obsearch.result;
   import net.obsearch.AbstractOBResult;
   import net.obsearch.ob.OBShort;
   
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
   * This class is used to store a single match result. A single result consists
   * of the object found, the distance of this object with the query and an
   * internal id.
   * 
   * @author Arnoldo Jose Muller Molina
   * @since 0.7
   */
  
  
  public  final class OBResultShort<O> extends AbstractOBResult<O> {
  
  
      /**
       * Distance of the object found and the query.
       */ 
      protected short distance;
  
      /**
       * Default constructor. Used mostly to minimize the amount of object
       * creations.
       */
      public OBResultShort(){
      }
  
      /**
       * Create a new OBResultShort.
       * @param object
       *            the result found.
       * @param id
       *            The internal id of the result.
       * @param distance
       *            Distance of the result and the original query.
       */
      public OBResultShort(O object, long id, short distance){
  
          super(object,id);
          this.distance = distance;
  
      }
  
      /**
       * @return The distance of the result and the original query.
       */
      public final short getDistance(){
          return distance;
      }
  
      /**
       * Sets the distance to a new value x.
       * @param x
       *            The new value to set.
       */
      public final void setDistance(short x){
          this.distance = x;
      }
  
      /**
       * We implement the interface comparable so we provide this method. The
       * only difference is that we return bigger objects first. (The
       * comparable contract is multiplied by -1)
       * @param o
       *            The object that will be compared.
       * @return 1 if this object is smaller than o 0 if this object is equal
       *         than o -1 if this object is greater than o
       */
      public int compareTo(Object o) {
          assert o instanceof OBResultShort;
  				OBResultShort<O> comp = (OBResultShort<O>) o;
  				int res = 0;
          
          
          if (distance < comp.distance) {
              res = 1;
          } else if (distance > comp.distance) {
              res = -1;
          }
         return res;
     }
 
     /**
      * @return the hash code of this object.
      */
     /*public int hashCode(){
         return  (int) distance;
 				}*/
 
 		
 
     /**
      * We do not care about the object itself, just that both objects are at
      * the same distance from the query.
      * @return true if both distances are the same.
      */
     /*public boolean equals(Object obj){                
         OBResultShort<O> comp = (OBResultShort<O>) obj;
         // a result object is the same if the distance is the same
         // we do not care about the id.
         return distance == comp.distance;
 				}*/
 
     /**
      * Return a human readable representation of the object.
      * @return a human readable representation of the object.
      */
     public String toString(){				
         return "<" + id + " " + distance + ">";
     }
 }
