package sions.android.sionsbeat;

import java.io.File;
import java.util.ArrayList;

import com.google.analytics.tracking.android.EasyTracker;

import sions.android.SQ;
import sions.android.sionsbeat.game.GameData;
import sions.android.sionsbeat.game.view.GamePad;
import sions.android.sionsbeat.maker.FFTVisualize;
import sions.android.sionsbeat.maker.MakerGLPadView;
import sions.android.sionsbeat.maker.MakerGameMode;
import sions.android.sionsbeat.maker.MakerPopupMenu;
import sions.android.sionsbeat.maker.MakerTimelineView;
import sions.android.sionsbeat.maker.NoteModifyPopup;
import sions.android.sionsbeat.maker.SingleNote;
import sions.android.sionsbeat.template.Note;
import sions.android.sionsbeat.template.NoteSet;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.template.NoteSet.NoteFile;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.SoundFXPlayer;
import sions.android.sionsbeat.window.AbsPopup;
import sions.android.sionsbeat.window.MarkerSelectPopup;
import sions.android.sionsbeat.window.MusicSelectPopup;
import sions.android.tooltip.ToolTip;
import sions.android.tooltip.ToolTipPopup;
import sions.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.internal.widget.ListPopupWindow;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

public class NoteMakerActivity extends BaseActivity implements OnClickListener, OnItemClickListener {
	
	public static final String INTENT_NOTESET = "noteset";
	
	private static final int[] tempoValues = new int[]{4,8,12,16,20,24,32,1000};
	private static String[] tempoString;
	
	public static final String INTENT_TAG = "data";
	public static final String INTENT_SONG_TAG = "song";

	private AbsPopup mPopupPopup;
	
	private ImageButton menuBtn, undoBtn, redoBtn, playBtn, recordBtn, analyzeBtn;
	private Button tempoBtn;
	
	private MakerTimelineView timelineView;
	private MakerGLPadView padView;
	private SongInfo song;
	
	private MakerGameMode mode;
	private FFTVisualize visualize;
	
	private ListPopupWindow tempoPopupListener;
	private ArrayAdapter<String> tempoAdapter;
	
	private NoteSet noteSet;
	private NoteFile currNoteFile;
	
	private boolean saveAfterExit;
	private boolean newNoteMake;
	private int saveCount;
	
	private float volumeTouch;
	
	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		SQ.SQ(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_maker);
		GameOptions.setupRootFile(this);

		//-- 배경음 종료
		SoundFXPlayer.get(this).bgStop();
		SoundFXPlayer.get(this).bgRestart();

		volumeTouch = GameOptions.get(this).getSettingInt(GameOptions.OPTION_SOUND_GAME_TOUCH)*0.01f;
		
		initialize();
		importView();
		setupEvent();
		createMaker();

		// default tempo
		tempoBtn.setText("1/16");
		mode.setTempo(16);
		timelineView.setDirty(true);
		
