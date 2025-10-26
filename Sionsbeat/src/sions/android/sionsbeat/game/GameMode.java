package sions.android.sionsbeat.game;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

import com.google.android.gms.games.Games;
import com.google.android.gms.wearable.NodeApi.GetConnectedNodesResult;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import sions.android.SQ;
import sions.android.sionsbeat.GameActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.game.view.GameCounterView;
import sions.android.sionsbeat.game.view.GameGraphView;
import sions.android.sionsbeat.game.view.GamePad;
import sions.android.sionsbeat.template.GameNote;
import sions.android.sionsbeat.template.Note;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.FileUtils;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.GoogleGameService;
import sions.android.sionsbeat.utils.JsonType;
import sions.android.sionsbeat.utils.SoundFXPlayer;
import sions.android.sionsbeat.window.ResultPopup;
import sions.android.sionsbeat.zresource.ZAnimation;
import sions.android.sionsbeat.zresource.ZResource;
import sions.android.spritebatcher.SpriteBatcher;
import sions.json.JSONObject;

/**
 * 
 * @author sions
 */
public class GameMode implements Runnable, GameModeInterface, OnErrorListener, OnPreparedListener, OnCompletionListener {

	public static final boolean PERFECTMODE = false;
	public static final float PERFECTMODE_PERCENT = 0.5f;

	public static final int RANK_F = 0;
	public static final int RANK_E = 1;
	public static final int RANK_D = 2;
	public static final int RANK_C = 3;
	public static final int RANK_B = 4;
	public static final int RANK_A = 5;
	public static final int RANK_S = 6;
	public static final int RANK_SS = 7;
	public static final int RANK_EXCELLENT = 8;
	public static final String[] RANK_TEXT = {"F", "E", "D", "C", "B", "A", "S", "SS", "EXC"};

	public static final int STATUS_SETUP = 0;
	public static final int STATUS_READY = 1;
	public static final int STATUS_PLAY = 2;
	public static final int STATUS_RESULT = 3;
	public static final int STATUS_END = 4;

	public static final int DEFAULT_NOTE_WIDTH = 4;
	public static final int DEFAULT_NOTE_HEIGHT = 4;

	public static final int PLAYTYPE_PLAY = 0;
	public static final int PLAYTYPE_PAUSE = 1;
	public static final int PLAYTYPE_RELEASE = 2;
	public static final int PLAYTYPE_FINISH = 2;

	private int noteWidthCount = DEFAULT_NOTE_WIDTH; // 노트 표시 갯수
	private int noteHeightCount = DEFAULT_NOTE_HEIGHT; // 노트 표시 갯수

	private int playType = PLAYTYPE_PLAY;

	private boolean finishing;
	private int currentTime = -1200;

	private Thread thread;

	private GameActivity context;
	private GameNote[] gameNotes; // 진행 중인 노트의 시간을 저장한다.

	private GameData gameData;
	private GameData[] gameDatas;
	private GameData originalData;

	private GameOption option;
	private GameScore score;

	private GameFever fever;
	private GamePad padView;
	private GameGraphView graphView;
	private GameCounterView counterView;
	private ZResource resource;

	private MediaPlayer player;

	private ZAnimation animFailed;
	private ZAnimation animCombo;
	private ZAnimation animGood;
	private ZAnimation animGreat;
	private ZAnimation animIntro;
	private ZAnimation animPerfect;

	private Vibrator vibe;

	private long sysTime = 0;
	private long startTime = 0;
	private long pauseTime;

	private int gameStatus;
	private int healthPoint = 10000;

	private int windowWidth;
	private int windowHeight;
	
	private boolean completeSong;

	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/

	public GameMode (GameActivity context)
	{
		this(context, DEFAULT_NOTE_WIDTH, DEFAULT_NOTE_HEIGHT);
	}

