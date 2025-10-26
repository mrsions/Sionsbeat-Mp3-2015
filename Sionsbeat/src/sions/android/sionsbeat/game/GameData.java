package sions.android.sionsbeat.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

import android.util.Log;
import sions.android.SQ;
import sions.android.sionsbeat.Base;
import sions.android.sionsbeat.game.exception.NotFoundMatchVersion;
import sions.android.sionsbeat.interpret.InterpretCollector;
import sions.android.sionsbeat.template.Note;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.NumericTools;
import sions.json.JSONArray;
import sions.json.JSONObject;

public class GameData
{
	/**
	 * NPM( 노트/분 )에 대해서 레벨 값을 산출해 준다.
	 * @param npm
	 * @return
	 */
	public static int getLevel(Note[] notes, int duration)
	{
		int bits = 0;
		for(Note note:notes){
			if(note == null)continue;
			bits += Integer.bitCount(note.getButton());
		}
		return getLevel( (int) ( bits * ( 60000f / duration ) ));
	}
	public static int getLevel(int npm)
	{
		if (npm>=GameOption.LEVEL[20])	return 20;
		else if (npm>=GameOption.LEVEL[19])	return 19;
		else if (npm>=GameOption.LEVEL[18])	return 18;
		else if (npm>=GameOption.LEVEL[17])	return 17;
		else if (npm>=GameOption.LEVEL[16])	return 16;
		else if (npm>=GameOption.LEVEL[15])	return 15;
		else if (npm>=GameOption.LEVEL[14])	return 14;
		else if (npm>=GameOption.LEVEL[13])	return 13;
		else if (npm>=GameOption.LEVEL[12])	return 12;
		else if (npm>=GameOption.LEVEL[11])	return 11;
		else if (npm>=GameOption.LEVEL[10])	return 10;
		else if (npm>=GameOption.LEVEL[9])	return 9;
		else if (npm>=GameOption.LEVEL[8])	return 8;
		else if (npm>=GameOption.LEVEL[7])	return 7;
		else if (npm>=GameOption.LEVEL[6])	return 6;
		else if (npm>=GameOption.LEVEL[5])	return 5;
		else if (npm>=GameOption.LEVEL[4])	return 4;
		else if (npm>=GameOption.LEVEL[3])	return 3;
		else if (npm>=GameOption.LEVEL[2])	return 2;
		else if (npm>=GameOption.LEVEL[1])	return 1;
		return 1;
	}

	public static File getNoteFile(SongInfo song, int level)
	{
		File root = new File(GameOptions.getRootFile(), "album");
		root.mkdirs();
		
		String fileName = song.getIdentity()+"-"+level;
		
		return new File(root, fileName+".json");
	}
	
	private SongInfo song;
	private Note[] notes;
	private int noteBits;
	private int npm;
	private int level;
	
	private int startOffset=-1;
	private int endOffset=-1;
	private int duration;
	
	private String authorContact;
	private String author;
	private ArrayList<Throwable> errorList;

	/**************************************************************************
	 * 
	 * @throws NotFoundMatchVersion 
	 * @Constructor
	 * 
	 *************************************************************************/

	public GameData(File file) throws IOException, NotFoundMatchVersion
	{
		this(file, null);
	}
	public GameData(File file, SongInfo song) throws IOException, NotFoundMatchVersion
	{
		FileInputStream fis = null;
		byte[] data = null;

		try{
			fis = new FileInputStream(file);
			fis.read(data = new byte[fis.available()]);
		}finally{
			fis.close();
		}
		
		this.song = song;
		
		if(_readJSON_1(data));
		else if(_readCSV_1(data));
		else if(_readCSV_0(file, data));
		else if(_readJMT(file, data));
		else{
			if(errorList.size() > 0){
				for(Throwable e:errorList){
					ErrorController.error(10, e);
				}
			}
			throw new NotFoundMatchVersion(file.getAbsolutePath());
		}

		if(startOffset == -1)
		{
			this.startOffset = 0;
			this.duration = this.song.getDuration();
			this.endOffset = this.song.getDuration();
		}
		else
		{
			this.duration = this.endOffset-this.startOffset;
		}
		
		initialize();
	}
	
	public GameData(SongInfo song, Note[] notes, int startOffset, int endOffset)
	{
		this.song = song;
		this.notes = notes;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.duration = endOffset - startOffset;
		
		initialize();
	}
	
