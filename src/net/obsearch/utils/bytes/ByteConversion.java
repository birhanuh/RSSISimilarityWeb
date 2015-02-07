package net.obsearch.utils.bytes;
   
   import java.nio.ByteBuffer;
   import net.obsearch.constants.ByteConstants;
   import java.nio.ByteOrder;
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
   * ByteConversion. Utilities to convert primitive types from and to byte arrays.
   * @author Arnoldo Jose Muller Molina
   */
  
  public class ByteConversion {
  		
  		public static final ByteOrder ORDERING = ByteOrder.nativeOrder();
      /**
       * Create a ByteBuffer of size n.
       * @param n size of the new buffer. 
       * @return The buffer.
       */
      public static ByteBuffer createByteBuffer(int n){
          byte [] r = new byte[n];        
          return createByteBuffer(r);
      }
      
      /**
       * Create a ByteBuffer from the given byte array.
       * @param n size of the new buffer. 
       * @return The buffer.
       */
      public static ByteBuffer createByteBuffer(byte[] data){        
          ByteBuffer res = ByteBuffer.wrap(data);
  				res.order(ORDERING);
          return res;
      }
  		
  
  				
      /**
       * Reads a(n) Byte from the beginning of the given data array.
       * No size checks are performed.
       * @param data  Data that holds the encoded byte.
       * @return byte parsed from data.
       */
  				public static byte bytesToByte(byte[] data){
              return createByteBuffer(data).get();        
      }
      
      /**
       * Convert a(n) Byte into bytes.
       * @param i value to convert.
       * @return The byte array that represents the value.
       */
    	public static byte[] byteToBytes(byte i){
          byte [] res = new byte[ByteConstants.Byte.getSize()];
  				createByteBuffer(res).put(i);
          return res;
      }
  
  		/**
       * Reads a(n) Byte from the beginning of the given ByteBuffer.
       * No size checks are performed.
       * @param data  Data that holds the encoded byte.
       * @return byte parsed from data.
       */
  				public static byte byteBufferToByte(ByteBuffer data){
              return data.get();        
      }
      
      /**
       * Convert a(n) Byte into bytes.
       * @param i value to convert.
       * @return The byte array that represents the value.
       */
    	public static ByteBuffer byteToByteBuffer(byte i){
          ByteBuffer res = createByteBuffer(ByteConstants.Byte.getSize());
  				res.put(i);
          return res;
      }
  
  
  				
     /**
      * Reads a(n) Short from the beginning of the given data array.
      * No size checks are performed.
      * @param data  Data that holds the encoded short.
      * @return short parsed from data.
      */
 				public static short bytesToShort(byte[] data){
 						return createByteBuffer(data).getShort();        
     }
     
     /**
      * Convert a(n) Short into bytes.
      * @param i value to convert.
      * @return The byte array that represents the value.
      */
   	public static byte[] shortToBytes(short i){
         byte [] res = new byte[ByteConstants.Short.getSize()];
         createByteBuffer(res).putShort(i);
         return res;
     }
 
 		/**
      * Reads a(n) Short from the beginning of the given ByteBuffer.
      * No size checks are performed.
      * @param data  Data that holds the encoded short.
      * @return short parsed from data.
      */
 				public static short byteBufferToShort(ByteBuffer data){
 						return data.getShort();        
     }
     
     /**
      * Convert a(n) Short into bytes.
      * @param i value to convert.
      * @return The byte array that represents the value.
      */
   	public static ByteBuffer shortToByteBuffer(short i){
         ByteBuffer res = createByteBuffer(ByteConstants.Short.getSize());
         res.putShort(i);
         return res;
     }
 
 
 				
     /**
      * Reads a(n) Int from the beginning of the given data array.
      * No size checks are performed.
      * @param data  Data that holds the encoded int.
      * @return int parsed from data.
      */
 				public static int bytesToInt(byte[] data){
 						return createByteBuffer(data).getInt();        
     }
     
     /**
      * Convert a(n) Int into bytes.
      * @param i value to convert.
      * @return The byte array that represents the value.
      */
   	public static byte[] intToBytes(int i){
         byte [] res = new byte[ByteConstants.Int.getSize()];
         createByteBuffer(res).putInt(i);
         return res;
     }
 
 		/**
      * Reads a(n) Int from the beginning of the given ByteBuffer.
      * No size checks are performed.
      * @param data  Data that holds the encoded int.
      * @return int parsed from data.
      */
 				public static int byteBufferToInt(ByteBuffer data){
 						return data.getInt();        
     }
     
     /**
      * Convert a(n) Int into bytes.
      * @param i value to convert.
      * @return The byte array that represents the value.
      */
   	public static ByteBuffer intToByteBuffer(int i){
         ByteBuffer res = createByteBuffer(ByteConstants.Int.getSize());
         res.putInt(i);
         return res;
     }
 
 
 				
     /**
      * Reads a(n) Long from the beginning of the given data array.
      * No size checks are performed.
      * @param data  Data that holds the encoded long.
      * @return long parsed from data.
      */
 				public static long bytesToLong(byte[] data){
 						return createByteBuffer(data).getLong();        
     }
     
     /**
      * Convert a(n) Long into bytes.
      * @param i value to convert.
      * @return The byte array that represents the value.
      */
   	public static byte[] longToBytes(long i){
         byte [] res = new byte[ByteConstants.Long.getSize()];
         createByteBuffer(res).putLong(i);
         return res;
     }
 
 		/**
      * Reads a(n) Long from the beginning of the given ByteBuffer.
      * No size checks are performed.
      * @param data  Data that holds the encoded long.
      * @return long parsed from data.
      */
 				public static long byteBufferToLong(ByteBuffer data){
 						return data.getLong();        
     }
     
     /**
      * Convert a(n) Long into bytes.
      * @param i value to convert.
      * @return The byte array that represents the value.
      */
   	public static ByteBuffer longToByteBuffer(long i){
         ByteBuffer res = createByteBuffer(ByteConstants.Long.getSize());
         res.putLong(i);
         return res;
     }
 
 
 				
     /**
      * Reads a(n) Float from the beginning of the given data array.
      * No size checks are performed.
      * @param data  Data that holds the encoded float.
      * @return float parsed from data.
      */
 				public static float bytesToFloat(byte[] data){
 						return createByteBuffer(data).getFloat();        
     }
     
     /**
      * Convert a(n) Float into bytes.
      * @param i value to convert.
      * @return The byte array that represents the value.
      */
   	public static byte[] floatToBytes(float i){
         byte [] res = new byte[ByteConstants.Float.getSize()];
         createByteBuffer(res).putFloat(i);
         return res;
     }
 
 		/**
      * Reads a(n) Float from the beginning of the given ByteBuffer.
      * No size checks are performed.
      * @param data  Data that holds the encoded float.
      * @return float parsed from data.
      */
 				public static float byteBufferToFloat(ByteBuffer data){
 						return data.getFloat();        
     }
     
     /**
      * Convert a(n) Float into bytes.
      * @param i value to convert.
      * @return The byte array that represents the value.
      */
   	public static ByteBuffer floatToByteBuffer(float i){
         ByteBuffer res = createByteBuffer(ByteConstants.Float.getSize());
         res.putFloat(i);
         return res;
     }
 
 
 				
     /**
      * Reads a(n) Double from the beginning of the given data array.
      * No size checks are performed.
      * @param data  Data that holds the encoded double.
      * @return double parsed from data.
      */
 				public static double bytesToDouble(byte[] data){
 						return createByteBuffer(data).getDouble();        
     }
     
     /**
      * Convert a(n) Double into bytes.
      * @param i value to convert.
      * @return The byte array that represents the value.
      */
   	public static byte[] doubleToBytes(double i){
         byte [] res = new byte[ByteConstants.Double.getSize()];
         createByteBuffer(res).putDouble(i);
         return res;
     }
 
 		/**
      * Reads a(n) Double from the beginning of the given ByteBuffer.
      * No size checks are performed.
      * @param data  Data that holds the encoded double.
      * @return double parsed from data.
      */
 				public static double byteBufferToDouble(ByteBuffer data){
 						return data.getDouble();        
     }
     
     /**
      * Convert a(n) Double into bytes.
      * @param i value to convert.
      * @return The byte array that represents the value.
      */
   	public static ByteBuffer doubleToByteBuffer(double i){
         ByteBuffer res = createByteBuffer(ByteConstants.Double.getSize());
         res.putDouble(i);
         return res;
     }
 
     
     
 }
