package sions.android.sionsbeat.utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class FileUtils {

	public static void deleteFile(File file)
	{
		if(!file.exists()) return;
		
		if(file.isDirectory())
		{
			for(File f: file.listFiles())
			{
				deleteFile(f);
			}
		}
		
		file.delete();
	}
	
	public static MediaPlayer getMediaPlayer(Context context, String path)
	{
		MediaPlayer player = null;
		Uri uri = Uri.parse(path);
		try{
			player = new MediaPlayer();
			FileInputStream fis = new FileInputStream(path);
			FileDescriptor fd = fis.getFD();
	                player.setDataSource(fd);
			player.prepare();
			if(player != null) return player;
		}catch(Throwable e){
			ErrorController.error(10, e);
		}
		try{	
			player = new MediaPlayer();
			player.setDataSource(path);
			player.prepare();
			if(player != null) return player;
		}catch(Throwable e){
			ErrorController.error(10, e);
		}
		try{
			player = new MediaPlayer();
			player.setDataSource(context, uri);
			player.prepare();
			if(player != null) return player;
		}catch(Throwable e){
			ErrorController.error(10, e);
		}
		try{
			player = MediaPlayer.create(context, uri);
			if(player != null) return player;
		}catch(Throwable e){
			ErrorController.error(10, e);
		}
		
		ErrorController.error(10, new Exception("music access denied : "+path));
		return player;
	}
	
}
