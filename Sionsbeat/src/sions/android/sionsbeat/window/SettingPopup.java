package sions.android.sionsbeat.window;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Picasso.LoadedFrom;

import sions.android.sionsbeat.Base;
import sions.android.sionsbeat.MusicSelectActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.adapter.MusicListAdapter;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.SoundFXPlayer;
import sions.android.sionsbeat.window.AbsPopup.PopupListener;
import sions.android.sionsbeat.zresource.ZAnimate;
import sions.android.sionsbeat.zresource.ZResource;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TabHost.TabSpec;
import android.widget.ToggleButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingPopup extends AbsPopup implements PopupListener
{

	/************************************************************************************************************
	 * 
	 * 				@STATIC_FIELDS
	 * 
	 ************************************************************************************************************/

	public static SettingItem[] settings = new SettingItem[]{

		new SettingItem(GameOptions.OPTION_GAME_SPLIT,					R.string.setting_game_split,			R.id.container_game,		SettingItem.BOOLEAN, 			true, false),
		new SettingItem(GameOptions.OPTION_GAME_FPS,					R.string.setting_game_fps,				R.id.container_game,		SettingItem.INTEGER_SPINNER, 	40, 20, 60, "fps"),
		new SettingItem(GameOptions.OPTION_GAME_SYNC,					R.string.setting_game_sync,				R.id.container_game,		SettingItem.INTEGER,			50),
		new SettingItem(GameOptions.OPTION_GAME_TOUCH_VIBRATOR,			R.string.setting_game_vibrate,			R.id.container_game,		SettingItem.BOOLEAN,			false),
		new SettingItem(GameOptions.OPTION_GAME_MISS_VIBRATOR,			R.string.setting_game_vibrate_miss,		R.id.container_game,		SettingItem.BOOLEAN,			true),
		new SettingItem(null,											R.string.setting_game_pad,				R.id.container_game,		SettingItem.BUTTON,				null),
		new SettingItem(GameOptions.OPTION_GAME_PAD_MARGIN,				0,										0,							(byte)-1,						100),

		new SettingItem(GameOptions.OPTION_GRAPHIC_LOAD_THUMBNAIL,		R.string.setting_graphic_album,			R.id.container_graphic,		SettingItem.BOOLEAN,			true),
		new SettingItem(GameOptions.OPTION_GRAPHIC_GAME_FEVER_PARTICLE,	R.string.setting_graphic_fever,			R.id.container_graphic,		SettingItem.BOOLEAN,			false),
		new SettingItem(GameOptions.OPTION_GRAPHIC_GAME_FEVER_EFFECTS,	R.string.setting_graphic_fever_effect,	R.id.container_graphic,		SettingItem.BOOLEAN,			true),

		new SettingItem(GameOptions.OPTION_SOUND_BACKGROUND_MUSIC,		R.string.setting_sound_background,		R.id.container_sound,		SettingItem.INTEGER_SPINNER, 	20, 0, 100, "%"),
		new SettingItem(GameOptions.OPTION_SOUND_GAME_TOUCH,			R.string.setting_sound_game_touch,		R.id.container_sound,		SettingItem.INTEGER_SPINNER, 	10, 0, 100, "%"),
		new SettingItem(GameOptions.OPTION_SOUND_SELECT_PREVIEW,		R.string.setting_sound_select_preview,	R.id.container_sound,		SettingItem.INTEGER_SPINNER, 	100, 0, 100, "%"),
		new SettingItem(GameOptions.OPTION_SOUND_SYSTEM_VOICE,			R.string.setting_sound_system_voice,	R.id.container_sound,		SettingItem.INTEGER_SPINNER, 	100, 0, 100, "%"),
		new SettingItem(GameOptions.OPTION_SOUND_GAME,					R.string.setting_sound_game,			R.id.container_sound,		SettingItem.INTEGER_SPINNER, 	100, 0, 100, "%"),
		
		new SettingItem(GameOptions.OPTION_POPUP_BLUR,					R.string.setting_other_popup_blur,		R.id.container_other,		SettingItem.BOOLEAN,			true),
		new SettingItem(null,											R.string.setting_other_import_folder,	R.id.container_other,		SettingItem.BUTTON, 			null)
	};
	
	public static void InitializeSettings(Context context)
	{
		for(SettingItem item : settings)
		{
			// 무료버전 클라이언트가 무료버전사용 가능한 경우가 아닌 경우.
			if(Base.FREE && !item.isFree)
			{
				item.value = item.def;
				switch(item.type)
				{
				case SettingItem.BOOLEAN:
					GameOptions.get(context).put(item.key, (Boolean)item.def);
					break;
				case SettingItem.INTEGER:
					GameOptions.get(context).put(item.key, (Integer)item.def);
					break;
				case SettingItem.INTEGER_SPINNER:
					GameOptions.get(context).put(item.key, (Integer)item.def);
					break;
				}
			}
		}
	}
	
	/************************************************************************************************************
	 * 
	 *				 @CONSTRUCTOR
	 * 
	 ************************************************************************************************************/
	
	public SettingPopup ( Activity context )
	{
		super( context );
	}
	
	/************************************************************************************************************
	 * 
	 * 				@MEMBER_FIELDS
	 * 
	 ************************************************************************************************************/
	private LinearLayout container1;
	private LinearLayout container2;
	private LinearLayout container3;
	private LinearLayout container4;
	private TabHost tabHost;
	private DisplayMetrics displayMetrics;
	
	private View view;
	
	private AbsPopup popup;
	
	/************************************************************************************************************
	 * 
	 * 				@OVERRIDE_METHODS
	 * 
	 ************************************************************************************************************/

	public boolean show ()
	{
		/**@TRACKING**/ErrorController.tracking(getContext(), this, "show", "", 0, true);
		
		this.view = LayoutInflater.from(getContext()).inflate( R.layout.window_setting, null );
		
		setupDefaults();
		setupEvent();
		if(!setupPref()){
			return false;
		}
		return super.show( this.view );
	}
	
	/************************************************************************************************************
	 * 
	 * 				@ACTIVE_METHODS
	 * 
	 ************************************************************************************************************/
	
	private void setupDefaults(){

		tabHost = (TabHost) view.findViewById(android.R.id.tabhost);
		tabHost.setup();

		String temp;
		tabHost.addTab(tabHost.newTabSpec(temp=getContext().getString(R.string.setting_tab_game)).setContent(R.id.tab_game).setIndicator(temp));
		tabHost.addTab(tabHost.newTabSpec(temp=getContext().getString(R.string.setting_tab_graphic)).setContent(R.id.tab_graphic).setIndicator(temp));
		tabHost.addTab(tabHost.newTabSpec(temp=getContext().getString(R.string.setting_tab_sound)).setContent(R.id.tab_sound).setIndicator(temp));
		tabHost.addTab(tabHost.newTabSpec(temp=getContext().getString(R.string.setting_tab_other)).setContent(R.id.tab_other).setIndicator(temp));
		
		TabWidget tabWidget = (TabWidget) tabHost.getTabWidget();
		for(int i=0; i<tabWidget.getChildCount(); i++)
		{
			ViewGroup rl = (ViewGroup) tabWidget.getChildAt(i);
			TextView tv = (TextView) rl.getChildAt(1);
			tv.setTextColor(getContext().getResources().getColor(R.color.font_color));
		}
		
		
		this.container1 = (LinearLayout) view.findViewById(R.id.container_game);
		this.container2 = (LinearLayout) view.findViewById(R.id.container_graphic);
		this.container3 = (LinearLayout) view.findViewById(R.id.container_sound);
		this.container4 = (LinearLayout) view.findViewById(R.id.container_other);
		this.displayMetrics = getContext().getResources().getDisplayMetrics();
		
		LayoutInflater inflater = LayoutInflater.from(getContext());
		
		for(int i=0; i<settings.length; i++){
			final SettingItem item = settings[i];
			
			switch(item.getType()){
				case SettingItem.BOOLEAN:
					{
						boolean value = GameOptions.get(getContext()).getBoolean(item.getKey(), (Boolean)item.getDef());
						item.setValue(value);
						
						item.layout = (LinearLayout) inflater.inflate(R.layout.item_setting_boolean, null);
						
						((TextView) item.layout.findViewById(R.id.key)).setText(item.resid);
						
						final ToggleButton toggle = (ToggleButton) item.layout.findViewById(R.id.value);
						toggle.setChecked(value);
						
						item.view = toggle;
						
						toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
								if(!Base.FREE || item.isFree)
								{
									item.value = isChecked;
									GameOptions.get(getContext()).put(item.key, isChecked);
	
									onChangeSetting(item);
								}
								else
								{
									toggle.setChecked((Boolean)item.value);
									Base.ShowPleasePurchaseFunction(getContext());
								}
							}
						});	
						
						item.layout.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick (View v) {
								if(!Base.FREE || item.isFree)
								{
									toggle.toggle();
								}
								else
								{
									Base.ShowPleasePurchaseFunction(getContext());
								}
							}
						});
					}
					break;
				case SettingItem.STRING:
					{
						String value = GameOptions.get(getContext()).getString(item.getKey(), (String)item.getDef());
						item.setValue(value);
						
						item.layout = (LinearLayout) inflater.inflate(R.layout.item_setting_string, null);
						
						((TextView) item.layout.findViewById(R.id.key)).setText(item.resid);
						
						EditText text = (EditText) item.layout.findViewById(R.id.value);
						text.setText(value);
						
						item.view = text;
						
						text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
							
							@Override
							public boolean onEditorAction (TextView v, int actionId, KeyEvent event) {
								item.value = v.getText();
								GameOptions.get(getContext()).put(item.key, (String)v.getText());

								onChangeSetting(item);
								return true;
							}
						});
					}
					break;
				case SettingItem.INTEGER_SPINNER:
					{
						int value = Math.max(item.min, Math.min(item.max, GameOptions.get(getContext()).getInt(item.getKey(), (Integer)item.getDef())));
						item.setValue(value);

						item.layout = (LinearLayout) inflater.inflate(R.layout.item_setting_integer_spinner, null);

						((TextView) item.layout.findViewById(R.id.key)).setText(item.resid);
						((TextView) item.layout.findViewById(R.id.value_hint)).setText(value+item.hint);
						
						final SeekBar sp = (SeekBar) item.layout.findViewById(R.id.value);
						sp.setMax(item.max-item.min);
						sp.setProgress(value-item.min);
						
						item.view = sp;
						
						sp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
							public void onStopTrackingTouch (SeekBar seekBar) {}
							public void onStartTrackingTouch (SeekBar seekBar) {}
							@Override
							public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {
								item.value = progress + item.min;
								GameOptions.get(getContext()).put(item.key, (Integer)item.value);

								((TextView) item.layout.findViewById(R.id.value_hint)).setText(item.value+item.hint);
								
								onChangeSetting(item);
							}
						});
						
					}
					break;
				case SettingItem.INTEGER:
					{
						int value = GameOptions.get(getContext()).getInt(item.getKey(), (Integer)item.getDef());
						item.setValue(value);
						
						item.layout = (LinearLayout) inflater.inflate(R.layout.item_setting_integer, null);
						
						((TextView) item.layout.findViewById(R.id.key)).setText(item.resid);
						
						final TextView text = (TextView) item.layout.findViewById(R.id.value);
						text.setText(Integer.toString(value));
						
						item.view = text;
						
						((Button)item.layout.findViewById(R.id.down)).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick (View v) {
								int value = (Integer)item.value -1;
								
								item.value = value;
								GameOptions.get(getContext()).put(item.key, value);
								text.setText(Integer.toString(value));

								onChangeSetting(item);
							}
						});
						
						((Button)item.layout.findViewById(R.id.up)).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick (View v) {
								int value = (Integer)item.value +1;
								
								item.value = value;
								GameOptions.get(getContext()).put(item.key, value);
								text.setText(Integer.toString(value));
								
								onChangeSetting(item);
							}
						});
					}
					break;
				case SettingItem.BUTTON:
					{
						item.layout = (LinearLayout) inflater.inflate(R.layout.item_setting_button, null);
						
						((TextView) item.layout.findViewById(R.id.key)).setText(item.resid);
						
						item.layout.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick (View v) {
								onChangeSetting(item);
							}
						});
					}
					break;
			}
			
			if(item.layout != null){
				LinearLayout layout = null;
				switch(item.parentID){
					case R.id.container_game:
						layout = container1;
						break;
					case R.id.container_graphic:
						layout = container2;
						break;
					case R.id.container_sound:
						layout = container3;
						break;
					case R.id.container_other:
						layout = container4;
						break;
				}
				if(layout != null){
					if(layout.getChildCount() != 0){
						layout.addView( inflater.inflate(R.layout.item_setting_spliter, null));
					}
					layout.addView( item.layout );
				}
			}
		}
		
	}
	
	private void setupEvent(){

	}
	
	private boolean setupPref()
	{
		return true;
	}
	
	@Override
	public boolean dispose () {
		if(popup!=null){
			return popup.dispose();
		}else{
			return super.dispose();
		}
	}

	@Override
        public void onPopupClose (AbsPopup popup) {
		this.popup = null;
        }
	
	public void onChangeSetting(SettingItem item){

		Log.d("test","111 "+item.key);
		switch ( item.resid )
		{
			case R.string.setting_sound_background:
				{
					SoundFXPlayer.get(getContext()).bgVolume(((Integer)item.value)*0.01f);
				}
				break;
			case R.string.setting_other_popup_blur:
				{
					setBackground((Boolean)item.getValue());
				}
				break;
			case R.string.setting_graphic_album:
				{
					MusicListAdapter.load_album_cover = (Boolean)item.getValue();
				}
				break;
			case R.string.setting_other_import_folder:
				{
					ImportMusicsPopup popup = new ImportMusicsPopup((MusicSelectActivity)getContext());
					popup.setListener(this);
					popup.show();
					this.popup = popup;
				}
				break;
			case R.string.setting_game_pad:
				{
					SettingPaddingPopup popup = new SettingPaddingPopup((MusicSelectActivity)getContext());
					popup.setListener(this);
					popup.show();
					this.popup = popup;
				}
				break;
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
	
	public static class SettingItem {

		public static final byte STRING = 0;
		public static final byte INTEGER = 1;
		public static final byte INTEGER_SPINNER = 11;
		public static final byte BOOLEAN = 3;
		public static final byte BUTTON = 10;
		
		private int parentID;
		
		private String key;
		private String hint;
		private Object value;
		private Object def;
		private byte type;
		private int resid;
		private boolean isFree;
		
		private int min;
		private int max;
		
		private LinearLayout layout;
		private View view;

		public SettingItem (String key, int resid, int parentID, byte type, Object def)
        {
        	this(key, resid, parentID, type, def, true);
        }
		public SettingItem (String key, int resid, int parentID, byte type, Object def, boolean isFree)
        {
            super();
            this.key = key;
            this.value = def;
            this.def = def;
            this.type = type;
            this.resid = resid;
            this.parentID = parentID;
            this.isFree = isFree;
        }
		public SettingItem (String key, int resid, int parentID, byte type, Object def, int min, int max, String hint)
		{
			this(key, resid, parentID, type, def, min, max, hint, true);
		}
		public SettingItem (String key, int resid, int parentID, byte type, Object def, int min, int max, String hint, boolean isFree)
        {
            super();
            this.key = key;
            this.value = def;
            this.def = def;
            this.type = type;
            this.resid = resid;
            this.parentID = parentID;
            this.max = max;
            this.min = min;
            this.hint = hint;
            this.isFree = isFree;
        }

		public boolean isFree() {
			return isFree;
		}
		public void setFree(boolean isFree) {
			this.isFree = isFree;
		}
		public String getKey () {
			return key;
		}
		public void setKey (String key) {
			this.key = key;
		}
		public Object getValue () {
			return value;
		}
		public void setValue (Object value) {
			this.value = value;
		}
		public Object getDef () {
			return def;
		}
		public void setDef (Object def) {
			this.def = def;
		}
		public byte getType () {
			return type;
		}
		public void setType (byte type) {
			this.type = type;
		}
		public int getResid () {
			return resid;
		}
		public void setResid (int resid) {
			this.resid = resid;
		}
		public int getParentID () {
			return parentID;
		}
		public void setParentID (int parentID) {
			this.parentID = parentID;
		}
		public int getMin () {
			return min;
		}
		public void setMin (int min) {
			this.min = min;
		}
		public int getMax () {
			return max;
		}
		public void setMax (int max) {
			this.max = max;
		}
		
	}
}
