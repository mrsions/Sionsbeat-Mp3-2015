package sions.android.sionsbeat.maker;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.Toast;
import sions.android.sionsbeat.NoteMakerActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.game.GameData;
import sions.android.sionsbeat.game.GameModeInterface;
import sions.android.sionsbeat.game.GameOption;
import sions.android.sionsbeat.game.view.GamePad;
import sions.android.sionsbeat.template.GameNote;
import sions.android.sionsbeat.template.Note;
import sions.android.sionsbeat.template.NoteSet.NoteFile;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.FileUtils;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.NumericTools;
import sions.android.sionsbeat.zresource.ZAnimation;
import sions.android.sionsbeat.zresource.ZResource;
import sions.utils.PTC;

public class MakerGameMode extends Thread implements GameModeInterface, OnCompletionListener, OnPreparedListener, OnErrorListener {

	public static int getTempoTime(int tempo, long time)
	{
		float tempoTime = 1000f/tempo;
		return (int)( Math.round(time/tempoTime) * tempoTime );
	}
	
	private NoteMakerActivity context;
	private MakerGLPadView padView;
	private MakerTimelineView timeline;
	
	private SongInfo song;
	private GameOption option;
	private GameNote[] gameNotes;
	
	private ZResource resource;
	private long sysTime;
	
	private int windowWidth, windowHeight;
	
	private int noteWidth = 4;
	
	private ZAnimation animIntro;
	private MediaPlayer player;
	
	private ArrayList<SingleNote> notes = new ArrayList<SingleNote>();
	
	private ArrayList<Object> historys = new ArrayList<Object>();
	private int historyIndex = 0;
	
	private int StartGab, EndGab;
	
	private int startTime, endTime;
	
	private int tempo = 4;

	private long targetSystemTime_startTime;
	private long targetSystemTime;
	
	private boolean dirty;
	private int reloadMusic;

	private boolean saveDirty;

	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/
	
	public MakerGameMode(NoteMakerActivity context){
		this.context = context;

		DisplayMetrics dm = this.context.getResources().getDisplayMetrics();
		windowWidth = dm.widthPixels;
		windowHeight = dm.heightPixels;
		
		this.option = new GameOption();
		this.StartGab = option.TIMING_GREAT + ((option.TIMING_PERFECT - option.TIMING_GREAT)/2);
		this.EndGab = 1300-EndGab;
	}
	
	public void prepare(){
		
		this.padView.setGameMode(this);
		this.timeline.setGameMode(this);

		this.gameNotes = new GameNote[16];
		for (int i = 0; i < this.gameNotes.length; i++) {
			this.gameNotes[i] = new GameNote();
		}
		
		this.startTime = 0;
		this.endTime = song.getDuration();

		player = FileUtils.getMediaPlayer(context, song.getPath());
		if(player == null)
		{
			context.doErrorFinish(new NullPointerException(context.getString(R.string.select_notfoundmusic)));
			return;
		}
		player.setOnErrorListener(this);
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		
	}
	
	/**************************************************************************
	 * 
	 * @MUSIC_EVENT
	 * 
	 *************************************************************************/

	@Override
        public void onPrepared (MediaPlayer mp) {
		this.player = mp;
	}

	@Override
        public void onCompletion (MediaPlayer mp) {
		switch(runStatus){
			case STATUS_RECORD:
				storeHistory();
				break;
			case STATUS_PREVIEW:
				break;
		}
		runStatus = STATUS_MAKE;
		context.doPlayRelease();
        }

	@Override
        public boolean onError (MediaPlayer mp, int what, int extra) {
		context.doErrorFinish(new Exception(what+"/"+extra));
	        return false;
        }

	/**************************************************************************
	 * 
	 * @LIFECYCLE
	 * 
	 *************************************************************************/

	public static final int STATUS_PAUSE = 0x00;
	public static final int STATUS_MAKE = 0x01;
	public static final int STATUS_RECORD = 0x02;
	public static final int STATUS_PREVIEW = 0x03;
	
	private boolean run = true;
	private int runStatus = 0;

	private boolean play = false;
	