	public GameMode (GameActivity context, int noteWidthCount, int noteHeightCount)
	{

		this.context = context;
		this.noteWidthCount = noteWidthCount;
		this.noteHeightCount = noteHeightCount;
		this.fever = new GameFever(context, this);

		DisplayMetrics dm = this.context.getResources().getDisplayMetrics();
		windowWidth = dm.widthPixels;
		windowHeight = dm.heightPixels;

		vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		clear();
	}

	private void setupGameNotes () {

		this.gameNotes = new GameNote[noteWidthCount * noteHeightCount];
		for (int i = 0; i < this.gameNotes.length; i++) {
			this.gameNotes[i] = new GameNote();
		}

	}

	public void prepareOriginal (GameData gameData) {
		this.originalData = gameData;

		Log.d("test", "comp " + gameData.getSong().isCompatibility());
		if (gameData.getSong().isCompatibility()) {
			this.gameDatas = new GameData[]{gameData};
		} else {
			this.gameDatas = gameData.splitGameData();
			Log.d("test", "split " + this.gameDatas.length);
			for (GameData gd : this.gameDatas) {
				Log.d("test", "a " + gd.getStartOffset() + " / " + gd.getEndOffset() + "   // " + gd.getDuration() + "  // " + gd.getNotes().length + "   // " + gd.getLevel() + "   // " + gd.getNoteBits() + "  //  " + gd.getNpm());
			}
		}

		this.gameData = this.gameDatas[0];

		gameData = this.gameData;
		Log.d("test", "gameData " + gameData.getStartOffset() + " / " + gameData.getEndOffset() + "   // " + gameData.getDuration() + "  // " + gameData.getNotes().length + "   // " + gameData.getLevel() + "   // " + gameData.getNoteBits() + "  //  " + gameData.getNpm());
	}

	public boolean prepare () {

		this.padView.setGameMode(this);
		this.graphView.setGameMode(this);

		this.graphView.setupBlocks(this.gameData);

		return true;
	}

	public void clear () {

		// -- thread clear;
		finish();
		while (thread != null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
		}

		thread = new Thread(this, "GameRunThread");

		// -- default Setting
		playType = PLAYTYPE_PLAY;
		finishing = false;
		completeSong = false;
		currentTime = -1200;

		sysTime = 0;
		startTime = 0;
		pauseTime = 0;

		gameStatus = STATUS_SETUP;
		healthPoint = 10000;

		// -- default Board
		this.option = new GameOption();
		this.score = new GameScore();

		// -- default Note
		setupGameNotes();

		// -- clearing
		if (this.padView != null) {
			this.padView.clear();
		}
		if (this.graphView != null) {
			this.graphView.clear();
		}
		if (this.fever != null) {
			this.fever.clear();
		}
	}

	public void onContextStart () {
		if (fever != null) {
			fever.onStart();
		}
	}

	public void onContextStop () {
		if (fever != null) {
			fever.onStop();
		}
	}

	public void start () {
		thread.start();
	}

	public void dispose () {

		if (resource != null) {
			resource.recycle();
		}

	}

	@Override
        public void onCompletion (MediaPlayer mp) {
		completeSong = true;
        }

	@Override
        public void onPrepared (MediaPlayer mp) {
	        this.player = mp;
        }

	@Override
        public boolean onError (MediaPlayer mp, int what, int extra) {
		if(this.player != null)
		{
			completeSong = true;
			return false;
		}
	        return true;
        }
	
	/**************************************************************************
	 * 
	 * @RUNNABLE
	 * 
	 *************************************************************************/

