package sions.android.sionsbeat.template;

import java.io.File;

import com.squareup.picasso.Picasso;

import android.content.Context;
import android.widget.ImageView;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.interpret.InterpretCollector;
import sions.json.JSONObject;

public class SongInfo {
	
	public static String getSongIdentity(SongInfo song){
		String name = song.getTitle()+"-"+song.getArtist()+"-"+song.getDuration();
		name = name.replace("|","-").replace("\\","-").replace("?", "-").replace("*", "-").replace("<", "[").replace(">", "]").replace("\"", "-").replace(":", "-").replace("/", "-").replace("%", "-").replace(" ", "-");
		
		if(name.length() > 100){
			char[] chars = name.toCharArray();
			int size = chars.length;
			for(char c:chars){
				if(c > 0xFF) size++;
			}

			int i = chars.length-1;
			for(;i>100 || size>100; i--){
				char c = chars[i];
				if(c > 0xFF){
					size -= 2;
				}else{
					size--;
				}
			}
			
			name = name.substring(0, i);
		}
		return name;
	}
	
	public static  String getTimeCode(int duration){
		int second = duration/1000;
		int minute = second/60;
		second %= 60;

		return String.format("%02d:%02d", minute, second);
	}
	
	public SongInfo(){}
	public SongInfo(JSONObject obj){
		ID = obj.getInt("id", 0);
		title = obj.getString("title", "unkown");
		artist = obj.getString("artist", "unkown");
		art = obj.getString("art", "");
		path = obj.getString("path", "");
		mimeType = obj.getString("mimeType", "audio/mpeg3");
		duration = obj.getInt("duration", 0);
		albumID = obj.getInt("albumID", 0);
		ccl = obj.getBoolean("ccl", false);
		compatibility = obj.getBoolean("compatibility", false);
		basic = obj.getBoolean("basic", false);
		interpreted = obj.getBoolean("Interpreted", false);
		contact = obj.getString("contact", "unkown");

		if(ID == 0){
			ID = obj.getInt("ID", 0);
		}if(art.equals("")){
			art = obj.getString("thumb", "");
		}
	}
	
	private int ID;
	private String title;
	private String artist;
	private String art;
	private String path;
	private String mimeType;
	private int duration;
	private int albumID;
	private boolean ccl;
	private boolean compatibility;
	private boolean basic;
	private boolean interpreted;
	private String contact;
	private String details;
	
	private Object tag;
	
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getArt() {
		return art;
	}
	public void setArt(String art) {
		this.art = art;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public int getAlbumID() {
		return albumID;
	}
	public void setAlbumID(int albumID) {
		this.albumID = albumID;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getDurationString(){
		return getTimeCode(duration);
	}
	public Object getTag ()
	{
		return tag;
	}
	public void setTag ( Object tag )
	{
		this.tag = tag;
	}

	public String getIdentity(){
		return getSongIdentity(this);
	}
	public boolean isCCL () {
		return ccl;
	}
	public void setCCL (boolean ccl) {
		this.ccl = ccl;
	}
	public String getContact () {
		return contact;
	}
	public void setContact (String contact) {
		this.contact = contact;
	}

	public boolean isCompatibility () {
		return compatibility;
	}

	public void setCompatibility (boolean compatibility) {
		this.compatibility = compatibility;
	}

	public boolean isBasic () {
		return basic;
	}

	public void setBasic (boolean basic) {
		this.basic = basic;
	}

	public boolean isInterpreted () {
		return interpreted;
	}

	public void setInterpreted (boolean interpreted) {
		this.interpreted = interpreted;
	}

	public String getDetails () {
		return details;
	}

	public void setDetails (String details) {
		this.details = details;
	}

	public void parseAlbumView50(Context context, ImageView iv, int placeholder){
		try{
			if(albumID != 0)
			{
				Picasso.with(context).load(InterpretCollector.getArtworkUri(albumID)).placeholder(placeholder).resize(50, 50).into(iv);
			}else if (getArt() != null && getArt().length() > 0) {
				Picasso.with(context).load(new File(art)).placeholder(placeholder).resize(50, 50).into(iv);
			} else {
				iv.setImageResource(placeholder);
			}
		}catch(Throwable e){
			iv.setImageResource(placeholder);
		}
	}

	public void parseAlbumView(Context context, ImageView iv, int placeholder){
		try{
			if(albumID != 0)
			{
				Picasso.with(context).load(InterpretCollector.getArtworkUri(albumID)).placeholder(placeholder).into(iv);
			}else if (getArt() != null && getArt().length() > 0) {
				Picasso.with(context).load(new File(art)).placeholder(placeholder).into(iv);
			} else {
				iv.setImageResource(placeholder);
			}
		}catch(Throwable e){
			iv.setImageResource(placeholder);
		}
	}
	
	public JSONObject toJSON(){
		JSONObject obj = new JSONObject();
		obj.put("id", ID);
		obj.put("title", title);
		obj.put("artist", artist);
		obj.put("art", art);
		obj.put("path", path);
		obj.put("mimeType", mimeType);
		obj.put("duration", duration);
		obj.put("albumID", albumID);
		obj.put("ccl", ccl);
		obj.put("compatibility", compatibility);
		obj.put("basic", basic);
		obj.put("interpreted", interpreted);
		obj.put("contact", contact);
		return obj;
	}

}