	@Override
        public void run () {

		try{
			runSetupResource();
			
			byte playIdx = 0;
			
			while(run)
			{
				try{
					switch(runStatus){
						case STATUS_MAKE:
							{
								if(runStatus == MakerGameMode.STATUS_MAKE && targetSystemTime_startTime!=0 && targetSystemTime_startTime+100 < System.currentTimeMillis() && targetSystemTime != sysTime)
								{
									long diff = targetSystemTime-sysTime;
									
									if(Math.abs(diff) < 3){
										sysTime = targetSystemTime;
										dirty = true;
										soundStop();
									}else{
										sysTime = sysTime+(int)(diff*0.5f);
										
										if(!play){
											soundPlay();
										}
									}
								}
								
								if(play)
								{
									if(++playIdx == 5){
										playIdx = 0;
										player.seekTo((int)getSysTime());
									}
								}
								
								if(dirty){
									dirty = false;
									refreshNotes();
								}
								Thread.sleep(30);
							}
							break;
						case STATUS_RECORD:
						case STATUS_PREVIEW:
							{
								int pos = player.getCurrentPosition();
								
								setTargetSystemTime(pos);
								setSysTime(pos);
								
								refreshNotes();
								Thread.sleep(30);
							}
							break;
						default:
							Thread.sleep(200);
							break;
					}
					
				}catch(Throwable e){
					e.printStackTrace();
					Thread.sleep(200);
				}
			}
		}catch(Throwable e){
			ErrorController.error(10, e);
			context.doErrorFinish(e);
		}
	}
	
	public void onPause () {
		runStatus = STATUS_PAUSE;
		try{
			player.pause();
		}catch(Throwable e){}
        }
	
	public void onResume() {
		runStatus = STATUS_MAKE;
	}

	public void finish()
	{
		runStatus = STATUS_PAUSE;
		run = false;
		try{
			player.release();
		}catch(Throwable e){}
	}

	
	/**************************************************************************
	 * 
	 * @SETUP
	 * 
	 *************************************************************************/

	private void runSetupResource() throws Exception{
		if(resource == null)
		{
			ZResource resource = new ZResource(GameOptions.get(context).getMarkerFile());
			
			int blockWidth = (int) ( windowWidth/7.2f );
			
			resource.setTargetWidth(blockWidth);
			resource.setTargetHeight(blockWidth);
			
			animIntro = resource.getAnimation("intro",blockWidth,blockWidth);
			
			this.resource = resource;
		}
		
		for (int i = 0; i < this.gameNotes.length; i++) {
			this.gameNotes[i].setAnim(animIntro.clone());
			this.gameNotes[i].getAnim().setBitmapNull(true); 
			this.gameNotes[i].getAnim().start(-100000);
		}
	}
	
	public void doClear(){
		notes.clear();
		historyIndex = 0;
		historys.clear();
		tempHistory = null;
		
		setTargetSystemTime(0);
		dirty = true;
		timeline.setDirty(true);
		timeline.setDirtyNotes(true);
	}
	
	/**************************************************************************
	 * 
	 * @ACTIVITY_EVENT
	 * 
	 *************************************************************************/
	
	public boolean previewToggle()
	{
		if(runStatus == STATUS_RECORD){
			recordToggle();
			context.doPlayRelease();
			return false;
		}
		else if(runStatus == STATUS_PREVIEW )
		{
			runStatus = STATUS_MAKE;
			try{
				player.pause();
			}catch(Throwable e){}
			return false;
		}
		else{
			runStatus = STATUS_PREVIEW;
			try{
				player.seekTo((int)getSysTime());
				player.start();
			}catch(Throwable e){}
			
			return true;
		}
	}
	public boolean recordToggle()
	{
		if(runStatus == STATUS_PREVIEW){
			previewToggle();
			context.doPlayRelease();
			return false;
		}
		else if(runStatus == STATUS_RECORD )
		{
			runStatus = STATUS_MAKE;
			try{
				player.pause();
			}catch(Throwable e){}
			storeHistory();
			return false;
		}
		else{
			runStatus = STATUS_RECORD;
			try{
				player.seekTo((int)getSysTime());
				player.start();
			}catch(Throwable e){}
			
			return true;
		}
	}

	public void soundPlay(){
		if(runStatus == STATUS_MAKE)
		{
			try{
				play = true;
				player.seekTo((int)getSysTime());
				player.start();
			}catch(Throwable e){}
		}
	}
	
	public void soundStop(){
		if(runStatus == STATUS_MAKE)
		{
			try{
				play = false;
				player.pause();
			}catch(Throwable e){}
		}
	}


	/**************************************************************************
	 * 
	 * @ACTION
	 * 
	 *************************************************************************/

	private HashMap<Integer, TouchInfo> touchInfos = new HashMap<Integer, MakerGameMode.TouchInfo>();
	
