package sions.android.sionsbeat;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class BaseActivity extends Activity
{
	protected String LOGTAG = "sionsbeat";

	protected void log(Object...text){
		StringBuilder sb = new StringBuilder();
		for(Object o:text){
			sb.append(o).append(" ");
		}
		
		Log.d( LOGTAG, sb.toString());
	}
	
	public void toastFinish(int resId){
		Toast.makeText( this, resId, Toast.LENGTH_SHORT ).show();
		finish();
	}
	@Override
	public boolean onKeyDown ( int keyCode, KeyEvent event )
	{
		switch(keyCode){
			case KeyEvent.KEYCODE_VOLUME_UP:
				setMediaVolume(true);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				setMediaVolume(false);
				return true;
		}
		return super.onKeyUp( keyCode, event );
	}
	
	private void setMediaVolume(boolean up)
	{
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int currVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		if(up && currVol < maxVol)
		{
			am.setStreamVolume(AudioManager.STREAM_MUSIC, currVol+1, AudioManager.FLAG_SHOW_UI|AudioManager.FLAG_ALLOW_RINGER_MODES);
		}
		else if(!up && currVol > 0){
			am.setStreamVolume(AudioManager.STREAM_MUSIC, currVol-1, AudioManager.FLAG_SHOW_UI|AudioManager.FLAG_ALLOW_RINGER_MODES);
		}
	}
	
}
