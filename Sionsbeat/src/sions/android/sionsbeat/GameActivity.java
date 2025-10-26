package sions.android.sionsbeat;

import java.io.File;
import java.util.HashMap;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;

import sions.android.SQ;
import sions.android.sionsbeat.fragment.AdsBanner;
import sions.android.sionsbeat.game.GameData;
import sions.android.sionsbeat.game.GameMode;
import sions.android.sionsbeat.game.view.GameCounterView;
import sions.android.sionsbeat.game.view.GameGraphView;
import sions.android.sionsbeat.game.view.GamePad;
import sions.android.sionsbeat.game.view.StrokeTextView;
import sions.android.sionsbeat.interpret.InterpretCollector;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.GoogleGameService;
import sions.android.sionsbeat.utils.JsonType;
import sions.android.sionsbeat.utils.SoundFXPlayer;
import sions.android.sionsbeat.view.BackgroundDrawable;
import sions.android.sionsbeat.window.AbsPopup;
import sions.android.sionsbeat.window.PausePopup;
import sions.android.sionsbeat.window.ResultPopup;
import sions.json.JSONObject;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewManager;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends BaseActivity implements OnTouchListener {

	public static final String INTENT_TAG = "data";
	public static final String INTENT_SONG_TAG = "song";

	private AbsPopup mPopupPopup;
	
	private String dataPath;
	private StrokeTextView pauseCounter;
	
	private Animation FeverEffectAnimation;
	
	private RelativeLayout gameContainer;
	private TextView ready, go;
	
	private TextView title;
	private TextView artist;
	private TextView duration;
	private TextView level;
	private TextView maxScore;
	
	private ImageView thumb;
	
	private ProgressBar hpBar;
	private GameData gameData;
	private GameMode mode;
	private GamePad padView;
	private GameGraphView graphView;
	private GameCounterView counterView;
	private HashMap<Integer, Float[]> map = new HashMap();
	
	private boolean finished;
	private boolean started;
	
	private InterstitialAd interstitial;
	private int interstitialType = 0;
	
	private GoogleGameService ggs;
	
	private Drawable gameBackground, gameFeverBackground;
	
	private AdsBanner adBanner;
	private View adContainer;

	private int gameSync;
	private float volumeGame;
	private float volumeVoice;
	private float volumeTouch;
	private boolean graphicFeverParticle;
	private boolean graphicFeverEffect;
	private boolean vibrateTouch;
	private boolean vibrateMiss;
	
	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		SQ.SQ(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		GameOptions.setupRootFile(this);

		//-- 인텐트 정보가 없습니다.
		if(getIntent() == null || getIntent().getExtras() == null || !getIntent().getExtras().containsKey( INTENT_TAG )){
			toastFinish( R.string.game_abnormal_intent );
			return;
		}

		gameSync = GameOptions.get(this).getSettingInt(GameOptions.OPTION_GAME_SYNC);
		
		graphicFeverParticle = GameOptions.get(this).getSettingBoolean(GameOptions.OPTION_GRAPHIC_GAME_FEVER_PARTICLE);
		graphicFeverEffect = GameOptions.get(this).getSettingBoolean(GameOptions.OPTION_GRAPHIC_GAME_FEVER_EFFECTS);
		
		volumeGame = GameOptions.get(this).getSettingInt(GameOptions.OPTION_SOUND_GAME)*0.01f;
		volumeVoice = GameOptions.get(this).getSettingInt(GameOptions.OPTION_SOUND_SYSTEM_VOICE)*0.01f;
		volumeTouch = GameOptions.get(this).getSettingInt(GameOptions.OPTION_SOUND_GAME_TOUCH)*0.01f;
		
		vibrateTouch = GameOptions.get(this).getSettingBoolean(GameOptions.OPTION_GAME_TOUCH_VIBRATOR);
		vibrateMiss = GameOptions.get(this).getSettingBoolean(GameOptions.OPTION_GAME_MISS_VIBRATOR);
		
		//-- 배경음 종료
		SoundFXPlayer.get(this).bgStop();
		SoundFXPlayer.get(this).bgRestart();

		importView();
		initBackground();
		createGame();
		
		ggs = new GoogleGameService(this);

		if(Base.FREE)
		{
			adBanner = new AdsBanner((AdView)findViewById(R.id.adView));
			adContainer = (View) findViewById(R.id.adView).getParent();
		}
		else
		{
			View v = findViewById(R.id.adView);
			if(v != null)
			{
				((ViewManager)v.getParent()).removeView(v);
			}
		}
	}
	
	private void initBackground(){
		DisplayMetrics dm = getResources().getDisplayMetrics();

		gameBackground = getResources().getDrawable(R.drawable.game_background);
		gameFeverBackground = getResources().getDrawable(R.drawable.game_background_fever);
		
	}
	
	private void importView(){
		
		gameContainer = (RelativeLayout) findViewById(R.id.gameContainer);

		padView = (GamePad) findViewById(R.id.padView);
		graphView = (GameGraphView) findViewById(R.id.graphView);
		counterView = (GameCounterView) findViewById(R.id.score);

		ready = (TextView) findViewById(R.id.ready);
		go = (TextView) findViewById(R.id.go);
		
		title = (TextView)findViewById(R.id.title);
		artist = (TextView)findViewById(R.id.artist);
		duration = (TextView)findViewById(R.id.duration);
		level = (TextView)findViewById(R.id.level);

		thumb = (ImageView) findViewById(R.id.thumbnail);
		pauseCounter = (StrokeTextView) findViewById( R.id.pauseCounter );
		hpBar = (ProgressBar)findViewById(R.id.hp_bar);
		maxScore = (TextView)findViewById(R.id.maxScore);

		padView.setGameTouchPadding(GameOptions.get(this).getSettingInt(GameOptions.OPTION_GAME_PAD_MARGIN)*0.01f);
		gameContainer.setOnTouchListener(this);
	}
	
	private boolean createGame(){
		
		//-- 데이터를 찾을 수 없습니다.
		dataPath = getIntent().getExtras().getString( INTENT_TAG );
		File file = new File(dataPath);
		if(!file.exists()){
			toastFinish( R.string.game_notfounddata );
			return false;
		}
		
		//-- 게임 데이터 생성
		try{
			String songJson = getIntent().getExtras().getString( INTENT_SONG_TAG );
			SongInfo song = null;
			if(songJson != null ){
				try{
					song = new SongInfo(new JSONObject(songJson));
				}catch(Throwable e){}
			}
			
			gameData = new GameData( file, song );
			if(gameData == null){
				toastFinish( R.string.game_notfounddata );
				return false;
			}
		}catch(Throwable e){
			ErrorController.error( 10, e );
			toastFinish( R.string.game_preparedError );
			return false;
		}
		
		//-- 게임 모드 생성
		mode = new GameMode(this);
		
		mode.setGraphView( graphView );
		mode.setPadView( padView );
		mode.setCounterView(counterView);
		mode.prepareOriginal( gameData );
		
		
		if(!mode.prepare()){
			toastFinish( R.string.game_preparedError);
			return false;
		}
		
		//-- 게임 정보 적용
		gameData.getSong().parseAlbumView(this, thumb, R.drawable.list_empty);
//		if(gameData.getSong().getArt()!=null && gameData.getSong().getArt().length()>5){
//			String path = gameData.getSong().getArt();
//			if(path.indexOf("://") == -1){
//				Picasso.with(this).load(new File(path)).placeholder(R.drawable.list_empty).into(thumb);
//			}else{
//				Picasso.with(this).load(Uri.parse(path)).placeholder(R.drawable.list_empty).into(thumb);
//			}
//		}
		
		title.setText( gameData.getSong().getTitle() );
		artist.setText( gameData.getSong().getArtist() );
		duration.setText( "00:00/"+gameData.getSong().getDurationString() );
		level.setText( "Lv"+gameData.getLevel() );
		setupMaxScore(0); // 데이터에서 수집
		
		//-- 게임 시작
		mode.start();
		
		return true;
	}
	
	public void setupMaxScore(int score)
	{
		while(score == 0){

			JSONObject musics = SQ.getJSONObject(JsonType.MUSIC);
			if(musics == null) break;

			JSONObject record = musics.getJSONObject(gameData.getSong().getIdentity());
			if(record == null) break;

			score = record.getInt("score", 0);
			break;
		}
		
		final String text = getString(R.string.game_maxScore, score);
		runOnUiThread(new Runnable(){
			public void run(){
				maxScore.setText(text);
			}
		});
		
	}
	
	private void initInterstitial(){
		interstitial = new InterstitialAd(this);
		interstitial.setAdUnitId("ca-app-pub-1093695024710196/9997454865");
		
		AdRequest adRequest = AdsBanner.getAdRequest();
		interstitial.loadAd(adRequest);
	}
	
	/**************************************************************************
	 * 
	 * @ORIGIN_EVENT
	 * 
	 *************************************************************************/

	@Override
	public boolean onKeyUp ( int keyCode, KeyEvent event )
	{

		if ( keyCode == KeyEvent.KEYCODE_BACK )
		{
			if( mPopupPopup instanceof PausePopup ){
				onPopupClose();
				doResum();
			}else if( mPopupPopup instanceof ResultPopup ){
				finish();
			}else if(mode.getGameStatus() < GameMode.STATUS_PLAY){
				Base.ShowImmersiveAds(this, new Runnable() {
					@Override
					public void run() {
						finish();
					}
				});
			}else{
				doPause(false);
			}
			return true;
		}
		return super.onKeyUp( keyCode, event );
	}
	
	public void onOptionBtn(View view){
		doPause(false);
	}
	
	
	/**************************************************************************
	 * 
	 * @LifeCycle
	 * 
	 *************************************************************************/
			
	@Override
	protected void onStart () {
	        super.onStart();
	        if(mode != null){
	        	mode.onContextStart();
	        }
	        ggs.onStart();
	}
	
	@Override
	protected void onStop () {
	        super.onStop();
	        if(mode != null){
	        	mode.onContextStop();
	        }
	        ggs.onStop();
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent intent) {
		ggs.onActivityResult(requestCode, resultCode, intent);
	}
	
	@Override
	protected void onResume() {
		SQ.SQ(this);
		super.onResume();
		
		if(adBanner != null)
		{		
			adBanner.onResume();
		}
		
		InterpretCollector.setSleepTime(5);
		
		started = true;

		if(mPopupPopup instanceof ResultPopup){
			((ResultPopup)mPopupPopup).onResum();
		}else if(mode.getGameStatus() < GameMode.STATUS_PLAY){
			mode.resum();
		}
	        if(SplashActivity.STORE_NETWORK_STATE) EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(adBanner != null)
		{		
			adBanner.onPause();
		}
		if(mPopupPopup instanceof ResultPopup){
			((ResultPopup)mPopupPopup).onPause();
		}else{
			doPause(true);
		}
	        if(SplashActivity.STORE_NETWORK_STATE) EasyTracker.getInstance(this).activityStop(this);
	}
	
	@Override
	protected void onDestroy () {
        super.onDestroy();
        if(adBanner != null)
        {
        	adBanner.onDestroy();
        }
	}
	
	@Override
	public void finish() {
		if(gameData != null && gameData.getSong() != null){
			ErrorController.tracking(this, "game", "exit", gameData.getSong().getIdentity(), gameData.getLevel(), true);
		}else{
			ErrorController.tracking(this, "game", "exit", "nodata", 0, true);
		}
		
		finished = true;
		onPopupClose();
		super.finish();
		if(mode != null){
			mode.finish();
			mode.dispose();
		}
	}

	@Override
        public boolean onTouch (View v, MotionEvent event) {
		if(mode != null && mode.getFever()!=null){
			mode.getFever().onShake();
		}
	        return false;
        }
	

	
	
	/**************************************************************************
	 * 
	 * @Animation
	 * 
	 *************************************************************************/
	
	public void animReady(){
		animView(ready, 0);
	}
	public void animGo(){
		animView(go, 0);
	}
	public void animView(final View view, final int resID){
		final Animation anim = AnimationUtils.loadAnimation(this, R.anim.scale_in);
		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart (Animation animation) {}
			
			@Override
			public void onAnimationRepeat (Animation animation) {}
			
			@Override
			public void onAnimationEnd (Animation animation) {
				view.setVisibility(View.GONE);
			}
		});
		runOnUiThread(new Runnable(){
			public void run(){
				view.setVisibility(View.VISIBLE);
				view.startAnimation(anim);
				
				if(resID != 0){
					if(view instanceof TextView){
						((TextView)view).setText(resID);
					}
				}
			}
		});
	}
	public void animView(final View view, final String text){
		final Animation anim = AnimationUtils.loadAnimation(this, R.anim.scale_in);
		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart (Animation animation) {}
			
			@Override
			public void onAnimationRepeat (Animation animation) {}
			
			@Override
			public void onAnimationEnd (Animation animation) {
				view.setVisibility(View.GONE);
			}
		});
		runOnUiThread(new Runnable(){
			public void run(){
				view.setVisibility(View.VISIBLE);
				view.startAnimation(anim);
				
				if(text != null){
					if(view instanceof TextView){
						((TextView)view).setText(text);
					}
				}
			}
		});
	}
	public void animAdView(final boolean visible){
		int resID = visible ? android.R.anim.fade_in : android.R.anim.fade_out;
		final Animation anim = AnimationUtils.loadAnimation(this, resID);

		if(!visible){
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart (Animation animation) {}
				public void onAnimationRepeat (Animation animation) {}
				public void onAnimationEnd (Animation animation) 
				{
					if(adContainer != null)
					{
						adContainer.setVisibility(View.GONE);
					}
				}
			});	
		}
		
		runOnUiThread(new Runnable(){
			public void run(){
				if(adContainer != null)
				{
					adContainer.setVisibility(View.VISIBLE);
					adContainer.startAnimation(anim);
				}
			}
		});
	}
	
	/**************************************************************************
	 * 
	 * @ACTION
	 * 
	 *************************************************************************/

	public void doFever(final boolean on){

		ErrorController.tracking(this, "game", "fever", gameData.getSong().getIdentity(), gameData.getLevel(), true);
		
		runOnUiThread(new Runnable(){
			public void run(){
				long startTime = System.currentTimeMillis();
				
				DisplayMetrics dm = getResources().getDisplayMetrics();
				
				if(on){
					if(graphicFeverEffect){
						gameContainer.setBackgroundDrawable(gameFeverBackground);
						
						View view = findViewById(R.id.fever_effect);
						view.setVisibility(View.VISIBLE);
						view.startAnimation(FeverEffectAnimation = AnimationUtils.loadAnimation(GameActivity.this, R.anim.fever_effect));
					}
					padView.setFever(true);
					
					SoundFXPlayer.get(GameActivity.this).play(R.raw.fever, getVolumeGame());
					
				}else{
					if(graphicFeverEffect){
						gameContainer.setBackgroundDrawable(gameBackground);

						View view = findViewById(R.id.fever_effect);
						view.clearAnimation();
						view.setVisibility(View.GONE);
					}
					padView.setFever(false);
					
				}
				
				Log.d("performance", "feverTime "+(System.currentTimeMillis()-startTime)+"ms");
			}
		});
		
	}

	public void doResum(){
		if(finished) return;
		
		if(mode != null) mode.resum();
	}
	
	public void doPause(boolean systemPause){
		if(finished || !started) return;
		
		if(mode != null){
			if(systemPause || mode.getGameStatus() == GameMode.STATUS_PLAY){
				mode.pause();
			}
			
			if(mode.getGameStatus() == GameMode.STATUS_PLAY)
			{
				doPauseCounterEnd();
				PausePopup popup = new PausePopup( this );
				popup.show(this);
				
				onPopupShow( popup );
			}
		
		}
	}

	public void doRestart ()
	{
		ErrorController.tracking(this, "game", "restart", gameData.getSong().getIdentity(), gameData.getLevel(), true);
		
		doPauseCounterEnd();
		
		if(interstitial != null && interstitial.isLoaded()){
			interstitial.show();
		}else{
			
			SoundFXPlayer.get(this).bgStop();
			SoundFXPlayer.get(this).bgRestart();

			setTime(mode.getGameData().getStartOffset());
			counterView.setDirectCount(0);
			
			setHP(10000);
			mode.clear();
			mode.prepare();
			mode.start();
			
			onPopupClose();
			
//			Intent intent = new Intent(this, getClass());
//			intent.putExtra( INTENT_TAG, dataPath );
//			intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
//			startActivity( intent );
			
		}
	}

	public void doNextGame ()
	{
		ErrorController.tracking(this, "game", "nextGame", gameData.getSong().getIdentity(), gameData.getLevel(), true);
		
		doPauseCounterEnd();
		
		if(interstitial != null && interstitial.isLoaded()){
			interstitial.show();
		}else{
			
			SoundFXPlayer.get(this).bgStop();
			SoundFXPlayer.get(this).bgRestart();

			mode.putNextGame();
			
			setTime(mode.getGameData().getStartOffset());
			counterView.setDirectCount(0);
			
			setHP(10000);
			mode.clear();
			mode.prepare();
			mode.start();
			
			onPopupClose();
			
//			Intent intent = new Intent(this, getClass());
//			intent.putExtra( INTENT_TAG, dataPath );
//			intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
//			startActivity( intent );
			
		}
	}

	
	/**************************************************************************
	 * 
	 * @POPUP
	 * 
	 *************************************************************************/
	
	public void onPopupShow(AbsPopup popup){
		onPopupClose();
		
		mPopupPopup = popup;
		popup.setListener( new AbsPopup.PopupListener()
		{
			@Override
			public void onPopupClose ( AbsPopup popup )
			{
				popup.dispose();
				GameActivity.this.mPopupPopup = null;
			}
		} );
		animAdView(true);
	}
	public boolean onPopupClose(){
		if(mPopupPopup!=null && mPopupPopup.dispose()){
			animAdView(false);
			return true;
		}else{
			return false;
		}
	}
	
	public void doPauseCounter(final int count){
		runOnUiThread( new Runnable(){
			public void run(){
				pauseCounter.setVisibility( View.VISIBLE );
				pauseCounter.setText( String.valueOf(count) );
			}
		} );
	}
	
	public void doPauseCounterEnd(){
		runOnUiThread( new Runnable(){
			public void run(){
				pauseCounter.setVisibility( View.GONE );
			}
		} );
	}
	
	/**************************************************************************
	 * 
	 * @GETSET
	 * 
	 *************************************************************************/

	public GameData getGameData ()
	{
		return gameData;
	}

	public void setGameData ( GameData gameData )
	{
		this.gameData = gameData;
	}

	public GameMode getMode ()
	{
		return mode;
	}

	public void setMode ( GameMode mode )
	{
		this.mode = mode;
	}
	
	public float getVolumeGame () {
		return volumeGame;
	}

	public float getVolumeVoice () {
		return volumeVoice;
	}

	public float getVolumeTouch () {
		return volumeTouch;
	}

	public boolean isGraphicFeverParticle () {
		return graphicFeverParticle;
	}

	public boolean isGraphicFeverEffect () {
		return graphicFeverEffect;
	}

	public int getGameSync () {
		return gameSync;
	}

	public boolean isVibrateTouch () {
		return vibrateTouch;
	}

	public boolean isVibrateMiss () {
		return vibrateMiss;
	}

	public void setHP ( final int hp ){
		runOnUiThread(new Runnable(){
			public void run(){
				hpBar.setProgress(hp);
			}
		});
	}
	
	public void adViewGone(){
		runOnUiThread(new Runnable(){
			public void run(){
				if(adContainer != null)
				{
					adContainer.setVisibility(View.GONE);
				}
			}
		});
	}
	
	public void setTime (int time){
		if(time < 0) time = 0;
		
		final String timeText = SongInfo.getTimeCode(time);
		final String durationText = gameData.getSong().getDurationString();
		
		runOnUiThread(new Runnable(){
			public void run(){
				duration.setText( timeText+"/"+durationText );
			}
		});
	}

	public GoogleGameService getGoogleGameService () {
		return ggs;
	}

	public void doErrorFinish (final Exception e) {
		runOnUiThread(new Runnable(){
			public void run(){
				Toast.makeText(GameActivity.this, getString(R.string.game_error_game_mode_exception, e.getMessage()), Toast.LENGTH_LONG).show();
				finish();
			}
		});
	        
        }


}
