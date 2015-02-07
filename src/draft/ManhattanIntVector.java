package draft;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import net.obsearch.asserts.OBAsserts;
import net.obsearch.constants.ByteConstants;
import net.obsearch.exception.OBException;
import net.obsearch.ob.OBInt;
import net.obsearch.utils.bytes.ByteConversion;

public class ManhattanIntVector implements OBInt {

	public int[] vector;
	public String name;
	
	public ManhattanIntVector(){}
	
	/**
	 * Construct an object from an array.
	 * @param vector
	 */
	public ManhattanIntVector(int [] vector){
		this.vector = vector;
	}
	
	public ManhattanIntVector(int [] vector, String name){
		this.vector = vector;
		this.name = name;
	}
	
	@Override
	public byte[] store() throws OBException, IOException {
		ByteBuffer b = ByteConversion.createByteBuffer(ByteConstants.Int.getSize() * vector.length);
		IntBuffer s = b.asIntBuffer();
		s.put(vector);
		return b.array();	
	}

	@Override
	public void load(byte[] input) throws OBException, IOException {
		IntBuffer s = ByteConversion.createByteBuffer(input).asIntBuffer();
		vector = new int[input.length / ByteConstants.Int.getSize()];
		s.get(vector);
	}

	@Override
	public int distance(OBInt object) throws OBException {
		ManhattanIntVector other = (ManhattanIntVector)object;
		OBAsserts.chkAssert(vector.length == other.vector.length, "Vector size mismatch");
		int i = 0;
		int res = 0;
		while(i < vector.length) {
			res += Math.abs(vector[i] - other.vector[i]);
			i++;
		}
		OBAsserts.chkAssert(res <= Integer.MAX_VALUE, "max value exceeded");
		return res;
	}
	//NOTE! this has to be implemented always, otherwise CommonsInt.fullMatchLite on line 54 fails 
    public final boolean equals(final Object object) {
        ManhattanIntVector o = (ManhattanIntVector) object;
        return Arrays.equals(vector, o.vector);
    }

    public int[] getVector() {
    	return this.vector;
    }
	
	public String toString() {
		return Arrays.toString(vector);
	}

}