	@Override
        public boolean onAction (TouchEvent event) {
		
		if(runStatus == STATUS_PREVIEW) return false;

		TouchInfo info = touchInfos.get(event.getTouchId());
		if(info == null){
			touchInfos.put(event.getTouchId(), info = new TouchInfo());
		}

		if(!event.isPress()){
			info.press = false;
			if(runStatus == STATUS_MAKE){
				storeHistory();
			}
			return false;
		}
		
		boolean first = !info.press;
		info.press = true;
		int pos = event.getX();
		
		if(pos<0 || pos>15) return false;

		long targetTime = getTargetSystemTime();
		SingleNote note = getCloserNote(pos, targetTime, 1300);
		
		if(note == null && (first || info.checked)){
			
			notes.add(note = new SingleNote((int) targetTime, 1<<pos));
			timeline.setDirtyNotes(true);
			dirty = true;
			
			info.checked = true;
			addHistory(note.getTiming(), note.getSingleButton());
			
		}else if(runStatus == STATUS_MAKE && note != null && Math.abs(sysTime-note.getTiming()) < 300 && (first || !info.checked)){
			
			notes.remove(note);
			timeline.setDirtyNotes(true);
			dirty = true;
			
			info.checked = false;

			addHistory(note.getTiming(), note.getSingleButton()-100); // 구별을 위해서
			
		}
		
	        return false;
        }
	
	class TouchInfo {
		private boolean press;
		private boolean checked;
	}
	
	private void refreshNotes(){

		do{
			try{
				Collections.sort(notes, new Comparator<SingleNote>() {
					@Override
		                        public int compare (SingleNote lhs, SingleNote rhs) {
			                        return NumericTools.Integer.compare(lhs.getTiming(), rhs.getTiming());
		                        }
				});
				break;
			}catch(Throwable e){}
		}while(true);
		
		for(int i=0; i<16; i++){
			SingleNote note = getCloserNote(i, sysTime, 1500);
			GameNote gnote = gameNotes[i];
			if(note != null)
			{
				gnote.setStartTime(note.getTiming()-StartGab);	
				gnote.getAnim().start(gnote.getStartTime());
			}
			else
			{
				gnote.setStartTime(-5000);
				gnote.getAnim().start(-100000);
			}
		}

		timeline.setDirty(true);
		padView.setDirty(true);
	}

	public SingleNote getCloserNote(int button){
		return getCloserNote(button, sysTime, 1500);
	}
	public SingleNote getCloserNote(int button, long time, int closer){

		SingleNote note = null;
		for(int i=0; i<notes.size(); i++){
			SingleNote nt = notes.get(i);
			if(nt != null && nt.isButton(button) && Math.abs(time-nt.getTiming()) < closer){
				note = nt;
				closer = (int) Math.abs(time-nt.getTiming());
			}
		}
		
		return note;
	}

	/**************************************************************************
	 * 
	 * @HISTORY
	 * 
	 *************************************************************************/
	
	private SparseIntArray tempHistory = null;
	private void storeHistory()
	{
		if(tempHistory == null || tempHistory.size() == 0) return;

		//-- undo 이후를 제거
		for(; historyIndex< historys.size();){
			historys.remove(historyIndex);
		}
		
		historys.add(tempHistory);
		historyIndex ++;
		
		tempHistory = null;
		
	}
	private void addHistory(int time, int button)
	{
		if(tempHistory == null)
		{
			tempHistory = new SparseIntArray(1);
		}
		tempHistory.put((time*16+button), button);
		
		saveDirty = true;
	}
	
	public void undo()
	{
		if(!isUndo()) return;
		
		int time = 0;

		Object undoTarget = historys.get(--historyIndex);
		if(undoTarget instanceof SparseIntArray){
			
			SparseIntArray beforeHistory = (SparseIntArray)undoTarget;
			for(int i=0; i<beforeHistory.size(); i++)
			{
				time = beforeHistory.keyAt(i)/16;
				int button = beforeHistory.valueAt(i);
				
				// 이전에 제거한 것. 추가를 해야 함
				if(button < 0)
				{
					notes.add(new SingleNote(time, 1<<(button+100)));
				}
				// 이전에 추가한 것. 제거를 해야 함
				else
				{
					SingleNote note = getCloserNote(button, time, 100);
					notes.remove(note);
				}
			}
			
			setTargetSystemTime(time);
			
			timeline.setDirtyNotes(true);
			dirty = true;
			
		}else if(undoTarget instanceof NoteFile[]){
			
			context.doChangeNoteFile(((NoteFile[])undoTarget)[0], false);
			
		}
	}
	
