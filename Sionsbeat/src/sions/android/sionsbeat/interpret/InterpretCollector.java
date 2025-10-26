package sions.android.sionsbeat.interpret;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javazoom.jl.decoder.BitstreamException;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore.Audio;
import android.util.Log;
import sions.android.SQ;
import sions.android.sionsbeat.adapter.MusicListAdapter;
import sions.android.sionsbeat.game.exception.UnkownSionsbeatException;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.JsonType;
import sions.json.JSONObject;

public class InterpretCollector extends Thread implements InterpretListener {

	private static String TAG = "songThread";
	
	/**
	 * ��������Ʈ ���¸� ǥ��
	 */
	public static final int INTERPRET_NONE = 0;
	public static final int INTERPRET_FAILED = -1;

	
	/**
	 * �ܺ� ���� ��Ҹ� ǥ��
	 */
	private static int SLEEPTIME = 0;
	
	public static void sleep() throws InterpretCancelException{
		if(SLEEPTIME == -1){
			throw new InterpretCancelException("restart");
		}else if(SLEEPTIME != 0){
			try{
				Thread.sleep(SLEEPTIME);
			}catch(Throwable e){}
		}
	}
	public static void setSleepTime(int time) {
		synchronized(TAG){
			SLEEPTIME = time;
		}
	}
	
	/**
	 * �̱��� ����
	 */
	private static InterpretCollector instance;
	public static InterpretCollector get (Activity context) {
		if (instance == null) instance = new InterpretCollector(context);
		return instance;
	}
	



	/**************************************************************************
	 * 
	 * @FIELDS
	 * 
	 *************************************************************************/
	
	private Activity context;
	private List<SongInfo> songList;

	private SongInfo interpretSong;
	private int interpretProgress;
	
	private JSONObject imported;
	private Runnable callback;
	

	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/

	public InterpretCollector (Activity context)
	{
		super(TAG);
		setPriority(Thread.MIN_PRIORITY);
		this.context = context;
		
		collectSongList();
		
		start();
	}
	
	private void setupImportPath()
	{
		imported = SQ.getJSONObject(JsonType.IMPORTED_MUSIC);
		if(imported == null) imported = new JSONObject();
	}

	public void callRecollect(Runnable callback){
		
		//-- �����尡 �м����ϰ�� Exception�� ����Ų��
		setSleepTime(-1);
		
		//--�����尡 �����ִ°�� ������Ų��
		synchronized (this) {
	                try{
	                	notify();
	                }catch(Throwable e){}
                }
		
		this.callback = callback;
	}

	/**************************************************************************
	 * 
	 * @LIFE_CYCLE
	 * 
	 *************************************************************************/
	
	public void run () {
		do{
			setSleepTime(0);
			if(callback != null) {
				callback.run();
				callback = null;
			}
			
			try {
				Log.d(TAG, "start");
	
				// -- �ʱ� �м�
				interpretNoneData();
				
				// -- ��ü�� �ٽ� �ۼ��Ѵ�
				interpretAllData();
				
				synchronized(this){
					try{
						wait();
					}catch(Throwable e){}
				}
	
			} catch(InterpretCancelException e){
				
			}catch (Exception e) {
				ErrorController.error(10, e);
				try{sleep(1000);}catch(Throwable ee){}
				continue;
			}
			
			//-- ���� ����۵ƴٴ� ���� ���ݷ�Ʈ�� ���ȱ� �����̴�.
			try{
				collectReload();
			}catch(Throwable e){}
		}while(true);
	}

	/**
	 * �뷡 �߿��� Interpret ��Ͽ� �Էµ��� ���� �뷡�� �ִٸ� �켱������ �м��� �����Ѵ�.
	 * 
	 * @throws Exception
	 */
	private void interpretNoneData () throws Exception {

		for(int i=0; i<songList.size(); i++){
		
			SongInfo song = songList.get(i);

			JSONObject obj = SQ.getJSONObject(JsonType.INTERPRET);
			if (obj == null || obj.getInt(song.getIdentity(), -2) == -2) {

				this.interpretSong = song;
				this.interpretProgress = 0;

				procInterpret(song);

			}

		}

	}

