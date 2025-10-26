package sions.json;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONArray extends JSONAbstractObject
{

	public JSONArray ()
	{

		this( new ArrayList() );
	}

	public JSONArray ( String json_text )
	{

		this( new JSONTokener( json_text ).parseArray() );
	}
	
	public JSONArray ( File file) throws FileNotFoundException, IOException{
		FileInputStream fis = null;
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		try{
			fis = new FileInputStream(file);
			byte[] data = new byte[2048];
			int size = 0;
			while((size=fis.read(data))!=-1){
				bao.write(data, 0, size);
			}
			List < Object > list = new JSONTokener( new String(bao.toByteArray(), "UTF-8") ).parseArray();
			this.objectTarget = list;
		}finally{
			try{ fis.close(); }catch(Exception e){}
			try{ bao.close(); }catch(Exception e){}
		}
	}

	public JSONArray ( Collection < Object > list )
	{
		if(list == null){
			this.objectTarget = new ArrayList <Object> ();
		}else{
			this.objectTarget = new ArrayList < Object > ( list );
		}
	}

	public JSONArray ( Object array )
	{

		this(( Collection < Object > )null);
		if ( array.getClass().isArray() )
		{
			int length = Array.getLength( array );
			for ( int i = 0; i < length; i++ )
			{
				add( Array.get( array, i ) );
			}
		}
	}

	private List < Object >	objectTarget;

	public List < Object > getObjectTarget ()
	{

		return this.objectTarget;
	}

	@Override
	protected Object _get ( Object key )
	{

		int index = (Integer) key;

		if ( objectTarget.size() > index )
		{
			return objectTarget.get( index );
		}
		else
		{
			return null;
		}
	}

	public boolean is ( int key )
	{

		return this.objectTarget.size() > key;
	}

	@Override
	public int length ()
	{

		return objectTarget.size();
	}

	public void add ( int key, Object value )
	{

		objectTarget.add( key, value );
	}

	public void add ( Object value )
	{

		objectTarget.add( value );
	}

	public void remove ( Object value )
	{
		objectTarget.remove( value );
	}
	
	public void removeAt ( int index )
	{
		objectTarget.remove( index );
	}

	public void set ( int key, Object value )
	{
		objectTarget.set( key, value );
	}

	@Override
	public void set ( Object key, Object value )
	{
		objectTarget.set( (Integer) key, value );
	}

	public Iterator iterator ()
	{

		return objectTarget.iterator();
	}
	
	public Object get ( int key )
	{
		return _get( key );
	}

	public String getString ( int key )
	{

		return _getString( key, null );
	}

	public String getString ( int key, String def )
	{

		return _getString( key, def );
	}

	public boolean getBoolean ( String key )
	{
		return _getBoolean( key, false );
	}

	public boolean getBoolean ( String key, boolean def )
	{
		return _getBoolean( key, def );
	}

	public int getInt ( int key )
	{

		return _getInt( key, 0 );
	}

	public int getInt ( int key, int def )
	{

		return _getInt( key, def );
	}

	public long getLong ( int key )
	{

		return _getLong( key, 0L );
	}

	public long getLong ( int key, long def )
	{

		return _getLong( key, def );
	}

	public short getShort ( int key )
	{

		return _getShort( key, (short) 0 );
	}

	public short getShort ( int key, short def )
	{

		return _getShort( key, def );
	}

	public byte getByte ( int key )
	{

		return _getByte( key, (byte) 0 );
	}

	public byte getByte ( int key, byte def )
	{

		return _getByte( key, def );
	}

	public char getChar ( int key )
	{

		return _getChar( key, (char) 0 );
	}

	public char getChar ( int key, char def )
	{

		return _getChar( key, def );
	}

	public float getFloat ( int key )
	{

		return _getFloat( key, 0F );
	}

	public float getFloat ( int key, float def )
	{

		return _getFloat( key, def );
	}

	public double getDouble ( int key )
	{

		return _getDouble( key, 0D );
	}

	public double getDouble ( int key, double def )
	{

		return _getDouble( key, def );
	}

	public JSONObject getJSONObject ( int key )
	{

		return _getJSONObject( key );
	}

	public JSONArray getJSONArray ( int key )
	{

		return _getJSONArray( key );
	}

	@Override
	public String toString ()
	{

		return new JSONStringer().parse( this ).toString();
	}

	@Override
	public String toStringFile ()
	{

		return null;
	}
}
