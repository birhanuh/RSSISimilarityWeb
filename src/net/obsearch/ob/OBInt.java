 package net.obsearch.ob;
   import net.obsearch.exception.OBException;
   import net.obsearch.OB;
   
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
   * Any Object whose distance function returns ints must implement this
   * interface. WARNING: The equals method *must* be implemented. The equals does
   * not have to be true when the distance returns 0. Note however that at search
   * time, elements of distance 0 are treated in the same way. Equals is only used
   * when the database is queried for the existence of an object.
   * @author Arnoldo Jose Muller Molina
   * @since 0.7
   */
  
  
  public interface OBInt extends OB{
  
      /**
       * Calculates the similarity of "this" and "object". The function must
       * satisfy the triangular inequality and return a int.
       * @param object
       *            The object of type OBInt to be compared
       * @param result
       *            The resulting distance
       * @since 0.7
       * @throws OBException
       *             User generated exception
       */
      int distance(OBInt object) throws OBException;
  }