	/**
	 * �뷡�� ��� ���� �ٽ� interpret�Ѵ�.
	 * 
	 * @throws Exception
	 */
	private void interpretAllData () throws Exception {

		for(int i=0; i<songList.size(); i++){
		
			SongInfo song = songList.get(i);
			procInterpret(song);

		}

	}

	/**
	 * ��������Ʈ�� �����Ͽ� ���� �м���Ų��. ���� �м��� ������ JSON�� �����Ѵ�.
	 * 
	 * @param song
	 * @throws InterpretCancelException
	 */
	private void procInterpret (SongInfo song) throws InterpretCancelException {
		
		if(song.isCompatibility() || song.isInterpreted()) return;
		Log.d(InterpretMusic.TAG, "Interpret Start " + song.getID() + ":" + song.getTitle());

		//-- interpret ��� ����
		InterpretMusic im = new InterpretMusic(song, this);
		int saveResult = INTERPRET_NONE;

		//-- ����������
		try{
			if (im.prepareSpectrum()) {
				im.doInterpretation();
				onInterpretSuccess(song);
				saveResult = im.getMaxLevel();
			} else {
				onInterpretFailed(song, new Exception("failed"));
				saveResult = INTERPRET_FAILED;
			}
		}catch(OutOfMemoryError e){
			ErrorController.error(1, e);
			onInterpretFailed(song, e);
			saveResult = INTERPRET_FAILED;
		}catch(BitstreamException e){
			e.printStackTrace();
			onInterpretFailed(song, e);
			saveResult = INTERPRET_FAILED;
		}catch(InterpretNotSupportException e){
			e.printStackTrace();
			onInterpretFailed(song, e);
			saveResult = INTERPRET_FAILED;
		}catch(InterpretCancelException e){
			throw e;
		}catch(Throwable e){
			ErrorController.error(1, e);
			onInterpretFailed(song, e);
			saveResult = INTERPRET_FAILED;
		}
		
		//-- ���� ����
		JSONObject obj = SQ.getJSONObject(JsonType.INTERPRET);
		if (obj == null) obj = new JSONObject();
		
		obj.put(song.getIdentity(), saveResult);
		SQ.setJSON(JsonType.INTERPRET, obj);
		
		//-- �м��� �Ϸ�ƴٰ� �˸����μ� �����尡 �ٽ� ���Ƶ� �м������ʴ´�
		song.setInterpreted(true);
		
		//-- ����
		InterpretCollector.sleep();
	}
	
	@Override
	public void onInterpretProgress (SongInfo song, int progress) 
	{
		this.interpretProgress = progress;
		
		MusicListAdapter.setupProgress(context, song, progress);
	}

	@Override
	public void onInterpretSuccess (final SongInfo song) 
	{
		MusicListAdapter.setupSuccess(context, song);
	}

	@Override
	public void onInterpretFailed (final SongInfo song, Throwable e) 
	{
		MusicListAdapter.setupFailed(context, song, e);
	}

	/**************************************************************************
	 * 
	 * @FOUND_SONGs
	 * 
	 *************************************************************************/

	/**
	 * �ʱ� �ѹ��� ȣ��Ǹ�, ������ �����Ѵ�.
	 */
	private void collectSongList () {
		Log.d(TAG, "collect Song List");
		
		setupImportPath();

		//-- �⺻ ������ �о�´�.
		List<SongInfo> tSongList = getMusicList(context, imported);
		tSongList = getAlbumFromList(context, tSongList);
		
		try{
			//-- �⺻ ������ �о�´�.
			inputMusicDirectory(new File(GameOptions.getRootFile(), "music"), tSongList);
			
			//-- ȣȯ������ �о�´�.
			{
				File file = context.getExternalFilesDir(null);
				if(file.exists()){
					String path = file.getAbsolutePath(); // ���� ���ø����̼��� �ܺ�����Ҹ� �����´�
					path = path.replace(context.getPackageName(), "com.dmelody.andjuist2");
					
					inputMusicDirectory(new File(path, "songs"), tSongList);
				}
			}
		
		}catch(Throwable e){
			ErrorController.error(10, e);
		}

		this.songList = tSongList;
	}
	
