package sions.json;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JSONObject extends JSONAbstractObject
{

	public JSONObject ()
	{
		this( new HashMap() );
	}

	public JSONObject ( String json_text )
	{
		this( new JSONTokener( json_text ).parseObject() );
	}
	
	public JSONObject ( File file) throws FileNotFoundException, IOException{
		FileInputStream fis = null;
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		try{
			fis = new FileInputStream(file);
			byte[] data = new byte[2048];
			int size = 0;
			while((size=fis.read(data))!=-1){
				bao.write(data, 0, size);
			}
			Map map = new JSONTokener( new String(bao.toByteArray(), "UTF-8") ).parseObject();
			this.objectTarget = map;
		}finally{
			try{ fis.close(); }catch(Exception e){}
			try{ bao.close(); }catch(Exception e){}
		}
	}

	public JSONObject ( Map map )
	{
		this.objectTarget = map;
//		this.objectTarget = new HashMap < String, Object >( map );
	}

	private Map < String, Object >	objectTarget;

	public Map < String, Object > getObjectTarget ()
	{
		return this.objectTarget;
	}

	@Override
	protected Object _get ( Object key )
	{
		return objectTarget.get( key );
	}

	@Override
	public int length ()
	{
		return objectTarget.size();
	}

	public boolean is ( String key )
	{
		return objectTarget.containsKey( key );
	}

	public void remove ( String key )
	{
		objectTarget.remove( key );
	}

	public void put ( String key, Object value )
	{
		objectTarget.put( key, value );
	}
	
	@Override
	public void set ( Object key, Object value )
	{
		objectTarget.put( (String) key, value );
	}

	public Iterator < String > keyIterator ()
	{
		return objectTarget.keySet().iterator();
	}

	public Iterator < Object > valueIterator ()
	{
		return objectTarget.values().iterator();
	}

	public Iterator < Map.Entry < String, Object >> entryIterator ()
	{
		return objectTarget.entrySet().iterator();
	}
	
	public Object get ( String key )
	{
		return _get( key );
	}

	public String getString ( String key )
	{
		return _getString( key, null );
	}

	public String getString ( String key, String def )
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

	public int getInt ( String key )
	{
		return _getInt( key, 0 );
	}

	public int getInt ( String key, int def )
	{
		return _getInt( key, def );
	}

	public long getLong ( String key )
	{
		return _getLong( key, 0L );
	}

	public long getLong ( String key, long def )
	{
		return _getLong( key, def );
	}

	public short getShort ( String key )
	{
		return _getShort( key, (short) 0 );
	}

	public short getShort ( String key, short def )
	{
		return _getShort( key, def );
	}

	public byte getByte ( String key )
	{
		return _getByte( key, (byte) 0 );
	}

	public byte getByte ( String key, byte def )
	{
		return _getByte( key, def );
	}

	public char getChar ( String key )
	{
		return _getChar( key, (char) 0 );
	}

	public char getChar ( String key, char def )
	{
		return _getChar( key, def );
	}

	public float getFloat ( String key )
	{
		return _getFloat( key, 0F );
	}

	public float getFloat ( String key, float def )
	{
		return _getFloat( key, def );
	}

	public double getDouble ( String key )
	{
		return _getDouble( key, 0D );
	}

	public double getDouble ( String key, double def )
	{
		return _getDouble( key, def );
	}

	public JSONObject getJSONObject ( String key )
	{
		return _getJSONObject( key );
	}

	public JSONArray getJSONArray ( String key )
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
