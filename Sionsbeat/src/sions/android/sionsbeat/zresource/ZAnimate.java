package sions.android.sionsbeat.zresource;

import java.util.Properties;

import android.graphics.Bitmap;

public abstract class ZAnimate {

	public ZAnimate(){}
	
	public abstract void prepare(int width, int height);
	
	public abstract void start(long time);

	public abstract Bitmap getBitmap(long time);
	
	public abstract boolean isPlay(long time);
	
	public abstract ZAnimate clone();

	public abstract void dispose();
	
	public abstract void recycle();
}