	public void run () {
		try {
			
			setGameStatus(STATUS_SETUP);
			pausing(10); // 잠시 쉬고 들어감

			score.setup(gameData.getDuration());

			// -- load marker
			runSetupResource();

			ErrorController.tracking(context, this, "gameMode", "load", 0);
			
			// -- prepared Music Player
			long prepareStartTime = System.currentTimeMillis();
			player = FileUtils.getMediaPlayer(context, gameData.getSong().getPath());
			if(player == null)
			{
				throw new NullPointerException(context.getString(R.string.select_notfoundmusic));
			}
			player.setOnErrorListener(this);
			player.setOnPreparedListener(this);
			player.setOnCompletionListener(this);
			
			context.adViewGone();
			
			pausing((int) Math.max(10, 1000 - ( System.currentTimeMillis() - prepareStartTime ))); // 잠시 쉬고 들어감

			ErrorController.tracking(context, this, "gameMode", "loadMusic", 0);

			setGameStatus(STATUS_READY);
			runReadyGo();
			ErrorController.tracking(context, this, "gameMode", "readyGo", 0);

			setGameStatus(STATUS_PLAY);
			runGame();
			ErrorController.tracking(context, this, "gameMode", "result", 0);

			setGameStatus(STATUS_RESULT);
			runGameResult();

			setGameStatus(STATUS_END);

		} catch (GameFinishException e) {
			Log.d("test","Finish Game");
			ErrorController.tracking(context, this, "gameMode", "finish game", 0, true);
		} catch (Exception e) {
			ErrorController.error(10, e);
			context.doErrorFinish(e);
		} finally {

			try {
				player.stop();
			} catch (Exception e) {}

			try {
				player.release();
			} catch (Exception e) {}

			thread = null;
		}
	}

	private void runSetupResource () throws Exception {

		if (resource == null) {
			ZResource resource = new ZResource(GameOptions.get(context).getMarkerFile());
			int blockWidth = SpriteBatcher.powerWidth((int) ( windowWidth / 8f ));
			resource.setTargetWidth(blockWidth);
			resource.setTargetHeight(blockWidth);

			animFailed = resource.getAnimation("failed", blockWidth, blockWidth);
			animCombo = resource.getAnimation("good", blockWidth, blockWidth);
			animGood = resource.getAnimation("good", blockWidth, blockWidth);
			animGreat = resource.getAnimation("great", blockWidth, blockWidth);
			animIntro = resource.getAnimation("intro", blockWidth, blockWidth);
			animPerfect = resource.getAnimation("perfect", blockWidth, blockWidth);
			
			this.resource = resource;
		}
	}

	private void runReadyGo () throws Exception {
		// -- Ready Go
		SoundFXPlayer.get(context).play(R.raw.readygo, context.getVolumeVoice());

		context.animReady();
		pausing(1000);

		context.animGo();
		System.gc(); // gc
		pausing(1000);
	}

