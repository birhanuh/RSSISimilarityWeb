package net.obsearch.pivots.muller2;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.obsearch.pivots.PivotResult;

/*
 OBSearch: a distributed similarity search engine This project is to
 similarity search what 'bit-torrent' is to downloads. 
 Copyright (C) 2008 Arnoldo Jose Muller Molina

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
 * RosaIncrementalSelector provides additional operations that allow
 * a Rosa Filter to determine the order of the pivots.
 * Returns the way the previous partition should be divided
	 * For example if the pivots form the following groups
	 * [1 2 3] [4 5 6] [7 8 9]
	 *   (0)     (1)     (2)
	 *  Then the following array is returned:
	 * [0 0 0   1 1 1   2 2 2]
	 * Representing the group of each pivot.
 * 
 * @author Arnoldo Jose Muller Molina
 */

public class RosaPivotResult extends PivotResult{
	
	private List<RosaGroupResult> pivotGroup;
	
	private int size;
		
	public RosaPivotResult() {		
		
		size = 0;
		pivotGroup = new LinkedList<RosaGroupResult>();
	}
	
	public void addPivotGroup(long[] ids, int groupId){
		pivotGroup.add(new RosaGroupResult(ids,groupId));
		size += ids.length;
	}
	
	/**
	 * Amount of pivots in this object.
	 * @return
	 */
	public int size(){
		return size;
	}

	/**
	 * Returns the way the previous partition should be divided
	 * For example if the pivots form the following groups
	 * [1 2 3] [4 5 6] [7 8 9]
	 *   (0)     (1)     (2)
	 *  Then the following array is returned:
	 * [0 0 0   1 1 1   2 2 2]
	 * Representing the group of each pivot.
	 * @return
	 */
	public List<RosaGroupResult> getPivotGroup(){
		assert null != pivotGroup;
		return pivotGroup;
	}
	
	public long [] getPivotIds(){
		long [] res = new long[size()];
		Iterator<RosaGroupResult> it = pivotGroup.iterator();
		int i = 0;
		while(it.hasNext()){
			for(long id: it.next().getPivots()){
				res[i] = id;
				i++;
			}
		}
		return res;
	}

}