	private void initialize(){

		this.noteBits = 0;
		for(Note note:this.notes){
			this.noteBits += Integer.bitCount(note.getButton());
		}
		
		this.npm = (int) ( noteBits * (60000f / duration) );

		this.level = getLevel(npm);
	}


	/**************************************************************************
	 * 
	 * @Store
	 * 
	 *************************************************************************/
	
	public void store(File file)
	{
		JSONObject obj = new JSONObject();
		obj.put("version", 1);
		obj.put("level", level);

		obj.put("song", song.toJSON());
		obj.put("startOffset", startOffset);
		obj.put("endOffset", endOffset);
		
		JSONArray noteData = new JSONArray();
		for(Note note:notes)
		{
			JSONObject ndata = new JSONObject();
			ndata.put("time", note.getTiming());
			ndata.put("note", note.getButton());
			noteData.add(ndata);
		}
		obj.put("note", noteData);
		
		FileOutputStream fos = null;
		try{
			
			fos = new FileOutputStream(file);
			fos.write(obj.toString().getBytes("UTF-8"));
			
		}catch(Throwable e){
			ErrorController.error(10, e);
		}finally{
			try{ fos.close(); }catch(Throwable e){}
		}
		
	}
	
	/**************************************************************************
	 * 
	 * @VersionReader
	 * 
	 *************************************************************************/
	
	private boolean _readJMT(File file, byte[] data){
		String tempToken = "not jmt";
		try{
			String dataString = new String(data);
			StringTokenizer stz = new StringTokenizer( dataString, "\r\n" ), cell;
			
			ArrayList<Note> notes = new ArrayList<Note>();
			
			while(stz.hasMoreTokens()){
				try{
					tempToken = stz.nextToken();
					
					int firstIndex = tempToken.indexOf("#")+1;
					if(firstIndex == 0) continue;
					
					int button = Integer.parseInt(tempToken.substring(firstIndex, tempToken.indexOf(".")));
					
					String timingString = tempToken.substring(tempToken.indexOf(".")+1);
					if(timingString.indexOf(".")!= -1){
						timingString = timingString.substring(0, timingString.indexOf("."));
					}
					int timing = Integer.parseInt(timingString);
					
					Note note = new Note(timing, 1<<(button-1));
					notes.add(note);
					
				}catch(Throwable e){
					Exception ee = new Exception(tempToken);
					ee.addSuppressed(e);
					ErrorController.error(10, ee);
				}
			}
			
			this.notes = (Note[]) notes.toArray(new Note[notes.size()]);
			Arrays.sort(this.notes, new Comparator<Note>() {
				@Override
                                public int compare (Note lhs, Note rhs) {
	                                return NumericTools.Integer.compare(lhs.getTiming(), rhs.getTiming());
                                }
			});

			Log.d("test", "song ? "+song);
			if(song == null){
				song = InterpretCollector.getAndJuistSongInfo(new File(file.getParentFile(), "manifest.txt"));
				Log.d("test", " readed > "+song);
			}
			
			return true;
		}catch(Throwable e){
			if(errorList == null) errorList = new ArrayList();
			Exception ee = new Exception(tempToken);
			ee.addSuppressed(e);
			
			errorList.add(ee);
			return false;
		}
	}
	
	private boolean _readCSV_0(File file, byte[] data)
	{
		try{
			Note note = null;
			
			String dataString = new String(data);
			if(!dataString.startsWith( "1024" )) return false;
			
			StringTokenizer stz = new StringTokenizer( dataString, "\r\n" ), cell;
	
			stz.nextToken();
			
			int noteLength = Integer.parseInt( stz.nextToken() );
			int noteCount = 0;
			
			notes = new Note[noteLength];
			for(int i=0; i<noteLength; i++)
			{
				cell = new StringTokenizer( stz.nextToken(), " ," );
				
				note = new Note( 0, 0 );
				note.setTiming( Integer.parseInt(cell.nextToken())+640 );
				note.setButton( Integer.parseInt(cell.nextToken()) );
				
				noteCount += Integer.bitCount(note.getButton());
				
				notes[i] = note;
			}
			
			if(song == null){
				song = InterpretCollector.getCompatibilitySongInfo(new File(file.getParentFile(), "info.txt"));
			}
			
			return true;
			
		}catch(Throwable e){
			if(errorList == null) errorList = new ArrayList();
			errorList.add(e);
			return false;
		}
	}