	private void runGame () throws Exception {

		int noteIdx = 0, noteLength = gameData.getNotes().length, timeIdx = 0;

		int noteCount = noteWidthCount * noteHeightCount;
		int duration = gameData.getEndOffset() - 1000;
		boolean playerStart = false;

		if (gameData.getStartOffset() > 1000) {

			player.seekTo(gameData.getStartOffset() - 1000);
			player.setVolume(0, 0);
			player.start();
			playerStart = true;

		}

		ErrorController.tracking(context, this, "gameMode", "start", 0);
		
		startTime = System.currentTimeMillis();
		sysTime = 0;

		// 시작 시간이 1200ms 이하일 경우에는 무조건 진행된다.
		// 재생시간이 (전체길이-1000ms)이하일 때 까지 게임이 진행된다. -> 재생 완료시간과 Duration이 일치하지 않기 때문에
		while (( playerStart && ( currentTime = player.getCurrentPosition() ) <= duration ) || sysTime < 1200 || completeSong) {

			sysTime = System.currentTimeMillis() - startTime;
			if (sysTime >= 1000) {
				if (!playerStart) {
					playerStart = true;
					player.start();
				}
			} else {
				if (playerStart) {
					float volume = sysTime / 1000f;
					player.setVolume(volume, volume);
				} else {
					currentTime = (int) -( 1000 - sysTime ) + gameData.getStartOffset();
				}
			}

			for (; noteIdx < noteLength; noteIdx++) {
				Note note = gameData.getNotes()[noteIdx];

				// -- 노트를 실행하기에 적법하다면
				if (note.getTiming() - option.ANIMATION_TIMING - context.getGameSync() <= currentTime) {

					// -- 노트키를 작동시킨다.
					for (int key = 0; key < noteCount; key++) {
						if (note.isButton(key)) {

							if (gameNotes[key].getAnim() != null) {
								if (gameNotes[key].getAnim().isPlay(sysTime) && gameNotes[key].getType() == GameNote.TYPE_NOTING) {
									continue;
								} else {
									gameNotes[key].getAnim().dispose();
								}
							}

							gameNotes[key].setNote(note);
							gameNotes[key].setType(GameNote.TYPE_NOTING);
							gameNotes[key].setStartTime(sysTime);
							gameNotes[key].setAnim(animIntro.clone());
							gameNotes[key].getAnim().start(sysTime);
						}
					}

				}
				// -- 노트를 실행하기에 이른 시간일 경우 다음 시간을 기다린다.
				else {
					break;
				}
			}

			if (PERFECTMODE) runGameTouchAll();
			runGameFailedCheck();
			runGameOver();
			fever.run();

			if (timeIdx++ % 50 == 0) {
				context.setTime(currentTime);
			}
			if(timeIdx%500 == 0){
				ErrorController.tracking(context, this, "gameMode", "playing", timeIdx);
			}

			pausing(10);
			// break;
		}

		for (int timer = 0; timer < 150; timer++) {
			currentTime += 10;
			sysTime += 10;

			if (PERFECTMODE) runGameTouchAll();
			runGameFailedCheck();
			runGameOver();
			fever.run();

			pausing(10);
		}

		if (gameData != gameDatas[gameDatas.length - 1] && !completeSong) {
			for (float i = 0.99f; i >= 0&&!completeSong; i -= 0.005) {
				player.setVolume(i, i);

				if (PERFECTMODE) runGameTouchAll();
				runGameFailedCheck();
				runGameOver();

				pausing(10);
			}
		}

		// score.setupEXCELLENT();
		try {
			player.stop();
		} catch (Exception e) {}
	}

	@SuppressWarnings ("unused")
	private void runGameTouchAll () {
		for (int i = 0; i < gameNotes.length; i++) {
			GameNote gn = gameNotes[i];
			if (gn.getType() == GameNote.TYPE_NOTING && sysTime - gn.getStartTime() >= option.TIMING_GREAT) {
				if (PERFECTMODE_PERCENT > Math.random()) {
					onAction(i, false);
				}
			}
		}
	}

	private void runGameFailedCheck () {
		for (int i = 0; i < gameNotes.length; i++) {
			GameNote gn = gameNotes[i];
			if (gn.getType() == GameNote.TYPE_NOTING && sysTime - gn.getStartTime() >= option.TIMING_FAILED + 100) {
				onAction(i, true);
			}
		}
	}

