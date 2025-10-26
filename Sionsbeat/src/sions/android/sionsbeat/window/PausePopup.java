package sions.android.sionsbeat.window;

import java.io.File;
import java.io.IOException;

import com.squareup.picasso.Picasso;

import sions.android.SQ;
import sions.android.sionsbeat.GameActivity;
import sions.android.sionsbeat.MusicSelectActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.game.GameData;
import sions.android.sionsbeat.game.exception.NotFoundMatchVersion;
import sions.android.sionsbeat.game.view.GameGraphView;
import sions.android.sionsbeat.game.view.LevelSelectorView;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.JsonType;
import sions.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PausePopup extends AbsPopup
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
	
	public PausePopup ( Activity context )
	{
		super( context );
	}
	
	/************************************************************************************************************
	 * 
	 * 				@MEMBER_FIELDS
	 * 
	 ************************************************************************************************************/

	private Button exitBtn;
	private Button continueBtn;
	private ImageButton replayBtn;
	
	private View view;
	private GameActivity gameActivity;
	private int maxLevel;
	
	/************************************************************************************************************
	 * 
	 * 				@OVERRIDE_METHODS
	 * 
	 ************************************************************************************************************/

	public boolean show ( GameActivity ga )
	{
		/**@TRACKING**/ErrorController.tracking(getContext(), this, "show", "", 0, true);
		
		this.gameActivity = ga;
		this.view = LayoutInflater.from(getContext()).inflate( R.layout.window_pause, null );
		
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

		replayBtn = (ImageButton) view.findViewById( R.id.replay );
		exitBtn = (Button) view.findViewById( R.id.exit );
		continueBtn = (Button) view.findViewById( R.id.continued );
		
	}
	
	private void setupEvent(){

		replayBtn.setOnClickListener( new View.OnClickListener(){
			public void onClick ( View v ){
				gameActivity.doRestart();
			}});

		exitBtn.setOnClickListener( new View.OnClickListener(){
			public void onClick ( View v ){
				gameActivity.finish();
			}});

		continueBtn.setOnClickListener( new View.OnClickListener(){
			public void onClick ( View v ){
				gameActivity.doResum();
				gameActivity.onPopupClose();
			}});
		
	}
	
	private boolean setupPref()
	{
		
		return true;
	}

	/************************************************************************************************************
	 * 
	 * 				@ACCESS_METHODS
	 * 
	 ************************************************************************************************************/
}
