package sions.android.sionsbeat.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import sions.android.sionsbeat.game.GameData;
import sions.android.sionsbeat.game.exception.NotFoundMatchVersion;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.json.JSONArray;
import sions.json.JSONObject;

public class NoteSet {
	
	private static final String rootTAG = "customs";

	private static final String FILE_NAME = "info.properties";
	
	private static final String PROP_NAME = "name";
	private static final String PROP_AUTHOR = "author";
	private static final String PROP_CONTACT = "contact";
	private static final String PROP_STARTOFFSET = "startOffset";
	private static final String PROP_ENDOFFSET = "endOffset";
	private static final String PROP_LASTUPDATE = "lastupdate";
	private static final String PROP_LEVEL = "level.";
	private static final String PROP_EDITABLE = "editable";
	
	public static final int TYPE_ANALYZE = 0;
	public static final int TYPE_CUSTOM = 1;
	public static final int TYPE_MAKE = 2;

	
	public static NoteSet getSongNoteSet(SongInfo song, File folder){

		Properties prop = new Properties();
		try{
			prop.load(new FileInputStream(new File(folder, FILE_NAME)));
		}catch(Throwable e){
			Exception ee = new FileNotFoundException(new File(folder, FILE_NAME).getAbsolutePath());
			ee.addSuppressed(e);
			ErrorController.error(10, ee);
			return null;
		}
		
		String name = prop.getProperty(PROP_NAME, null);
		if(name == null){
			Exception ee = new NotFoundMatchVersion(new File(folder, FILE_NAME).getAbsolutePath()+"/notfound name");
			ErrorController.error(10, ee);
			return null;
		}
		
		NoteSet noteSet = new NoteSet(song, NoteSet.TYPE_CUSTOM, 3);
		noteSet.setNoteName(name);
		noteSet.setAuthorName(prop.getProperty(PROP_AUTHOR, "unkown"));
		noteSet.setContact(prop.getProperty(PROP_CONTACT, "unkown"));
		noteSet.setStartOffset(Integer.parseInt(prop.getProperty(PROP_STARTOFFSET, "0")));
		noteSet.setEndOffset(Integer.parseInt(prop.getProperty(PROP_ENDOFFSET, "0")));
		noteSet.setEditable(Boolean.parseBoolean(prop.getProperty(PROP_EDITABLE, "false")));
		noteSet.setRootFile(folder);
		try{
			noteSet.setLastUpdate(Long.parseLong(prop.getProperty(PROP_LASTUPDATE, "0")));
		}catch(NumberFormatException e){
			noteSet.setLastUpdate(0);
		}
		
		noteSet.setRootFile(folder);
		
		for(int i=0; i<3; i++){
			NoteFile nf = new NoteFile(i, 0, new File(folder, i+".json"));
			int level = Integer.parseInt(prop.getProperty(PROP_LEVEL+i, "0"));
			if(level == 0){
				try{
					GameData data = new GameData( nf.getFile(), song );
					level = data.getLevel();
				}catch ( Exception e ){
					continue;
				}
			}
			
			nf.setLevel(level);
			noteSet.getNotes().set(i, nf);
		}
		
		return noteSet;
	}
	
	
	private String noteName;
	private String authorName;
	private String contact;
	private int startOffset;
	private int endOffset;
	private long lastUpdate;
	private int type;
	
	private boolean editable;
	
	private NoteFile select;
	private SongInfo song;
	private ArrayList<NoteFile> notes;
	
	private File rootFile;
	private Properties prop;

	/************************************************************************************************************
	 * 
	 * 				@CONSTRUCTOR
	 * 
	 ************************************************************************************************************/
	
	public NoteSet(SongInfo song, int type, int size){
		this.type = type;
		this.song = song;
		this.noteName = String.valueOf(type);
		this.notes = new ArrayList<NoteFile>(size);
		for(int i=0; i<size; i++){
			this.notes.add(i,null);
		}
	}
	