	private void runGameResult () throws Exception {

		SoundFXPlayer.get(context).bgPlay();

		// -- calculate Result

		int userScore = (int) score.getScore();
		int userRealScore = Math.min(900000, (int) ( userScore - score.getFeverScore() ));
		int bonusScore = Math.min(100000, (int) ( ( userRealScore / 900000f ) * GameScore.SCORE_LAST ));
		int totalScore = userScore + bonusScore;
		int totalRealScore = userRealScore + bonusScore;

		int rankType = score.getRank();

		SoundFXPlayer.get(context).play(R.raw.result, context.getVolumeVoice());

		// -- setup Game Result
		final GameResult result = new GameResult();
		result.setData(gameData);
		result.setGameScore(score);
		result.setGameMode(this);

		result.setUserScore(userScore);
		result.setBonusScore(bonusScore);

		result.setRankType(rankType);
		result.setRank(RANK_TEXT[rankType]);

		result.setFullCombo(score.getMaxCombo() == score.getNoteCount());
		result.setNewRecord(isNewRecordAndStore(result.getRankType(), gameData.getLevel(), totalScore));

		submitAchievement(result);

		// -- Show Result
		final ResultPopup popup = new ResultPopup(context);
		final boolean[] temp = new boolean[1];
		context.runOnUiThread(new Runnable() {
			public void run () {
				popup.show(result);
				temp[0] = true;
			}
		});

		while (!temp[0]) {
			pausing(10);
		}

		context.onPopupShow(popup);
		pausing(600);

		// -- FULLCOMBO
		if (result.isFullCombo()) {

			SoundFXPlayer.get(context).play(R.raw.fullcombo, context.getVolumeVoice());
			popup.animFullCombo();
			pausing(1800);

		}

		// -- ADD BONUS
		popup.animBonus();
		pausing(1000);

		// -- bonusAnim
		SoundFXPlayer.get(context).play(R.raw.diring, context.getVolumeGame());
		for (int i = 0; i < 20; i++) {
			float percent = Math.min(1, ( i + 1 ) / 20f);
			int changeScore = (int) ( userScore + ( bonusScore * Math.sin(percent * Math.PI / 2) ) );
			popup.setChangeScore(changeScore);

			pausing(50);
		}
		popup.setChangeScore(totalScore);

		// -- new Record
		if (result.isNewRecord()) {

			SoundFXPlayer.get(context).play(R.raw.newrecord, context.getVolumeVoice());
			popup.animNewRecord();

			context.setupMaxScore(totalScore); // 재시작을 할 수 있기때문에 적용해보기로 한다.

			pausing(1000);

		}

		pausing(500);

		// -- RESULT
		switch ( rankType )
		{
			case RANK_EXCELLENT:
				SoundFXPlayer.get(context).play(R.raw.excellent, context.getVolumeVoice());
				break;

			case RANK_F:
				SoundFXPlayer.get(context).play(R.raw.failed, context.getVolumeVoice());
				break;

			default:
				SoundFXPlayer.get(context).play(R.raw.clear, context.getVolumeVoice());
				break;
		}

		popup.animRank();
		pausing(500);

		popup.animMenuLayout();

	}

	private void submitAchievement (GameResult result) {

		if (result.getRankType() >= RANK_C) {

			GoogleGameService ggs = context.getGoogleGameService();

			if (gameData.getSong().isBasic()) {
				ggs.achivementsIncrement(R.string.achievement_play_a_basic_song, 1);
			} else {
				ggs.achivementsIncrement(R.string.achievement_play_with_your_own_songs, 1);
			}

			if (gameData.getLevel() >= 5) {
				ggs.achivementsIncrement(R.string.achievement_play_a_level_more_than_five, 1);
			}

			if (result.getRankType() >= RANK_B) {
				ggs.achivementsIncrement(R.string.achievement_get_a_b_rank_or_higher, 1);
			}

			int score = (int) ( ( ( gameData.getLevel() * 0.05 ) + 0.5 ) * result.getUserScore() + result.getBonusScore() );
			ggs.leaderboardSubmit(R.string.leaderboard_single_score, score);

		}

	}

	private boolean isNewRecordAndStore (int rank, int level, int score) {

		// -- 기록 저장
		JSONObject musics = SQ.getJSONObject(JsonType.MUSIC);
		if (musics == null) {
			musics = new JSONObject();
		}

		JSONObject record = musics.getJSONObject(gameData.getSong().getIdentity());
		if (record == null) {

			record = new JSONObject();
			musics.put(gameData.getSong().getIdentity(), record);

		} else {

			int befScore = record.getInt("score");

			if (befScore >= score) { return false; }

		}

		record.put("level", level);
		record.put("score", score);
		record.put("rank", rank);
		record.put("block", graphView.getBlockRates());

		SQ.setJSON(JsonType.MUSIC, musics);

		return true;
	}