	private void collectReload()
	{
		setupImportPath();
		
		List<SongInfo> tSongList = getMusicList(context, imported);
		tSongList = getAlbumFromList(context, tSongList);

		for(int i=0; i<tSongList.size(); i++)
		{
			SongInfo tSong = tSongList.get(i);
			
			for(SongInfo song:songList){
				if(song.getID() == tSong.getID()){
					tSongList.set(i, tSong = song);
				}
			}
		}
		
		for(int i=0, idx=0; i<songList.size(); i++){
			SongInfo song = songList.get(i);
			if(song.isCompatibility() || song.isBasic()){
				tSongList.add(idx++, song);
			}
		}
		
		this.songList = tSongList;
	}
	


	/**************************************************************************
	 * 
	 * @�ȵ���̵�_����
	 * 
	 *************************************************************************/
	
	/**
	 * �̵�� ������ �̿��Ͽ� ������ �����Ѵ�. 
	 * 
	 * @param context
	 * @param (JSONObject) imported	
	 * @return
	 */
	public static List<SongInfo> getMusicList (Context context, JSONObject imported) {

		String[] projection = {Audio.Media._ID, Audio.Media.ALBUM_ID, Audio.Media.TITLE, Audio.Media.ARTIST, Audio.Media.DURATION, Audio.Media.DATA, Audio.Media.MIME_TYPE};

		Cursor cursor = context.getContentResolver().query(Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

		if (cursor == null) {
			Log.d(TAG, "Not found Audio Query");
			return null;
		}

		ArrayList<SongInfo> list = new ArrayList<SongInfo>();

		if (cursor.moveToFirst()) {

			int[] ids = {cursor.getColumnIndex(Audio.Media._ID), cursor.getColumnIndex(Audio.Media.ALBUM_ID), cursor.getColumnIndex(Audio.Media.TITLE), cursor.getColumnIndex(Audio.Media.ARTIST), cursor.getColumnIndex(Audio.Media.DURATION), cursor.getColumnIndex(Audio.Media.DATA), cursor.getColumnIndex(Audio.Media.MIME_TYPE)};

			SongInfo song = null;
			do {

				song = new SongInfo();
				song.setID(cursor.getInt(ids[0]));
				song.setAlbumID(cursor.getInt(ids[1]));
				song.setTitle(cursor.getString(ids[2]));
				song.setArtist(cursor.getString(ids[3]));
				song.setDuration(cursor.getInt(ids[4]));
				song.setPath(cursor.getString(ids[5]));
				song.setMimeType(cursor.getString(ids[6]));
				
				Log.d("Song", "Song Dur "+song.getTitle() +" / "+song.getDuration());
				
				if(song.getPath() == null) continue;
				
				File file = new File(song.getPath());
				if(!file.exists()){
					continue;
				}

				
				if(imported != null){
					file = file.getParentFile();
					if(file == null || !file.exists() || !imported.getBoolean(file.getAbsolutePath(), true)){
						continue;
					}
				}

				if (song.getMimeType().equalsIgnoreCase("audio/mpeg") || song.getMimeType().equalsIgnoreCase("audio/mp3")) {
					list.add(song);
				}

			} while (cursor.moveToNext());
		}
		return list;

	}

	/**
	 * �ҷ��� ���ǵ��� �ٹ��̹����� �Է��Ѵ�.
	 * 
	 * @param list
	 * @return
	 */
	private static List<SongInfo> getAlbumFromList (Context context, List<SongInfo> list) {
		String[] projection = {Audio.Albums._ID, Audio.Albums.ALBUM_ART};

		Cursor cursor = context.getContentResolver().query(Audio.Albums.EXTERNAL_CONTENT_URI, projection, null, null, null);

		if (cursor == null) {
			Log.d(TAG, "Not found Audio Query");
			return null;
		}
		if (cursor.moveToFirst()) {

			int[] ids = {cursor.getColumnIndex(Audio.Albums._ID), cursor.getColumnIndex(Audio.Albums.ALBUM_ART)};

			int tempId;
			String tempPath;

			do {

				tempId = cursor.getInt(ids[0]);
				tempPath = cursor.getString(ids[1]);

				for (SongInfo info : list) {
					if (info.getAlbumID() == tempId) {
						info.setArt(tempPath);
					}
				}

			} while (cursor.moveToNext());
		}

		
		return list;
	}

	public static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
	public static Uri getArtworkUri(int albumId){
		return ContentUris.withAppendedId(sArtworkUri, albumId);
	}
//	public static Bitmap checkAlbumArt(ContentResolver contentResolver, int albumId, int width, int height)
//	{
//		Bitmap bm = null;
//		try{
//			
//			Uri uri = ContentUris.withAppendedId(sArtworkUri, albumId);
//			
//			ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(uri, "r");
//			
//			if(pfd != null)
//			{
//			}
//		}catch(Throwable e){
//			e.printStackTrace();
//		}
//	}
	

	
	/**************************************************************************
	 * 
	 * @����_����
	 * 
	 *************************************************************************/
	
	private void inputMusicDirectory (File root, List<SongInfo> tSongList) 
	{
		if (!root.exists()) return;

		SongInfo info = null;

		File[] musicList = root.listFiles();
		for (File mFile : musicList) {

			if (!mFile.isDirectory()) continue;

			File targetFile;

			try{
				
				//-- �ÿ½���Ʈ �����
				if( (targetFile=new File(mFile, "info.properties")).exists() )
				{
					info = getDefaultSongInfo(targetFile);
				}
				//-- (��)�ÿ½���Ʈ ȣȯ��
				else if( (targetFile=new File(mFile, "info.txt")).exists() )
				{
					info = getCompatibilitySongInfo(targetFile);
				}
				//-- �ȵ��� ȣȯ��
				else if( (targetFile=new File(mFile, "manifest.txt")).exists() )
				{
					info = getAndJuistSongInfo(targetFile);
				}
	
				
				if(info != null){
					tSongList.add(0, info);
				}
				
			}catch(Throwable e){
				ErrorController.error(10, e);
			}
		}
	}
	
	/**
	 * �⺻ ȣȯ������ �����´�
	 * 
	 * @param target
	 * @return
	 * @throws Exception 
	 */
	public static SongInfo getDefaultSongInfo(File target) throws Exception{
		
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(target));
		} catch (Exception e) {
			ErrorController.error(1, e);
			return null;
		}
		