	public NoteSet(JSONObject obj){
		
		JSONObject songJson = obj.getJSONObject("song");
		if(songJson != null){
			song = new SongInfo(songJson);
		}
		
		noteName = obj.getString("noteName", "unkown");
		authorName = obj.getString("authorName", "unkown");
		contact = obj.getString("contact", "unkown");
		
		startOffset = obj.getInt("startOffset", 0);
		endOffset = obj.getInt("endOffset", song.getDuration());
		lastUpdate = obj.getInt("lastUpdate", 0);
		type = obj.getInt("type", 0);
		
		rootFile = new File(obj.getString("rootFile"));
		editable = obj.getBoolean("editable", false);
		
		JSONArray noteArrays = obj.getJSONArray("notes");
		notes = new ArrayList<NoteFile>(noteArrays.length());
		
		for(int i=0; i<noteArrays.length(); i++)
		{
			NoteFile nf = null;
			if(noteArrays.is(i)){
				JSONObject noteJson = noteArrays.getJSONObject(i);
				nf = new NoteFile(noteJson);
			}
			notes.add(nf);
		}
		
	}
	
	public JSONObject toJson(){
		JSONObject obj = new JSONObject();
		
		obj.put("song", song.toJSON());
		
		obj.put("noteName", noteName);
		obj.put("authorName", authorName);
		obj.put("contact", contact);
		
		obj.put("startOffset", startOffset);
		obj.put("endOffset", endOffset);
		obj.put("lastUpdate", lastUpdate);
		obj.put("type", type);

		obj.put("rootFile", rootFile.getAbsolutePath());
		obj.put("editable", editable);
		
		JSONArray noteArrays = new JSONArray();
		for(int i=0; i<notes.size(); i++)
		{
			NoteFile nf = notes.get(i);
			if(nf != null)
			{
				noteArrays.add(nf.toJson());
			}
			else
			{
				noteArrays.add(null);
			}
		}
		obj.put("notes", noteArrays);
		
		return obj;
	}
	
	/************************************************************************************************************
	 * 
	 * 				@EVNET
	 * 
	 ************************************************************************************************************/

	
	public void doCreateNoteFolder(){
		
		if(rootFile == null){
		
			File roots = new File(new File(GameOptions.getRootFile(), rootTAG), song.getIdentity());
			File targetFile = null;
			for(int i=1; i<Integer.MAX_VALUE; i++){
				targetFile = new File(roots, String.valueOf(i));
				if(!targetFile.exists()){
					break;
				}
			}
			targetFile.mkdirs();
			rootFile = targetFile;
			
			prop = new Properties();
			
		}
		
		for(int i=0; i<notes.size(); i++){
			if(notes.get(i).getFile() == null){
				notes.get(i).setFile(new File(rootFile, i+".json"));
			}
		}
	}
	
	public void doStoreProperty(boolean editable){
		
		if(prop == null){
			prop = new Properties();
			
			FileInputStream fis = null;
			try{
				fis = new FileInputStream(new File(rootFile, FILE_NAME));
				prop.load(fis);
			}catch(FileNotFoundException e){
				e.printStackTrace();
			} catch(Throwable e){
				ErrorController.error(10, e);
			}finally{
				try{fis.close();}catch(Throwable e){}
			}
		}

		prop.setProperty(PROP_NAME, noteName == null? "": noteName);
		prop.setProperty(PROP_AUTHOR, authorName == null? "" : authorName);
		prop.setProperty(PROP_CONTACT, contact == null? "" : contact);
		prop.setProperty(PROP_STARTOFFSET, String.valueOf(startOffset));
		prop.setProperty(PROP_ENDOFFSET, String.valueOf(endOffset));
		prop.setProperty(PROP_LASTUPDATE, String.valueOf(getLastUpdate()));
		prop.setProperty(PROP_EDITABLE, String.valueOf(editable));
		
		this.editable = editable;
		
		for(int i=0; i<notes.size(); i++)
		{
			NoteFile nf = (NoteFile)notes.get(i);
			if(nf != null)
			{
				prop.setProperty("LEVEL."+i, String.valueOf(nf.getLevel()));
			}else{
				prop.setProperty("LEVEL."+i, "0");
			}
		}
		
		FileOutputStream fos = null;
		try{
			fos = new FileOutputStream(new File(rootFile, FILE_NAME));
			prop.store(fos, null);
		}catch(Throwable e){
			ErrorController.error(10, e);
		}finally{
			try{fos.close();}catch(Throwable e){}
		}
		
	}
	
