package net.obsearch.index;
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
  import java.io.IOException;
  import net.obsearch.constants.ByteConstants;
  import net.obsearch.exception.OBException;
  import net.obsearch.ob.OBFloat;
  import java.nio.ByteBuffer;
  import net.obsearch.utils.bytes.ByteBufferFactoryConversion;
  import java.util.Random;
  import java.util.Arrays;
  /**
   * L1 distance implementation for floats.
   * @author Arnoldo Jose Muller Molina
   *
   */
  //*************************************************************************
  //****** Warning: this is a generated file ********************************
  //****** The source file is: OBVector.java    
  //*************************************************************************
  public class OBVectorFloat implements OBFloat {
  	
  	private float[] data;
  	
  	/**
       * Default constructor must be provided by every object that implements the
       * interface OB.
       */
      public OBVectorFloat() {
      	data = null;
      }
  	
  	public OBVectorFloat(float[] data){
  		this.data = data;
  	}
  
  		/**
  	 * Creates a new vector with dim dimensions created by Random.nextFloat()
  	 */
  	public OBVectorFloat(Random r, int dim){
  			int i = 0;
  			data = new float[dim];
  			while(i < dim){
  								data[i] = r.nextFloat();
  
  					i++;
  			}
  	}
  
  	public boolean equals(Object o){
  			OBVectorFloat other = (	OBVectorFloat) o;
  			return Arrays.equals(data, other.data);
  	}
  
  
  	@Override
  	public float distance(OBFloat object) throws OBException {
  		// TODO Auto-generated method stub
  		OBVectorFloat o = (OBVectorFloat) object;
  		assert data.length == o.data.length;
  		float res = 0;
  		int i = 0;
  		while(i < data.length){
  				assert (data[i] - o.data[i]) >= Float.NEGATIVE_INFINITY 
   && (data[i] - o.data[i]) <= Float.MAX_VALUE : "a: " + data[i] + " b: "  + o.data[i] ;		 
  
  				
  			res += Math.abs(data[i] - o.data[i]);
  			i++;
  		}
  		return res;
  	}
  
  //*************************************************************************
  //****** Warning: this is a generated file ********************************
  //****** The source file is: OBVector.java    
  //*************************************************************************
  	@Override
  	public void load(byte[] input) throws OBException, IOException {
  		ByteBuffer in = ByteBufferFactoryConversion.createByteBuffer(input);
  		int size = in.getInt();
  		
  		data = new float[size];
 		int i = 0;
 		while(i < size){
 			data[i] = in.getFloat();
 			i++;
 		}
 	}
 	
 //*************************************************************************
 //****** Warning: this is a generated file ********************************
 //****** The source file is: OBVector.java    
 //*************************************************************************
 	@Override
 	public byte[] store() throws OBException, IOException {
 			ByteBuffer out = ByteBufferFactoryConversion.createByteBuffer(ByteConstants.Float.getSize() * data.length + ByteConstants.Int.getSize() );
 		
 		out.putInt(data.length);
 		for(float v : data){
 			out.putFloat(v);
 		}
 			return out.array();
 	}
 
 }