		timelineView.postDelayed(new Runnable(){
			public void run(){
//				showTooltip();
			}
		}, 10);
	}
	
	private void initialize()
	{
		tempoString = new String[tempoValues.length];
		for(int i=0; i<tempoValues.length; i++)
		{
			if(tempoValues[i] == 1000)
				tempoString[i] = "free";
			else
				tempoString[i] = "1/"+tempoValues[i];
		}
		tempoAdapter = new ArrayAdapter<String>(this, R.layout.layout_maker_tempo_item, tempoString);
		
	}
	
	private void importView(){
		
		padView = (MakerGLPadView) findViewById(R.id.padView);
		timelineView = (MakerTimelineView) findViewById(R.id.timeline);

		menuBtn = (ImageButton) findViewById(R.id.menu);
		undoBtn = (ImageButton) findViewById(R.id.undo);
		redoBtn = (ImageButton) findViewById(R.id.redo);
		tempoBtn = (Button) findViewById(R.id.tempo);
		analyzeBtn = (ImageButton) findViewById(R.id.analyze);
		
		recordBtn = (ImageButton) findViewById(R.id.record);
		playBtn = (ImageButton) findViewById(R.id.play);

		padView.setGameTouchPadding(GameOptions.get(this).getSettingInt(GameOptions.OPTION_GAME_PAD_MARGIN)*0.01f);
	}
	
	private void setupEvent()
	{
		menuBtn.setOnClickListener(this);
		undoBtn.setOnClickListener(this);
		redoBtn.setOnClickListener(this);
		playBtn.setOnClickListener(this);
		recordBtn.setOnClickListener(this);
		analyzeBtn.setOnClickListener(this);
		tempoBtn.setOnClickListener(this);
	}
	
	private void createMaker(){

		if(getSongInfo() == null)
		{
			toastFinish(R.string.select_notfoundmusic); // 음악을 찾을 수 없습니다.
			return;
		}
		
		File music = new File(getSongInfo().getPath());
		if(!music.exists())
		{
			toastFinish(R.string.select_notfoundmusic); // 음악을 찾을 수 없습니다.
			return;
		}
		
		try{
			visualize = new FFTVisualize(music);
		}catch(Throwable e){
			toastFinish(R.string.select_notsupported); // 지원하지 않는 음악입니다
			return;
		}
		
		noteSet = readNoteSet();
		
		
		mode = new MakerGameMode(this);
		
		mode.setSong(song);
		mode.setPadView( padView );
		mode.setTimeline( timelineView );
		mode.prepare();
		
		timelineView.setVisualize(visualize);
		visualize.setTimelineView(timelineView);
		
		mode.setupNoteFile(currNoteFile);
		
		mode.start();
		
	}
	
	private NoteSet readNoteSet()
	{
		NoteSet noteSet = null;

		if(getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(NoteMakerActivity.INTENT_NOTESET));
		{
			try{
				String jsonText = getIntent().getExtras().getString(NoteMakerActivity.INTENT_NOTESET, null);
				if(jsonText != null){
					noteSet = new NoteSet(new JSONObject(jsonText));
					
					for(NoteFile nf:noteSet.getNotes()){
						if(nf != null)
						{
							GameData gd = new GameData(nf.getFile(), song);
							
							nf.setNotes(gd.getNotes());
							currNoteFile = nf;
						}
					}
					
					return noteSet;
				}
			}catch(Throwable e){
				ErrorController.error(10, e);
			}
		}
		
		noteSet = new NoteSet(song, NoteSet.TYPE_CUSTOM, 3);
		noteSet.setNoteName("");
		noteSet.setStartOffset(0);
		noteSet.setEndOffset(song.getDuration());
		for(int i=0; i<3; i++)
		{
			currNoteFile = new NoteFile(i, 0, null);
			currNoteFile.setNotes(new SingleNote[0]);
			noteSet.getNotes().set(i, currNoteFile);
		}
		newNoteMake = true;
		
		return noteSet;
	}
	
	private SongInfo getSongInfo()
	{
		if(this.song == null)
		{
			String songJson = getIntent().getExtras().getString( INTENT_SONG_TAG );
			if(songJson != null ){
				try{
					return this.song = new SongInfo(new JSONObject(songJson));
				}catch(Throwable e){
					ErrorController.error(10, e);
					doErrorFinish(e);
					return null;
				}
			}
		}
		return this.song;
	}
	
	/**************************************************************************
	 * 
	 * @ORIGIN_EVENT
	 * 
	 *************************************************************************/

	private boolean breakPrevRecord(){
		if(mode.getRunStatus() == MakerGameMode.STATUS_RECORD){
			boolean start = mode.recordToggle();
			if(start){
				recordBtn.setImageResource(R.drawable.make_icon_record_pause);
			}else{
				recordBtn.setImageResource(R.drawable.make_icon_record);
			}
			return true;
		}else if(mode.getRunStatus() == MakerGameMode.STATUS_PREVIEW){
			boolean start = mode.previewToggle();
			if(start){
				playBtn.setImageResource(R.drawable.make_icon_pause);
			}else{
				playBtn.setImageResource(R.drawable.make_icon_play);
			}
			return true;
		}
		return false;
	}
	
	@Override
        public void onClick (View v) {
		
		switch ( v.getId() )
		{
			case R.id.menu:
				{
					breakPrevRecord();
					mode.doInputNoteFile(currNoteFile);
					MakerPopupMenu menu = new MakerPopupMenu(this, menuBtn);
					menu.show();
				}
				break;
			case R.id.undo:
				{
					breakPrevRecord();
					mode.undo();
				}
				break;
			case R.id.redo:
				{
					breakPrevRecord();
					mode.redo();
				}
				break;
			case R.id.play:
				{
					boolean start = mode.previewToggle();
					if(start){
						playBtn.setImageResource(R.drawable.make_icon_pause);
					}else{
						playBtn.setImageResource(R.drawable.make_icon_play);
					}
				}
				break;
			case R.id.record:
				{
					boolean start = mode.recordToggle();
					if(start){
						recordBtn.setImageResource(R.drawable.make_icon_record_pause);
					}else{
						recordBtn.setImageResource(R.drawable.make_icon_record);
					}
				}
				break;
			case R.id.analyze:
				{
					int analyze = (timelineView.getAnaylzeStyle()+1)%3;
					timelineView.setAnaylzeStyle(analyze);
					switch(analyze)
					{
						case MakerTimelineView.ANALYZE_NONE:
							analyzeBtn.setImageResource(R.drawable.make_icon_analyze_none);
							break;
						case MakerTimelineView.ANALYZE_GRAYSCALE:
							analyzeBtn.setImageResource(R.drawable.make_icon_analyze_white);
							break;
						case MakerTimelineView.ANALYZE_COLOR:
							analyzeBtn.setImageResource(R.drawable.make_icon_analyze_color);
							break;
					}
					timelineView.setDirty(true);
				}
				break;
			case R.id.tempo:
				{
					breakPrevRecord();
					int mWidth = tempoBtn.getMeasuredWidth();
					int width = (int) ( mWidth * 1.5f );
					
					tempoPopupListener = new ListPopupWindow(this);
					tempoPopupListener.setAdapter(tempoAdapter);
					tempoPopupListener.setAnchorView(tempoBtn);
					tempoPopupListener.setOnItemClickListener(this);
					
					tempoPopupListener.setWidth(width);
					tempoPopupListener.setHorizontalOffset((mWidth-width)/2);
					
					tempoPopupListener.setModal(true);
					tempoPopupListener.show();
					
				}
				break;
		}
	}

	@Override
        public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
		if(parent.getAdapter() == tempoAdapter){
			int tempo = tempoValues[position];
			String text = tempoString[position];
			
			tempoBtn.setText(text);
			mode.setTempo(tempo);
			timelineView.setDirty(true);
			
			tempoPopupListener.dismiss();
		}
        }
	
	@Override
	public boolean onKeyUp ( int keyCode, KeyEvent event )
	{
		Log.d("test","popup close "+keyCode +" / "+KeyEvent.KEYCODE_BACK);
		if ( keyCode == KeyEvent.KEYCODE_BACK )
		{
			if(breakPrevRecord()){
				return true;
			}
			if(mPopupPopup != null)
			{
				Log.d("test","popup close "+mPopupPopup);
				onPopupClose();
				return true;
			}
			doFinish();
			return true;
		}
		return super.onKeyUp( keyCode, event );
	}
	
	/**************************************************************************
	 * 
	 * @LifeCycle
	 * 
	 *************************************************************************/
			
	@Override
	protected void onStart () {
	        super.onStart();
	}
	
	@Override
	protected void onStop () {
	        super.onStop();
	}
	
	@Override
	protected void onResume() {
		SQ.SQ(this);
		super.onResume();
		if(mode != null) mode.onResume();
	        if(SplashActivity.STORE_NETWORK_STATE) EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mode != null) mode.onPause();
	        if(SplashActivity.STORE_NETWORK_STATE) EasyTracker.getInstance(this).activityStop(this);
	        doPlayRelease();
	}
	
	@Override
	public void finish() {
		if(mode != null){
			mode.finish();
			
			if(saveCount > 0 && noteSet.getRootFile()!=null){
				Intent intent = new Intent();
				intent.putExtra(INTENT_NOTESET, noteSet.toJson().toString());
				setResult(Activity.RESULT_OK, intent);
			}else{
				setResult(Activity.RESULT_CANCELED);
			}
		}
		if(visualize != null){
			visualize.dispose();
		}
		if(padView != null){
			padView.setFinish(true);
		}
		super.finish();
	}

	/**************************************************************************
	 * 
	 * @Animation
	 * 
	 *************************************************************************/
	
	/**************************************************************************
	 * 
	 * @ACTION
	 * 
	 *************************************************************************/

	public void doPlayRelease()
	{
		playBtn.setImageResource(R.drawable.make_icon_play);
		recordBtn.setImageResource(R.drawable.make_icon_record);
	}
	
	public void doErrorFinish (final Throwable e) {
		runOnUiThread(new Runnable(){
			public void run(){
				Toast.makeText(NoteMakerActivity.this, getString(R.string.game_error_game_mode_exception, e.getMessage()), Toast.LENGTH_LONG).show();
				finish();
			}
		});
	        
        }

	public void doSaveBefore () {
		saveCount++;
		Note[] notes = mode.getNotes().toArray(new Note[mode.getNotes().size()]);
		currNoteFile.setNotes(notes);
		mode.setSaveDirty(false);
        }
	
	public void doChangeNoteFile(NoteFile noteFile, boolean historySave)
	{
		mode.doChangeNoteFile(currNoteFile, noteFile, historySave);
		this.currNoteFile = noteFile;
		timelineView.setDirtyNotes(true);
	}
	
	public void doFinish(){
		if(mode != null){
			
			if(mode.isSaveDirty()){
				
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick (DialogInterface dialog, int which) {
						switch(which){
							case DialogInterface.BUTTON_POSITIVE:
								{
									setSaveAfterExit(true);
									
									NoteModifyPopup npopup = new NoteModifyPopup(NoteMakerActivity.this);
									onPopupShow(npopup);
									npopup.show( getNoteSet() );
								}
								break;
							case DialogInterface.BUTTON_NEUTRAL:
								finish();
								break;
						}
					}
				};
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.note_maker_save_dirty);
				builder.setPositiveButton(R.string.note_maker_save_dirty_save, listener);
				builder.setNeutralButton(R.string.note_maker_save_dirty_dont, listener);
				builder.setNegativeButton(R.string.note_maker_save_dirty_cancel, listener);
				builder.show();
				return;
				
			}
			
		}
		
		finish();
	}
	
	public void showTooltip()
	{
		ToolTipPopup popup = new ToolTipPopup(this);
		
		ArrayList<ToolTip> tooltips = new ArrayList<ToolTip>();
		tooltips.add(new ToolTip(menuBtn, getString(R.string.note_maker_menu_menu), 0.5f, 1f, 0.05f, 0.1f));
		tooltips.add(new ToolTip(undoBtn, getString(R.string.note_maker_menu_undo), 0.5f, 1f, 0.15f, 0.1f));
		tooltips.add(new ToolTip(redoBtn, getString(R.string.note_maker_menu_redo), 0.5f, 1f, 0.25f, 0.1f));
		tooltips.add(new ToolTip(tempoBtn, getString(R.string.note_maker_menu_tempo), 0.5f, 1f, 0.35f, 0.1f));
		tooltips.add(new ToolTip(analyzeBtn, getString(R.string.note_maker_menu_spectrum), 0.5f, 1f, 0.60f, 0.1f));

		tooltips.add(new ToolTip(recordBtn, getString(R.string.note_maker_menu_switch_record), 0.5f, 1f, 0.76f, 0.1f));
		tooltips.add(new ToolTip(playBtn, getString(R.string.note_maker_menu_switch_preview), 0.5f, 1f, 0.88f, 0.1f));
		popup.add(tooltips);

		popup.show();
	}

