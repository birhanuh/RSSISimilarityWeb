package net.obsearch.storage.bdb;
   
   import hep.aida.bin.StaticBin1D;
   
   import java.nio.ByteBuffer;
   import java.util.Iterator;
   import java.util.NoSuchElementException;
   import java.util.Arrays;
   
  import net.obsearch.Status;
  import net.obsearch.exception.OBException;
  import net.obsearch.exception.OBStorageException;
  import net.obsearch.index.utils.ByteArrayComparator;
  import net.obsearch.storage.CloseIterator;
  import net.obsearch.storage.OBStore;
  import net.obsearch.storage.Tuple;
  import net.obsearch.storage.TupleBytes;
  import net.obsearch.utils.bytes.ByteConversion;
  import net.obsearch.storage.OBStoreFactory;
  
  
  
  import com.sleepycat.je.Cursor;
  import com.sleepycat.je.CursorConfig;
  import com.sleepycat.je.Database;
  import com.sleepycat.je.DatabaseEntry;
  import com.sleepycat.je.DatabaseException;
  import com.sleepycat.je.LockMode;
  import com.sleepycat.je.OperationStatus;
  import com.sleepycat.je.Sequence;
  import com.sleepycat.je.SequenceConfig;
  import net.obsearch.asserts.OBAsserts;
  
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
   * BDBOBStore is a storage abstraction for Berkeley DB. It is designed to work
   * on byte array keys storing byte array values.
   * 
   * @author Arnoldo Jose Muller Molina
   */
  
  public abstract class AbstractBDBOBStoreJe<T extends Tuple> implements OBStore<T> {
  
  	private static ByteArrayComparator comp = new ByteArrayComparator();
  
  	protected StaticBin1D stats = new StaticBin1D();
  
  	/**
  	 * Berkeley DB database.
  	 */
  	protected Database db;
  
  	/**
  	 * Database for sequences.
  	 */
  	protected Database sequence;
  
  	/**
  	 * Sequence counter
  	 */
  	protected Sequence counter;
  
  	/**
  	 * Name of the database.
  	 */
  	private String name;
  
  	/**
  	 * If this storage system accepts duplicates or not.
  	 */
  	private boolean duplicates;
  
  	/**
  	 * Factory of this storage device.
  	 */
  	private OBStoreFactory fact;
  
  	/**
  	 * Builds a new Storage system by receiving a Berkeley DB database.
  	 * 
  	 * @param db
 	 *            The database to be stored.
 	 * @param name
 	 *            Name of the database.
 	 * @param sequences
 	 *            Database used to store sequences.
 	 * @throws DatabaseException
 	 *             if something goes wrong with the database.
 	 */
 	public AbstractBDBOBStoreJe(String name, Database db, Database sequences, OBStoreFactory fact, boolean duplicates)
 			throws DatabaseException {
 		this.db = db;
 		this.name = name;
 		this.duplicates = duplicates;
 		this.sequence = sequences;
 		this.fact =fact;
 		// initialize sequences
 		SequenceConfig config = new SequenceConfig();
 		config.setAllowCreate(true);
 		DatabaseEntry key = new DatabaseEntry((name + "_seq").getBytes());
 		if (!duplicates) {
 			this.counter = sequence.openSequence(null, key, config);
 		}
 	}
 
 	public OBStoreFactory getFactory(){
 			return fact;
 	}
 
 	public void close() throws OBStorageException {
 			try {
 			db.close();
 					if(sequence != null){
 							sequence.close();
 					}
 		} catch (DatabaseException d) {
 			throw new OBStorageException(d);
 		}
 	}
 	
 	
 
 	public net.obsearch.OperationStatus delete(byte[] key)
 			throws OBStorageException {
 		net.obsearch.OperationStatus r = new net.obsearch.OperationStatus();
 		try {
 			OperationStatus res = db.delete(null, new DatabaseEntry(key));
 			if (res.NOTFOUND == res) {
 				r.setStatus(Status.NOT_EXISTS);
 			} else if (res.SUCCESS == res) {
 				r.setStatus(Status.OK);
 			} else {
 				assert false;
 			}
 		} catch (Exception e) {
 			throw new OBStorageException(e);
 		}
 		return r;
 	}
 
 	public void deleteAll() throws OBStorageException {
 		try {
 			db.getEnvironment().truncateDatabase(null, name,
 					false);
 		} catch (Exception d) {
 			//throw new OBStorageException(d);
 			d.printStackTrace();
 		}
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public byte[] getValue(byte[] key) throws IllegalArgumentException,
 			OBStorageException {
 		if (duplicates) {
 			throw new IllegalArgumentException();
 		}
 		DatabaseEntry search = new DatabaseEntry(key);
 		DatabaseEntry value = new DatabaseEntry();
 		try {
       
 
 			OperationStatus res = db.get(null, search, value, LockMode.READ_UNCOMMITTED 
 );
 
 			if (res == OperationStatus.SUCCESS) {
 				if (this.stats != null) {
 					stats.add(value.getData().length);
 				}
 				return value.getData();
 			} else {
 				return null;
 			}
 		} catch (DatabaseException e) {
 			throw new OBStorageException(e);
 		}
 	}
 
 	public net.obsearch.OperationStatus put(byte[] key, byte[] value)
 			throws OBStorageException {
 
 		DatabaseEntry k = new DatabaseEntry(key);
 		
 		net.obsearch.OperationStatus res = new net.obsearch.OperationStatus();
 		try {
 				DatabaseEntry v = new DatabaseEntry(value);
 			OperationStatus r = db.put(null, k, v);
 			if (r == OperationStatus.SUCCESS) {
 				res.setStatus(Status.OK);
 			} // Result() is always initialized with error.
 		} catch (DatabaseException e) {
 			throw new OBStorageException(e);
 		}
 		return res;
 	}
 
 
 	public net.obsearch.OperationStatus putIfNew(byte[] key, byte[] value)
 			throws OBStorageException {
 
 		DatabaseEntry k = new DatabaseEntry(key);
 		
 		net.obsearch.OperationStatus res = new net.obsearch.OperationStatus();
 		try {
 				DatabaseEntry v = new DatabaseEntry(value);
 			OperationStatus r = db.putNoOverwrite(null, k, v);
 			if (r == OperationStatus.SUCCESS) {
 				res.setStatus(Status.OK);
 			}else if (r == OperationStatus.KEYEXIST) {
 				res.setStatus(Status.EXISTS);	
 			}
 		} catch (DatabaseException e) {
 			throw new OBStorageException(e);
 		}
 		return res;
 	}
 
 
 	public boolean allowsDuplicatedData() {
 		return duplicates;
 	}
 
 	/**
 	 * Transform Bytes in a format that can be used by the 
 	 * underlying index.
 	 * @param in Input byte array
 	 * @return transformed bytes ready to be sorted.`
 	 */
 	public CloseIterator<byte[]> processAllKeys() throws OBStorageException{
 			return null;
 	}
   
   public byte[] prepareBytes(byte[] in){
 			byte[] res = new byte[in.length];
 			int i = 0;
 			while(i < res.length){
 					res[i] = (byte) (in[i] ^ 0x80);
 					i++;
 			}
 			return res;
 	}
 	
 	public CloseIterator<TupleBytes> processRange(byte[] low, byte[] high)
 	throws OBStorageException{
     	return new ByteArrayIterator(low,high,false, false,true);
     }
 
 	public CloseIterator<TupleBytes> processRangeNoDup(byte[] low, byte[] high)
 	throws OBStorageException{
     	return new ByteArrayIterator(low,high,false, false,false);
     }
 	
 	public CloseIterator<TupleBytes> processRangeReverse(byte[] low, byte[] high)
 	throws OBStorageException{
     	return new ByteArrayIterator(low,high,false, true, true);
     }
 	
 		public CloseIterator<TupleBytes> processRangeReverseNoDup(byte[] low, byte[] high)
 	throws OBStorageException{
 				return new ByteArrayIterator(low,high,false, true,false);
     }
 
 	/**
 	 * Base class used to iterate over cursors.
 	 * 
 	 * @param <O>
 	 *            The type of tuple that will be returned by the iterator.
 	 */
 	protected abstract class CursorIterator<T> implements CloseIterator<T> {
 
 		protected Cursor cursor;
 
 		private boolean cursorClosed = false;
 
 		protected DatabaseEntry keyEntry = new DatabaseEntry();
 
 		protected DatabaseEntry dataEntry = new DatabaseEntry();
 
 		/**
 		 * Previous key entry
 		 */
 		protected DatabaseEntry prevKeyEntry = null;
 
 		/**
 		 * Previous data entry
 		 */
 		protected DatabaseEntry prevDataEntry = null;
 
 		protected OperationStatus retVal;
 
 		private T next = null;
 
 		private byte[] min;
 
 		private byte[] max;
 
 		private byte[] current;
 
 		private boolean full;
 
 		private boolean dups;
 
 		/**
 		 * If this iterator goes backwards.
 		 */
 		private boolean backwardsMode;
 
 		/**
 		 * Creates a cursor iterator in full mode.
 		 * 
 		 * @throws OBStorageException
 		 */
 		protected CursorIterator() throws OBStorageException {
 				this(null, null, true, false,true);
 		}
 
 		public CursorIterator(byte[] min, byte[] max) throws OBStorageException {
 				this(min, max, false, false, true);
 		}
 
 		protected CursorIterator(byte[] min, byte[] max, boolean full,
 														 boolean backwards, boolean dups) throws OBStorageException {
 
 			
 			this.max = max;
 			this.min = min;
 			this.dups = dups;
 
 			this.full = full;
 			this.backwardsMode = backwards;
 
 			if (backwardsMode) {
 				this.current = max;
 			} else {
 				this.current = min;
 			}
 			try {
 				this.cursor = db.openCursor(null, CursorConfig.READ_UNCOMMITTED );
 
 				
 				keyEntry.setData(current);
 				if (!full) {
 						//          OBAsserts.chkAssertStorage(comp.compare(min, max) <= 0, "min must be smaller or equal than max: " + 
 						//																		 Arrays.toString(min) + " " + Arrays.toString(max));
 					retVal = cursor
 							.getSearchKeyRange(keyEntry, dataEntry,LockMode.READ_UNCOMMITTED 
  );
 					// we must comopare if the returned key is within range.
 					
 
 					if(withinRange()){
 								loadNext();
 						}
 			
 				} else {
 					if (backwardsMode) {
 						retVal = cursor.getLast(keyEntry, dataEntry, LockMode.READ_UNCOMMITTED 
 );
 					} else {
 						retVal = cursor.getFirst(keyEntry, dataEntry, LockMode.READ_UNCOMMITTED 
 );
 					}
 					loadNext();
 				}
 
 			} catch (DatabaseException e) {
 				throw new OBStorageException(e);
 			}
 			
 		}
 
 		/**
 		 * Verifies if the current key is within range.
 		 */
 		private  boolean  withinRange(){
 				int c0 = comp.compare(keyEntry.getData(), min);				
 				int c1 = comp.compare(keyEntry.getData(), max);
 				return c0 >= 0 && c1 <= 0;
 		}
 
 		public boolean hasNext() {
 			return next != null;
 		}
 
 		/**
 		 * Loads data from keyEntry and dataEntry and puts it into next. If we
 		 * go beyond max, we set next to null so that everybody will work
 		 * properly.
 		 */
 		private void loadNext() throws NoSuchElementException {
 			if (retVal == OperationStatus.SUCCESS) {
 				current = keyEntry.getData();
 
 			
 				if (backwardsMode) {
 						if (full || withinRange()) {
 						next = createT(current, 
 															 (dataEntry.getData()));
 						stats.add(dataEntry.getData().length);
 					} else { // end of the loop
 						next = null;
 						// close the cursor
 						// closeCursor();
 					}
 
 				} else {
 						if (full || withinRange()) {
 						next = createT(current, (dataEntry.getData()));
 						stats.add(dataEntry.getData().length);
 					} else { // end of the loop
 						next = null;
 						// close the cursor
 						// closeCursor();
 					}
 				}
 			} else { // we are done
 				next = null;
 				// close the cursor
 				// closeCursor();
 			}
 		}
 
 		protected  T createT(byte[] key, byte[] value){
 									return		 createTuple(key,value);
 		}
 
 
 		/**
 		 * Creates a tuple from the given key and value.
 		 * 
 		 * @param key
 		 *            raw key.
 		 * @param value
 		 *            raw value.
 		 * @return A new tuple of type T created from the raw data key and
 		 *         value.
 		 */
 		protected abstract T createTuple(byte[] key, byte[] value);
 
 		public T next() {
 			
 				if (next == null) {
 					throw new NoSuchElementException(
 							"You tried to access an iterator with no next elements");
 				}
 				T res = next;
 				try {
 					prevKeyEntry = keyEntry;
 					prevDataEntry = dataEntry;
 					if(backwardsMode){
 							if(dups){
 						retVal = cursor.getPrev(keyEntry, dataEntry, LockMode.READ_UNCOMMITTED 
 );
 							}else{
 						retVal = cursor.getPrevNoDup(keyEntry, dataEntry, LockMode.READ_UNCOMMITTED 
 );
 							}
 					}else{
 							if(dups){
 						retVal = cursor.getNext(keyEntry, dataEntry, LockMode.READ_UNCOMMITTED 
  );
 							}else{
 						retVal = cursor.getNextNoDup(keyEntry, dataEntry, LockMode.READ_UNCOMMITTED 
  );			
 							}
 					}
 					
 				} catch (DatabaseException e) {
 					throw new NoSuchElementException("Berkeley DB's error: "
 							+ e.getMessage());
 				}
 				// get the next elements.
 				loadNext();
 				return res;
 			
 		}
 
 		public void closeCursor() throws OBException {
 			try {
 			
 					if (!cursorClosed) {
 						cursor.close();
 						cursorClosed = true;
 					}
 			
 			} catch (DatabaseException e) {
 				throw new NoSuchElementException(
 						"Could not close the internal cursor");
 			}
 		}
 
 		public void remove() {
 			try {
 				if(backwardsMode){
 					throw new UnsupportedOperationException();
 				}
 				// double x1 = SortedDoubleBinding.entryToDouble(keyEntry);
 				OperationStatus ret = null; // cursor.getPrev(keyEntry,
 				// dataEntry, null);
 				// double x = SortedDoubleBinding.entryToDouble(keyEntry);
 				if (this.retVal != OperationStatus.SUCCESS) {
 						try{
 					closeCursor();
 						}catch(OBException e){
 								throw new IllegalArgumentException(e);
 						}
 					Cursor c = db.openCursor(null, CursorConfig.READ_UNCOMMITTED );
 					ret = c.getLast(keyEntry, dataEntry, LockMode.READ_UNCOMMITTED 
 );
 					if (ret != OperationStatus.SUCCESS) {
 						throw new NoSuchElementException();
 					}
 					ret = c.delete();
 					c.close();
 				} else {
 
 					ret = cursor.getPrev(keyEntry, dataEntry, LockMode.READ_UNCOMMITTED 
 );
 					if (ret != OperationStatus.SUCCESS) {
 						throw new NoSuchElementException();
 					}
 					ret = cursor.delete();
 				}
 
 				if (ret != OperationStatus.SUCCESS) {
 					throw new NoSuchElementException();
 				}
 
 			} catch (DatabaseException e) {
 				throw new IllegalArgumentException(e);
 			}
 		}
 
 		/*
 		 * public void finalize() throws Throwable { try { closeCursor(); }
 		 * finally { super.finalize(); } }
 		 */
 
 	}
 
 	public long size() throws OBStorageException {
 		long res;
 		
 		try {
 				 res = db.count();
 		} catch (DatabaseException e) {
 			throw new OBStorageException(e);
 		}
 		return res;
 		
 	
 	}
 
 	public void optimize(){
 	}
 
 	/**
 	 * Returns the next id from the database (incrementing sequences).
 	 * 
 	 * @return The next id that can be inserted.
 	 */
 	public long nextId() throws OBStorageException {
 		long res;
 		try {
 			res = this.counter.get(null, 1);
 		} catch (DatabaseException e) {
 			throw new OBStorageException(e);
 		}
 		return res;
 	}
 
 	@Override
 	public StaticBin1D getReadStats() {
 		return this.stats;
 	}
 
 	@Override
 	public void setReadStats(StaticBin1D stats) {
 		this.stats = stats;
 	}
 
 	public Object getStats(){
 			return null;
 	}
 
 	/**
 	 * Iterator used to process range results.
 	 */
 	/*
 	 * TODO: I am leaving the closing of the cursor to the last iteration or the
 	 * finalize method (whichever happens first). We should test if this is ok,
 	 * or if there is an issue with this because Berkeley's iterator explicitly
 	 * have a "close" method.
 	 */
 	protected class ByteArrayIterator extends CursorIterator<TupleBytes> {
 
 		protected ByteArrayIterator() throws OBStorageException {
 			super();
 		}
 
 		protected ByteArrayIterator(byte[] min, byte[] max)
 				throws OBStorageException {
 			super(min, max);
 		}
 
 		protected ByteArrayIterator(byte[] min, byte[] max, boolean full, boolean backwardsMode, boolean dups)
 				throws OBStorageException {
 				super(min, max, full, backwardsMode, dups);
 		}
 
 		@Override
 		protected TupleBytes createTuple(byte[] key, byte[] value) {
 			return new TupleBytes(key, value);
 		}
 	}
 
 
 	
 
 }
 
