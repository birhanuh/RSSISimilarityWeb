package net.obsearch.pivots.muller2;

/**
 * Reproduction, use and study of this code is forbidden.
 */
import hep.aida.bin.StaticBin1D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import net.obsearch.Index;
import net.obsearch.OB;
import net.obsearch.exception.IllegalIdException;
import net.obsearch.exception.OBException;
import net.obsearch.exception.OBStorageException;
import net.obsearch.exception.PivotsUnavailableException;
import org.apache.log4j.Logger;

import cern.colt.list.LongArrayList;
import net.obsearch.pivots.AbstractIncrementalPivotSelector;
import net.obsearch.pivots.IncrementalPivotSelector;
import net.obsearch.pivots.Pivotable;

/**
 * 
 * @author Arnoldo Jose Muller Molina
 * 
 * @param <O>
 */
public abstract class AbstractIncrementalMuller<O extends OB> extends
		AbstractIncrementalPivotSelector<O>  implements
		IncrementalPivotSelector<O>{

	private final int MAX_RETRIES = 100;

	// must be 2 for 2 dimensions. (maybe for all dimensions?)
	public static final int ACCEPTABLE_RANGE = 1;

	/**
	 * Pivot sample size to use.
	 */
	private int pivotSamples;
	/**
	 * Data sample to use.
	 */
	private int dataSamples;

	private static final transient Logger logger = Logger
			.getLogger(AbstractIncrementalPivotSelector.class);

	/**
	 * Receives the object that accepts pivots as possible candidates. Selects l
	 * pairs of objects to compare which set of pivots is better, and selects m
	 * possible pivot candidates from the data set.
	 * 
	 * @param pivotable
	 * @param l
	 * @param m
	 */
	protected AbstractIncrementalMuller(Pivotable<O> pivotable,
			int pivotSamples, int dataSamples) {
		super(pivotable);
		this.pivotSamples = pivotSamples;
		this.dataSamples = dataSamples;
	}

	@Override
	public RosaPivotResult generatePivots(int pivotCount,
			LongArrayList elements, Index<O> index) throws OBException,
			IllegalAccessException, InstantiationException, OBStorageException,
			PivotsUnavailableException {
		int dLocal = (int) Math.min(dataSamples, index.databaseSize());
		int pLocal = (int) Math.min(pivotSamples, index.databaseSize());
		// do not process more than the amount of pivots in the DB.

		int max;
		if (elements == null) {
			max = (int) Math.min(index.databaseSize(), Integer.MAX_VALUE);
		} else {
			max = elements.size();
		}
		LongArrayList pivotList = new LongArrayList(pivotCount);
		Random r = new Random();
		// select m objects from which we will select pivots
		RosaPivotResult result = new RosaPivotResult();
		
		int i = 0;
		

		int groupId = 0;
		long[] x = select(dLocal, r, elements, index, null);
		
		int retries = 0;
		while (pivotList.size() < pivotCount) {
			if (retries > MAX_RETRIES) {
				// too many retries!
				throw new PivotsUnavailableException();
			}
			long[] possiblePivots = select(pLocal, r, elements, index,
					pivotList);
			// select l pairs of objects to validate the pivots.

			// select the pivot in possiblePivots that maximizes the median
			// projected distances.
			logger.debug("Selecting pivot: " + pivotList.size());
			RosaPivotMetrics selectedPivotGroup = null;
			try {
				selectedPivotGroup = selectPivots(pivotList, possiblePivots, x,
						index);
			} catch (PivotsUnavailableException u) {
				retries++;
				continue;
			}
			if (logger.isDebugEnabled()) {
				logger
						.debug("Group selected: "
								+ selectedPivotGroup.toString());
			}
			// add the pivots to the final result
			result.addPivotGroup(selectedPivotGroup.getPivotId(), groupId);
			for(long id : selectedPivotGroup.getPivotId()){
				pivotList.add(id);
			}
			groupId++;
			i++;
		}

		return result;

	}

	/**
	 * Validates that the lower layers have been processing everything fine.
	 * 
	 * @param pivots
	 *            the pivots that were selected
	 * @param id
	 *            Id of the object
	 * @param index
	 *            Index from which we will load objects.
	 */
	protected abstract boolean validatePivots(long[] pivots, long id,
			Index<O> index) throws IllegalIdException, IllegalAccessException,
			InstantiationException, OBException;

	/**
	 * Reset the cache.
	 * 
	 * @param x
	 *            Size of the cache.
	 */
	protected abstract void resetCache(int x);

	/**
	 * Generates a pivot group from the given set of pivots.
	 * 
	 * @param previousPivots
	 *            All the pivots that have been selected
	 * @param possiblePivots
	 *            The possible set of pivots.
	 * @param x
	 *            (left item of the pair)
	 * @param y
	 *            (right item of the pair)
	 * @return The best element in possiblePivots
	 */
	private RosaPivotMetrics selectPivotsX(LongArrayList previousPivots,
			long[] possiblePivots, long[] x, Index<O> index)
			throws IllegalIdException, IllegalAccessException,
			InstantiationException, OBException, PivotsUnavailableException {
		resetCache(x.length);
		previousPivots.trimToSize();
		logger.info("selectPivots" + previousPivots.size());
		LongArrayList currentPivotSet = new LongArrayList();
		currentPivotSet.add(possiblePivots[0]);

		boolean ok = true;
		RosaPivotMetrics res = null;
		while (ok) {
			// select one pivot and add it to the currentPivotSet
			long[] pivots = Arrays.copyOf(currentPivotSet.elements(),
					currentPivotSet.size() + 1);

			RosaPivotMetrics result = new RosaPivotMetrics();
			ok = false;
			for (long pivotId : possiblePivots) {
				// do not select a pivot that was already selected.
				if (previousPivots.contains(pivotId)
						|| currentPivotSet.contains(pivotId)) {
					continue;
				}

				pivots[pivots.length - 1] = pivotId;
				RosaPivotMetrics t;
				try {
					t = calculateGroup(pivots, x, index, previousPivots);
				} catch (PivotsUnavailableException p) {
					continue; // go to next iteration because this pivot does
					// not satisfy.
				}
				if (result.compareTo(t) <= 0
						&& t.getDifferentCombinations() >= pivots.length) {
					result = t;
					// found a pivot.
					ok = true;
				}
			}
			if (ok) {
				// add the pivot found.
				// logger.info("Found group!" + result);
				if (res == null || res.compareTo(result) <= 0) {
					currentPivotSet.add(result.getPivotId()[result
							.getPivotCount() - 1]);
					res = result;
				} else {
					break;
				}
			}

		}

		if (res == null || res.getPivotCount() < 2) {
			throw new PivotsUnavailableException();
		}

		return res;
	}

	// only for 2 pivots.
	private RosaPivotMetrics selectPivots(LongArrayList previousPivots,
			long[] possiblePivots, long[] x, Index<O> index)
			throws IllegalIdException, IllegalAccessException,
			InstantiationException, OBException, PivotsUnavailableException {
		//resetCache(x.length * possiblePivots.length);
		previousPivots.trimToSize();
		logger.info("selectPivots" + previousPivots.size());
		LongArrayList currentPivotSet = new LongArrayList();
		currentPivotSet.add(possiblePivots[0]);

		boolean ok = false;
		RosaPivotMetrics res = new RosaPivotMetrics();
		for (long k : possiblePivots) {
			// select one pivot and add it to the currentPivotSet
			long[] pivots = new long[2];
			for (long j : possiblePivots) {
				if (j == k) {
					continue;
				}
				pivots[0] = k;
				pivots[1] = j;
				RosaPivotMetrics t;
				try {
					t = calculateGroup(pivots, x, index, previousPivots);
				} catch (PivotsUnavailableException p) {
					continue; // go to next iteration because this pivot does
					// not satisfy.
				}
				if (res.compareTo(t) < 0
						&& t.getDifferentCombinations() >= pivots.length) {
					res = t;
					//logger.debug("NEW: " + t);
					// found a pivot.
					ok = true;
				}

			}

		}
		if (ok) {
			return res;
		} else {
			throw new PivotsUnavailableException();
		}
	}

	/**
	 * Calculates how good is the given pivot set on the given set of objects
	 * 
	 * @param pivots
	 *            The pivots used to map the space
	 * @param x
	 *            The left part of the pair
	 * @param index
	 *            The underlying index (used to extract the objects and
	 *            calculate the distances)
	 * @throws PivotsUnavailableException
	 */
	protected abstract RosaPivotMetrics calculateGroup(long[] pivots, long[] x,
			Index<O> index, LongArrayList previousPivots) throws IllegalIdException, IllegalAccessException,
			InstantiationException, OBException, PivotsUnavailableException;

	/**
	 * Calculates the factorial of i.
	 * 
	 * @param i
	 * @return factorial of i
	 */
	protected final long factorial(int i) {
		if (i == 1) {
			return i;
		} else {
			return i * factorial(i - 1);
		}
	}

	/**
	 * Values that help us to detect which pivot combination is the best. We
	 * assume that min range is greater than the minimum desired range.
	 * 
	 * @author amuller
	 * 
	 */
	protected class RosaPivotMetrics implements Comparable<RosaPivotMetrics> {

		/**
		 * The pivot that we are talking about.
		 */
		private long[] pivotId;

		/**
		 * Minimum range obtained. The greater, the better.
		 */
		private double minRange;

		/**
		 * Ids created from the pivots.
		 */
		private HashMap<Long, Integer> ids;

		/**
		 * Statistics used to make sure the ids are well distributed. Variance
		 * should be minimal.
		 */
		private StaticBin1D stats;

		/**
		 * Create a dummy metrics object to start comparisons.
		 */
		public RosaPivotMetrics() {
			this(0, null, null);
		}

		/**
		 * Create a Rosa pivot metrics with the given parameters. We will pick
		 * up the "biggest" metrics object.
		 * 
		 * @param pivotCount
		 *            Total # of pivots (including only the rightmost pivot with
		 *            pivotId)
		 * @param minRange
		 *            minimum range.
		 * @param pivotId
		 *            pivot identification id.
		 */
		public RosaPivotMetrics(double minRange, long[] pivotId,
				HashMap<Long, Integer> ids) {
			super();
			this.minRange = minRange;
			this.pivotId = pivotId;
			this.ids = ids;
			if (ids != null) {
				// lower variance is better.
				stats = new StaticBin1D();
				for (Entry<Long, Integer> e : ids.entrySet()) {
					stats.add(e.getValue());
				}

			}
		}

		public double getStandardDeviation() {
			if (stats == null) {
				return Double.MAX_VALUE;
			} else {
				return stats.standardDeviation();
			}
		}

		/**
		 * Return the # of items stored here.
		 * 
		 * @return
		 */
		private int getSize() {
			if(stats == null){
				return 0;
			}
			return (int) stats.sum();
		}

		@Override
		public int compareTo(RosaPivotMetrics o) {
			float me = 0;
			float other = 0;
			if (getSize() > o.getSize()) {
				me+=1.5;
			} 
			
			if (getSize() < o.getSize()) {
				other+=1.5;
			} 
			if (getStandardDeviation() < o.getStandardDeviation()) {
				me += 2.5;
			} 
			if (getStandardDeviation() > o.getStandardDeviation()) {
				other += 2.5;
			} 
			
			if (getDifferentCombinations() > o
					.getDifferentCombinations()) {
				me += 2;
			} 
			if (getDifferentCombinations() < o
					.getDifferentCombinations()) {
				other +=2;
			} 

			if (minRange > o.minRange) {
				me+= 1.5;
			} 
			if (minRange < o.minRange) {
				other+=1.5;
			} 
			
			/*if (getPivotCount() < o.getPivotCount()) {
				other += 1;
			} 
			if (getPivotCount() > o.getPivotCount()) {
				me += 1;
			} */
			
			if(me > other){
				return 1;
			}else if(me < other){
				return -1;
			}else{
				return 0;
			}
			
		}

		public int getPivotCount() {
			if (pivotId == null) {
				return 0;
			}
			return this.pivotId.length;
		}

		public long[] getPivotId() {
			return pivotId;
		}
		
		

		public void setPivotId(long[] pivotId) {
			this.pivotId = pivotId;
		}

		public int getDifferentCombinations() {
			if (ids == null) {
				return 0;
			}
			return ids.size();
		}

		public double getMinRange() {
			return minRange;
		}

		public void setMinRange(double minRange) {
			this.minRange = minRange;
		}

		public String toString() {
			return "[ids: " + Arrays.toString(pivotId) + " minRange: "
					+ minRange + " count: " + getSize() + " ids: ("
					+ ids.size() + ") " + ids + " std.dev: "
					+ this.getStandardDeviation() + "]";

		}

	}

	protected abstract class Dimension {

		protected int pivot;

		public int getPivot() {
			return pivot;
		}

		public void setPivot(int pivot) {
			this.pivot = pivot;
		}

	}

}
