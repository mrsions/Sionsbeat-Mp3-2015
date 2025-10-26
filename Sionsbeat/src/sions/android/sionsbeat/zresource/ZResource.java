package sions.android.sionsbeat.zresource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import sions.android.sionsbeat.utils.ErrorController;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.util.Log;

public class ZResource {
	
	private File root;
	private HashMap<String, Object> container;
	private int targetWidth = 0;
	private int targetHeight = 0;
	private boolean compatibility = false;
	private int cut;

	public ZResource (File file) throws FileNotFoundException, IOException
	{

		this.root = file;
		this.container = new HashMap<String, Object>();

		// -- path size;
		String path = file.getAbsolutePath();
		cut = path.length();

		if (path.endsWith("/") || path.endsWith("\\") || path.endsWith(File.separator)) {
			cut--;
		}

		// -- setup
		addFolderFiles(file, cut);

		setupAnimation();

	}

	private void setupAnimation () {
		if(compatibility){
			for(File file: root.listFiles()){
				if(file.isDirectory()){
					
					String path = file.getAbsolutePath().substring(cut);
					path = path.replace("\\", "/").replace(File.separator, "/");
					if (path.startsWith("/")) {
						path = path.substring(1);
					}

					Properties prop = new Properties();
					prop.put("type", path);
					prop.put("play", "1");
					prop.put("delay", "40");
					prop.put("length", Integer.toString(file.listFiles().length));
					
					ZAnimation anim = new ZAnimation(this, prop);
					container.put(anim.getKey(), anim);
					
				}
			}
		}
	}

	public void addFolderFiles (File dirs, int cut) throws IOException {
		for (File file : dirs.listFiles()) {
			if (file.isDirectory()) {
				addFolderFiles(file, cut);
			} else if (file.isFile()) {
				addFile(file.getAbsolutePath().substring(cut), file);
			}
		}
	}

	/********************************************************************************
	 *
	 * @ADD_FIILE
	 * 
	 ********************************************************************************/

	public void addFile (String path, File file) throws IOException {
		path = path.replace("\\", "/").replace(File.separator, "/");

		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		
		int extensionIndex = path.lastIndexOf(".");
		if (extensionIndex == -1) {
			compatibility = true;
			addImage(path, file);
			return;
		}

		String extension = path.substring(extensionIndex);
		path = path.substring(0, extensionIndex);


		if (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".png") || extension.equalsIgnoreCase(".jpeg")) {

			addImage(path, file);
			
		} else if (extension.equalsIgnoreCase(".properties")) {
			
			addAnimation(file);
			
		}
	}

	public void addImage (String path, File file) {
		container.put(path, file);
	}

	public void addGIFImage (String path, File file) {
		container.put(path, file);
	}

	public void addAnimation (File file) throws IOException {
		ZAnimation anim = new ZAnimation(this, file);
		container.put(anim.getKey(), anim);
	}
	
	public void put(String path, Object obj){
		container.put(path, obj);
	}
	
	/********************************************************************************
	 *
	 * @GET
	 * 
	 ********************************************************************************/

	public Bitmap getBitmap (String path) {
		return getBitmap(path, 0, 0);
	}
	public Bitmap getBitmap (String path, int width, int height) {
		Object obj = container.get(path);
		
		if(width == 0){
			width = targetWidth;
			height = targetHeight;
		}

		if (obj instanceof File) {
			try {
				String filePath = ((File)obj).getAbsolutePath();
				
				Bitmap bitmap = null;
				if(width == 0 && height == 0){
					bitmap = BitmapFactory.decodeFile(filePath);	
				}else{
					bitmap = _getBitmap(filePath, width, height, Bitmap.Config.ARGB_8888);
				}
				container.put(path, bitmap);
				return bitmap;
			} catch (Exception e) {
				e.printStackTrace();
				container.remove(path);
			}
		} else if (obj instanceof Bitmap) { return (Bitmap) obj; }
		
		return null;
	}
	private Bitmap _getBitmap(String path, int width, int height, Bitmap.Config bmConfig){
		
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, bmOptions);

		int photoWidth = bmOptions.outWidth;
		int photoHeight = bmOptions.outHeight;

		int scaleFactor = Math.min(photoWidth / width, photoHeight / height);
		
		bmOptions.inPreferredConfig = bmConfig;
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
		
		return BitmapFactory.decodeFile(path, bmOptions);
	}
	
	

	public ZAnimation getAnimation (String path) {
		return getAnimation(path, 0, 0);
	}
	public ZAnimation getAnimation (String path, int width, int height) {
		Object obj = container.get(path);
		
		if (obj instanceof ZAnimation){
			ZAnimation anim = (ZAnimation) obj;
			anim.prepare(width, height);
			return anim;
		}
		
		return null;
	}
	
	public void remove(String path){
		container.remove(path);
	}

	/********************************************************************************
	 *
	 * @RECYCLE
	 * 
	 ********************************************************************************/

	public void recycle () {
		for (Object o : container.values()) {
			if (o instanceof Bitmap) {
				( (Bitmap) o ).recycle();
			} else if (o instanceof ZAnimate) {
				( (ZAnimate) o ).recycle();
				( (ZAnimate) o ).dispose();
			}
		}
	}

	public int getTargetWidth () {
		return targetWidth;
	}

	public void setTargetWidth (int targetWidth) {
		this.targetWidth = targetWidth;
	}

	public int getTargetHeight () {
		return targetHeight;
	}

	public void setTargetHeight (int targetHeight) {
		this.targetHeight = targetHeight;
	}
	
	public HashMap<String, Object> getMap(){
		return container;
	}

}
