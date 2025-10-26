package sions.android.sionsbeat;

import sions.android.sionsbeat.utils.SoundFXPlayer;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BGMActivity extends BaseActivity
{
	private boolean keepBGM = false;
	public boolean isKeepBGM() {
		return keepBGM;
	}public void setKeepBGM(boolean keepBGM) {
		this.keepBGM = keepBGM;
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		keepBGM = true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Log.d("bgm", "onResum");
		
		keepBGM = false;
		SoundFXPlayer.get(this).bgPlay();
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		Log.d("bgm", "onPause");
		
		if(!keepBGM){
			SoundFXPlayer.get(this).bgStop();
		}
	}
	
}
