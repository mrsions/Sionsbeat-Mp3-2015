
package sions.android.sionsbeat.window;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;

import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;

import sions.android.SQ;
import sions.android.sionsbeat.Base;
import sions.android.sionsbeat.GameActivity;
import sions.android.sionsbeat.MusicSelectActivity;
import sions.android.sionsbeat.NoteMakerActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.SplashActivity;
import sions.android.sionsbeat.fragment.AdsBanner;
import sions.android.sionsbeat.game.GameData;
import sions.android.sionsbeat.game.exception.NotFoundMatchVersion;
import sions.android.sionsbeat.game.view.GameGraphView;
import sions.android.sionsbeat.game.view.LevelSelectorView;
import sions.android.sionsbeat.game.view.LevelSelectorView.OnChangeListener;
import sions.android.sionsbeat.template.NoteSet;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.template.NoteSet.NoteFile;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.FileUtils;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.JsonType;
import sions.android.sionsbeat.utils.NumericTools;
import sions.android.sionsbeat.utils.SoundFXPlayer;
import sions.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MusicSelectPopup extends AbsPopup implements OnChangeListener, OnCheckedChangeListener
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
	
	public MusicSelectPopup ( Activity context )
	{
		super( context );
	}
	
	/************************************************************************************************************
	 * 
	 * 				@MEMBER_FIELDS
	 * 
	 ************************************************************************************************************/
	
	private View infoDetails;
	private TextView title;
	private TextView artist;
	private TextView duration;
	private ImageView thumb;
	private ImageView isCCL;

	private LevelSelectorView levelSelector;
	private RadioGroup levelSelector2;
	private GameGraphView gameGraph;
	
	private Button startBtn;
	private Button notes_elect;
	
	private View view;
	private SongInfo song;
	
	private NoteSet selectNoteSet;
	private ArrayList<NoteSet> noteSets;
	
	private MediaPlayer player;
	
	private AbsPopup anotherPopup;
	
	/************************************************************************************************************
	 * 
	 * 				@OVERRIDE_METHODS
	 * 
	 ************************************************************************************************************/

	public boolean show ( SongInfo song )
	{
		/**@TRACKING**/ErrorController.tracking(getContext(), this, "show", "", 0, true);
		
		this.song = song;
		if(song == null){
			Toast.makeText(getContext(), R.string.select_notfoundmusic, Toast.LENGTH_SHORT).show(); // 음악을 찾을 수 없습니다
			return false;
		}
		
		this.view = LayoutInflater.from(getContext()).inflate( R.layout.window_start, null );
		
		setupDefaults();
		setupEvent();
		if(!setupPref()){
			return false;
		}
		return super.show( this.view );
	}
	
	@Override
	public void onPause () {
		try{
			player.pause();
		}catch(Throwable e){}
	}
	
	@Override
	public void onResum () {
		SoundFXPlayer.get(getContext()).bgStop();
		try{
			player.start();
		}catch(Throwable e){}
	}
	
	@Override
	public boolean dispose () {
		/**@TRACKING**/ErrorController.tracking(getContext(), this, "dispose", "", 0, true);
		
		if(anotherPopup != null){
			anotherPopup.dispose();
			anotherPopup = null;
			return true;
		}
		
		try{
			player.stop();
			player.release();
		}catch(Throwable e){}
		
		SoundFXPlayer.get(getContext()).bgPlay();
		
	        return super.dispose();
	}
	
	@Override
	public void onActivityResult (int requestCode, int resultCode, Intent intent) {
		
		if(intent != null && resultCode == Activity.RESULT_OK){
			if(intent.getExtras().containsKey(NoteMakerActivity.INTENT_NOTESET)){
				String text = intent.getExtras().getString(NoteMakerActivity.INTENT_NOTESET);
				try{
					NoteSet noteSet = new NoteSet(new JSONObject(text));
					switch(requestCode)
					{
						case MusicSelectActivity.REQUEST_NOTEMAKE:
							{
								noteSets.add(noteSet);
							}
							break;
						case MusicSelectActivity.REQUEST_NOTEMODIFY:
							{
								String path = noteSet.getRootFile().getAbsolutePath();
								for(int i=0; i<noteSets.size(); i++)
								{
									NoteSet ns = noteSets.get(i);
									if(ns != null && ns.getType() == NoteSet.TYPE_CUSTOM && ns.getRootFile().getPath().equalsIgnoreCase(path)){
										noteSets.set(i, noteSet);
										break;
									}
								}
							}
							break;
					}
					showMusicNotePopup();
					
				}catch(Throwable e){
					ErrorController.error(10, e);
				}
			}
		}
		
		if(anotherPopup != null)
		{
			anotherPopup.onActivityResult(requestCode, resultCode, intent);
		}
	}
	
	/************************************************************************************************************
	 * 
	 * 				@ACTIVE_METHODS
	 * 
	 ************************************************************************************************************/
	
	private void setupDefaults(){

		title = (TextView) view.findViewById( R.id.title );
		title.setText( song.getTitle() );
		
		artist = (TextView) view.findViewById( R.id.artist );
		artist.setText( song.getArtist() );
		
		duration = (TextView) view.findViewById( R.id.duration );
		duration.setText( song.getDurationString() );
		
		thumb = (ImageView) view.findViewById( R.id.thumbnail );
		song.parseAlbumView(getContext(), thumb, R.drawable.list_empty);

		levelSelector = (LevelSelectorView) view.findViewById( R.id.game_level );
		levelSelector2 = (RadioGroup) view.findViewById( R.id.game_level_2 );
		
		gameGraph = (GameGraphView) view.findViewById( R.id.graphView );
		startBtn = (Button) view.findViewById( R.id.game_start );
		notes_elect = (Button) view.findViewById( R.id.notes_select );

		infoDetails = (View) view.findViewById(R.id.details);
		isCCL = (ImageView) view.findViewById(R.id.isCCL);
		
	}
	
	private void setupEvent(){

		levelSelector.setOnChangeListener( this );
		levelSelector2.setOnCheckedChangeListener( this );
		startBtn.setOnClickListener(this);
		notes_elect.setOnClickListener(this);
		
		thumb.setOnClickListener(this);
		infoDetails.setOnClickListener(this);
		
	}
	
	private boolean setupPref()
	{
		isCCL.setVisibility(song.isCCL() ? View.VISIBLE : View.GONE);
		
		int analyzeLevel;
		String target;

		noteSets = new ArrayList<NoteSet>();	
		noteSets.add(new NoteSet(song, NoteSet.TYPE_MAKE, 0)); //만들기를 삽입
		
		NoteSet analyze=null, comp=null, select=null;

		//-- 분석된 대상이라면
		if(song.isCompatibility()){
			noteSets.add(comp = getComparatorNoteSet());
		}else if((analyzeLevel = SQ.getJSONInteger(0, JsonType.INTERPRET, song.getIdentity())) > 0){
			noteSets.add(analyze = getAnalyzeNoteSet(analyzeLevel));
		}
		
		
		File roots = new File(GameOptions.getRootFile(), "customs/"+song.getIdentity());
		if(roots.exists() && roots.isDirectory()){
			for(File folder:roots.listFiles()){
				if(folder.isDirectory()){
					NoteSet nts = NoteSet.getSongNoteSet(song, folder);
					if(nts != null)
					{
						noteSets.add(nts);	
					}
				}
			}
		}
		
		
		//-- 노트를 선택한다.
		target = SQ.getJSONString(null, JsonType.MUSIC_NOTE_SELECT, song.getIdentity());
		if(noteSets.size() == 1)
		{
			Toast.makeText(getContext(), R.string.select_notinterpret, Toast.LENGTH_LONG).show(); //음악이 아직 분석되지 않았습니다. 분석이 완료된 음악들을 플레이 하시다보면 완료될 것입니다.
			return false;
		}
		else if(target == null)
		{
			select = noteSets.get(1);
		}
		else if(analyze!=null && target.equals(String.valueOf(NoteSet.TYPE_ANALYZE)))
		{
			select = analyze;
		}
		else if(comp!=null && target.equals(String.valueOf(NoteSet.TYPE_CUSTOM)))
		{
			select = comp;
		}
		else
		{
			for(NoteSet ns : noteSets){
				if(ns != null && ns.getNoteName()!=null && ns.getNoteName().equals(target)){
					select = ns;
					break;
				}
			}
			
			if(select == null){
				select = noteSets.get(1);
			}
		}
		
		
		//-- 음악을 찾는다.
		if(select == null){
			Log.d("test","no music??");
			Toast.makeText(getContext(), R.string.select_notfoundmusic, Toast.LENGTH_SHORT).show(); // 음악을 찾을 수 없습니다
			return false;
		}
		
		
		//-- 음악 선택
		setupSelectNoteSet(select);

		SoundFXPlayer.get(getContext()).bgStop();
		
		float volume = GameOptions.get(getContext()).getSettingInt(GameOptions.OPTION_SOUND_SELECT_PREVIEW)*0.01f;
		if(volume > 0)
		{
			player = FileUtils.getMediaPlayer(getContext(), song.getPath());
			if(player == null)
			{
				Log.d("test","no music access??");
				Toast.makeText(getContext(), R.string.select_notfoundmusic, Toast.LENGTH_SHORT).show();
				return false;
			}
			try{
				player.setVolume(volume, volume);
				player.seekTo((int) ( player.getDuration()*0.2 ));
				player.start();
			}catch(Exception e){
				ErrorController.error(10, e);
			}
		}
		return true;
	}


	/************************************************************************************************************
	 * 
	 * 				@EVENT
	 * 
	 ************************************************************************************************************/
	@Override public void onStartTouch ( LevelSelectorView view ){}
	@Override public void onChange ( LevelSelectorView view, int progress ){}

	@Override
	public void onStopTouch ( LevelSelectorView view )
	{
		GameOptions.get(getContext()).put(JsonType.SELECTED_LEVEL, view.getProgress());
		
		int selectLevel = Math.min(levelSelector.getMaxProgress(), Math.max(levelSelector.getMinProgress(), levelSelector.getProgress()))-1;
		
		NoteFile nf = selectNoteSet.getNotes().get(selectLevel);
		if(nf == null) return;
		
		selectNoteSet.setSelect(nf);
		
		GameData data = null;
		try{
			data = new GameData( nf.getFile(), song );
		}catch ( Exception e ){
			ErrorController.error(10, e);
		}
		gameGraph.setupBlocks( data );
		gameGraph.invalidate();
		
	}

	@Override
	public void onCheckedChanged (RadioGroup group, int checkedId) {
		RadioButton view = (RadioButton) group.findViewById(checkedId);
		NoteFile nf = (NoteFile) view.getTag();
		selectNoteSet.setSelect(nf);

		GameOptions.get(getContext()).put(JsonType.SELECTED_LEVEL_COM, nf.getType());

		GameData data = null;
		try{
			data = new GameData( nf.getFile(), song );
			gameGraph.setupBlocks( data );
			gameGraph.invalidate();
		}catch ( Exception e ){
			ErrorController.error(10, e);
		}
	}
	
	@Override
	public void onClick ( View v )
	{
		switch(v.getId()){
			case R.id.game_start:
				{
					if(gameStart()) return;
				}
				break;
			case R.id.notes_select:
				{
					showMusicNotePopup();
				}
				break;
			case R.id.thumbnail:
			case R.id.details:
				{
					MusicDetailPopup popup = new MusicDetailPopup(getContext());
					popup.show(this, song);
					this.anotherPopup = popup;
				}
				break;
		}
		super.onClick(v);
	}
	
	public void showMusicNotePopup(){
		MusicNotesPopup popup = new MusicNotesPopup(getContext());
		popup.show(this, noteSets);
		this.anotherPopup = popup;
	}
	
	public void deleteNoteSet(NoteSet noteSet)
	{
		Iterator<NoteSet> it = noteSets.iterator();
		while(it.hasNext()){
			if(it.next() == noteSet){
				it.remove();
			}
		}
	}
	
	public boolean gameStart()
	{
		if(selectNoteSet == null) return false;
		if(selectNoteSet.getSelect() != null)
		{
			if(Base.FREE)
			{
				boolean success = Base.ShowImmersiveAds(getContext(), new Runnable() {
					@Override
					public void run() {
						PlayDirect();
					}
				});
				
				if(success) return true;
			}
			PlayDirect();				
		}
		return true;
	}
	public void PlayDirect()
	{
		File file = selectNoteSet.getSelect().getFile();
		
		Intent intent = new Intent(getContext(), GameActivity.class);
		intent.putExtra( GameActivity.INTENT_TAG, file.getAbsolutePath() );
		intent.putExtra( GameActivity.INTENT_SONG_TAG, song.toJSON().toString() );
		getContext().startActivity( intent );

		ErrorController.tracking(getContext(), "music_select", "game_start", song.getIdentity(), levelSelector.getProgress(), true);
	}
	
	/************************************************************************************************************
	 * 
	 * 				@NoteSets
	 * 
	 ************************************************************************************************************/
	
	private NoteSet getAnalyzeNoteSet(int analyzeLevel){

		NoteSet noteSet = new NoteSet(song, NoteSet.TYPE_ANALYZE, analyzeLevel);
		boolean existFile = false;
		for(int i=0; i<analyzeLevel; i++){
			NoteFile nd = new NoteFile(NoteFile.TYPE_UNKOWN, i+1, GameData.getNoteFile( song, i+1 ));
			
			if(nd.getFile().exists()){
				existFile = true;
				noteSet.getNotes().set(i, nd);
			}
		}
		if(existFile){
			return noteSet;
		}else{
			return null;
		}
	}
	
	private NoteSet getComparatorNoteSet(){

		File folder = new File(song.getPath()).getParentFile();

		boolean[] exists = new boolean[4];
		NoteFile[] noteFiles = new NoteFile[4];
		NoteFile nf;
		
		ArrayList<NoteFile> listFiles = new ArrayList<NoteFile>();
		for(File file: folder.listFiles()){
			String fileName = file.getName().toLowerCase();
			if(fileName.endsWith(".csv") || fileName.endsWith(".jmt"))
			{

				nf = new NoteFile();
				nf.setFile(file);
	
				if(fileName.indexOf("basic")!=-1 || fileName.indexOf("bsc")!=-1 ){
					nf.setType(NoteFile.TYPE_BASIC);
					nf.setLevel(3);
				}else if(fileName.indexOf("advance")!=-1 || fileName.indexOf("adv")!=-1 ){
					nf.setType(NoteFile.TYPE_ADVANCE);
					nf.setLevel(7);
				}else if(fileName.indexOf("extreme")!=-1 || fileName.indexOf("extream")!=-1  || fileName.indexOf("ext")!=-1){
					nf.setType(NoteFile.TYPE_EXTREME);
					nf.setLevel(10);
				}else{
					continue;
				}

				try{
					GameData data = new GameData( nf.getFile(), song );
					nf.setLevel(data.getLevel());
				}catch ( Exception e ){
					continue;
				}
				
				exists[nf.getType()] = true;
				noteFiles[nf.getType()] = nf;
				listFiles.add(nf);
			}
		}
		
		//-- unkown이 있을경우
		if(exists[NoteFile.TYPE_UNKOWN]){
			
			Collections.sort(listFiles, new Comparator<NoteFile>() {
				@Override
                                public int compare (NoteFile lhs, NoteFile rhs) {
	                                return NumericTools.Integer.compare(lhs.getLevel(), rhs.getLevel());
                                }
			});
			
			Arrays.fill(noteFiles, null);
			for(int i=0; i<listFiles.size(); i++){
				if(i >= 3){
					listFiles.remove(i--);
				}else{
					nf = listFiles.get(i);
					nf.setType( i+(3-Math.min(3,listFiles.size())) );
					noteFiles[nf.getType()] = nf;
				}
			}
		}
		
		if(listFiles.size() == 0){
			return null;
		}
		
		NoteSet noteSet = new NoteSet(song, NoteSet.TYPE_CUSTOM, 3);
		for(int i=0; i<listFiles.size(); i++){
			nf = listFiles.get(i);
			noteSet.getNotes().set(nf.getType(), nf);
		}
		
		return noteSet;
	}

	/************************************************************************************************************
	 * 
	 * 				@ACCESS_METHODS
	 * 
	 ************************************************************************************************************/
	
	public boolean setupSelectNoteSet(NoteSet set){

		
		JSONObject target = SQ.getJSONObject(JsonType.MUSIC_NOTE_SELECT);
		if(target == null){
			target = new JSONObject();
		}
		target.put(song.getIdentity(), set.getNoteName());
		SQ.setJSON(JsonType.MUSIC_NOTE_SELECT, target);

		
		if(set.getType() == NoteSet.TYPE_ANALYZE)
		{
			this.selectNoteSet = set;
			
			levelSelector.setVisibility(View.VISIBLE);
			levelSelector2.setVisibility(View.GONE);
			
			int level = GameOptions.get(getContext()).getInt( JsonType.SELECTED_LEVEL, 1 );

			levelSelector.setMaxProgress( Math.min(14, set.getNotes().size()) );
			levelSelector.setProgress( level );
			levelSelector.getOnChangeListener().onStopTouch( levelSelector );
			
		}
		else if(set.getType() == NoteSet.TYPE_CUSTOM)
		{
			levelSelector.setVisibility(View.GONE);
			levelSelector2.setVisibility(View.VISIBLE);

			int baseSelect = -1;
			for(int i=0; i<3; i++){
				RadioButton view = (RadioButton)levelSelector2.getChildAt(i);
				NoteFile nf = set.getNotes().get(i);
				
				if(nf != null){
					if(baseSelect == -1){
						baseSelect = i;
					}
					view.setEnabled(true);
					view.setTextColor(getContext().getResources().getColor(R.color.font_color));
					view.setTag(nf);
					nf.setTag(view);
				}else{
					view.setEnabled(false);
					view.setTag(null);
					view.setTextColor(getContext().getResources().getColor(R.color.font_disabled_color));
				}
			}
			
			if(baseSelect == -1){
				return false;
			}
			this.selectNoteSet = set;
			
			int select = GameOptions.get(getContext()).getInt(JsonType.SELECTED_LEVEL_COM, baseSelect);
			if(set.getNotes().get(select) == null){
				select = baseSelect;
			}

			set.setSelect(set.getNotes().get(select));
			RadioButton rBtn = (RadioButton)set.getSelect().getTag();
			if(!rBtn.isChecked()){
				rBtn.setChecked(true);
			}else{
				onCheckedChanged(levelSelector2, rBtn.getId());
			}
		}
		
		return true;
	}

	public SongInfo getSong () {
		return song;
	}

	public NoteSet getSelectNoteSet () {
		return selectNoteSet;
	}

	public ArrayList<NoteSet> getNoteSets () {
		return noteSets;
	}

	public void setNoteSets (ArrayList<NoteSet> noteSets) {
		this.noteSets = noteSets;
	}

}