	public void doStoreNotes(){
		
		for(int i=0; i<notes.size(); i++){
			NoteFile nf = notes.get(i);
			
			GameData data = new GameData(song, nf.getNotes(), startOffset, endOffset);
			data.store(nf.getFile());
			
			nf.level = data.getLevel();
		}
	}

	/************************************************************************************************************
	 * 
	 * 				@ACCESS_METHODS
	 * 
	 ************************************************************************************************************/
	
	public int getType () {
		return type;
	}
	public void setType (int type) {
		this.type = type;
	}
	public SongInfo getSong () {
		return song;
	}
	public void setSong (SongInfo song) {
		this.song = song;
	}
	public ArrayList<NoteFile> getNotes () {
		return notes;
	}
	public void setNotes (ArrayList<NoteFile> notes) {
		this.notes = notes;
	}
	public String getNoteName () {
		return noteName;
	}
	public void setNoteName (String noteName) {
		this.noteName = noteName;
	}
	public NoteFile getSelect () {
		return select;
	}
	public void setSelect (NoteFile select) {
		this.select = select;
	}
	public String getAuthorName () {
		return authorName;
	}
	public void setAuthorName (String authorName) {
		this.authorName = authorName;
	}
	public String getContact () {
		return contact;
	}
	public void setContact (String contact) {
		this.contact = contact;
	}
	public long getLastUpdate () {
		return lastUpdate;
	}
	public void setLastUpdate (long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public int getStartOffset () {
		return startOffset;
	}
	public void setStartOffset (int startOffset) {
		this.startOffset = startOffset;
	}
	public int getEndOffset () {
		return endOffset;
	}
	public void setEndOffset (int endOffset) {
		this.endOffset = endOffset;
	}
	public File getRootFile () {
		return rootFile;
	}
	public void setRootFile (File rootFile) {
		this.rootFile = rootFile;
	}
	public boolean isEditable () {
		return editable;
	}
	public void setEditable (boolean editable) {
		this.editable = editable;
	}

	public static class NoteFile {
		
		public static final int TYPE_BASIC = 0;
		public static final int TYPE_ADVANCE = 1;
		public static final int TYPE_EXTREME = 2;
		public static final int TYPE_UNKOWN = 3;

		public NoteFile (int type, int level, File file)
                {
			this.type = type;
	                this.level = level;
	                this.file = file;
                }
		public NoteFile ()
                {}
		public NoteFile (JSONObject obj)
                {
			this.type = obj.getInt("type");
			this.level = obj.getInt("level");
			this.file = new File(obj.getString("file"));
                }
		
		private int type;
		private int level;
		private File file;
		private Object tag;
		private Note[] notes;

		public int getType () {
			return type;
		}
		public void setType (int type) {
			this.type = type;
		}
		public int getLevel () {
			return level;
		}
		public void setLevel (int level) {
			this.level = level;
		}
		public File getFile () {
			return file;
		}
		public void setFile (File file) {
			this.file = file;
		}
		public Object getTag () {
			return tag;
		}
		public void setTag (Object tag) {
			this.tag = tag;
		}
		public Note[] getNotes () {
			return notes;
		}
		public void setNotes (Note[] notes) {
			this.notes = notes;
		}
		
		public JSONObject toJson(){
			JSONObject obj = new JSONObject();
			obj.put("type", type);
			obj.put("level", level);
			obj.put("file", file.getAbsolutePath());
			return obj;
		}
		
	}
	
}
