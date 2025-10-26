package sions.android.sionsbeat.window;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import sions.android.sionsbeat.MusicSelectActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.zresource.ZAnimate;
import sions.android.sionsbeat.zresource.ZResource;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.GridLayout;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class MarkerSelectPopup extends AbsPopup implements Runnable
{

	/************************************************************************************************************
	 * 
	 * 				@STATIC_FIELDS
	 * 
	 ************************************************************************************************************/

	/************************************************************************************************************
	 * 
	 *				 @CONSTRUCTOR
	 * 
	 ************************************************************************************************************/
	
	public MarkerSelectPopup ( MusicSelectActivity context )
	{
		super( context );
		this.activity = context;
		this.map = new HashMap<String, Marker>();
	}
	
	/************************************************************************************************************
	 * 
	 * 				@MEMBER_FIELDS
	 * 
	 ************************************************************************************************************/
	
	private MusicSelectActivity activity;
	private GridLayout container;
	private DisplayMetrics displayMetrics;
	
	private HashMap<String, Marker> map;
	
	private View view;
	private Marker selectMarker;
	private ZAnimate animate;
	
	private Thread runThread;
	
	private int window_margin;
	private int background_margin;
	private int marker_size;
	private int width;
	private int columnSize;
	
	/************************************************************************************************************
	 * 
	 * 				@OVERRIDE_METHODS
	 * 
	 ************************************************************************************************************/

	public boolean show ()
	{
		this.view = LayoutInflater.from(getContext()).inflate( R.layout.window_marker, null );

		/**@TRACKING**/ErrorController.tracking(getContext(), this, "show", "", 0, true);
		
		setupDefaults();
		setupEvent();
		if(!setupPref()){
			return false;
		}
		setupList();
		return super.show( this.view );
	}
	
	/************************************************************************************************************
	 * 
	 * 				@ACTIVE_METHODS
	 * 
	 ************************************************************************************************************/
	
	private void setupDefaults(){

		this.container = (GridLayout) view.findViewById(R.id.container);
		this.displayMetrics = getContext().getResources().getDisplayMetrics();
		
		width = displayMetrics.widthPixels;
		window_margin = getContext().getResources().getDimensionPixelOffset(R.dimen.popup_window_margin);
		background_margin = getContext().getResources().getDimensionPixelOffset(R.dimen.markerselect_background_margin);
		marker_size = getContext().getResources().getDimensionPixelOffset(R.dimen.markerselect_marker_size);
		
		columnSize = ( width - ( window_margin+background_margin )*2 ) / marker_size;
		
		this.container.setColumnCount(columnSize);
		
	}
	
	private void setupEvent(){

	}
	
	private boolean setupPref()
	{
		
		File markerDir = new File(GameOptions.get(getContext()).getRootFile(), "marker");
		if(!markerDir.exists() || !markerDir.isDirectory()){
			Toast.makeText(getContext(), R.string.marker_notfound_markerFolder, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		File[] files = markerDir.listFiles();
		File usedMarker = GameOptions.get(getContext()).getMarkerFile();
		
		for(File file: files){
			String name = file.getName();
			name = name.substring(name.lastIndexOf("/")+1);

			if(file.isDirectory()){
			
				Marker marker = new Marker();
				marker.name = name;
				marker.root = file;
				map.put(name,  marker);
				
				if(file.getAbsolutePath().equalsIgnoreCase(usedMarker.getAbsolutePath())){
					this.selectMarker = marker;
				}
			
			}
		}
		
		if(map.size() == 0){
			Toast.makeText(getContext(), R.string.marker_notfound_markerFolder, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}
	
	private void setupList(){
		
		Iterator<Entry<String, Marker>> it = map.entrySet().iterator();
		Entry<String, Marker> e;
		
		while(it.hasNext()){
			
			e = it.next();
			Marker marker = e.getValue();
			
			Bitmap bitmap = null;
			if((bitmap = marker.getBitmap()) == null){
				continue;
			}
			
			marker.imageView = new ImageView(getContext());
			marker.imageView.setImageBitmap(bitmap);
			marker.imageView.setBackgroundResource(R.drawable.marker_background);
			
			ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(marker_size, marker_size);
			container.addView(marker.imageView, lp);

			final Marker mMarker = marker;
			
			marker.imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick (View v) {
					try {
						activity.getGoogleGameService().achivementsIncrement(R.string.achievement_lets_change_the_marker, 1);
						
	                                        setupMarker(mMarker);

	        				ErrorController.tracking(getContext(), this, "marker_change", mMarker.name, 0, true);
                                        } catch (Exception e) {
                        			ErrorController.error(10, e);
                                        }
				}
			});
			
			map.put(e.getKey(), e.getValue());
		}
		
		if(selectMarker != null){
			try {
				Marker marker = selectMarker;
				selectMarker = null;
	                        setupMarker(marker);
                        } catch (Exception ex) {
	                        ex.printStackTrace();
                        }
		}
		
		runThread = new Thread(this);
		runThread.start();
		
	}
	
	private void setupMarker(Marker marker) throws Exception{
		/**@TRACKING**/ErrorController.tracking(getContext(), this, "setupMarker", marker.name, 0, true);
		
		synchronized(this){
			
			if(marker != selectMarker){
				if(selectMarker != null){
					selectMarker.imageView.setImageBitmap(selectMarker.getBitmap());
					animate = null;
					selectMarker.resource.recycle();
					selectMarker.resource = null;
					
				}
				
				try{
					selectMarker = marker;
					
					if(marker.resource == null){
						marker.resource = new ZResource(marker.root);
					}
					animate = marker.resource.getAnimation("intro", 100, 100);
					animate.start(System.currentTimeMillis());
					
					GameOptions.get(getContext()).put("marker", marker.name);
					
				}catch(OutOfMemoryError e)
				{
					Toast.makeText(getContext(), R.string.out_of_memoery, Toast.LENGTH_SHORT).show();
				}finally{
					GameOptions.get(getContext()).put("marker", marker.name);
				}
				
			}
		}
	}
	
	@Override
	public boolean dispose () {
		/**@TRACKING**/ErrorController.tracking(getContext(), this, "setupMarker", "dispose", 0, true);

		Thread t = runThread;
		runThread = null;
		
		while(t != null){
			try{
				t.join();
				break;
			}catch(Throwable e){}
		}

		boolean result = super.dispose();

		selectMarker = null;
		animate = null;
		
		if(map != null && map.size() > 0){
			for(Marker marker: map.values()){
				if(marker.imageView != null)
				{
					try{ marker.imageView.setImageDrawable(null);
					}catch(Throwable e){
						e.printStackTrace();
					}
				}
				if(marker.bitmap != null){
					marker.bitmap.recycle();
				}
				if(marker.resource != null){
					marker.resource.recycle();
				}
				marker.imageView = null;
				marker.root = null;
				marker.name = null;
				marker.resource = null;
			}
		}
		map = null;
		
		return result;
	}

	public void run(){
		Bitmap before = null;
		
		while(runThread != null){
			try{
				synchronized (this){
					final Marker marker = selectMarker;
					final ZAnimate anim= animate;
					
					if(marker != null && anim != null){
						
						long time = System.currentTimeMillis();
						if(!anim.isPlay(time)){
							anim.start(time);
						}
						
						final Bitmap bitmap = anim.getBitmap(time);
						if(bitmap != before){
							before = bitmap;
							getContext().runOnUiThread(new Runnable(){
								public void run(){
									synchronized(MarkerSelectPopup.this){
										if(selectMarker == marker && ( bitmap == null || !bitmap.isRecycled())){
											selectMarker.imageView.setImageBitmap(bitmap);
										}
									}
								}
							}); 
						}
					}
				}
				
				Thread.sleep(10);
				
			}catch(Throwable e){
				ErrorController.error(10, e);
			}
		}
	}
	
	/************************************************************************************************************
	 * 
	 * 				@ACCESS_METHODS
	 * 
	 ************************************************************************************************************/
	
	public int getDipToPx(float dip){
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics);
	}
	
	private class Marker {
		
		private String name;
		private Bitmap bitmap;
		private File root;
		private ImageView imageView;
		private ZResource resource;
		
		public Bitmap getBitmap(){
			
			if(bitmap == null || bitmap.isRecycled()){

				File file = new File(root, "intro/14.png");
				if(!file.exists()) file = new File(root, "intro/14.jpg");
				if(!file.exists()) file = new File(root, "intro/14.gif");
				if(!file.exists()) file = new File(root, "intro/14");
				
				if(!file.exists()){
					return null;
				}
				
				try{
					bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
				}catch(Throwable ex){
					ErrorController.error(10, ex);
				}
			}
			return bitmap;
		}
		
	}
}
