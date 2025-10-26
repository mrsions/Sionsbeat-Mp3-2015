package sions.android.sionsbeat.zresource;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import sions.android.sionsbeat.utils.ErrorController;
import android.graphics.Bitmap;
import android.util.Log;

public class ZAnimation extends ZAnimate{

	public ZAnimation(){}
	public ZAnimation(ZResource res, File file){
		this.res = res;
		this.prop = new Properties();

		FileInputStream fis = null;
		try{
			fis = new FileInputStream(file);
			prop.load(fis);
		}catch(Throwable e){
			ErrorController.error(10, e);
		}finally{
			try{fis.close();}catch(Throwable e){}
		}

		key = prop.getProperty("type");
		playCount = Integer.parseInt(prop.getProperty("play", "0"));
		delay = Integer.parseInt(prop.getProperty("delay", "40"));
		bitmaps = new Bitmap[Integer.parseInt(prop.getProperty("length", "0"))];
		
		prop = null;
	}
	public ZAnimation(ZResource res, Properties prop){
		this.res = res;
		this.prop = prop;

		key = prop.getProperty("type");
		playCount = Integer.parseInt(prop.getProperty("play", "0"));
		delay = Integer.parseInt(prop.getProperty("delay", "40"));
		bitmaps = new Bitmap[Integer.parseInt(prop.getProperty("length", "0"))];
		
		this.prop = null;
	}
	
	private ZResource res;
	
	private String key;
	private Properties prop;
	
	private int delay;
	private int playCount;
	private Bitmap[] bitmaps = new Bitmap[0];
	private long startTime;
	
	private boolean prepared = false;
	private boolean bitmapNull = false;
	
	@Override
	public void prepare(int width, int height)
	{
		synchronized(this){
			if(!prepared){
				prepared = true;
				for(int i=0; i<bitmaps.length; i++)
				{
					String key = this.key+"/"+i;
					
					bitmaps[i] = res.getBitmap(key, width, height);
					
					res.remove(key);
				}
			}
		}
	}

	@Override
	public void start(long time)
	{
		this.startTime = time;
	}

	@Override
	public Bitmap getBitmap(long time)
	{
		int idx = (int)( ( time - startTime ) / delay ) % (bitmaps.length);

		if(bitmapNull){
			if(idx < 0 || idx >= bitmaps.length){
				return null;
			}
		}else if(idx < 0){
			idx = 0;
		}else if(idx >= bitmaps.length){
			idx = bitmaps.length-1;
		}
		return bitmaps[idx];
	}

	@Override
	public boolean isPlay(long time)
	{
		try{
			if(bitmaps == null){
				return false;
			}else if(playCount == -1){
				return true;
			}else{
				int oneOfTime = delay * bitmaps.length;
				if(oneOfTime == 0){
					return false;
				}else{
					return ((time-startTime) / oneOfTime) < playCount;
				}
			}
		}catch(NullPointerException e){
			return false;
		}
	}

	@Override
	public ZAnimation clone(){
		
		ZAnimation anim = new ZAnimation();
		anim.delay = this.delay;
		anim.playCount = this.playCount;
		anim.bitmaps = this.bitmaps;
		anim.startTime = this.startTime;
		
		return anim;
	}

	@Override
	public void dispose() {
		bitmaps = null;
	}

	@Override
        public void recycle () {
		if(bitmaps != null){
			for(Bitmap bitmap: bitmaps){
				if(bitmap != null){
					bitmap.recycle();
				}
			}
		}
        }
	
	public String getKey(){
		return key;
	}
	public Bitmap[] getBitmaps () {
		return bitmaps;
	}
	public boolean isPrepared(){
		return prepared;
	}
	public boolean isBitmapNull () {
		return bitmapNull;
	}
	public void setBitmapNull (boolean bitmapNull) {
		this.bitmapNull = bitmapNull;
	}
}
