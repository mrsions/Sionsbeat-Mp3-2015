package sions.android.sionsbeat.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import sions.android.SQ;
import sions.android.sionsbeat.SplashActivity;
import android.content.Context;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

public class ErrorController {
	
	public static String viewPage = "";
	public static String action = "";
	public static String action2 = "";
	public static int value = 0;
	public static void tracking(Context context, Object target, String action, String action2, int value){
		tracking(context, target, action, action2, value, true);
	}
	public static void tracking(Context context, Object target, String action, String action2, int value, boolean send){
		if(target == null){
			viewPage = null;
		}else if(target instanceof String)
		{
			viewPage = (String) target;
		}
		else{
			viewPage = target.getClass().getName();
		}
		
		ErrorController.action = action;
		ErrorController.action2 = action2;
		ErrorController.value = value;
		
		if(send) if(SplashActivity.STORE_NETWORK_STATE) EasyTracker.getInstance(context).send(MapBuilder.createEvent(viewPage, action, action2, (long)value).build());
	}

	public static void error(int level, Throwable e){
		
		e.printStackTrace();
		
		if(!SplashActivity.STORE_NETWORK_STATE) return;
		Context context = SQ.SQ().getContext();
		
		try{
			EasyTracker.getInstance(context).send(MapBuilder.createException(new StandardExceptionParser(context, null).getDescription(Thread.currentThread().getName(), e), false).build());
		}catch(Throwable ee){
			ee.printStackTrace();
		}

		try{
			inputTrackingData(e);
			
			BugSenseHandler.sendException( (Exception) e );
			BugSenseHandler.flush( context );
			
		}catch(Throwable ee){
			ee.printStackTrace();
		}
		
	}
	
	public static void setupUncaughtException()
	{
		
		final UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
		Log.d("test", handler+"");
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException (Thread thread, Throwable ex) {
				
				if(!SplashActivity.STORE_NETWORK_STATE){
					Log.e("CriticalException", thread.getName(), ex);
					System.exit(0);
					return;
				}
				inputTrackingData(ex);
				
				if(handler != null){
					handler.uncaughtException(thread, ex);
				}
			}
		});
	}
	
	private static void inputTrackingData(Throwable ex){
		
		try{
			StackTraceElement[] stacks = ex.getStackTrace();
			StackTraceElement[] copys = new StackTraceElement[stacks.length+1];
			System.arraycopy(stacks, 0, copys, 0, stacks.length);
			copys[copys.length-1] = new StackTraceElement(viewPage, action, action2, value);
			
			ex.setStackTrace(copys);
		}catch(Throwable exx){
			ErrorController.error(10, exx);
		}
	}
	
}
