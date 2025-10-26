package sions.android.sionsbeat.utils;

import java.util.ArrayList;

import sions.android.sionsbeat.R;
import sions.android.sionsbeat.game.view.CapturedView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class ViewCapture {

	public static Bitmap capture(View view)
	{
		return capture(view, 0);
	}
	
	public static Bitmap capture(View view, int height)
	{
		Bitmap bitmap = null; 
		try{
			view.buildDrawingCache();
			bitmap = view.getDrawingCache();
			
			if(height != 0)
			{
				Bitmap src = Bitmap.createBitmap(bitmap.getWidth(), height, Bitmap.Config.ARGB_8888);
				
				Canvas canvas = new Canvas(src);
				canvas.drawBitmap(bitmap, 0, 0, null);
				
				bitmap = src;
				Log.d("test","draw");
			}
			
			ArrayList<CapturedView> list = new ArrayList<CapturedView>();
			findSurfaceView(view, list);
			
			if(list.size() > 0 && bitmap != null)
			{
				Canvas canvas = new Canvas(bitmap);
				for(CapturedView v: list){
					
					View vv = (View)v;
					
					canvas.save();
					canvas.translate(getX(vv), getY(vv));
					
					Bitmap bm = v.onCapturBitmap(canvas);
					if(bm != null){
						canvas.drawBitmap(bm, 0,  0, null);
						bm.recycle();
					}
					
					canvas.restore();
				}
			}
			return bitmap;
		}catch(OutOfMemoryError e){
			e.printStackTrace();
		}catch(Throwable e){
			ErrorController.error(10, e);
		}
		return bitmap;
	}
	
	public static float getX(View view)
	{
		try{
			return view.getX();
		}catch(Throwable e){
			return view.getLeft();
		}
	}

	public static float getY(View view)
	{
		try{
			return view.getY();
		}catch(Throwable e){
			return view.getTop();
		}
	}
	
	private static void findSurfaceView(View view, ArrayList<CapturedView> list){
		if(view instanceof ViewGroup){
			ViewGroup group = (ViewGroup)view;
			
			for(int i=0; i<group.getChildCount(); i++){
				findSurfaceView(group.getChildAt(i), list);
			}
		}else if(view instanceof CapturedView){
			list.add((CapturedView)view);
		}
	}
	
}
