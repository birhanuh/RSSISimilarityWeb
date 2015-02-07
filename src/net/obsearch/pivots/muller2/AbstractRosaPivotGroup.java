package net.obsearch.pivots.muller2;

import java.util.BitSet;
import java.util.LinkedList;



public abstract class AbstractRosaPivotGroup {

	
	/**
	 * Total number of pivots.
	 */
	protected int pivotsTotal;
	/**
	 * Registers the pivots that are being held 
	 * in this group.
	 */
	protected BitSet pivots;
	
	
	
	

	/**
	 * Return true if the current group has the given pivot number.
	 * @param pivotNumber
	 * @return
	 */
	public boolean hasPivot(int pivotNumber) {
		return pivots.get(pivotNumber);
	}
	
	/**
	 * Generates the id used to store this group set.
	 * @return The id of the object.
	 */
	public long generateId(){
		return generateIdAux(1);
	}
	
	/**
	 * Generates the id used to store this group set.
	 * @return The id of the object.
	 */
	protected abstract long generateIdAux(int count);
	
	
	protected abstract class Dimension{

		protected int pivot;

		public int getPivot() {
			return pivot;
		}

		public void setPivot(int pivot) {
			this.pivot = pivot;
		}
		
	}
	
	/**
	 * Return the group information.
	 * Example: [0 0 1 1 1] means that pivots 0 and 1 belong to group 0
	 * and pivots 2 3 4 belong to group 1.
	 * @return
	 */
	public abstract int [] getGroupInformation();
	
	
	/**
	 * Returns the # of groups available.
	 * @return
	 */
	public abstract int groupCount();

}
