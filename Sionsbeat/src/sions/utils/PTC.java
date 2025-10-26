package sions.utils;

import java.util.ArrayList;

import android.util.Log;

public class PTC
{
	private static ArrayList<Long> timeHistory = new ArrayList();

	public static int THRESHOLD = -1;	
	public static int VISIBLE_LOG_ID = -1;
	
	public static final synchronized int start()
	{
		int index = timeHistory.size();
		timeHistory.add( System.currentTimeMillis() );
		
		return index;
	}
	
	public static final void end(int index, String tag, String text)
	{
		if(VISIBLE_LOG_ID == index){
			long time = timeHistory.get(index);
			time = System.currentTimeMillis()-time;
			
			if(time > THRESHOLD){
				Log.d(tag, String.format("%5dms : %s", time, text));
			}
		}
	}
	
	public static final int restart(int index, String tag, String text)
	{
		if(VISIBLE_LOG_ID == index){
			end(index, tag, text);
			timeHistory.set( index, System.currentTimeMillis() );
		}
		return index;
	}
	
}
