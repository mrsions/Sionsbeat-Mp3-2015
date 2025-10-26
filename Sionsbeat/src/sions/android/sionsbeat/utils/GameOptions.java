package sions.android.sionsbeat.utils;

import java.io.File;

import sions.android.sionsbeat.window.SettingPopup;
import sions.android.sionsbeat.window.SettingPopup.SettingItem;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class GameOptions 
{

	public static final String OPTION_GAME_SPLIT = "gameSplit";
	public static final String OPTION_GAME_FPS = "gameFPS";
	public static final String OPTION_GAME_SYNC = "gameSync";
	public static final String OPTION_GAME_TOUCH_VIBRATOR = "touchVibrator";
	public static final String OPTION_GAME_MISS_VIBRATOR = "missVibrator";
	public static final String OPTION_GAME_PAD_MARGIN= "padMargin";	//적용해야함

	public static final String OPTION_GRAPHIC_LOAD_THUMBNAIL = "loadThumbnail";
	public static final String OPTION_GRAPHIC_GAME_FEVER_PARTICLE= "gameFeverBackground";
	public static final String OPTION_GRAPHIC_GAME_FEVER_EFFECTS = "gameFeverAnotherEffects";

	public static final String OPTION_SOUND_BACKGROUND_MUSIC = "playBackgroundMusic";
	public static final String OPTION_SOUND_GAME_TOUCH = "touchSound";
	public static final String OPTION_SOUND_SELECT_PREVIEW = "previewSound";
	public static final String OPTION_SOUND_SYSTEM_VOICE = "systemVoice";
	public static final String OPTION_SOUND_GAME = "gameSoundEffect";

	public static final String OPTION_POPUP_BLUR = "popupBlur";
	
	
	private static GameOptions instance = null;
	public static GameOptions get(Context context){
		if(instance == null){
			instance = new GameOptions(context);
		}else if(instance.context != context){
			instance.setupPreference(context);
		}
		return instance;
	}
	
	private static File rootFile;
	public static File getRootFile(){
		return rootFile;
	}public static void setupRootFile (Context context) {
		if(rootFile == null)
		{	
			rootFile = new File(Environment.getExternalStorageDirectory(), ".sionsbeat");
//			Log.d("test",rootFile+"  / "+rootFile.canWrite());
//			if(rootFile.canWrite()){
//				return;
//			}
//			
//			File[] files = ContextCompat.getExternalFilesDirs(context, null);
//			for(File file:files)
//			{
//				if(file != null)
//				{
//					rootFile = file;
//				}
//			}
//			
//			if(rootFile == null)
//			{
//				rootFile = context.getFilesDir();
//			}
		}
	}public static File getCompatRootFile (Context context) {
		File rootFile = null;
	
		File[] files = ContextCompat.getExternalFilesDirs(context, null);
		for(File file:files)
		{
			if(file != null && file.exists() && file.list().length>0)
			{
				rootFile = file;
			}
		}
		
		if(rootFile == null)
		{
			rootFile = context.getFilesDir();
		}
		
		return rootFile;
	}

	private Context context;
	private Editor edit;
	private SharedPreferences pref;

	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/
	
	public GameOptions(Context context){
		setupPreference(context);
	}
	
	private void setupPreference(Context context){
		this.context = context;
		this.pref = PreferenceManager.getDefaultSharedPreferences( context );
		this.edit = this.pref.edit();
	}


	/**************************************************************************
	 * 
	 * @ACTION
	 * 
	 *************************************************************************/

	public File getMarkerFile(){
		String marker = getString("marker", "GalaxyShutter");
		return getMarkerFile(marker);
	}
	public File getMarkerFile(String marker){
		File root = new File(getRootFile(), "marker");
		return new File(root, marker);
	}
	
	/**************************************************************************
	 * 
	 * @GESET
	 * 
	 *************************************************************************/
	
	public int getInt(String key){
		return getInt(key, 0);
	}
	public int getInt(String key, int def){
		try{
			return this.pref.getInt(key, def);
		}catch(ClassCastException e){
			Log.e("option",key+":"+e.getMessage());
			put(key, def);
			return def;
		}
	}

	public long getLong(String key){
		return getLong(key, 0L);
	}
	public long getLong(String key, Long def){
		try{
			return this.pref.getLong(key, def);
		}catch(ClassCastException e){
			Log.e("option",key+":"+e.getMessage());
			put(key, def);
			return def;
		}
	}

	public float getFloat(String key){
		return getFloat(key, 0F);
	}
	public float getFloat(String key, Float def){
		try{
			return this.pref.getFloat(key, def);
		}catch(ClassCastException e){
			Log.e("option",key+":"+e.getMessage());
			put(key, def);
			return def;
		}
	}

	public String getString(String key){
		return getString(key, "");
	}
	public String getString(String key, String def){
		try{
			return this.pref.getString(key, def);
		}catch(ClassCastException e){
			Log.e("option",key+":"+e.getMessage());
			put(key, def);
			return def;
		}
	}

	public boolean getBoolean(String key){
		return getBoolean(key, false);
	}
	public boolean getBoolean(String key, boolean def){
		try{
			return this.pref.getBoolean(key, def);
		}catch(ClassCastException e){
			Log.e("option",key+":"+e.getMessage());
			put(key, def);
			return def;
		}
	}
	
	public boolean contains(String key){
		return this.pref.contains(key);
	}

	public boolean put(String key, int value){
		edit.putInt(key, value);
		return edit.commit();
	}
	public boolean put(String key, long value){
		edit.putLong(key, value);
		return edit.commit();
	}
	public boolean put(String key, float value){
		edit.putFloat(key, value);
		return edit.commit();
	}
	public boolean put(String key, boolean value){
		edit.putBoolean(key, value);
		return edit.commit();
	}
	public boolean put(String key, String value){
		edit.putString(key, value);
		return edit.commit();
	}

	public int getSettingInt(String key){
		for(SettingItem item:SettingPopup.settings){
			if(key.equals(item.getKey())){
				try{
					return getInt(key, (Integer)item.getDef());
				}catch(Throwable e){
					return (Integer)item.getDef();
				}
			}
		}
		return 0;
	}
	public boolean getSettingBoolean(String key){
		for(SettingItem item:SettingPopup.settings){
			if(key.equals(item.getKey())){
				try{
					return getBoolean(key, (Boolean)item.getDef());
				}catch(Throwable e){
					return (Boolean)item.getDef();
				}
			}
		}
		return false;
	}
}
