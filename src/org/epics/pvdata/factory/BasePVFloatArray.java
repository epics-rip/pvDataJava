/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.FloatArrayData;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.SerializableControl;


/**
 * Base class for implementing PVFloatArray.
 * @author mrk
 *
 */
public class BasePVFloatArray extends AbstractPVScalarArray implements PVFloatArray
{
    protected float[] value;
    private FloatArrayData floatArrayData = new FloatArrayData();
    
    /**
     * Constructor.
     * @param parent The parent.
     * @param array The Introspection interface.
     */    
    public BasePVFloatArray(PVStructure parent,ScalarArray array)
    {
        super(parent,array);
        value = new float[capacity];
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.factory.AbstractPVArray#setCapacity(int)
     */
    @Override
    public void setCapacity(int len) {
    	if(capacity==len) return;
        if(!capacityMutable) {
            super.message("not capacityMutable", MessageType.error);
            return;
        }
        if(length>len) length = len;
        float[]newarray = new float[len];
        if(length>0) System.arraycopy(value,0,newarray,0,length);
        value = newarray;
        capacity = len;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVFloatArray#get(int, int, org.epics.pvdata.pv.FloatArrayData)
     */
    @Override
    public int get(int offset, int len, FloatArrayData data) {
        int n = len;
        if(offset+len > length) n = Math.max(0, length-offset);
        data.data = value;
        data.offset = offset;
        return n;         
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVFloatArray#put(int, int, float[], int)
     */
    @Override
    public int put(int offset, int len, float[]from, int fromOffset) {
        if(super.isImmutable()) {
            super.message("field is immutable", MessageType.error);
            return 0;
        }
        if(from==value) return len;
        if(offset+len > length) {
            int newlength = offset + len;
            if(newlength>capacity) {
                setCapacity(newlength);
                newlength = capacity;
                len = newlength - offset;
                if(len<=0) return 0;
            }
            length = newlength;
        }
        System.arraycopy(from,fromOffset,value,offset,len);
        super.postPut();
        return len;           
    }
	/* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVFloatArray#shareData(float[])
     */
    @Override
    public void shareData(float[] from) {
        this.value = from;
        super.capacity = from.length;
        super.length = from.length;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.SerializableArray#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl, int, int)
     */
    @Override
	public void serialize(ByteBuffer buffer, SerializableControl flusher, int offset, int count) {
    	// cache
    	final int length = this.length;
    	final float[] value = this.value;

    	// check bounds
		if (offset < 0) offset = 0;
		else if (offset > length) offset = length;
		if (count < 0) count = length;

		final int maxCount = length - offset;
		if (count > maxCount)
			count = maxCount;
		
		// write
		SerializeHelper.writeSize(count, buffer, flusher);
		final int end = offset + count;
		int i = offset;
		while (true)
		{
        	final int maxIndex = Math.min(end-i, buffer.remaining()/(Float.SIZE/Byte.SIZE))+i;
			for (; i < maxIndex; i++)
				buffer.putFloat(value[i]);
			if (i < end)
				flusher.flushSerializeBuffer();
			else
				break;
		}
	}
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
     */
    @Override
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		final int size = SerializeHelper.readSize(buffer, control);
		if (size >= 0) {
			// prepare array, if necessary
			if (size > capacity)
				setCapacity(size);
			// retrieve value from the buffer
			int i = 0;
			while (true)
			{
				final int maxIndex = Math.min(size-i, buffer.remaining()/(Float.SIZE/Byte.SIZE))+i;
				for (; i < maxIndex; i++)
					value[i] = buffer.getFloat();
				if (i < size)
					control.ensureData(Float.SIZE/Byte.SIZE);
				else
					break;
			}
			// set new length
			length = size;
		}
		// TODO null arrays (size == -1) not supported
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// TODO anything else?
		if (obj instanceof PVFloatArray) {
			PVFloatArray b = (PVFloatArray)obj;
			b.get(0, b.getLength(), floatArrayData);
			if(floatArrayData.data==value) return true;
			return Arrays.equals(floatArrayData.data, value);
		}
		else
			return false;
	}
    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}
}