//	menuBtn = (ImageButton) findViewById(R.id.menu);
//	undoBtn = (ImageButton) findViewById(R.id.undo);
//	redoBtn = (ImageButton) findViewById(R.id.redo);
//	tempoBtn = (Button) findViewById(R.id.tempo);
//	analyzeBtn = (ImageButton) findViewById(R.id.analyze);
//	
//	recordBtn = (ImageButton) findViewById(R.id.record);
//	playBtn = (ImageButton) findViewById(R.id.play);
	
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
				NoteMakerActivity.this.mPopupPopup = null;

			}
		} );
	}
	public boolean onPopupClose(){
		return mPopupPopup!=null && mPopupPopup.dispose();
	}
	
	/**************************************************************************
	 * 
	 * @GETSET
	 * 
	 *************************************************************************/

	public MakerTimelineView getTimelineView () {
		return timelineView;
	}

	public GamePad getPadView () {
		return padView;
	}

	public MakerGameMode getMode () {
		return mode;
	}

	public FFTVisualize getVisualize () {
		return visualize;
	}

	public NoteSet getNoteSet () {
		return noteSet;
	}

	public void setNoteSet (NoteSet noteSet) {
		this.noteSet = noteSet;
	}

	public SongInfo getSong () {
		return song;
	}

	public NoteFile getCurrNoteFile () {
		return currNoteFile;
	}

	public boolean isNewNoteMake () {
		return newNoteMake;
	}

	public void setNewNoteMake (boolean newNoteMake) {
		this.newNoteMake = newNoteMake;
	}

	public boolean isSaveAfterExit () {
		return saveAfterExit;
	}

	public void setSaveAfterExit (boolean saveAfterExit) {
		this.saveAfterExit = saveAfterExit;
	}

	public float getVolumeTouch () {
		return volumeTouch;
	}
	
}