	private void runGameOver () throws Exception {

		if (healthPoint > 0) return;

		try {
			player.stop();
		} catch (Exception e) {}
		SoundFXPlayer.get(context).bgPlay();

		ErrorController.tracking(context, this, "gameMode", "gameOver", 0, true);
		
		// -- Create Result
		final GameResult result = new GameResult();
		result.setData(gameData);
		result.setGameScore(score);
		result.setGameMode(this);

		result.setUserScore((int) score.getScore());
		result.setBonusScore(0);

		result.setRankType(RANK_F);
		result.setRank(RANK_TEXT[RANK_F]);

		result.setFullCombo(false);
		result.setNewRecord(false);

		// -- Show Result
		final ResultPopup popup = new ResultPopup(context);
		final boolean[] temp = new boolean[1];
		context.runOnUiThread(new Runnable() {
			public void run () {
				popup.show(result);
				temp[0] = true;
			}
		});

		while (!temp[0]) {
			pausing(10);
		}

		context.onPopupShow(popup);
		pausing(600);

		SoundFXPlayer.get(context).play(R.raw.failed, context.getVolumeVoice());
		popup.animRank();
		pausing(500);

		popup.animMenuLayout();

		throw new GameFinishException("");

	}

	/**************************************************************************
	 * 
	 * @LIFECYCLE
	 * 
	 *************************************************************************/

	private void pausing (int sleep) throws InterruptedException, GameFinishException {

		if (sleep != 0) Thread.sleep(sleep);

		do {
			if (finishing) {
				throw new GameFinishException("");
			} else if (playType == PLAYTYPE_PLAY) {
				return;
			} else if (playType == PLAYTYPE_RELEASE) {
				if (player != null) {
					if (gameStatus == STATUS_PLAY) {

						Thread.sleep(1000);
						if (playType == PLAYTYPE_PAUSE) continue;

						SoundFXPlayer.get(context).play(R.raw.touch3, context.getVolumeVoice());
						context.doPauseCounter(3);
						Thread.sleep(1000);
						if (playType == PLAYTYPE_PAUSE) continue;

						SoundFXPlayer.get(context).play(R.raw.touch3, context.getVolumeVoice());
						context.doPauseCounter(2);
						Thread.sleep(1000);
						if (playType == PLAYTYPE_PAUSE) continue;

						SoundFXPlayer.get(context).play(R.raw.touch3, context.getVolumeVoice());
						context.doPauseCounter(1);
						Thread.sleep(1000);
						if (playType == PLAYTYPE_PAUSE) continue;

						context.doPauseCounterEnd();
						try {
							player.start();
						} catch (Exception e) {}

						startTime += System.currentTimeMillis() - pauseTime;

					}
				}
				playType = PLAYTYPE_PLAY;
				return;
			}
			Thread.sleep(100);
		} while (true);
	}

	public void resum () {
		if (playType == PLAYTYPE_PAUSE) {
			playType = PLAYTYPE_RELEASE;
		}
	}

	public void pause () {
		if (playType != PLAYTYPE_PAUSE) {
			playType = PLAYTYPE_PAUSE;
			pauseTime = System.currentTimeMillis();

			if (player != null && gameStatus == STATUS_PLAY) {
				try {
					player.pause();
				} catch (Exception e) {}
			}
		}
	}

	public void finish () {
		finishing = true;
		if (player != null) {
			try {
				player.stop();
			} catch (Exception e) {}
			player = null;
		}
	}

	/**************************************************************************
	 * 
	 * @ACTION
	 * 
	 *************************************************************************/

	public void onActionAll () {
		for (int i = 0; i < gameNotes.length; i++) {
			GameNote gn = gameNotes[i];
			if (gn.getType() == GameNote.TYPE_NOTING) {
				gn.setStartTime(sysTime - ( option.TIMING_GREAT + 1 ));
				onAction(i, false);
			}
		}
	}

	/**
	 * 게임 버튼을 눌렀을 때를 나타낸다.
	 * 
	 * @param touch
	 */
	@Override
	public boolean onAction (TouchEvent touch) {
		return touch.isPress() && onAction(touch.getX(), false);
	}

