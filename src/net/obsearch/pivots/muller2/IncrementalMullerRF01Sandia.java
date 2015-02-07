package net.obsearch.pivots.muller2;

import hep.aida.bin.MightyStaticBin1D;
import hep.aida.bin.QuantileBin1D;
import hep.aida.bin.StaticBin1D;


import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


import weka.core.Instances;


import cern.colt.Arrays;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.LongArrayList;
import cern.jet.random.engine.DRand;

import net.obsearch.Index;
import net.obsearch.asserts.OBAsserts;
import net.obsearch.dimension.DimensionShort;
import net.obsearch.exception.IllegalIdException;
import net.obsearch.exception.OBException;
import net.obsearch.exception.OBStorageException;
import net.obsearch.index.utils.medians.MedianCalculatorShort;
import net.obsearch.ob.OBShort;
import net.obsearch.pivots.AcceptAll;
import net.obsearch.pivots.Pivotable;
import net.obsearch.pivots.bustos.impl.IncrementalBustosNavarroChavezShort;
import net.obsearch.result.OBPriorityQueueInvertedShort;
import net.obsearch.result.OBPriorityQueueShort;
import net.obsearch.result.OBResultInvertedShort;
import net.obsearch.result.OBResultShort;
import net.obsearch.utils.Pair;

