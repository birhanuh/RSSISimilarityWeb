package net.obsearch.pivots.muller2;


import java.util.Arrays;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;



import org.apache.log4j.Logger;



import hep.aida.bin.StaticBin1D;
import cern.colt.list.LongArrayList;
import net.obsearch.Index;
import net.obsearch.OB;
import net.obsearch.asserts.OBAsserts;
import net.obsearch.exception.IllegalIdException;
import net.obsearch.exception.OBException;
import net.obsearch.exception.OBStorageException;
import net.obsearch.exception.PivotsUnavailableException;
import net.obsearch.pivots.AbstractIncrementalPivotSelector;
import net.obsearch.pivots.PivotResult;
import net.obsearch.pivots.Pivotable;

public abstract class AbstractIncrementalMullerRF01Sandia<O extends OB, N>
		extends AbstractIncrementalPivotSelector<O> {

	private static final transient Logger logger = Logger
			.getLogger(AbstractIncrementalMullerRosa.class);
	/**
	 * How many times we are going to repeat the search
	 */
	private int repetitions;
	private int dataSample;
	protected int height;
	private Random r = new Random();

	protected AbstractIncrementalMullerRF01Sandia(Pivotable<O> pivotable,
			int repetitions, int dataSample, int height) {
		super(pivotable);
		this.repetitions = repetitions;
		this.dataSample = dataSample;
		this.height = height;
	}

	
	public PivotResult generatePivots(int pivotCount, LongArrayList elements,
			Index<O> index) throws OBException, IllegalAccessException,
			InstantiationException, OBStorageException,
			PivotsUnavailableException {
		OBAsserts.chkAssert(pivotCount % height == 0,
				"Only multiples of kHeight accepted");
		long[] data = super.select(dataSample, r, elements, index, null);
		List<List<N>> preComputedData = new LinkedList<List<N>>();
		for (int i = 0; i < dataSample; i++) {
			preComputedData.add(new LinkedList<N>());
		}
		
		int i = 0;
		Score best = null;
		List<Long> pivots = new LinkedList<Long>();
		
		List<List<N>>  bestPrecomp = preComputedData;
		while (pivots.size() < pivotCount) {
			best = null;
			i = 0;
			preComputedData = bestPrecomp;
			while (i < repetitions) {
				long[] pivotIds = select(height, r, elements, index, null);
				List<Long> copy = new LinkedList<Long>(pivots);
				for (long l : pivotIds) {
					copy.add(l);
				}

				Score s = calculateScore(pivotIds, data, height, index, copy,
						preComputedData);

				if (best == null) {

					best = s;
					bestPrecomp = clone(preComputedData);
				} else if (s.isBetter(best)) {
					best = s;
					logger.debug(s);
					bestPrecomp = clone(preComputedData);
				} 

				for (List<N> l : preComputedData) {
					// remove the last guys.
					for (int cx = 0; cx < pivotIds.length; cx++) {
						l.remove(l.size() - 1);
					}
				}

				i++;
			}
			logger.debug("Found: " + height + " ^ "
					+ ((pivots.size() + height) / 2) + " :" + best.toString());
			for (long l : best.getIds()) {
				pivots.add(l);
			}

		}
		long[] ids = new long[pivots.size()];
		i = 0;
		for (Long p : pivots) {
			ids[i] = p;
			i++;
		}
		PivotResult res = new PivotResult(ids);
		logger.debug("***" + best.toString());
		/*
		 * for(i = 0; i < 15; i++){ debug(result.getIds(), data, pivotCount
		 * ,index); }
		 */

		return res;
	}
	
	private List<List<N>> clone(List<List<N>> c){
		List<List<N>> preComputedData = new LinkedList<List<N>>();
		for (List<N> l : c){
			LinkedList<N> n = new LinkedList<N>();	
			for(N nl : l){
				n.add(nl);
			}			
			preComputedData.add(n);
		}
		
		
		
		return preComputedData;
	}

	/**
	 * calculates the score of the given pivotIds
	 * 
	 * @param pivotIds
	 *            the selected pivot ids.
	 * @param data
	 *            the data used to validate the pivots
	 * @param pivotCount
	 *            number of pivots to select.
	 * @param index
	 *            the index where we take all the data
	 * @param previousIds
	 *            previously selected pivots.
	 * @return return a score of the current selected pivot set.
	 * @throws IllegalIdException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws OBException
	 */
	protected abstract Score calculateScore(long[] pivotIds, long[] data,
			int pivotCount, Index<O> index, List<Long> previousIds,
			List<List<N>> preData) throws IllegalIdException,
			IllegalAccessException, InstantiationException, OBException;

	/**
	 * Keeps track of statistics related to the pivot set.
	 * 
	 * @author amuller
	 * 
	 */
	protected class Score {
		private StaticBin1D bucketDistribution = new StaticBin1D();
		private float percentageSmallerThan2R;
		private int pivotCount;
		private long[] ids;
		private double minDistance;
		private double interPivotDistance;
		private int dimension; // the dimension where we are.

		private StaticBin1D distances = new StaticBin1D();

		private int uniqueCount;

		public void setMinDistance(double minDistance) {
			this.minDistance = minDistance;
		}

		public Score(int pivotCount, long[] ids) {
			this.pivotCount = pivotCount;
			this.ids = ids;
		}

		public void setDimension(int d) {
			this.dimension = d;
		}

		public void setUniqueCount(int uniq) {
			this.uniqueCount = uniq;
		}

		/**
		 * Adds the size of each of the buckets found.
		 * 
		 * @param v
		 */
		public void addBucketSize(int v) {
			bucketDistribution.add(v);
		}

		public double getInterPivotDistance() {
			return interPivotDistance;
		}

		public void setInterPivotDistance(double interPivotDistance) {
			this.interPivotDistance = interPivotDistance;
		}

		public void setDistances(StaticBin1D stats) {
			this.distances = stats;
		}

		/**
		 * Sets how many elements are at least 2r units away from the next
		 * closest pivot(s).
		 * 
		 * @param p
		 */
		public void setMultipleVisits(float p) {
			this.percentageSmallerThan2R = p;
		}

		/**
		 * True if this (our object) Score is better than other Score
		 * 
		 * @param other
		 *            The other Score
		 * @return True if this Score is better than other Score
		 */
		public boolean isBetter(Score other) {
			/*
			 * double perfect = bucketDistribution.sum() /
			 * bucketDistribution.size(); assert perfect ==
			 * other.bucketDistribution.sum() / other.bucketDistribution.size();
			 * // the closest to the perfect is better double distance =
			 * Math.abs(perfect - bucketDistribution.mean()); double
			 * distanceOther = Math.abs(perfect -
			 * other.bucketDistribution.mean()); float me = 0; float another =
			 * 0;
			 * 
			 * if(bucketDistribution.standardDeviation() <
			 * other.bucketDistribution.standardDeviation()){ me += 2; }else{
			 * another += 2; }
			 * 
			 * 
			 * 
			 * if(percentageSmallerThan2R > other.percentageSmallerThan2R){ me
			 * += 3; }else{ another +=3; }
			 * 
			 * return me > another;
			 */
			// double max = Math.max(distances.max(), other.distances.max());
			// double meStd = (1000 * bucketDistribution.standardDeviation()
			// * (interPivotDistance/ max)) * getNormalizedUniqueCount();
			// double anotherStd = (1000 *
			// other.bucketDistribution.standardDeviation()
			// * (other.interPivotDistance / max)) *
			// other.getNormalizedUniqueCount();
			long total = (long) Math.pow(2, dimension);

			double meStd = (bucketDistribution.standardDeviation())
					+ (-uniqueCount);

			double anotherStd = (other.bucketDistribution.standardDeviation())
					+ (-other.uniqueCount);
			;

			/*
			 * if (Math.abs(bucketDistribution.standardDeviation() -
			 * other.bucketDistribution.standardDeviation()) < 0.1) { return
			 * uniqueCount > other.uniqueCount; } else { return meStd <
			 * anotherStd; }
			 */

			/*
			 * if(uniqueCount == other.uniqueCount){ if( meStd == anotherStd){
			 * return interPivotDistance > other.interPivotDistance; } return
			 * meStd < anotherStd; }else{ return uniqueCount >
			 * other.uniqueCount; }
			 */

			return meStd < anotherStd;
		}

		public double getNormalizedUniqueCount() {
			return uniqueCount / bucketDistribution.sum();
		}

		public String toString() {
			return "STD: " + bucketDistribution.standardDeviation() + "Inter: "
					+ interPivotDistance + "ids: " + Arrays.toString(ids)
					+ " uniq: " + this.uniqueCount;
		}

		public long[] getIds() {
			return ids;
		}
	}

}
