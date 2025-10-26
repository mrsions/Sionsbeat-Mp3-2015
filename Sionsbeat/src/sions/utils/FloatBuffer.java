package sions.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FloatBuffer
{
	private static final int	MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	private float[] elementData;
	private int size = 0;
	private int addCapacity = 10;
	
	public FloatBuffer(int defCapacity, int addCapacity)
	{
		this.elementData = new float[defCapacity];
		this.addCapacity = addCapacity;
	}

	
	private void ensureCapacityInternal ( int capacity )
	{
		if ( capacity >= elementData.length )
		{
			grow(capacity);
		}
	}

	private void grow ( int capacity )
	{
		int newCapacity = elementData.length + addCapacity;

		if ( newCapacity < capacity ) newCapacity = capacity;
		if ( newCapacity > MAX_ARRAY_SIZE || newCapacity < 0 ){
			throw new OutOfMemoryError();
		}
		
		// minCapacity is usually close to size, so this is a win:
		elementData = Arrays.copyOf( elementData, newCapacity );
	}

	public int size ()
	{
		return size;
	}

	public float[] toArray ()
	{
	        return Arrays.copyOf(elementData, size);
	}

	public void add ( float e )
	{
		ensureCapacityInternal( size + 1 );
		elementData[size++] = e;
	}

	public void addAll ( float[] c )
	{
		int len = c.length;
		ensureCapacityInternal( size + len );
		System.arraycopy( c, 0, elementData, size, len );
		size += len;
	}

	public void clear ()
	{
		this.size = 0;
	}

	public Float get ( int index )
	{
		return elementData[index];
	}

	public void set ( int index, float element )
	{
		elementData[index] = element;
	}

}