		File folder = target.getParentFile();

		SongInfo info = new SongInfo();
		info.setTitle(prop.getProperty("title", "unkown"));
		info.setArtist(prop.getProperty("artist", "unkown"));
		info.setArt(new File(folder, prop.getProperty("album", "album.jpg")).getAbsolutePath());
		info.setPath(new File(folder, prop.getProperty("data", "music.mp3")).getAbsolutePath());
		info.setMimeType(prop.getProperty("mime", "audio/mpeg3"));
		info.setContact(prop.getProperty("contact", "unkown"));
		info.setCCL(Boolean.parseBoolean(prop.getProperty("ccl", "false")));
		info.setBasic(true);
		
		try{
			if(info.isCCL()){
				StringBuilder details = new StringBuilder("\n");
				details.append("MUSIC : ").append(prop.getProperty("ccl.music","")).append("\n");
				details.append("MUSIC.TYPE : ").append(prop.getProperty("ccl.music.type","")).append("\n");
				details.append("MUSIC.CHANGE : ").append(prop.getProperty("ccl.music.change","")).append("\n").append("\n");

				details.append("IMAGE : ").append(prop.getProperty("ccl.image","")).append("\n");
				details.append("IMAGE.AUTHOR : ").append(prop.getProperty("ccl.image.author","")).append("\n");
				details.append("IMAGE.TYPE : ").append(prop.getProperty("ccl.image.type","")).append("\n");
				details.append("IMAGE.CHANGE : ").append(prop.getProperty("ccl.image.change","")).append("\n");
				
				info.setDetails(details.toString());
			}
		}catch(Throwable e){
			ErrorController.error(10, e);
		}

