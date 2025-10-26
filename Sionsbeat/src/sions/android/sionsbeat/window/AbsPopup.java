package sions.android.sionsbeat.window;

import java.io.File;
import java.io.FileOutputStream;

import sions.android.sionsbeat.R;
import sions.android.sionsbeat.game.GameMode;
import sions.android.sionsbeat.utils.BlurUtils;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.ViewCapture;
import sions.utils.PTC;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public abstract class AbsPopup implements View.OnClickListener
{
	public interface PopupListener
	{
		public void onPopupClose(AbsPopup popup);
	}

	public AbsPopup(Activity context){
		this.context = context;
	}
	
	private Activity context;
	private View view;
	private View windowContainer;
	private PopupWindow mPopupWindow;
	private PopupListener listener;
	
	private Bitmap background;
	private int height;

	protected boolean show(View view){
		return show(view, Gravity.FILL, false);
	}
	protected boolean show(View view, boolean focus){
		return show(view, Gravity.FILL, focus);
	}
	protected boolean show(View view, int gravity, boolean focus){
		if(mPopupWindow != null && mPopupWindow.isShowing()) return false;

		this.view = view;
		mPopupWindow = new PopupWindow( view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, focus );
		onBeforeWindowSetup(mPopupWindow);
		
		View close = view.findViewById( R.id.close );
		if(close != null){
			close.setOnClickListener( this );
		}
		
		bluringBackground(view);

		if(height != 0) mPopupWindow.setHeight(height);
		Log.d("test",height+"");
		mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
		mPopupWindow.showAtLocation(view, Gravity.TOP, 0, 0);
		
		return true;
	}
	
	protected void onBeforeWindowSetup(PopupWindow window){
		
	}
	
	private void bluringBackground(View view){

		windowContainer = view.findViewById(R.id.window_container);
		final View activityContainer = getContext().findViewById(R.id.activity_container);
		
		if(activityContainer == null || windowContainer == null){
			return;
		}
		
		View adView = context.findViewById(R.id.adView);
		if(adView!= null){
			height = (int) ViewCapture.getY((View) adView.getParent());
		}
		
		if(GameOptions.get(getContext()).getBoolean(GameOptions.OPTION_POPUP_BLUR, true)){
		
			Bitmap bitmap = ViewCapture.capture(activityContainer, height);
			if(bitmap != null){
			
				background = BlurUtils.fastblur(getContext(), bitmap,5, 25);
				if(background != null){
					final BitmapDrawable drawable = new BitmapDrawable(getContext().getResources(), background);
					windowContainer.setBackgroundDrawable(drawable);
				}
				return;
			}
		
		}
		
		view.setBackgroundDrawable(null);
	}
	
	public void setBackground(boolean visible){
		final View windowContainer = view.findViewById(R.id.window_container);
		
		if(windowContainer == null) return;
		
		if(visible){
			if(background != null){
				final BitmapDrawable drawable = new BitmapDrawable(getContext().getResources(), background);
				windowContainer.setBackgroundDrawable(drawable);
			}else{
				bluringBackground(view);
			}
		}else{
			windowContainer.setBackgroundDrawable(null);
		}
	}
	
	public boolean dispose(){
		if(mPopupWindow==null || !mPopupWindow.isShowing()) return false;
		
		PopupListener mListener = listener;
		
		listener = null;
		mPopupWindow.dismiss();
		mPopupWindow = null;
		view = null;
		
		if(background != null){
			if( windowContainer != null)
			{
				windowContainer.setBackgroundDrawable(null);
			}
			background.recycle();
			background = null;
		}
		
		if(mListener != null) mListener.onPopupClose( this );
		return true;
	}
	

	@Override
	public void onClick ( View v )
	{
		switch(v.getId()){
			case R.id.close:
				dispose();
				break;
		}
	}
	
	public void onActivityResult (int requestCode, int resultCode, Intent intent) {}

	public PopupListener getListener ()
	{
		return listener;
	}

	public AbsPopup setListener ( PopupListener listener )
	{
		this.listener = listener;
		return this;
	}

	public Activity getContext ()
	{
		return context;
	}

	public void setContext ( Activity context )
	{
		this.context = context;
	}
	
	public PopupWindow getWindow(){
		return mPopupWindow;
	}

	public void onPause(){
		
	}
	
	public void onResum(){
		
	}
	
}
