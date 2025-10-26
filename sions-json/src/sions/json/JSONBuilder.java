package sions.json;

import java.io.StringWriter;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.
 * @author JSON.org
 * @version 2012-02-16
 */
public class JSONBuilder implements JSONDirect{

	public JSONBuilder(){
		w = new StringWriter();
	}
	
	protected StringWriter w;
	private boolean start = true;
	
	
	// 오브젝트
	public JSONBuilder so(){
		if(start){
			start = false; 
		}else{
			w.write(",");
		}
		
		w.write("{");
		start = true;
		return this;
	}public JSONBuilder eo(){
		w.write("}");
		start = false;
		return this;
	}
	
	
	// 어레이
	public JSONBuilder sa(){
		if(start){
			start = false; 
		}else{
			w.write(",");
		}
		
		w.write("[");
		start = true;
		return this;
	}public JSONBuilder ea(){
		w.write("]");
		start = false;
		return this;
	}
	
	

	// 벨류를 어레이로 더함
	public JSONBuilder a(Object value){
		if(start){
			start = false; 
		}else{
			w.write(",");
		}
		
		if(value != null){
			if(value instanceof JSONDirect ||
					value instanceof Number ||
					value instanceof Boolean){
				w.write(value.toString());
			}else{
				quote(value.toString());
			}
		}else{
			w.write("null");
		}
		return this;
	}
	
	// 키, 벨류로 더함
	public JSONBuilder a(String key, Object value){
		if(value!=null){
			if(start){
				start = false; 
			}else{
				w.write(",");
			}
			
			quote(key);
			w.write(":");
			if(value instanceof String){
				quote(value.toString());
			}else{
				w.write(value.toString());
			}
		}
		return this;
	}
	
	// 키, 벨류로 더함. 단 벨류는 ""가 없는 상태임. 숫자, boolean 등
	public JSONBuilder as(String key, Object value){
		if(value!=null){
			if(start){
				start = false; 
			}else{
				w.write(",");
			}
			
			quote(key);
			w.write(":");
			w.write(value.toString());
		}
		return this;
	}
	
	// 키만 생성
	public JSONBuilder ad(String key){
		if(start){
			start = false; 
		}else{
			w.write(",");
		}
		
		quote(key);
		w.write(":");
		start = true;
		return this;
	}

	// 키, 벨류로 더함
	public JSONBuilder undefined(){
		if(start){
			start = false; 
		}else{
			w.write(",");
		}
		
		w.write("null");
		return this;
	}
    private JSONBuilder quote(String string){
        if (string == null || string.length() == 0) {
            w.write("\"\"");
            return this;
        }

        char b;
        char c = 0;
        String hhhh;
        int i;
        int len = string.length();

        w.write('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                w.write('\\');
                w.write(c);
                break;
            case '/':
                if (b == '<') {
                    w.write('\\');
                }
                w.write(c);
                break;
            case '\b':
                w.write("\\b");
                break;
            case '\t':
                w.write("\\t");
                break;
            case '\n':
                w.write("\\n");
                break;
            case '\f':
                w.write("\\f");
                break;
            case '\r':
                w.write("\\r");
                break;
            default:
                if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
                        || (c >= '\u2000' && c < '\u2100')) {
                    w.write("\\u");
                    hhhh = Integer.toHexString(c);
                    w.write("0000", 0, 4 - hhhh.length());
                    w.write(hhhh);
                } else {
                    w.write(c);
                }
            }
        }
        w.write('"');
        return this;
    }

	@Override
    public String toString() {
        return w.toString();
    }
}
