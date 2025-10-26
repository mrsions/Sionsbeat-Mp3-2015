package sions.android.spritebatcher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Texture {

	private float[] textureCoords; // Texture map coordinates

	private FloatBuffer textureBuffer;
	
	private String name="";
	private int textureId;
	private Bitmap bitmap;
	private Rect bounds;
	private boolean visible = true;
	
	public Texture(Bitmap bitmap){
		if(bitmap != null){
			this.bitmap = bitmap;
			this.bounds = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
		}else{
			this.bounds = new Rect(0,0,0,0);
		}
		updateTransform();
	}
	
	private void updateTransform(){
		
		textureCoords = new float[]{
				0.0f, 0.0f, //left top
				0.0f, 1.0f, //left bottom
				1.0f, 1.0f, //right bottom
				1.0f, 0.0f  //right top
			};

		// Texture
		ByteBuffer tbb = ByteBuffer.allocateDirect(textureCoords.length * 4);
		tbb.order(ByteOrder.nativeOrder());
		textureBuffer = tbb.asFloatBuffer();
		textureBuffer.put(textureCoords);
		textureBuffer.position(0);
		
	}
	
	public int getTextureId () {
		return textureId;
	}
	public void setTextureId (int textureId) {
		this.textureId = textureId;
	}
	public Bitmap getBitmap () {
		return bitmap;
	}
	public void setBitmap (Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	public Rect getBounds () {
		return bounds;
	}
	public void setBounds (Rect bounds) {
		this.bounds = bounds;
	}
	public float[] getTextureCoords () {
		return textureCoords;
	}

	public String getName () {
		return name;
	}

	public void setName (String name) {
		this.name = name;
	}

	public FloatBuffer getTextureBuffer () {
		return textureBuffer;
	}

	public boolean isVisible () {
		return visible;
	}

	public void setVisible (boolean visible) {
		this.visible = visible;
	}
	
}
