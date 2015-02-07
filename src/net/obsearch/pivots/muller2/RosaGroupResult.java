package net.obsearch.pivots.muller2;

/**
 * Convenience class for storing pivots and their
 * corresponding groups.
 * @author Arnoldo Jose Muller Molina
 *
 */
public class RosaGroupResult {
	
	private long []pivots;
	private int groupId;
	
	
	public RosaGroupResult(long[] pivots, int groupId) {
		super();
		this.groupId = groupId;
		this.pivots = pivots;
	}
	
	public long[] getPivots() {
		return pivots;
	}
	public void setPivots(long[] pivots) {
		this.pivots = pivots;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	
	
	
}
