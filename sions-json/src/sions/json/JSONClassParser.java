package sions.json;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

public class JSONClassParser {

	public <T> T parse(JSONObject obj, Class<T> cls){
		T instance = null;
		try{
			
			instance = cls.newInstance();
			_parseMap(cls, instance, obj.entryIterator());
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return instance;
	}

	public <T> T parse(JSONArray obj, Class<T> cls){
		T instance = null;
		try{
			instance = cls.newInstance();
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return instance;
	}
	
	
	private void _parseMap(Class cls, Object target, Iterator<Entry<String, Object>> it){

		Entry<String, Object> entry;
		while(it.hasNext()){
			entry = it.next();
			_parse(cls, target, entry.getKey(), entry.getValue());
		}
		
	}
	
	private void _parseArray(Class cls, Object target, Iterator<Object> it){

		while(it.hasNext()){
//			_parse(cls, target, it.next());
		}
		
	}
	
	private void _parse(Class cls, Object target, String name, Object obj){
		if(obj == null) return;
		
		boolean isArray = obj.getClass().isArray();
		
		try{
			Field field = null;
			do{
				field = cls.getField(name);
				if(field != null){

					if(!field.isAccessible()) field.setAccessible(true);
					
					Class type = field.getType();
					if(type.isArray()){
						
						
						
					}else{
						
						if(type == Long.class || type == long.class){
							field.setLong(target, getLong(obj));
						}else if(type == Integer.class || type == int.class){
							field.setInt(target, getInt(obj));
						}else if(type == Short.class || type == short.class){
							field.setShort(target, getShort(obj));
						}else if(type == Byte.class || type == byte.class){
							field.setByte(target, getByte(obj));
						}else if(type == Character.class || type == char.class){
							field.setChar(target, getChar(obj));
						}else if(type == Float.class || type == float.class){
							field.setFloat(target, getFloat(obj));
						}else if(type == Double.class || type == double.class){
							field.setDouble(target, getDouble(obj));
						}else if(type == String.class){
							field.set(target, getString(obj));
						}else if(obj instanceof JSONAbstractObject){
							Object typeTarget = field.get(target);
							if(typeTarget != null){
								if(obj instanceof JSONObject){
									_parseMap(type, typeTarget, ((JSONObject)obj).entryIterator());
								}else if(obj instanceof JSONArray){
									_parseArray(type, typeTarget, ((JSONArray)obj).iterator());
								}
							}else{
								if(obj instanceof JSONObject){
									field.set(target, parse((JSONObject)obj, type));
								}else if(obj instanceof JSONArray){
									field.set(target, parse((JSONArray)obj, type));
								}
							}
						}
						
					}
					return;
					
				}
				
			}while((cls = cls.getSuperclass()) != null && cls != Object.class);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	

	protected String getString(Object obj){
		if(obj == null){
			throw new NullPointerException();
		}else if(obj instanceof String){
			return (String)obj;
		}else{
			return obj.toString();
		}
		
	}

	protected int getInt(Object obj){
		if(obj == null){
			throw new NullPointerException();
		}else if(obj instanceof Integer){
			return (Integer)obj;
		}else if(obj instanceof Number){
			return ((Number)obj).intValue();
		}else if(obj instanceof String){
			return Integer.parseInt((String)obj);
		}else{
			throw new NullPointerException();
		}
	}

	protected long getLong(Object obj){
		if(obj == null){
			throw new NullPointerException();
		}else if(obj instanceof Long){
			return (Long)obj;
		}else if(obj instanceof Number){
			return ((Number)obj).longValue();
		}else if(obj instanceof String){
			return Long.parseLong((String)obj);
		}else{
			throw new NullPointerException();
		}
	}

	protected short getShort(Object obj){
		if(obj == null){
			throw new NullPointerException();
		}else if(obj instanceof Short){
			return (Short)obj;
		}else if(obj instanceof Number){
			return ((Number)obj).shortValue();
		}else if(obj instanceof String){
			return Short.parseShort((String)obj);
		}else{
			throw new NullPointerException();
		}
	}

	protected byte getByte(Object obj){
		if(obj == null){
			throw new NullPointerException();
		}else if(obj instanceof Byte){
			return (Byte)obj;
		}else if(obj instanceof Number){
			return ((Number)obj).byteValue();
		}else if(obj instanceof String){
			return Byte.parseByte((String)obj);
		}else{
			throw new NullPointerException();
		}
	}

	protected char getChar(Object obj){
		if(obj == null){
			throw new NullPointerException();
		}else if(obj instanceof Character){
			return (Character)obj;
		}else if(obj instanceof String){
			String target = (String)obj;
			if(target.length() == 0){
				throw new NullPointerException();
			}else{
				return target.charAt(0);
			}
		}else{
			throw new NullPointerException();
		}
	}

	protected float getFloat(Object obj){
		if(obj == null){
			throw new NullPointerException();
		}else if(obj instanceof Float){
			return (Float)obj;
		}else if(obj instanceof Number){
			return ((Number)obj).floatValue();
		}else if(obj instanceof String){
			return Float.parseFloat((String)obj);
		}else{
			throw new NullPointerException();
		}
	}

	protected double getDouble(Object obj){
		if(obj == null){
			throw new NullPointerException();
		}else if(obj instanceof Double){
			return (Double)obj;
		}else if(obj instanceof Number){
			return ((Double)obj).doubleValue();
		}else if(obj instanceof String){
			return Double.parseDouble((String)obj);
		}else{
			throw new NullPointerException();
		}
	}
}
