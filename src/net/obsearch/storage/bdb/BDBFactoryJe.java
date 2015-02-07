 package net.obsearch.storage.bdb;
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
  import java.io.File;
  import java.io.IOException;
  import java.io.FileNotFoundException;
  import java.math.BigInteger;
  
  import net.obsearch.asserts.OBAsserts;
  import net.obsearch.exception.OBStorageException;
  import net.obsearch.exception.OBException;
  import net.obsearch.storage.OBStorageConfig.IndexType;
  import net.obsearch.storage.OBStore;
  import net.obsearch.storage.OBStorageConfig;
  import net.obsearch.constants.OBSearchProperties;
  
  
  
  import net.obsearch.storage.OBStoreByte;
  
  import net.obsearch.storage.OBStoreShort;
  
  import net.obsearch.storage.OBStoreInt;
  
  import net.obsearch.storage.OBStoreLong;
  
  import net.obsearch.storage.OBStoreFloat;
  
  import net.obsearch.storage.OBStoreDouble;
  
  
  
  import net.obsearch.storage.TupleBytes;
  import net.obsearch.storage.OBStoreFactory;
  
  import com.sleepycat.je.Database;
  import com.sleepycat.je.DatabaseConfig;
  import com.sleepycat.je.DatabaseException;
  import com.sleepycat.je.Environment;
  import com.sleepycat.je.EnvironmentConfig;
  import com.sleepycat.bind.tuple.*;
  import com.sleepycat.je.DatabaseEntry;
  
  
  
  import java.io.File;
  import org.apache.log4j.Logger;
  
  /**
   * BDBFactory generates an environment in the given directory, and creates
   * databases as OBSearch requests.
   * @author Arnoldo Jose Muller Molina
   */
  
  public final class BDBFactoryJe implements OBStoreFactory {
  		/*
  			  je
       */
  		/**
  		 * Logger.
  		 */
  		private static final transient Logger logger = Logger
  			.getLogger(BDBFactoryJe.class);
  
  		private String directory;
  
      /**
       * The environment.
       */
      private Environment env;
  
      /**
       * Creates a new factory that will be based in the given directory.
       * @param directory
       *                The directory where the Berkeley DB files will be stored.
       * @throws IOException
       *                 If the given directory does not exist.
       */
      public BDBFactoryJe(File directory) throws IOException, DatabaseException, OBStorageException 	 {
  				this.directory = directory.getAbsolutePath();
  				logger.debug("Factory created on dir: " + directory);
          directory.mkdirs();
          OBAsserts.chkFileExists(directory);
         EnvironmentConfig envConfig = createEnvConfig();
         env = new Environment(directory, envConfig);
 				if(logger.isDebugEnabled()){
 								logger.debug("Environment config: \n" + env.getConfig().toString());
 								logger.debug("Buffer size " + env.getConfig().getConfigParam("je.log.bufferSize"));
 								logger.debug("Cache % " + env.getConfig().getConfigParam("je.maxMemoryPercent"));
 								
 				}
     }
 
 		public String getFactoryLocation(){
 				return directory;
 		}
     
     /**
      * Creates the default environment configuration.
      * @return Default environment configuration.
      */
     private EnvironmentConfig createEnvConfig() 	{
         /* Open a transactional Oracle Berkeley DB Environment. */
         EnvironmentConfig envConfig = new EnvironmentConfig();
         envConfig.setAllowCreate(true);
         envConfig.setTransactional(false);
         envConfig.setConfigParam("java.util.logging.DbLogHandler.on", "false");
 				envConfig.setLocking(false);
 				envConfig.setTxnNoSync(true);
         //envConfig.setTxnWriteNoSync(true);
 				// envConfig.setCachePercent(20);
 				// 100 k gave the best performance in one thread and for 30 pivots of
         // shorts
 				
 				//						envConfig.setConfigParam("je.log.faultReadSize", "140000");	 
 				//    envConfig.setConfigParam("je.evictor.lruOnly", "false");
         //    envConfig.setConfigParam("je.evictor.nodesPerScan", "100");
         return envConfig;
     }
 
     public void close() throws OBStorageException {
         try {
             env.cleanLog();
             env.compress();
             env.checkpoint(null);
  
             env.close();
         } catch (DatabaseException e) {
             throw new OBStorageException(e);
         }
     }
 
     public OBStore<TupleBytes> createOBStore(String name, OBStorageConfig config) throws OBStorageException{       
 
         OBStore res = null;
 				
        
         try{
 
 DatabaseConfig dbConfig = createDefaultDatabaseConfig();
 				boolean temp = config.isTemp();
 				boolean bulkMode = config.isBulkMode();
 				boolean duplicates = config.isDuplicates();							 
 								
 								// bulk mode has priority over deferred write.
 										 dbConfig.setSortedDuplicates(duplicates);
 								if(bulkMode){
 										dbConfig.setDeferredWrite(bulkMode);										
 								}else{
 //										dbConfig.setTemporary(temp);
 								}
 								
 											Database seq = null;
 						if(!duplicates){
 								seq = env.openDatabase(null,  sequentialDatabaseName(name), dbConfig)
 ;
 						}
         
 						res = new BDBOBStoreJeByteArray(name, env.openDatabase(null, name, dbConfig)
  , seq, this, duplicates);
 					
         }catch(DatabaseException e){
             throw new OBStorageException(e);
         }
        return res;
     }
 
 		/**
 	 * Generate the name of the sequential database based on name.
 	 * @param name The name of the database.
 	 * @return The sequential database name.
 	 */
 	private String sequentialDatabaseName(String name){
 		return name + "seq";
 	}
 
     
     /**
      * Creates a default database configuration.
      * @return default database configuration.
      */
     protected DatabaseConfig createDefaultDatabaseConfig() {
         DatabaseConfig dbConfig = new DatabaseConfig();
         dbConfig = new DatabaseConfig();
         dbConfig.setTransactional(false);
         dbConfig.setAllowCreate(true);
 						 
         return dbConfig;
     }
 
 		public void removeOBStore(OBStore storage) throws OBStorageException{
 						storage.close();
 						try{
 						env.removeDatabase(null, storage.getName());
 						env.removeDatabase(null, sequentialDatabaseName(storage.getName()));
 						}catch(DatabaseException e){
 								throw new OBStorageException(e);
 						}
 				}
 
 
 				
 				
 
 				public OBStoreByte createOBStoreByte(String name, OBStorageConfig config) throws OBStorageException{
         
         OBStoreByte res = null;
         try{
             
 DatabaseConfig dbConfig = createDefaultDatabaseConfig();
 				boolean temp = config.isTemp();
 				boolean bulkMode = config.isBulkMode();
 				boolean duplicates = config.isDuplicates();							 
 								
 								// bulk mode has priority over deferred write.
 										 dbConfig.setSortedDuplicates(duplicates);
 								if(bulkMode){
 										dbConfig.setDeferredWrite(bulkMode);										
 								}else{
 //										dbConfig.setTemporary(temp);
 								}
 								
 											Database seq = null;
 						if(!duplicates){
 								seq = env.openDatabase(null,  sequentialDatabaseName(name), dbConfig)
 ;
 						}
         
 						res = new BDBOBStoreJeByte(name, env.openDatabase(null, name, dbConfig)
  , seq, this, duplicates);
 								
         }catch(DatabaseException e){
             throw new OBStorageException(e);
         }
        return res;
     }
 
 		
 		public  byte[] serializeByte(byte value){
 				return BDBFactoryJe.byteToBytes(value);
 		}
 
 
 		public byte deSerializeByte(byte[] value){
 				DatabaseEntry entry = new DatabaseEntry(value);
 				return ByteBinding.entryToByte(entry);
 		}
 
 		public static byte[] byteToBytes(byte value){
 				DatabaseEntry entry = new DatabaseEntry();
 				ByteBinding.byteToEntry(value, entry);
 				assert entry.getData().length == net.obsearch.constants.ByteConstants.Byte.getSize();
 				return entry.getData();
 		}
 
 		public static byte bytesToByte(byte[] value){
 				DatabaseEntry entry = new DatabaseEntry(value);
 				return ByteBinding.entryToByte(entry);
 		}
 
 
 				
 				
 
 				public OBStoreShort createOBStoreShort(String name, OBStorageConfig config) throws OBStorageException{
         
         OBStoreShort res = null;
         try{
             
 DatabaseConfig dbConfig = createDefaultDatabaseConfig();
 				boolean temp = config.isTemp();
 				boolean bulkMode = config.isBulkMode();
 				boolean duplicates = config.isDuplicates();							 
 								
 								// bulk mode has priority over deferred write.
 										 dbConfig.setSortedDuplicates(duplicates);
 								if(bulkMode){
 										dbConfig.setDeferredWrite(bulkMode);										
 								}else{
 //										dbConfig.setTemporary(temp);
 								}
 								
 											Database seq = null;
 						if(!duplicates){
 								seq = env.openDatabase(null,  sequentialDatabaseName(name), dbConfig)
 ;
 						}
         
 						res = new BDBOBStoreJeShort(name, env.openDatabase(null, name, dbConfig)
  , seq, this, duplicates);
 								
         }catch(DatabaseException e){
             throw new OBStorageException(e);
         }
        return res;
     }
 
 		
 		public  byte[] serializeShort(short value){
 				return BDBFactoryJe.shortToBytes(value);
 		}
 
 
 		public short deSerializeShort(byte[] value){
 				DatabaseEntry entry = new DatabaseEntry(value);
 				return ShortBinding.entryToShort(entry);
 		}
 
 		public static byte[] shortToBytes(short value){
 				DatabaseEntry entry = new DatabaseEntry();
 				ShortBinding.shortToEntry(value, entry);
 				assert entry.getData().length == net.obsearch.constants.ByteConstants.Short.getSize();
 				return entry.getData();
 		}
 
 		public static short bytesToShort(byte[] value){
 				DatabaseEntry entry = new DatabaseEntry(value);
 				return ShortBinding.entryToShort(entry);
 		}
 
 
 				
 				
 
 				public OBStoreInt createOBStoreInt(String name, OBStorageConfig config) throws OBStorageException{
         
         OBStoreInt res = null;
         try{
             
 DatabaseConfig dbConfig = createDefaultDatabaseConfig();
 				boolean temp = config.isTemp();
 				boolean bulkMode = config.isBulkMode();
 				boolean duplicates = config.isDuplicates();							 
 								
 								// bulk mode has priority over deferred write.
 										 dbConfig.setSortedDuplicates(duplicates);
 								if(bulkMode){
 										dbConfig.setDeferredWrite(bulkMode);										
 								}else{
 //										dbConfig.setTemporary(temp);
 								}
 								
 											Database seq = null;
 						if(!duplicates){
 								seq = env.openDatabase(null,  sequentialDatabaseName(name), dbConfig)
 ;
 						}
         
 						res = new BDBOBStoreJeInt(name, env.openDatabase(null, name, dbConfig)
  , seq, this, duplicates);
 								
         }catch(DatabaseException e){
             throw new OBStorageException(e);
         }
        return res;
     }
 
 		
 		public  byte[] serializeInt(int value){
 				return BDBFactoryJe.intToBytes(value);
 		}
 
 
 		public int deSerializeInt(byte[] value){
 				DatabaseEntry entry = new DatabaseEntry(value);
 				return IntegerBinding.entryToInt(entry);
 		}
 
 		public static byte[] intToBytes(int value){
 				DatabaseEntry entry = new DatabaseEntry();
 				IntegerBinding.intToEntry(value, entry);
 				assert entry.getData().length == net.obsearch.constants.ByteConstants.Int.getSize();
 				return entry.getData();
 		}
 
 		public static int bytesToInt(byte[] value){
 				DatabaseEntry entry = new DatabaseEntry(value);
 				return IntegerBinding.entryToInt(entry);
 		}
 
 
 				
 				
 
 				public OBStoreLong createOBStoreLong(String name, OBStorageConfig config) throws OBStorageException{
         
         OBStoreLong res = null;
         try{
             
 DatabaseConfig dbConfig = createDefaultDatabaseConfig();
 				boolean temp = config.isTemp();
 				boolean bulkMode = config.isBulkMode();
 				boolean duplicates = config.isDuplicates();							 
 								
 								// bulk mode has priority over deferred write.
 										 dbConfig.setSortedDuplicates(duplicates);
 								if(bulkMode){
 										dbConfig.setDeferredWrite(bulkMode);										
 								}else{
 //										dbConfig.setTemporary(temp);
 								}
 								
 											Database seq = null;
 						if(!duplicates){
 								seq = env.openDatabase(null,  sequentialDatabaseName(name), dbConfig)
 ;
 						}
         
 						res = new BDBOBStoreJeLong(name, env.openDatabase(null, name, dbConfig)
  , seq, this, duplicates);
 								
         }catch(DatabaseException e){
             throw new OBStorageException(e);
         }
        return res;
     }
 
 		
 		public  byte[] serializeLong(long value){
 				return BDBFactoryJe.longToBytes(value);
 		}
 
 
 		public long deSerializeLong(byte[] value){
 				DatabaseEntry entry = new DatabaseEntry(value);
 				return LongBinding.entryToLong(entry);
 		}
 
 		public static byte[] longToBytes(long value){
 				DatabaseEntry entry = new DatabaseEntry();
 				LongBinding.longToEntry(value, entry);
 				assert entry.getData().length == net.obsearch.constants.ByteConstants.Long.getSize();
 				return entry.getData();
 		}
 
 		public static long bytesToLong(byte[] value){
 				DatabaseEntry entry = new DatabaseEntry(value);
 				return LongBinding.entryToLong(entry);
 		}
 
 
 				
 				
 
 				public OBStoreFloat createOBStoreFloat(String name, OBStorageConfig config) throws OBStorageException{
         
         OBStoreFloat res = null;
         try{
             
 DatabaseConfig dbConfig = createDefaultDatabaseConfig();
 				boolean temp = config.isTemp();
 				boolean bulkMode = config.isBulkMode();
 				boolean duplicates = config.isDuplicates();							 
 								
 								// bulk mode has priority over deferred write.
 										 dbConfig.setSortedDuplicates(duplicates);
 								if(bulkMode){
 										dbConfig.setDeferredWrite(bulkMode);										
 								}else{
 //										dbConfig.setTemporary(temp);
 								}
 								
 											Database seq = null;
 						if(!duplicates){
 								seq = env.openDatabase(null,  sequentialDatabaseName(name), dbConfig)
 ;
 						}
         
 						res = new BDBOBStoreJeFloat(name, env.openDatabase(null, name, dbConfig)
  , seq, this, duplicates);
 								
         }catch(DatabaseException e){
             throw new OBStorageException(e);
         }
        return res;
     }
 
 		
 		public  byte[] serializeFloat(float value){
 				return BDBFactoryJe.floatToBytes(value);
 		}
 
 
 		public float deSerializeFloat(byte[] value){
 				DatabaseEntry entry = new DatabaseEntry(value);
 				return SortedFloatBinding.entryToFloat(entry);
 		}
 
 		public static byte[] floatToBytes(float value){
 				DatabaseEntry entry = new DatabaseEntry();
 				SortedFloatBinding.floatToEntry(value, entry);
 				assert entry.getData().length == net.obsearch.constants.ByteConstants.Float.getSize();
 				return entry.getData();
 		}
 
 		public static float bytesToFloat(byte[] value){
 				DatabaseEntry entry = new DatabaseEntry(value);
 				return SortedFloatBinding.entryToFloat(entry);
 		}
 
 
 				
 				
 
 				public OBStoreDouble createOBStoreDouble(String name, OBStorageConfig config) throws OBStorageException{
         
         OBStoreDouble res = null;
         try{
             
 DatabaseConfig dbConfig = createDefaultDatabaseConfig();
 				boolean temp = config.isTemp();
 				boolean bulkMode = config.isBulkMode();
 				boolean duplicates = config.isDuplicates();							 
 								
 								// bulk mode has priority over deferred write.
 										 dbConfig.setSortedDuplicates(duplicates);
 								if(bulkMode){
 										dbConfig.setDeferredWrite(bulkMode);										
 								}else{
 //										dbConfig.setTemporary(temp);
 								}
 								
 											Database seq = null;
 						if(!duplicates){
 								seq = env.openDatabase(null,  sequentialDatabaseName(name), dbConfig)
 ;
 						}
         
 						res = new BDBOBStoreJeDouble(name, env.openDatabase(null, name, dbConfig)
  , seq, this, duplicates);
 								
         }catch(DatabaseException e){
             throw new OBStorageException(e);
         }
        return res;
     }
 
 		
 		public  byte[] serializeDouble(double value){
 				return BDBFactoryJe.doubleToBytes(value);
 		}
 
 
 		public double deSerializeDouble(byte[] value){
 				DatabaseEntry entry = new DatabaseEntry(value);
 				return SortedDoubleBinding.entryToDouble(entry);
 		}
 
 		public static byte[] doubleToBytes(double value){
 				DatabaseEntry entry = new DatabaseEntry();
 				SortedDoubleBinding.doubleToEntry(value, entry);
 				assert entry.getData().length == net.obsearch.constants.ByteConstants.Double.getSize();
 				return entry.getData();
 		}
 
 		public static double bytesToDouble(byte[] value){
 				DatabaseEntry entry = new DatabaseEntry(value);
 				return SortedDoubleBinding.entryToDouble(entry);
 		}
 
 
 		public byte[] serializeBigInteger(BigInteger value){
 				DatabaseEntry entry = new DatabaseEntry();
 				BigIntegerBinding.bigIntegerToEntry(value, entry);
 				return entry.getData();
 		}
 
 		public BigInteger deSerializeBigInteger(byte[] value){
 				DatabaseEntry entry = new DatabaseEntry(value);
 				return BigIntegerBinding.entryToBigInteger(entry);
 		}
 		
 		public Object stats() throws OBStorageException{
 		try{
 				return	env.getStats(null);
 		}catch(DatabaseException d){
 				throw new OBStorageException(d);    
     }
 }
 }