		try {
			info.setDuration(Integer.parseInt(prop.getProperty("duration", "0")));
		} catch (NumberFormatException e) {
			info.setDuration(getDuration(folder, info));
		}
		
		return info;
		
	}
	
	/**
	 * (��) �ÿ½���Ʈ ȣȯ������ �����´�
	 * 
	 * @param target
	 * @return
	 * @throws Exception
	 */
	public static SongInfo getCompatibilitySongInfo(File target) throws Exception{

		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(target));
		} catch (Exception e) {
			ErrorController.error(1, e);
			return null;
		}
		
		File folder = target.getParentFile();
		File music = new File(folder, "music");
		if(!music.exists()) music = new File(folder, "music.mp3");
		if(!music.exists()) return null;
		
		SongInfo info = new SongInfo();
		info.setTitle(prop.getProperty("title", "unkown"));
		info.setArtist(prop.getProperty("artist", "unkown"));
		info.setArt(new File(folder, "splash").getAbsolutePath());
		info.setPath(music.getAbsolutePath());
		info.setMimeType(prop.getProperty("mime", "audio/mpeg3"));
		info.setContact(prop.getProperty("contact", "unkown"));
		info.setCCL(Boolean.parseBoolean(prop.getProperty("ccl", "false")));
		info.setCompatibility(true);

		try {
			info.setDuration(Integer.parseInt(prop.getProperty("time", "0")));
			if(info.getDuration() == 0) throw new NumberFormatException();
		} catch (NumberFormatException e) {
			info.setDuration(getDuration(folder, info));
		}

		return info;
		
	}
	
	/**
	 * �ȵ��� ȣȯ������ �����´�
	 * 
	 * @param target
	 * @return
	 * @throws Exception 
	 */
	public static SongInfo getAndJuistSongInfo(File target) throws Exception{
		
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(target));
		} catch (Exception e) {
			return null;
		}
		
		File folder = target.getParentFile();
		File music = new File(folder, "song.mp3");
		if(!music.exists()) music = new File(folder, "song.mp3");
		if(!music.exists()) return null;
		
		SongInfo info = new SongInfo();
		info.setTitle(prop.getProperty("Name", "unkown"));
		info.setArtist(prop.getProperty("Composer", "unkown"));
		info.setArt(new File(folder, "pic.jpg").getAbsolutePath());
		info.setPath(new File(folder, "song.mp3").getAbsolutePath());
		info.setMimeType(prop.getProperty("mime", "audio/mpeg3"));
		info.setContact(prop.getProperty("from", "unkown"));
		info.setCCL(Boolean.parseBoolean(prop.getProperty("ccl", "false")));
		info.setCompatibility(true);

		try {
			info.setDuration(Integer.parseInt(prop.getProperty("time", "0")));
			if(info.getDuration() == 0) throw new NumberFormatException();
		} catch (NumberFormatException e) {
			info.setDuration(getDuration(folder, info));
		}

		return info;
		
	}
	
	/**
	 * 
	 * @param folder
	 * @param info
	 * @return
	 * @throws Exception
	 */
	private static int getDuration(File folder, SongInfo info) throws Exception{
		try{
			MediaMetadataRetriever mmr = new MediaMetadataRetriever();
			mmr.setDataSource(info.getPath());
			
			String durationString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			int duration = Integer.parseInt(durationString);
			
			mmr.release();
			
			return duration;
			
		}catch(Throwable e){
			return 0;
		}
	}


	/**************************************************************************
	 * 
	 * @FOUND_SONGs
	 * 
	 *************************************************************************/

	public List<SongInfo> getSongList () {
		return songList;
	}

	public void setSongList (List<SongInfo> songList) {
		this.songList = songList;
	}

	public SongInfo getInterpretSong () {
		return interpretSong;
	}

	public void setInterpretSong (SongInfo interpretSong) {
		this.interpretSong = interpretSong;
	}

	public int getInterpretProgress () {
		return interpretProgress;
	}

	public void setInterpretProgress (int interpretProgress) {
		this.interpretProgress = interpretProgress;
	}
}
