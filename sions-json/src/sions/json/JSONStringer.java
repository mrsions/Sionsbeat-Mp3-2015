package sions.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import sions.json.annotation.JsonColumn;
import sions.json.annotation.JsonIgnore;


public class JSONStringer {

	public static final int MODIFY_PUBLIC	= 0x01;
	public static final int MODIFY_FINAL	= 0x10;
	public static final int MODIFY_STATIC	= 0x08;
	
	public static boolean PARSE_POJO_CLASS = false;
	
	
	public JSONStringer () {
		jb = new JSONBuilder();
	}
	
	private JSONBuilder jb;
	
	@Override
	public String toString() {
		return jb.toString();
	}
	
	public JSONStringer parse(Object obj){
		if(obj == null){
			jb.undefined();
		}else if(obj instanceof Number ||
				obj instanceof Boolean ||
				obj instanceof Character ||
				obj instanceof String ||
				obj instanceof JSONDirect){
			jb.a(obj);
		}else if(obj instanceof JSONObject){
			parseJSONObject((JSONObject)obj);
		}else if(obj instanceof JSONArray){
			parseJSONArray((JSONArray)obj);
		}else if(obj instanceof Map){
			parseMap(((Map<String, Object>)obj).entrySet().iterator());
		}else if(obj instanceof Collection){
			parseList(((Collection<Object>)obj).iterator());
		}else if(obj.getClass().isArray()){
			parseArray(obj);
		}else{
			if(PARSE_POJO_CLASS){
				parseClass(obj);
			}
		}
		
		return this;
	}

	private void parseJSONObject(JSONObject obj){
		parseMap(obj.entryIterator());
	}
	private void parseMap(Iterator<Entry<String, Object>> it){
		jb.so();
		
		Entry<String, Object> e;
		while(it.hasNext()){
			e = it.next();

			jb.ad(e.getKey());
			parse(e.getValue());
			
		}
		
		jb.eo();
	}
	
	private void parseJSONArray(JSONArray obj){
		parseList(obj.iterator());
	}
	private void parseList(Iterator<Object> it){
		jb.sa();
		
		while(it.hasNext()){
			parse(it.next());
		}
		
		jb.ea();
	}
	private void parseArray(Object it){
		jb.sa();
		
		int length = Array.getLength(it);
		for(int i=0; i<length ;i++){
			parse(Array.get(it, i));
		}
		
		jb.ea();
	}
	
	private void parseClass(Object obj){
		Class cls = obj.getClass();
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		Field[] fields = cls.getFields();
		JsonColumn anno;
		String name;
		for(Field field:fields){
			try{
				name = field.getName();
				
				if(!field.isAccessible()){
					field.setAccessible(true);
				}
				
				//-- 오로지 public만 저장한다. static final 데이터는 저장하지 않는다.
				if(field.getModifiers() != MODIFY_PUBLIC) continue; 
				
				if(field.getAnnotation(JsonIgnore.class) != null) continue;
				
				if((anno = field.getAnnotation(JsonColumn.class)) != null){
					if( anno.name().length() != 0){
						name = anno.name();
					}
				}
				
				map.put(name, field.get(obj));
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}


		Method[] methods = cls.getMethods();
		for(Method method : methods){
			name = method.getName();
			if(method.getParameterTypes().length == 0 && name.startsWith("get") && Character.isUpperCase(name.charAt(3))){
				try{

					name = Character.toLowerCase(name.charAt(3))+name.substring(4);
					if(!method.isAccessible()){
						method.setAccessible(true);
					}
					
					//-- 오로지 public만 저장한다. static final 데이터는 저장하지 않는다.
					if(method.getModifiers() != MODIFY_PUBLIC) continue;

					if(method.getAnnotation(JsonIgnore.class) != null) continue;
					
					if((anno = method.getAnnotation(JsonColumn.class)) != null){
						if( anno.name().length() != 0){
							name = anno.name();
						}
					}
					
					if(!map.containsKey(name)){
						
						map.put(name, method.invoke(obj));
						
					}
					
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
		}
		
		parseMap(map.entrySet().iterator());
	}
	
	
}