public class IncrementalMullerRF01Sandia<O extends OBShort> extends
		AbstractIncrementalMullerRF01Sandia<O, Short> {
	
	private static transient final Logger logger = Logger
    .getLogger(IncrementalMullerRosaShort.class);
	
	private boolean debug = false;

	private short range;
	public IncrementalMullerRF01Sandia(Pivotable<O> pivotable,
			int repetitions, int dataSample, short targetRange, int height) {
		super(pivotable, repetitions, dataSample, height);		
		this.range = targetRange; 
	}
	
	/**
	 * Return the holder for the given tuple
	 * @param tuple The tuple
	 * @return A list of short holders
	 */
	private List<ShortHolder> getHolder(short[] tuple){
		ArrayList<ShortHolder> holder =  new ArrayList<ShortHolder>(tuple.length);
		int i = 0;
		while(i < tuple.length){
			holder.add(new ShortHolder(i, tuple[i]));
			i++;
		}
		Collections.sort(holder);
		return holder;
	}
	
	
	
	
	

	private class Dimensionelo {
		private QuantileBin1D[] medians;
		private ArrayList<Short>[] items;
		private int count;
		private int total;
		public Dimensionelo(int size, int toAdd){
			medians = new QuantileBin1D[size];
			items = new ArrayList[size];
			count =0;
			total = toAdd;
			int i = 0;
			while(i < size){
				medians[i] = new QuantileBin1D(false, toAdd, 0.0f, 0.0f, 10000, new DRand() );
				items[i] = new ArrayList<Short>();
				i++;
			}
		}
		
		public void add(short[] item) throws OBException{
			OBAsserts.chkAssert(count < total, "cannot add more than " + total);
			assert medians.length == item.length;
			int i = 0;
			for(short s : item){
				medians[i].add(s);
				items[i].add(s);
				i++;
			}
			count++;
		}
		
		public int getSmallestSTDDevDim(){
			int i = 0;
			double current = Double.POSITIVE_INFINITY;
			int res = -1;
			for(QuantileBin1D m : medians){
				if(m.standardDeviation() < current){
					res = i;
					current = m.standardDeviation();
				}
				i++;
			}
			return res;
		}
		
		public int getLargestSTDDevDim(){
			int i = 0;
			double current = Double.NEGATIVE_INFINITY;
			int res = -1;
			for(QuantileBin1D m : medians){
				if(m.standardDeviation() > current){
					res = i;
					current = m.standardDeviation();
				}
				i++;
			}
			return res;
		}
		
		
		/**
		 * Get the pivots of the dimensions with less spread
		 * @return
		 * @throws OBException 
		 */
		public Pair<short[], short[]> getNewPivots() throws OBException{
			short[] res1 = center();
			short[] res2 = center();
			int smallest = getLargestSTDDevDim();
			Pair<List<Short>, List<Short>> split = split(items[smallest]);
			// get the median of each split.
			short medianA = getMedian(split.getA());
			short medianB = getMedian(split.getB());
			//res1[smallest] = (short)this.medians[smallest].min();
			//res2[smallest] = (short)this.medians[smallest].max();
			res1[smallest] = medianA;
			res2[smallest] = medianB;
			return new Pair<short[], short[]>(res1,res2);
		}
		
		private short[] toArray(ArrayList<Short> arr){
			short[] res = new short[arr.size()];
			int i = 0;
			for(short x : arr){
				res[i] = x;
				i++;
			}
			return res;
		}
		
		/**
		 * Split in two a collection
		 * @param in
		 * @return
		 */
		public Pair<List<Short>, List<Short>> split(ArrayList<Short> in){
			List<Short> a = (List<Short>) in.subList(0, in.size()/2);
			List<Short> b = (List<Short>) in.subList(in.size()/2, in.size());
			return new Pair<List<Short>, List<Short>>(a,b);
		}
		
		public short[] center() throws OBException{
			short[] res = new short[medians.length];
			int i = 0;
			for(QuantileBin1D m : medians){
				OBAsserts.chkAssert(m.median() <= Short.MAX_VALUE, "precision error");
				Collections.sort(items[i]);
				short median = (short) m.median();
				assert median == getMedian(items[i]);
				res[i] = median;
				i++;
			}
			return res;
		}
		
		private short getMedian(List<Short> array){
			return array.get(array.size() / 2);
		}
	}
	
	private double[] toArray(List<Double> list){
		double[] res = new double[list.size()];
		int i = 0;
		for(double d : list){
			res[i] = d;
			i++;
		}
		return res;
	}

	
	protected Score calculateScore(
			long[] pivotIds, long[] data, int pivotCount, Index<O> index, List<Long> selected, List<List<Short>> preComputedData) throws IllegalIdException, IllegalAccessException, InstantiationException, OBException {
		int[] counts = new int[pivotCount];
		int withinRange = 0;
		StaticBin1D stats = new StaticBin1D();
		// also add the objects here to the pre-computed list.
		Iterator<List<Short>> it = preComputedData.iterator();
		assert data.length == preComputedData.size();
		for(long o : data){
			short[] tuple = DimensionShort.getPrimitiveTuple(pivotIds, o, index);
			List<Short> l = it.next();
			for(short t : tuple){
				l.add(t);
			}
			List<ShortHolder> holder = getHolder(tuple);
			counts[holder.get(0).position]++; 
			short value = (short)Math.abs(holder.get(0).value - holder.get(1).value);
			if((value) >= 2 * range ){
				withinRange++;
				
			}
			stats.add(value);
		}
		Score res = new Score(pivotCount, pivotIds);
		// fill the stuff.
		for(int c : counts){
			res.addBucketSize(c);
		}
		res.setMultipleVisits(1f -  (float)withinRange/ (float)data.length);
		// calculate the avg distance between the pivots.
		List<O> pivs = getPivots(pivotIds, index); 
		StaticBin1D distances = new StaticBin1D();
		for(O p1 : pivs){
			for(O p2 : pivs){
				if(!p1.equals(p2)){
					distances.add(p1.distance(p2));
				}
			}	
		}
		res.setMinDistance(stats.mean());
		res.setInterPivotDistance(distances.mean());
		res.setDistances(distances);
		res.setDimension((int) (selected.size() / height));
		//debug(pivotIds, data, pivotCount, index,res);
		
		// now we should calculate the # of different buckets generated so far.
		List<O> fullPivots = getPivots(selected, index);
		OBShort[] full = convert(fullPivots);
		HashSet<String> uniqueIds = new HashSet<String>();
		for(List<Short> tuple : preComputedData){
			// add the 
			uniqueIds.add(generateId(tuple));
									
		}
		res.setUniqueCount(uniqueIds.size());
		return res;
	}
	
	private OBShort[] convert(List<O> list){
		OBShort[] result = (OBShort[]) java.lang.reflect.Array.newInstance(OBShort.class, list.size());
		int i = 0;
		for(O o : list){
			result[i] = o;
			i++;
		}
		return result;
	}
	
	private String generateId(List<Short> tuple){
		StringBuilder res = new StringBuilder();
		int cx = 0;
		short min = Short.MAX_VALUE;
		int idx = -1;
		for(short t : tuple){
			if(cx == height){// reset the counter
				cx = 0;
				min = Short.MAX_VALUE;
				res.append(idx);
				idx = -1;
				
			}
			if(t < min){
				min = t;
				idx = cx;
			}
			
			cx++; // always increase
		}
		res.append(idx); // write down the last guy
		String result = res.toString();
		return result;
	}
	
	private List<O> getPivots(long[] pivots, Index<O> index) throws IllegalIdException, IllegalAccessException, InstantiationException, OBException{
		List<O> result = new LinkedList<O>();
		for(long l : pivots){
			result.add(index.getObject(l));			
		}
		return result;
	}
	
	private List<O> getPivots(List<Long>pivots, Index<O> index) throws IllegalIdException, IllegalAccessException, InstantiationException, OBException{
		List<O> result = new LinkedList<O>();
		for(long l : pivots){
			result.add(index.getObject(l));			
		}
		return result;
	}
	
	/*protected long[] select(int k, Random r, LongArrayList source,
			Index<O> index, LongArrayList excludes) throws IllegalIdException, OBException, IllegalAccessException, InstantiationException {
		return DimensionShort.select(k, r, source, (Index<OBShort>)index, excludes, (short)100);
	}*/
	
	private class ShortHolder implements Comparable<ShortHolder>{
		short value;
		int position;
		public ShortHolder(int position, short value) {
			super();
			this.position = position;
			this.value = value;
		}
		public short getValue() {
			return value;
		}
		public void setValue(short value) {
			this.value = value;
		}
		public int getPosition() {
			return position;
		}
		public void setPosition(int position) {
			this.position = position;
		}
		@Override
		public int compareTo(ShortHolder o) {
			if(value < o.value){
				return -1;
			}else if(value == o.value){
				return 0;
			}else{
				return 1;
			}
		}
		
		
		
	}

	

}