	public synchronized boolean onAction (int pos, boolean failed) {
		switch ( playType )
		{
			case PLAYTYPE_PLAY:
				break;
			default:
				return false;
		}

		if (pos < 0) return false;
		else if (pos > 15) return false;

		GameNote gnote = gameNotes[pos];

		int addHP = 0;

		// -- 타이밍 입력
		if (failed) {

			gnote.getAnim().dispose();
			gnote.setType(GameNote.TYPE_FAILED);
			gnote.setAnim(null);
			addHP(-1000);

		} else {

			gnote.setTouchTime(sysTime);

			// -- 터치 할 수 있는 대상인지를 판별
			if (!gnote.isTouch()) { return false; }

			long time = fever.onGameTime(option, gnote, sysTime - gnote.getStartTime());

			if (time < option.TIMING_EARLY) {
				failed = true;
				gnote.setAnim(animFailed.clone());
				gnote.setType(GameNote.TYPE_FAILED);
				addHP = -1000;

			} else if (time < option.TIMING_COMBO) {
				gnote.setAnim(animCombo.clone());
				gnote.setType(GameNote.TYPE_COMBO);
				addHP = -100;

			} else if (time < option.TIMING_GOOD) {
				gnote.setAnim(animGood.clone());
				gnote.setType(GameNote.TYPE_GOOD);
				addHP = 0;

			} else if (time < option.TIMING_GREAT) {
				gnote.setAnim(animGreat.clone());
				gnote.setType(GameNote.TYPE_GREAT);
				addHP = 50;

			} else if (time < option.TIMING_PERFECT) {
				gnote.setAnim(animPerfect.clone());
				gnote.setType(GameNote.TYPE_PERFECT);
				addHP = 100;

			} else if (time < option.TIMING_I_GOOD) {
				gnote.setAnim(animGood.clone());
				gnote.setType(GameNote.TYPE_GOOD);
				addHP = 0;

			} else if (time < option.TIMING_I_COMBO) {
				gnote.setAnim(animCombo.clone());
				gnote.setType(GameNote.TYPE_COMBO);
				addHP = -100;

			} else if (time < option.TIMING_FAILED) {
				failed = true;
				gnote.setAnim(animFailed.clone());
				gnote.setType(GameNote.TYPE_FAILED);
				addHP = -1000;

			} else {
				return true;
			}
			gnote.getAnim().start(sysTime);
			gnote.setStartTime(sysTime);

			if (context.isVibrateTouch()) {
				vibe.vibrate(50);
			}

			SoundFXPlayer.get(context).play(R.raw.touch2, context.getVolumeTouch());

		}

		double addTotalScore = 0;
		// -- 점수 적용
		switch ( gnote.getType() )
		{
			case GameNote.TYPE_FAILED:
				fever.addFever(false);
				addTotalScore = score.addScore(fever, gnote.getType(), false);
				break;
			case GameNote.TYPE_COMBO:
			case GameNote.TYPE_GOOD:
			case GameNote.TYPE_GREAT:
			case GameNote.TYPE_PERFECT:
				fever.addFever(true);
				addTotalScore = score.addScore(fever, gnote.getType(), true);
				break;
		}

		addHP(addHP + ( score.getCombo() / 2 ));

		int idx = (int) Math.min(59, ( gnote.getNote().getTiming() - gameData.getStartOffset() ) / (double) gameData.getDuration() * 60);
		switch ( gnote.getType() )
		{
			case GameNote.TYPE_GOOD:
				graphView.addBlockRates(idx, 0.6f);
				break;
			case GameNote.TYPE_GREAT:
				graphView.addBlockRates(idx, 0.8f);
				break;
			case GameNote.TYPE_PERFECT:
				graphView.addBlockRates(idx, 1f);
				break;
		}

		counterView.setCount((int) score.getScore());
		padView.doCombo(score.getCombo(), sysTime);

		if (failed) {
			if (context.isVibrateMiss()) {
				vibe.vibrate(50);
			}

		}
		return true;
	}

	private void addHP (int hp) {
		if (healthPoint != 0) {

			healthPoint += hp;

			if (healthPoint < 0) {
				healthPoint = 0;
			} else if (healthPoint > 10000) {
				healthPoint = 10000;
			}

		}

		context.setHP(healthPoint);
	}

