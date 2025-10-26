package sions.android.sionsbeat.utils;

import java.io.IOException;
import java.util.HashMap;

import sions.android.sionsbeat.R;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

public class SoundFXPlayer {

	private static SoundFXPlayer instance;
	public static synchronized SoundFXPlayer get(Context context){
		if(instance == null){
			instance = new SoundFXPlayer(context);
		}else if(context != instance.context){
			instance.initialize(context);
		}
		return instance;
	}

	static int SOUNDFXIDS = 0;
	
	private int SoundFXID = SOUNDFXIDS++;
	private boolean bgStart = false;
	
	private Context context;
	private SoundPool soundPool;
	private MediaPlayer bgPlayer;
	private HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();

	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/
	
	public SoundFXPlayer(Context context){
		initialize (context);
	}
	
	private void initialize(Context context){
		
		this.context = context;
		
		if(soundPool == null){
			soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);

			addSound(R.raw.clear);
			addSound(R.raw.touch);
			addSound(R.raw.touch2);
			addSound(R.raw.touch3);
			addSound(R.raw.failed);
			addSound(R.raw.musicselect);
			addSound(R.raw.wellcome);
			addSound(R.raw.readygo);
			addSound(R.raw.result);
			addSound(R.raw.fullcombo);
			addSound(R.raw.excellent);
			addSound(R.raw.screenshot);
			addSound(R.raw.diring);
			addSound(R.raw.newrecord);
			addSound(R.raw.gameover);
			addSound(R.raw.fever);
			
		}
		
	}
	
	private void loadBgPlayer()
	{
		if(bgPlayer == null){
			bgPlayer = MediaPlayer.create(context, R.raw.bgsound);
			if(bgPlayer != null){
				bgPlayer.setLooping(true);
				bgPlayer.setVolume(0.2F, 0.2F);
			}
		}
	}

	/**************************************************************************
	 * 
	 * @LiefCycle
	 * 
	 *************************************************************************/
	

	/**************************************************************************
	 * 
	 * @ACTION
	 * 
	 *************************************************************************/
	
	private void addSound(int resId){
		soundMap.put(resId, soundPool.load(context, resId, 1));
	}

	public void play(int resId, float volume){
		play(resId, 0, volume);
	}
	public void play(int resId, int loop, float volume){
		if(volume == 0)return;
		try{
			soundPool.play(soundMap.get(resId), volume, volume, 0, loop, 1);
		}catch(Throwable e){
			Log.e("soundFX", "Not found play id "+resId);
		}
	}
	
	public void stop(int resId){
		try{
			soundPool.stop(soundMap.get(resId));
		}catch(Throwable e){}
	}
	
	public void bgPlay(){
		Log.d("bgm", SoundFXID+")Start "+bgStart);
		if(bgPlayer == null){
			loadBgPlayer();
			if(bgPlayer == null) return;
		}
		
		synchronized(this){
			if(!bgStart){
				bgVolume(GameOptions.get(context).getSettingInt(GameOptions.OPTION_SOUND_BACKGROUND_MUSIC)*0.01f);
				Log.d("bgm", "real Start");
				bgStart = true;
				bgPlayer.start();
			}
		}
	}
	public void bgRestart(){
		if(bgPlayer == null){
			loadBgPlayer();
			if(bgPlayer == null) return;
		}
		
		synchronized(this){
			bgPlayer.seekTo(0);	
		}
	}
	public void bgStop(){
		if(bgPlayer == null){
			loadBgPlayer();
			if(bgPlayer == null) return;
		}
		
		Log.d("bgm", SoundFXID+") Stop "+bgStart);
		synchronized(this){
			if(bgStart){
				Log.d("bgm", "real Stop");
				bgStart = false;
				bgPlayer.pause();
			}
		}
	}
	
	public void bgVolume(float volume)
	{
		try{
			bgPlayer.setVolume(volume, volume);
		}catch(Throwable e){
			ErrorController.error(10, e);
		}
	}

	/**************************************************************************
	 * 
	 * @GESET
	 * 
	 *************************************************************************/
	
	
	
}
