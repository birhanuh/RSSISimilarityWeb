package net.obsearch.pivots.muller2;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import hep.aida.bin.StaticBin1D;
import cern.colt.list.LongArrayList;
import net.obsearch.Index;
import net.obsearch.OB;
import net.obsearch.exception.IllegalIdException;
import net.obsearch.exception.OBException;
import net.obsearch.exception.OBStorageException;
import net.obsearch.exception.PivotsUnavailableException;
import net.obsearch.pivots.AbstractIncrementalPivotSelector;
import net.obsearch.pivots.IncrementalPivotSelector;
import net.obsearch.pivots.PivotResult;
import net.obsearch.pivots.Pivotable;
import net.obsearch.stats.Statistics;

public abstract class AbstractIncrementalMullerRosa<O extends OB> extends
		AbstractIncrementalPivotSelector<O> implements
		IncrementalPivotSelector<O>{
	
	private static final transient Logger logger = Logger
	.getLogger(AbstractIncrementalMullerRosa.class);
	/**
	 * How many times we are going to repeat the search
	 */
	private int repetitions;
	private int dataSample;
	private Random r = new Random();
	
	protected AbstractIncrementalMullerRosa(Pivotable<O> pivotable, int repetitions, int dataSample){
		super(pivotable);
		this.repetitions = repetitions;
		this.dataSample = dataSample;
	}

	
	public PivotResult generatePivots(int pivotCount, LongArrayList elements,
			Index<O> index) throws OBException, IllegalAccessException,
			InstantiationException, OBStorageException,
			PivotsUnavailableException {
		
		long[] data = super.select(dataSample, r, elements, index, null);
		int i = 0;
		
		Score result = null;
		while(i < repetitions){
			long[] pivotIds = select(pivotCount, r, elements, index, null);
			
			Score s = calculateScore(pivotIds, data, pivotCount, index);
			
			if(result == null){
				
				result = s;				
			}else if(s.isBetter(result)){
				result = s;	
				logger.debug(s);
			}
			
			i++;
		}
		PivotResult res = new PivotResult(result.getIds());
		logger.debug("***" + result.toString());
		
		/*for(i = 0; i < 15; i++){
			debug(result.getIds(), data, pivotCount ,index);
		}*/
		
		displaySelection(result, data, index);
        
		return res;
	}
	
	protected abstract void debug(long[] pivotIds, long[] data, int pivotCount, Index<O> index) throws IllegalIdException, IllegalAccessException, InstantiationException, OBException;
	protected abstract Score calculateScore(long[] pivotIds, long[] data, int pivotCount, Index<O> index) throws IllegalIdException, IllegalAccessException, InstantiationException, OBException;
	
	protected abstract void displaySelection(Score s, long[] data,Index<O> index ) throws IllegalIdException, IllegalAccessException, InstantiationException, OBException;
	/**
	 * Keeps track of statistics related to the pivot set.
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
		// distance to the hyperplane
		private StaticBin1D distanceToHyperplane;
		
		public void setMinDistance(double minDistance) {
			this.minDistance = minDistance;
		}
		public Score(int pivotCount, long[] ids){
			this.pivotCount = pivotCount;
			this.ids = ids;
		}
		
		public void setDistanceToHyperplane(StaticBin1D s){
			this.distanceToHyperplane = s;
		}
		/**
		 * Adds the size of each of the buckets found.
		 * @param v
		 */
		public void addBucketSize(int v){
			bucketDistribution.add(v);
		}
		
		
		
		public double getInterPivotDistance() {
			return interPivotDistance;
		}
		public void setInterPivotDistance(double interPivotDistance) {
			this.interPivotDistance = interPivotDistance;
		}
		/**
		 * Sets how many elements are at least 2r units
		 * away from the next closest pivot(s).
		 * @param p
		 */
		public void setMultipleVisits(float p){
			this.percentageSmallerThan2R = p;
		}
		
		/**
		 * True if this (our object) Score is better than other Score
		 * @param other The other Score
		 * @return True if this Score is better than other Score
		 */
		public boolean isBetter(Score other){
			/*double perfect = bucketDistribution.sum() / bucketDistribution.size();
			assert perfect == other.bucketDistribution.sum() / other.bucketDistribution.size();
			// the closest to the perfect is better
			double distance = Math.abs(perfect - bucketDistribution.mean());
			double distanceOther = Math.abs(perfect - other.bucketDistribution.mean());
			float me = 0;
			float another = 0;
			
			if(bucketDistribution.standardDeviation() < other.bucketDistribution.standardDeviation()){
				me += 2;
			}else{
				another += 2;
			}
			
			
			
			if(percentageSmallerThan2R > other.percentageSmallerThan2R){
				me += 3;
			}else{
				another +=3;
			}
			
			return  me > another;*/
			
			double meStd = bucketDistribution.standardDeviation(); //* interPivotDistance; // this was active during scala approx.
			double anotherStd = other.bucketDistribution.standardDeviation(); //* other.interPivotDistance;
			if( Math.abs(bucketDistribution.standardDeviation() - other.bucketDistribution.standardDeviation()) < ((double)dataSample * 0.001)){
				//return   this.distanceToHyperplane.mean() > other.distanceToHyperplane.mean();   //interPivotDistance >  other.interPivotDistance;
				return interPivotDistance >  other.interPivotDistance;
			}else{
				return meStd < anotherStd;
			}
			
			/*double max = Math.max(distanceToHyperplane.max() , other.distanceToHyperplane.max());
			double stdNorm = bucketDistribution.standardDeviation() / dataSample;
			double otherStdNorm = other.bucketDistribution.standardDeviation() / dataSample;
			double disH = 1 - (distanceToHyperplane.mean() / max);
			double otherDisH = 1 - (other.distanceToHyperplane.mean() / max);
			return (bucketDistribution.standardDeviation() * disH) < (other.bucketDistribution.standardDeviation() * otherDisH);
			*/
		}
		
		public String toString(){
			
			return "STD: " + bucketDistribution.standardDeviation() + "Hyper distance" + distanceToHyperplane.mean() + "ids: " + Arrays.toString(ids);
		}
		
		
		
		public long[] getIds(){
			return ids;
		}
	}

}