	public void redo()
	{
		if(!isRedo()) return;
		
		int time = 0;

		Object redoTarget = historys.get(historyIndex++);
		if(redoTarget instanceof SparseIntArray){
			
			SparseIntArray nextHistory = (SparseIntArray)redoTarget;
			for(int i=0; i<nextHistory.size(); i++)
			{
				time = nextHistory.keyAt(i)/16;
				int button = nextHistory.valueAt(i);
				
				// 다음에 추가될 것
				if(button < 0)
				{
					SingleNote note = getCloserNote(button+100, time, 100);
					notes.remove(note);
				}
				// 다음에 제거 될 것
				else
				{
					notes.add(new SingleNote(time, 1<<button));
				}
			}
			
			setTargetSystemTime(time);
			
			timeline.setDirtyNotes(true);
			dirty = true;
			
		}else{

			context.doChangeNoteFile(((NoteFile[])redoTarget)[1], false);
		}
	}
	
	public boolean isUndo()
	{
		return historyIndex > 0;
	}
	public boolean isRedo()
	{
		return historyIndex < historys.size();
	}


	/**************************************************************************
	 * 
	 * @ACTION
	 * 
	 *************************************************************************/

	public void doChangeNoteFile(NoteFile before, NoteFile after, boolean save)
	{
		storeHistory();
		if(save){
			historys.add(new NoteFile[]{before, after});
			historyIndex++;
		}

		doInputNoteFile(before);
		setupNoteFile(after);

		dirty = true;
	}

	public void doInputNoteFile(NoteFile note)
	{
		note.setNotes((Note[]) this.notes.toArray(new Note[this.notes.size()]));
	}

	public void setupNoteFile(NoteFile note)
	{
		ArrayList<SingleNote> tempNotes = new ArrayList<SingleNote>();
		for(Note sn:note.getNotes()){
			tempNotes.add(new SingleNote(sn.getTiming(), sn.getButton()));
		}

		this.notes = tempNotes;
	}
	
	/**************************************************************************
	 * 
	 * @TIMER
	 * 
	 *************************************************************************/


	public void setSysTime (long sysTime) {
		this.sysTime = sysTime;
		dirty = true;
	}
	
	
	/**************************************************************************
	 * 
	 * @GETSET
	 * 
	 *************************************************************************/

	@Override
        public ZResource getResource () {
	        return resource;
        }

	@Override
        public long getSysTime () {
	        return sysTime;
        }

	@Override
        public int getNoteWidth () {
	        return noteWidth;
        }

	@Override
        public GameNote[] getGameNotes () {
	        return gameNotes;
        }

	@Override
        public GameOption getOption () {
	        return option;
        }

	@Override
        public GameData getGameData () {
	        return null;
        }

	public MakerGLPadView getPadView () {
		return padView;
	}

	public void setPadView (MakerGLPadView padView) {
		this.padView = padView;
	}

	public MakerTimelineView getTimeline () {
		return timeline;
	}

	public void setTimeline (MakerTimelineView timeline) {
		this.timeline = timeline;
	}

	public SongInfo getSong () {
		return song;
	}

	public void setSong (SongInfo song) {
		this.song = song;
	}

	public int getStartTime () {
		return startTime;
	}
	public void setStartTime (int startTime) {
		this.startTime = startTime;
	}
	public int getEndTime () {
		return endTime;
	}
	public void setEndTime (int endTime) {
		this.endTime = endTime;
	}
	public int getDuration () {
		return endTime-startTime;
	}

	public ArrayList<SingleNote> getNotes () {
		return notes;
	}
	public void setNotes (ArrayList<SingleNote> notes) {
		this.notes = notes;
	}

	public int getTempo () {
		return tempo;
	}

	public void setTempo (int tempo) {
		this.tempo = tempo;
	}

	public int getRunStatus () {
		return runStatus;
	}

	public void setRunStatus (int runStatus) {
		this.runStatus = runStatus;
	}
	public void setTargetSystemTime(long time)
	{
		targetSystemTime = getTempoTime(tempo, time);
		dirty = true;
	}
	public long getTargetSystemTime () {
		return targetSystemTime;
	}

	public long getTargetSystemTime_startTime () {
		return targetSystemTime_startTime;
	}

	public void setTargetSystemTime_startTime (long targetSystemTime_startTime) {
		this.targetSystemTime_startTime = targetSystemTime_startTime;
	}

	public boolean isSaveDirty () {
		return saveDirty;
	}

	public void setSaveDirty (boolean saveDirty) {
		this.saveDirty = saveDirty;
	}
}
