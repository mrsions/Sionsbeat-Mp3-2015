package sions.android.sionsbeat;

import java.io.File;
import java.util.List;

import com.androidquery.util.AQUtility;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import sions.android.MarketService;
import sions.android.SQ;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.adapter.MusicListAdapter;
import sions.android.sionsbeat.fragment.AdsBanner;
import sions.android.sionsbeat.game.GameData;
import sions.android.sionsbeat.interpret.InterpretCollector;
import sions.android.sionsbeat.interpret.InterpretMusic;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.GoogleGameService;
import sions.android.sionsbeat.utils.JsonType;
import sions.android.sionsbeat.utils.SoundFXPlayer;
import sions.android.sionsbeat.view.BackgroundDrawable;
import sions.android.sionsbeat.window.AbsPopup;
import sions.android.sionsbeat.window.MarkerSelectPopup;
import sions.android.sionsbeat.window.MusicSelectPopup;
import sions.android.sionsbeat.window.SettingPopup;
import sions.json.JSONObject;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class MusicSelectActivity extends BGMActivity implements AdapterView.OnItemClickListener {

	public static final int REQUEST_ACHIEVEMENTS = 1;
	public static final int REQUEST_NOTEMAKE = 0x100;
	public static final int REQUEST_NOTEMODIFY = 0x101;
	
	public static String TAG = "music_select";
	
	public static InterstitialAd immersiveAds;

	private MusicListAdapter adapter;
	private ListView musicListView;
	private List<SongInfo> songList;
	private AbsPopup mPopupPopup;
	private AdsBanner adBanner;
	private boolean isCreate = true;
	
	private long existTime;
	
	private GoogleGameService ggs;

	/***************************************************************************
	 * 
	 * @LIFE_CYCLE
	 * 
	 ***************************************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SQ.SQ(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_select);
		GameOptions.setupRootFile(this);
		
		
		BackgroundDrawable.setup(this);
		
		musicListView = (ListView) findViewById(R.id.musicListView);
		musicListView.setOnItemClickListener(this);
		setupMusicList();
		setupMarkerIcon();
		
		SoundFXPlayer.get( this ).play( R.raw.musicselect, GameOptions.get(this).getSettingInt(GameOptions.OPTION_SOUND_SYSTEM_VOICE)*0.01f );

		ggs = new GoogleGameService(this);

		if(Base.FREE)
		{
			adBanner = new AdsBanner((AdView)findViewById(R.id.adView));
			Base.InitializeImmersiveAds(this);
			Base.ShowPleasePurchaseNew(this);
		}
		else{
			View v = findViewById(R.id.adView);
			if(v != null)
			{
				((ViewManager)v.getParent()).removeView(v);
			}
		}

//	        if(!GameOptions.get(this).contains(SettingPressPopup.TOUCH_RANGE_MAX)){
//	        	musicListView.postDelayed(new Runnable() {
//				@Override`1
//				public void run () {
//			        	SettingPressPopup popup = new SettingPressPopup(MusicSelectActivity.this);
//			        	onPopupShow(popup);
//			        	popup.setDisableClose(false);
//			        	popup.show();
//				}
//			}, 10);
//	        }

	        musicListView.postDelayed(new Runnable(){
			public void run(){
			        marketUpdateCheck();	
			}
		}, 10);
	}

	@Override
	protected void onStart () {
	        super.onStart();
	        ggs.onStart();
	}
	
	@Override
	protected void onStop () {
	        super.onStop();
	        ggs.onStop();
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent intent) {
		ggs.onActivityResult(requestCode, resultCode, intent);
		if(mPopupPopup != null){
			mPopupPopup.onActivityResult(requestCode, resultCode, intent);
		}
	};
	
	@Override
	protected void onResume() {
		SQ.SQ(this);
		super.onResume();
		isCreate = false;

		SettingPopup.InitializeSettings(this);
		InterpretCollector.setSleepTime(0);
		
		for(SongInfo song:songList){
			adapter.onStatusSetup(song);
		}
		
		if(adBanner != null) adBanner.onResume();
		if(mPopupPopup != null) mPopupPopup.onResum();
		
		System.gc();
		
	        if(SplashActivity.STORE_NETWORK_STATE) EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	protected void onPause () {
	        super.onPause();
	        if(adBanner != null) adBanner.onPause();
		if(mPopupPopup != null) mPopupPopup.onPause();

	        if(SplashActivity.STORE_NETWORK_STATE) EasyTracker.getInstance(this).activityStop(this);
	}
	
	@Override
	protected void onDestroy () {
	        super.onDestroy();
		if(adBanner != null) adBanner.onDestroy();
	}
	
	@Override
	public void finish() {
		super.finish();
		System.exit(0);
	}

	@Override
	public boolean onKeyUp ( int keyCode, KeyEvent event )
	{

		if ( keyCode == KeyEvent.KEYCODE_BACK )
		{
			if(onPopupClose()) return true;
			
			if ( System.currentTimeMillis() < existTime )
			{
				finish();
				System.exit( 0 );
			}
			else
			{
				existTime = System.currentTimeMillis() + 2000;
				Toast.makeText( this, R.string.select_exit_message, Toast.LENGTH_SHORT ).show();
			}
			return true;
		}
		return super.onKeyUp( keyCode, event );
	}

	/***************************************************************************
	 * 
	 * @EVENT
	 * 
	 ***************************************************************************/
	
	public void onPopupShow(AbsPopup popup){
		onPopupClose();
		
		mPopupPopup = popup;
		popup.setListener( new AbsPopup.PopupListener()
		{
			@Override
			public void onPopupClose ( AbsPopup popup )
			{
				popup.dispose();
				MusicSelectActivity.this.mPopupPopup = null;

				if(popup instanceof MarkerSelectPopup){
					setupMarkerIcon();
				}
			}
		} );
	}
	public boolean onPopupClose(){
		return mPopupPopup!=null && mPopupPopup.dispose();
	}

	public void onRankingBtn(View view) 
	{
		ggs.showLeaderboard(this, R.string.leaderboard_single_score);

		ErrorController.tracking(this, "music_select", "rank_open", "", 0, true);
	}

	public void onAchievementBtn(View view) 
	{
		ggs.showAchievements(this);
		ErrorController.tracking(this, "music_select", "achievement_open", "", 0, true);
	}
	
	public void onSettingBtn(View view) 
	{
		SettingPopup popup = new SettingPopup(this);
		onPopupShow(popup);
		popup.show();
		
		ErrorController.tracking(this, "music_select", "setting_open", "", 0, true);
	}

	public void onMarkerBtn(View view) {
		MarkerSelectPopup popup = new MarkerSelectPopup(this);
		onPopupShow(popup);
		popup.show();
		
		ErrorController.tracking(this, "music_select", "marker_open", "", 0, true);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		final SongInfo song = (SongInfo) songList.get(position);

		ErrorController.tracking(this, "music_select", "open", song.getIdentity(), 0, true);
		
		MusicSelectPopup popup = new MusicSelectPopup( this );
		popup.show( song );
		
		onPopupShow( popup );
		
	}
	
	/***************************************************************************
	 * 
	 * @SETUP
	 * 
	 ***************************************************************************/

	private void marketUpdateCheck(){
		MarketService ms = new MarketService(this);
		ms.level(MarketService.REVISION).checkVersion();
	}
	
	private void setupMusicList() {

		this.songList = InterpretCollector.get( this ).getSongList();
		this.adapter = new MusicListAdapter(this, this.songList);
		
		this.musicListView.setAdapter(this.adapter);
	}
	
	private void setupMarkerIcon() {
		
		File folder = GameOptions.get(this).getMarkerFile(), file;
		if(folder != null && folder.exists())
		{
			file = new File(folder, "intro/14.png");
			if(!file.exists()) file = new File(folder, "intro/14.jpg");
			if(!file.exists()) file = new File(folder, "intro/14.gif");
			if(!file.exists()) file = new File(folder, "intro/14");
			
			if(file.exists()){
				try{
					Bitmap marker = BitmapFactory.decodeFile(file.getAbsolutePath());
					if(marker != null){
						
						ImageButton markerButton = (ImageButton)findViewById(R.id.marker_btn);
						markerButton.setImageBitmap(marker);
						
					}
				}catch(Throwable e){
					ErrorController.error(10, e);
				}
			}
		}
		
	}

	public GoogleGameService getGoogleGameService () {
		return ggs;
	}

	public void doReloadList () {
		this.songList = InterpretCollector.get( this ).getSongList();
		this.adapter.setList(this.songList);
		runOnUiThread(new Runnable(){
			public void run(){
				adapter.notifyDataSetChanged();	
			}
		});
        }

	
}