	/**************************************************************************
	 * 
	 * @GETSET
	 * 
	 *************************************************************************/

	public int getCurrentTime () {
		return currentTime;
	}

	public GameData getGameData () {
		return gameData;
	}

	public void setGameData (GameData gameData) {
		this.gameData = gameData;
	}

	public GameNote[] getGameNotes () {
		return gameNotes;
	}

	public void setGameNotes (GameNote[] gameNotes) {
		this.gameNotes = gameNotes;
	}

	public GamePad getPadView () {
		return padView;
	}

	public void setPadView (GamePad padView) {
		this.padView = padView;
	}

	public GameGraphView getGraphView () {
		return graphView;
	}

	public void setGraphView (GameGraphView graphView) {
		this.graphView = graphView;
	}

	public ZResource getResource () {
		return resource;
	}

	public void setResource (ZResource resource) {
		this.resource = resource;
	}

	public int getNoteWidth () {
		return noteWidthCount;
	}

	public void setNoteWidth (int noteWidth) {
		this.noteWidthCount = noteWidth;
	}

	public int getNoteHeight () {
		return noteHeightCount;
	}

	public void setNoteHeight (int noteHeight) {
		this.noteHeightCount = noteHeight;
	}

	public GameOption getOption () {
		return option;
	}

	public void setOption (GameOption option) {
		this.option = option;
	}

	public GameCounterView getCounterView () {
		return counterView;
	}

	public void setCounterView (GameCounterView counterView) {
		this.counterView = counterView;
	}

	public GameFever getFever () {
		return fever;
	}

	public long getSysTime () {
		return sysTime;
	}

	public int getDuration () {
		return gameData.getDuration();
	}

	public int getPlayType () {
		return playType;
	}

	public GameScore getGameScore () {
		return score;
	}

	public int getGameStatus () {
		return gameStatus;
	}

	public void setGameStatus (int gameStatus) {
		this.gameStatus = gameStatus;
	}

	public boolean isFinishing () {
		return finishing;
	}

	public int getNextGameIndex () {
		int indexOf = -1;
		for (int i = 0; i < gameDatas.length; i++) {
			if (gameDatas[i] == gameData) {
				indexOf = i;
			}
		}

		return indexOf + 1;
	}

	public boolean isNextGame () {
		return getNextGameIndex() < gameDatas.length;
	}

	public void putNextGame () {
		this.gameData = gameDatas[getNextGameIndex()];
	}

	public class GameResult {

		private GameData data;
		private GameMode mode;

		private int userScore;
		private int bonusScore;

		private int rankType;
		private String rank;

		private GameScore gameScore;

		private boolean fullCombo;
		private boolean newRecord;

		public GameData getData () {
			return data;
		}

		public void setData (GameData data) {
			this.data = data;
		}

		public int getUserScore () {
			return userScore;
		}

		public void setUserScore (int userScore) {
			this.userScore = userScore;
		}

		public int getBonusScore () {
			return bonusScore;
		}

		public void setBonusScore (int bonusScore) {
			this.bonusScore = bonusScore;
		}

		public int getRankType () {
			return rankType;
		}

		public void setRankType (int rankType) {
			this.rankType = rankType;
		}

		public String getRank () {
			return rank;
		}

		public void setRank (String rank) {
			this.rank = rank;
		}

		public GameScore getGameScore () {
			return gameScore;
		}

		public void setGameScore (GameScore gameScore) {
			this.gameScore = gameScore;
		}

		public boolean isFullCombo () {
			return fullCombo;
		}

		public void setFullCombo (boolean fullCombo) {
			this.fullCombo = fullCombo;
		}

		public boolean isNewRecord () {
			return newRecord;
		}

		public void setNewRecord (boolean newRecord) {
			this.newRecord = newRecord;
		}

		public GameMode getGameMode () {
			return mode;
		}

		public void setGameMode (GameMode mode) {
			this.mode = mode;
		}

	}

}