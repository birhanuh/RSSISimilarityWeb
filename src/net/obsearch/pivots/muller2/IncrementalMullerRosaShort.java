package net.obsearch.pivots.muller2;

import hep.aida.bin.MightyStaticBin1D;
import hep.aida.bin.QuantileBin1D;
import hep.aida.bin.StaticBin1D;


import java.awt.RenderingHints;
import java.util.ArrayList;
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
import net.obsearch.stats.Statistics;
import net.obsearch.utils.Pair;

public class IncrementalMullerRosaShort<O extends OBShort> extends
		AbstractIncrementalMullerRosa<O> {
	
	private static transient final Logger logger = Logger
    .getLogger(IncrementalMullerRosaShort.class);
	
	private boolean debug = false;

	private short range;
	public IncrementalMullerRosaShort(Pivotable<O> pivotable,
			int repetitions, int dataSample, short targetRange) {
		super(pivotable, repetitions, dataSample);		
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
	
	protected void displaySelection(Score s, long[] data, Index<O> index) throws IllegalIdException, IllegalAccessException, InstantiationException, OBException{
		if(debug){
			Statistics st = new Statistics();
			HashSet<Long> viewed = new HashSet<Long>();
			for(long id : s.getIds()){
				viewed.add(id);
			}
			for(long id : data){
				if(viewed.contains(id)){
					continue;
				}else{
					viewed.add(id);
				}
				logger.debug("<<< Object: " + index.getObject(id).toString());
				O obj = index.getObject(id);
				O pivotA = index.getObject(s.getIds()[0]);
				O pivotB = index.getObject(s.getIds()[1]);
				short distA = obj.distance(pivotA);
				short distB = obj.distance(pivotB);
				if(distA <= distB){
					logger.debug("Pivot: " + pivotA.toString() + " dist " + distA);
					logger.debug("Not Pivot: " + pivotB.toString()+ " dist " + distB);
					st.incExtra("A");
				}else{
					logger.debug("Pivot: " + pivotB.toString()+ " dist " + distB);
					logger.debug("Not Pivot: " + pivotA.toString()+ " dist " + distA );
					st.incExtra("B");
				}
				
			}
			logger.debug("Dist: " + st.toString());
		}
	}
	
	protected void debug(long[] pivotIds, long[] data, int pivotCount, Index<O> index) throws IllegalIdException, IllegalAccessException, InstantiationException, OBException{
		if(debug){
			
			// create a new projection for the points.
			
			IncrementalBustosNavarroChavezShort<O> sel = new IncrementalBustosNavarroChavezShort<O>(new AcceptAll(),
		              100, 100);
			long[] displayIds = null;
			try{
				displayIds = sel.generatePivots(2, index).getPivotIds();
				//displayIds = super.select(2, new Random(), null, index, new LongArrayList(pivotIds));
			}catch(Exception e){
				throw new OBException(e);
			}
			Random r = new Random();
			int[] counts = new int[pivotCount];
			
			
			XYSeries[] plotSeries = new XYSeries[pivotIds.length];
			for(int i = 0; i < pivotIds.length;i++){
				plotSeries[i] = new XYSeries("p" + i);
			}
			Dimensionelo dim = new Dimensionelo(displayIds.length, data.length);
			Painterelo paint = new Painterelo(displayIds.length,  data.length );
			for(long o : data){
				short[] tuple = DimensionShort.getPrimitiveTuple(pivotIds, o, index);
				List<ShortHolder> holder = getHolder(tuple);
				short[] projection = DimensionShort.getPrimitiveTuple(displayIds, o, index);
				dim.add(projection);
				paint.add(projection);
				plotSeries[holder.get(0).position ].add(projection[0], projection[1]);
				//
			}
			XYSeries[] centers = new XYSeries[pivotIds.length];
			for(int i = 0; i < pivotIds.length;i++){
				centers[i] = new XYSeries("center" + i);
			}
			int i = 0;
			
			for(long o : pivotIds){				
				short[] projection = DimensionShort.getPrimitiveTuple(displayIds, o, index);
				
				centers[i].add(projection[0], projection[1]);
				i++;
			}
			XYSeriesCollection collection = new XYSeriesCollection();
			for(XYSeries s : centers){
				collection.addSeries(s);
			}
			
			for(XYSeries s : plotSeries){
				collection.addSeries(s);
			}
			
			XYSeries pivots = new XYSeries("Yay");
			// estos pivots basicamente estan muy cerca de los pivotes optimos
			Pair<short[],short[]> p = dim.getNewPivots();
			logger.info("OBS: " + Arrays.toString( p.getA()) + " obs2 " + Arrays.toString( p.getB()));
			pivots.add(p.getA()[0], p.getA()[1]);
			pivots.add(p.getB()[0], p.getB()[1]);
				collection.addSeries(pivots);		
			  final NumberAxis domainAxis = new NumberAxis("X");
		        //domainAxis.setAutoRange(true);
		        final NumberAxis rangeAxis = new NumberAxis("Y");
		        //rangeAxis.setAutoRange(true);
		        
		        StandardXYItemRenderer ren = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES);
			XYPlot plot = new XYPlot(collection, domainAxis, rangeAxis, ren);
			//plot.setForegroundAlpha(0.3f);
	        final JFreeChart chart = new JFreeChart("Fast Scatter Plot", plot);
//	        chart.setLegend(null);

	        // force aliasing of the rendered content..
	        chart.getRenderingHints().put
	            (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	        final ChartPanel panel = new ChartPanel(chart, true);
	        
			JFrame frame = new JFrame("Debug");
	        frame.getContentPane().add(panel);
	        frame.setVisible(true);	
	        frame.pack();
	        JOptionPane.showMessageDialog(frame, "Make a pause...");
	        frame.setVisible(false);
	        //frame.dispose();
	        // 	        paint with painterelo
	        paint.visualPaint();
		}
	}
	
	/**
	 * Paint a circle from the center of the space.
	 */	
	private class Painterelo {
		Dimensionelo hey;
		List<short[]> data;
		public Painterelo(int size, int toAdd){
			hey = new Dimensionelo(size, toAdd);
			data = new LinkedList<short[]>();
		}

		
		
		private void add(short[] item) throws OBException {
			OBAsserts.chkAssert(item.length == 2, "Painterelo Can only accept dimensions of 2");
			hey.add(item);
			data.add(item);
		}
		
		/**
		 * Fill in instances!
		 * @throws OBException 
		 * @throws IllegalAccessException 
		 * @throws InstantiationException 
		 */
		public void paint(Instances ins) throws OBException, InstantiationException, IllegalAccessException{
			short[] center  = hey.center();
			OBPriorityQueueInvertedShort<short[]> q = new OBPriorityQueueInvertedShort<short[]>(data.size());

			for(short[] d : data){
				short distance = DimensionShort.lInfinite(d, center);
				q.add(-1, d, distance);
			}
			
			XYSeries p1 = new XYSeries("P1");
			XYSeries p2 = new XYSeries("P2");
			int i = 0;
			Iterator<OBResultInvertedShort<short[]>> it =   q.iterator();
			
			while(it.hasNext() ){
				assert it.hasNext();
				OBResultInvertedShort<short[]> o = it.next();
				// fill in the attrs.
				if(i < data.size() / 2){
					p1.add(o.getObject()[0], o.getObject()[1]);
				}else{
					p2.add(o.getObject()[0], o.getObject()[1]);
				}
				i++;
			}
		}
		
		
		
		/**
		 * Make an image of the data.
		 * @throws OBException 
		 * @throws IllegalAccessException 
		 * @throws InstantiationException 
		 */
		public void visualPaint() throws OBException, InstantiationException, IllegalAccessException{
			short[] center  = hey.center();
			OBPriorityQueueInvertedShort<short[]> q = new OBPriorityQueueInvertedShort<short[]>(data.size());

			for(short[] d : data){
				short distance = DimensionShort.euclidean (d, center);
				q.add(-1, d, distance);
			}
			
			XYSeries p1 = new XYSeries("P1");
			XYSeries p2 = new XYSeries("P2");
			int i = 0;
			Iterator<OBResultInvertedShort<short[]>> it =   q.iterator();
			
			for(OBResultInvertedShort<short[]> o : q.getSortedElements() ){				
				if(i < data.size() / 2){
					p1.add(o.getObject()[0], o.getObject()[1]);
				}else{
					p2.add(o.getObject()[0], o.getObject()[1]);
				}
				i++;
			}
			
			XYSeriesCollection collection = new XYSeriesCollection();
			collection.addSeries(p1);
			collection.addSeries(p2);
			  final NumberAxis domainAxis = new NumberAxis("X");
		        //domainAxis.setAutoRange(true);
		        final NumberAxis rangeAxis = new NumberAxis("Y");
		     //StandardXYItemRenderer ren = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES);
		     XYDotRenderer ren = new XYDotRenderer();
				XYPlot plot = new XYPlot(collection, domainAxis, rangeAxis, ren);
				plot.setForegroundAlpha(0.3f);
		        final JFreeChart chart = new JFreeChart("Fast Scatter Plot", plot);
//		        chart.setLegend(null);

		        // force aliasing of the rendered content..
		        chart.getRenderingHints().put
		            (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		        final ChartPanel panel = new ChartPanel(chart, true);
		        panel.setMouseZoomable(true);
		        panel.setFillZoomRectangle(true);
		        //panel.setZoomInFactor(1.0);
				JFrame frame = new JFrame("Debug");
		        frame.getContentPane().add(panel);
		        frame.setVisible(true);	
		        frame.pack();
		        JOptionPane.showMessageDialog(frame, "Make a pause...");
		        //frame.setVisible(false);
		        frame.dispose();
		}
		
		
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
	
	/*
	protected void debug(long[] pivotIds, long[] data, int pivotCount, Index<O> index) throws IllegalIdException, IllegalAccessException, InstantiationException, OBException{
		if(debug){
			
			// create a new projection for the points.
			
			//IncrementalBustosNavarroChavezShort<O> sel = new IncrementalBustosNavarroChavezShort<O>(new AcceptAll(),
		    //          1000, 1000);
			long[] displayIds = null;
			try{
				//displayIds = sel.generatePivots(2, index).getPivotIds();
				displayIds = super.select(3, new Random(), null, index, new LongArrayList(pivotIds));
			}catch(Exception e){
				throw new OBException(e);
			}
			
			int[] counts = new int[pivotCount];
			
			
			List<Double>[] x = new LinkedList[pivotIds.length];
			List<Double>[] y = new LinkedList[pivotIds.length];
			List<Double>[] z = new LinkedList[pivotIds.length];
			for(int i = 0; i < pivotIds.length;i++){
				x[i] = new LinkedList<Double>();
				y[i] = new LinkedList<Double>();
				z[i] = new LinkedList<Double>();
			}
			for(long o : data){
				short[] tuple = DimensionShort.getPrimitiveTuple(pivotIds, o, index);
				List<ShortHolder> holder = getHolder(tuple);
				short[] projection = DimensionShort.getPrimitiveTuple(displayIds, o, index);
				x[holder.get(0).position].add((double)projection[0]);
				y[holder.get(0).position].add((double)projection[1]);
				z[holder.get(0).position].add((double)projection[2]);				
			}
			
			List<Double>[] xp = new LinkedList[pivotIds.length];
			List<Double>[] yp = new LinkedList[pivotIds.length];
			List<Double>[] zp = new LinkedList[pivotIds.length];
			for(int i = 0; i < pivotIds.length;i++){
				xp[i] = new LinkedList<Double>();
				yp[i] = new LinkedList<Double>();
				zp[i] = new LinkedList<Double>();
			}
			int i = 0;
			for(long o : pivotIds){				
				short[] projection = DimensionShort.getPrimitiveTuple(displayIds, o, index);
				xp[i].add((double)projection[0]);
				yp[i].add((double)projection[1]);
				zp[i].add((double)projection[2]);	
				i++;
			}
			Plot3DPanel plot = new Plot3DPanel("Data");

			for(i = 0; i < pivotIds.length ; i++){
				plot.addScatterPlot("p" + i, toArray(x[i]), toArray(y[i]), toArray(z[i]));
				plot.addScatterPlot("c" + i, toArray(xp[i]), toArray(yp[i]), toArray(zp[i]));
			}
			
			
			
			JFrame frame = new JFrame("Debug");
	        frame.getContentPane().add(plot);
	        frame.setVisible(true);	
	        frame.pack();
	        JOptionPane.showMessageDialog(frame, "Make a pause...");

	        frame.setVisible(false);
		}
	}*/
	
	
	
	
	protected Score calculateScore(
			long[] pivotIds, long[] data, int pivotCount, Index<O> index) throws IllegalIdException, IllegalAccessException, InstantiationException, OBException {
		int[] counts = new int[pivotCount];
		int withinRange = 0;
		StaticBin1D stats = new StaticBin1D();
		StaticBin1D distanceToHyperplane = new StaticBin1D();
		OBAsserts.chkAssert(pivotCount == 2, "only kHeight==2 supported");
		for(long o : data){
			short[] tuple = DimensionShort.getPrimitiveTuple(pivotIds, o, index);
			List<ShortHolder> holder = getHolder(tuple);
			counts[holder.get(0).position]++; 
			short value = (short)Math.abs(holder.get(0).value - holder.get(1).value);
			if((value) >= 2 * range ){
				withinRange++;
				
			}
			distanceToHyperplane.add( Math.max(value / 2, 0)); // calculate distance to hyperplane
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
		//debug(pivotIds, data, pivotCount, index,res);
		res.setDistanceToHyperplane(distanceToHyperplane);
		return res;
	}
	
	private List<O> getPivots(long[] pivots, Index<O> index) throws IllegalIdException, IllegalAccessException, InstantiationException, OBException{
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