	private boolean _readCSV_1(byte[] data)
	{
		try{
			Note note = null;
			
			String dataString = new String(data, "UTF-8");
			if(!dataString.startsWith( "1025" )) return false;
			
			StringTokenizer stz = new StringTokenizer( dataString, "\r\n" ), cell;
	
			stz.nextToken();
			
			int noteLength = Integer.parseInt( stz.nextToken() );
			notes = new Note[noteLength];
			for(int i=0; i<noteLength; i++)
			{
				cell = new StringTokenizer( stz.nextToken(), " ," );
				
				note = new Note( 0, 0 );
				note.setTiming( Integer.parseInt(cell.nextToken()) );
				note.setButton( Integer.parseInt(cell.nextToken()) );
				
				notes[i] = note;
			}
			
			song = new SongInfo();
			song.setID( Integer.parseInt(stz.nextToken()) );
			song.setTitle( stz.nextToken() );
			song.setArtist( stz.nextToken() );
			song.setArt( stz.nextToken() );
			song.setPath( stz.nextToken() );
			song.setDuration( Integer.parseInt(stz.nextToken()) );
			
			this.level = Integer.parseInt(stz.nextToken());
			this.authorContact = stz.nextToken();
					
			return true;
			
		}catch ( UnsupportedEncodingException e ){
			return false;
		}catch(Throwable e){
			if(errorList == null) errorList = new ArrayList();
			errorList.add(e);
			return false;
		}
	}
	
	private boolean _readJSON_1(byte[] data)
	{
		Note note;
		try{
			JSONObject obj = new JSONObject(new String(data, "UTF-8"));
			if(obj.getInt( "version" ) != 1){
				return false;
			}
			
			//--Default
			level = obj.getInt( "level", 0 );
			startOffset = obj.getInt("startOffset", -1);
			endOffset = obj.getInt("endOffset", -1);

			//--Song
			JSONObject songData = obj.getJSONObject( "song" );
			song = new SongInfo(songData);
			
			//--Notes
			JSONArray noteData = obj.getJSONArray( "note" );
			notes = new Note[noteData.length()];
			for(int i=0; i<noteData.length(); i++){
				JSONObject ndata = noteData.getJSONObject( i );

				note = new Note( 0, 0 );
				note.setTiming( ndata.getInt( "time" ) );
				note.setButton( ndata.getInt( "note" ) );

				notes[i] = note;
			}
			
			return true;
			
		}catch ( UnsupportedEncodingException e ){
			return false;
		}catch(Throwable e){
			if(errorList == null) errorList = new ArrayList();
			errorList.add(e);
			return false;
		}
	}

	/**************************************************************************
	 * 
	 * @GETSET
	 * 
	 *************************************************************************/

	private final float SPLIT_DURATION = 130000;
	public GameData[] splitGameData(){
		
		Log.d("test", "split "+song.getDuration()+"   >  "+(SPLIT_DURATION+20000));
		if((Base.FREE || GameOptions.get(SQ.SQ().getContext()).getBoolean(GameOptions.OPTION_GAME_SPLIT, true)) && song.getDuration() > SPLIT_DURATION+20000){ // 한계점 20초 추가
			
			int count = (int) Math.ceil(song.getDuration() / SPLIT_DURATION );
			int countDuration = song.getDuration() / count;
			GameData[] data = new GameData[count];
			
			for(int i=0; i<count; i++){
				
				int start = ( i==0? 0 : countDuration * i );
				int end = ( i==count-1? song.getDuration() : countDuration * (i+1) );

				ArrayList<Note> notes = new ArrayList<Note>();
				for(Note note: this.notes){
					if(start <= note.getTiming() && note.getTiming() < end){
						notes.add(note);
					}
				}

				data[i] = new GameData(song, (Note[])notes.toArray(new Note[notes.size()]), start, end);
				data[i].level = this.level;
				
			}
			
			return data;
			
		}
		return new GameData[]{this};
	}
	

	/**************************************************************************
	 * 
	 * @GETSET
	 * 
	 *************************************************************************/

	public SongInfo getSong ()
	{
		return song;
	}

	public Note[] getNotes ()
	{
		return notes;
	}

	public int getLevel ()
	{
		return level;
	}

	public String getAuthorContact ()
	{
		return authorContact;
	}

	public String getAuthor ()
	{
		return author;
	}

	public int getStartOffset () {
		return startOffset;
	}

	public int getEndOffset () {
		return endOffset;
	}

	public int getNoteBits () {
		return noteBits;
	}

	public int getNpm () {
		return npm;
	}

	public int getDuration () {
		return duration;
	}


	
}
