package sions.android.sionsbeat.window;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import com.google.analytics.tracking.android.MapBuilder;
import com.squareup.picasso.Picasso;

import sions.android.SQ;
import sions.android.sionsbeat.GameActivity;
import sions.android.sionsbeat.MusicSelectActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.SplashActivity;
import sions.android.sionsbeat.game.GameData;
import sions.android.sionsbeat.game.GameMode;
import sions.android.sionsbeat.game.GameOption;
import sions.android.sionsbeat.game.GameScore;
import sions.android.sionsbeat.game.GameMode.GameResult;
import sions.android.sionsbeat.game.exception.NotFoundMatchVersion;
import sions.android.sionsbeat.game.view.GameGraphView;
import sions.android.sionsbeat.game.view.LevelSelectorView;
import sions.android.sionsbeat.game.view.StrokeTextView;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.JsonType;
import sions.android.sionsbeat.utils.SoundFXPlayer;
import sions.android.sionsbeat.utils.ViewUtils;
import sions.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ResultPopup extends AbsPopup implements View.OnClickListener
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
	
	public ResultPopup ( GameActivity context )
	{
		super( context );
		gameActivity = context;
	}
	
	/************************************************************************************************************
	 * 
	 * 				@MEMBER_FIELDS
	 * 
	 ************************************************************************************************************/

	private Button exitBtn;
	private ImageButton shareBtn;
	private ImageButton replayBtn;
	private Button startBtn;
	
	private ImageView infoThumb;
	private TextView infoText, infoText2;

	private TextView title;
	private TextView score;
	private TextView bonus;
	
	private TextView fullCombo;
	private TextView newRecord;
	
	private TextView rank;
	private TextView perfect;
	private TextView great;
	private TextView good;
	private TextView failed;
	private TextView combo;
	
	private LinearLayout menu_layout;
	private GameResult result;
	
	private View view;
	private GameActivity gameActivity;
	private int maxLevel;
	
	/************************************************************************************************************
	 * 
	 * 				@OVERRIDE_METHODS
	 * 
	 ************************************************************************************************************/

	public boolean show (GameResult result)
	{
		/**@TRACKING**/ErrorController.tracking(getContext(), this, "show", "", 0, true);
		
		this.view = LayoutInflater.from(getContext()).inflate( R.layout.window_result, null );
		
		setupDefaults();
		setupEvent();
		setupPref(result);
		return super.show( this.view );
	}
	
	/************************************************************************************************************
	 * 
	 * 				@ACTIVE_METHODS
	 * 
	 ************************************************************************************************************/
	
	private void setupDefaults(){

		exitBtn = (Button) view.findViewById( R.id.exit );
		startBtn = (Button) view.findViewById( R.id.game_start );
		replayBtn = (ImageButton) view.findViewById( R.id.replay );
		shareBtn = (ImageButton) view.findViewById( R.id.share );

		infoThumb = (ImageView) view.findViewById( R.id.info_thumb );
		infoText = (TextView) view.findViewById( R.id.info_text );
		infoText2 = (TextView) view.findViewById( R.id.info_text2 );
		
		title = (TextView) view.findViewById( R.id.title );
		score = (TextView) view.findViewById( R.id.score );
		bonus = (TextView) view.findViewById( R.id.bonus );
		fullCombo = (TextView) view.findViewById( R.id.fullcombo );
		newRecord = (TextView) view.findViewById( R.id.newrecord );
		
		rank = (TextView) view.findViewById( R.id.rank );
		
		perfect = (TextView) view.findViewById( R.id.perfect );
		great = (TextView) view.findViewById( R.id.great );
		good = (TextView) view.findViewById( R.id.good );
		failed = (TextView) view.findViewById( R.id.failed );
		combo = (TextView) view.findViewById( R.id.combo );
		
		menu_layout = (LinearLayout) view.findViewById( R.id.menu_layout );

		ViewUtils.setAlpha(fullCombo, 0);
		ViewUtils.setAlpha(newRecord, 0);
		ViewUtils.setAlpha(bonus, 0);
		ViewUtils.setAlpha(rank, 0);
		ViewUtils.setAlpha(startBtn, 0);

		ViewUtils.setAlpha(menu_layout, 0);

	}
	
	private void setupEvent(){

		replayBtn.setOnClickListener(this);
		exitBtn.setOnClickListener(this);
		shareBtn.setOnClickListener(this);
		startBtn.setOnClickListener(this);
	}
	
	private boolean setupPref(GameResult result)
	{
		this.result = result;
		
		if(result.getRankType() >= GameMode.RANK_C && result.getGameMode().isNextGame()){
			startBtn.setVisibility(View.VISIBLE);
		}
		
		score.setText(String.valueOf( result.getUserScore()));
		bonus.setText(getContext().getString(R.string.result_name_bonus, result.getBonusScore()));
		
		rank.setText(result.getRank());
		
		GameScore score = result.getGameScore();

		perfect.setText( String.valueOf(score.getPerfectCount()) );
		great.setText( String.valueOf(score.getGreatCount()) );
		good.setText( String.valueOf(score.getGoodCount()) );
		failed.setText( String.valueOf(score.getFailedCount()) );
		combo.setText( String.valueOf(score.getMaxCombo()) );

		
		GameData gameData = result.getData();
		gameData.getSong().parseAlbumView50(getContext(), infoThumb, R.drawable.list_empty);
//		if(gameData.getSong().getArt()!=null && gameData.getSong().getArt().length()>5){
//			String path = gameData.getSong().getArt();
//			if(path.indexOf("://") == -1){
//				Picasso.with(getContext()).load(new File(path)).placeholder(R.drawable.list_empty).into(infoThumb);
//			}else{
//				Picasso.with(getContext()).load(Uri.parse(path)).placeholder(R.drawable.list_empty).into(infoThumb);
//			}
//		}
		
		infoText.setText("Lv."+gameData.getLevel()+"  "+gameData.getSong().getTitle());
		infoText2.setText(gameData.getSong().getArtist()+" | "+SongInfo.getTimeCode(gameData.getStartOffset())+"~"+SongInfo.getTimeCode(gameData.getEndOffset()));
		
		return true;
	}

	/************************************************************************************************************
	 * 
	 * 				@EVENT
	 * 
	 ************************************************************************************************************/

	@Override
        public void onClick(View v){ 
        	switch(v.getId()){
        		case R.id.exit:
				gameActivity.finish();
        			break;
        		case R.id.replay:
				gameActivity.doRestart();
        			break;
        		case R.id.share:
        			try{
        				ErrorController.tracking(getContext(), "game", "share", result.getData().getSong().getIdentity()+"-"+result.getRank(), result.getUserScore()+result.getBonusScore(), true);
        				
                			this.view.buildDrawingCache();
                			Bitmap cache = this.view.getDrawingCache();
                			if(cache.getWidth()>480){
                				cache = cache.createScaledBitmap(cache, 480, (int)(cache.getHeight()*(480f/cache.getWidth())), true);
                			}
                			
                			Bitmap watermark = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.watermark);

                			Bitmap bitmap = Bitmap.createBitmap(cache.getWidth()+watermark.getWidth(), cache.getHeight(), Bitmap.Config.ARGB_8888);
                			Canvas canvas = new Canvas(bitmap);

                			canvas.drawColor(0xFFEEEEEE);
                			canvas.drawBitmap(cache, 0, 0, null);
                			canvas.drawBitmap(watermark, cache.getWidth(), bitmap.getHeight()-watermark.getHeight(), null);
                			
                			
                			String fileName = Calendar.getInstance().getTime().toGMTString().replace(" ", "").replace("\t", "").replace("\n", "").toLowerCase();
                			File saveFile = new File( GameOptions.getRootFile(), "screenshot/"+fileName+".jpg" );
                			saveFile.getParentFile().mkdirs();
                			
        				FileOutputStream fos = null;
        				try{
        					fos= new FileOutputStream(saveFile);
        					bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        				}finally{
        					try{ fos.close(); }catch(Throwable e){}
        				}
        				
        				Uri uri = Uri.fromFile(saveFile);
        				
        				Intent intent = new Intent();
        				intent.setAction(Intent.ACTION_SEND);
        				intent.setType("image/*");

        				intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "subject");
        				intent.putExtra(android.content.Intent.EXTRA_TEXT, "text");
        				intent.putExtra(Intent.EXTRA_STREAM, uri);
        				getContext().startActivity(Intent.createChooser(intent, getContext().getString(R.string.result_share_text)));
        				
        			}catch(Throwable e){
        				e.printStackTrace();
        			}
        			break;
        		case R.id.game_start:
        			gameActivity.doNextGame();
        			break;
        	}
        }
	

	/************************************************************************************************************
	 * 
	 * 				@ACCESS_METHODS
	 * 
	 ************************************************************************************************************/
	
	public void animFullCombo(){

		final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fullcombo);

		getContext().runOnUiThread(new Runnable(){
			public void run(){
				ViewUtils.setAlpha(fullCombo, 1f);
				fullCombo.startAnimation(anim);
			}
		});
		
	}
	
	public void animNewRecord(){

		final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fullcombo);
		getContext().runOnUiThread(new Runnable(){
			public void run(){
				ViewUtils.setAlpha(newRecord, 1f);
				newRecord.startAnimation(anim);
			}
		});
		
	}
	
	public void animBonus(){
		
		final Animation anim2 = AnimationUtils.loadAnimation(getContext(), R.anim.bonus);
		
		getContext().runOnUiThread(new Runnable(){
			public void run(){
				ViewUtils.setAlpha(bonus, 1f);
				anim2.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart (Animation animation) {
						
					}
					@Override
					public void onAnimationRepeat (Animation animation) {
						
					}
					@Override
					public void onAnimationEnd (Animation animation) {
						bonus.setVisibility(View.GONE);
					}
				});
				bonus.startAnimation(anim2);
			}
		});
		
	}
	
	public void animRank(){
		
		final Animation anim3 = AnimationUtils.loadAnimation(getContext(), R.anim.rank_scale);

		getContext().runOnUiThread(new Runnable(){
			public void run(){
				ViewUtils.setAlpha(rank, 1f);
				rank.startAnimation(anim3);
			}
		});
		
	}
	
	public void animMenuLayout(){

		final Animation anim4 = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
		final Animation anim5 = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
		getContext().runOnUiThread(new Runnable(){
			public void run(){
				ViewUtils.setAlpha(menu_layout, 1f);
				menu_layout.startAnimation(anim4);

				ViewUtils.setAlpha(startBtn, 1f);
				startBtn.startAnimation(anim5);
			}
		});
		
	}
	
	public void setChangeScore(final int changeScore){
		getContext().runOnUiThread(new Runnable(){
			public void run(){
				score.setText(String.valueOf(changeScore));
			}
		});
	}
	
	public void onPause () {
		SoundFXPlayer.get(getContext()).bgStop();
        }

	public void onResum () {
		SoundFXPlayer.get(getContext()).bgPlay();
        }
	
}
