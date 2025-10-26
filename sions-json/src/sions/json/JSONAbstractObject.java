package sions.json;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class JSONAbstractObject
{

	private JSONAbstractObject	parent;

	private Object			name;

	public Object getName ()
	{
		return name;
	}

	public void setName ( Object name )
	{
		this.name = name;
	}

	public JSONAbstractObject getParent ()
	{
		return parent;
	}

	public void setParent ( JSONAbstractObject parent )
	{
		this.parent = parent;
	}

	protected abstract Object _get ( Object key );

	public abstract int length ();

	public abstract String toStringFile ();
	
	public abstract void set (Object key, Object val);

	protected String _getString ( Object key, String def )
	{
		Object obj = _get( key );

		if ( obj == null )
		{
		}
		else if ( obj instanceof String )
		{
			return (String) obj;
		}
		else
		{
			set( key, def = obj.toString() );
		}

		return def;
	}

	protected int _getInt ( Object key, int def )
	{
		Object obj = _get( key );

		if ( obj == null )
		{
		}
		else if ( obj instanceof Integer )
		{
			return (Integer) obj;
		}
		else if ( obj instanceof Number )
		{
			set( key, def = ( (Number) obj ).intValue() );
		}
		else if ( obj instanceof String )
		{
			try
			{
				set( key, def = Integer.parseInt( (String) obj ) );
			}
			catch ( NumberFormatException e )
			{
			}
		}
		return def;
	}

	protected long _getLong ( Object key, long def )
	{
		Object obj = _get( key );

		if ( obj == null )
		{
		}
		else if ( obj instanceof Long )
		{
			return (Long) obj;
		}
		else if ( obj instanceof Number )
		{
			set( key, def = ( (Number) obj ).longValue() );
		}
		else if ( obj instanceof String )
		{
			try
			{
				set( key, def = Long.parseLong( (String) obj ) );
			}
			catch ( NumberFormatException e )
			{
			}
		}
		return def;
	}

	protected short _getShort ( Object key, short def )
	{
		Object obj = _get( key );

		if ( obj == null )
		{
		}
		else if ( obj instanceof Short )
		{
			return (Short) obj;
		}
		else if ( obj instanceof Number )
		{
			set( key, def = ( (Number) obj ).shortValue() );
		}
		else if ( obj instanceof String )
		{
			try
			{
				set( key, def = Short.parseShort( (String) obj ) );
			}
			catch ( NumberFormatException e )
			{
			}
		}
		return def;
	}

	protected byte _getByte ( Object key, byte def )
	{
		Object obj = _get( key );

		if ( obj == null )
		{
		}
		else if ( obj instanceof Byte )
		{
			return (Byte) obj;
		}
		else if ( obj instanceof Number )
		{
			set( key, def = ( (Number) obj ).byteValue() );
		}
		else if ( obj instanceof String )
		{
			try
			{
				set( key, def = Byte.parseByte( (String) obj ) );
			}
			catch ( NumberFormatException e )
			{}
		}
		return def;
	}

	protected char _getChar ( Object key, char def )
	{
		Object obj = _get( key );

		if ( obj == null )
		{
		}
		else if ( obj instanceof Character )
		{
			return (Character) obj;
		}
		else if ( obj instanceof String )
		{
			String target = (String) obj;
			if ( target.length() == 0 )
			{
				return def;
			}
			else
			{
				return target.charAt( 0 );
			}
		}
		return def;
	}

	protected float _getFloat ( Object key, float def )
	{
		Object obj = _get( key );

		if ( obj == null )
		{
		}
		else if ( obj instanceof Float )
		{
			return (Float) obj;
		}
		else if ( obj instanceof Number )
		{
			set( key, def = ( (Number) obj ).floatValue() );
		}
		else if ( obj instanceof String )
		{
			try
			{
				set( key, def = Float.parseFloat( (String) obj ) );
			}
			catch ( NumberFormatException e )
			{
			}
		}
		return def;
	}

	protected double _getDouble ( Object key, double def )
	{
		Object obj = _get( key );

		if ( obj == null )
		{
		}
		else if ( obj instanceof Double )
		{
			return (Double) obj;
		}
		else if ( obj instanceof Number )
		{
			set( key, def =  ( (Number) obj ).doubleValue() );
		}
		else if ( obj instanceof String )
		{
			try
			{
				set( key, def = Double.parseDouble( (String) obj ) );
			}
			catch ( NumberFormatException e )
			{
			}
		}
		return def;
	}

	protected boolean _getBoolean ( String key, boolean def )
	{
		Object obj = _get( key );
		
		if ( obj == null )
		{
		}
		else if ( obj instanceof Boolean )
		{
			return (Boolean) obj;
		}
		else if ( obj instanceof Number )
		{
			set( key, def = ( (Number) obj ).intValue() != 0);
		}
		else if ( obj instanceof String )
		{
			try
			{
				set( key, def = Boolean.parseBoolean( (String) obj ) );
			}
			catch ( NumberFormatException e )
			{
			}
		}
		return def;
	}

	protected JSONObject _getJSONObject ( Object key )
	{
		Object obj = _get( key );
		JSONObject result = null;

		if ( obj instanceof JSONObject )
		{
			result = (JSONObject) obj;
		}
		else if ( obj instanceof Map )
		{
			set( key, result = new JSONObject( (Map) obj ));
		}
		else if ( obj instanceof String )
		{
			set( key, result = new JSONObject( (String) obj ));
		}

		if ( result != null )
		{
			result.setParent( this );
		}

		return result;
	}

	protected JSONArray _getJSONArray ( Object key )
	{
		Object obj = _get( key );
		JSONArray result = null;

		if ( obj instanceof JSONArray )
		{
			result = (JSONArray) obj;
		}
		else if ( obj instanceof String )
		{
			set( key, result = new JSONArray( (String) obj ));
		}
		else if ( obj instanceof Collection )
		{
			set( key, result = new JSONArray( (Collection) obj ));
		}
		else if ( obj instanceof Object )
		{
			if ( obj.getClass().isArray() )
			{
				set( key, result = new JSONArray( (Collection) obj ));
			}
		}

		if ( result != null )
		{
			result.setParent( this );
		}

		return result;
	}

}
