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

import sions.android.sionsbeat.MusicSelectActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.adapter.MusicListAdapter;
import sions.android.sionsbeat.game.view.PaddingSettingView;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.SoundFXPlayer;
import sions.android.sionsbeat.zresource.ZAnimate;
import sions.android.sionsbeat.zresource.ZResource;
import android.app.Activity;
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

public class SettingPaddingPopup extends AbsPopup implements SeekBar.OnSeekBarChangeListener
{

	/************************************************************************************************************
	 * 
	 *				 @CONSTRUCTOR
	 * 
	 ************************************************************************************************************/
	
	public SettingPaddingPopup ( Activity context )
	{
		super( context );
	}
	
	/************************************************************************************************************
	 * 
	 * 				@MEMBER_FIELDS
	 * 
	 ************************************************************************************************************/
	
	private TextView textView;
	private SeekBar padding;
	private PaddingSettingView paddingView;
	
	private View view;
	
	/************************************************************************************************************
	 * 
	 * 				@OVERRIDE_METHODS
	 * 
	 ************************************************************************************************************/

	public boolean show ()
	{
		/**@TRACKING**/ErrorController.tracking(getContext(), this, "show", "", 0, true);
		
		this.view = LayoutInflater.from(getContext()).inflate( R.layout.window_setting_padding, null );
		
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
		textView = (TextView)view.findViewById(R.id.text);
		padding = (SeekBar) view.findViewById(R.id.padding);
		paddingView = (PaddingSettingView) view.findViewById(R.id.paddingView);
		paddingView.setSeekBar(padding);
	}
	
	private void setupEvent(){
		padding.setOnSeekBarChangeListener(this);
	}
	
	private boolean setupPref()
	{
		padding.setProgress(GameOptions.get(getContext()).getSettingInt(GameOptions.OPTION_GAME_PAD_MARGIN)-50);
		return true;
	}

	@Override
        public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {
		progress += 50;
		GameOptions.get(getContext()).put(GameOptions.OPTION_GAME_PAD_MARGIN, progress);
		textView.setText(progress+"%");
		paddingView.setBlockPadding(progress*0.01f);
		paddingView.invalidate();
        }

        public void onStartTrackingTouch (SeekBar seekBar) {}
        public void onStopTrackingTouch (SeekBar seekBar) {}
	

	/************************************************************************************************************
	 * 
	 * 				@ACCESS_METHODS
	 * 
	 ************************************************************************************************************/
	
	@Override
	public boolean dispose () {
	        return super.dispose();
	}
	
}
