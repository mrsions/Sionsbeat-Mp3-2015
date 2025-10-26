package sions.android.spritebatcher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class SpriteBatcher implements Renderer {

	public static final int TEXTURE_SUCCESS = 0x00;
	public static final int TEXTURE_DOWN_SIZE = 0x01;
	public static final int TEXTURE_IGNORE= 0x02;
	
	private static final String TAG = "SpriteBatcher";

	private Drawer drawer;

	private int width;
	private int height;

	private int maxFPS;
	private long lastTime;

	private Context context;
	
	private ArrayList<Sprite> sprites;

	private GLErrorListener handler;
	private int drawCount;
	
	private static int support_texture_size;
	
	private int textureLoadStatus = TEXTURE_SUCCESS;

	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/

	public SpriteBatcher (Context context, Drawer drawer)
	{
		this.context = context;
		this.drawer = drawer;
		
		this.sprites = new ArrayList<Sprite>();
	}
	
	/**************************************************************************
	 * 
	 * @TEXTURING
	 * 
	 *************************************************************************/

	public void addTextures(GL10 gl, Collection<Texture> textures){

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		int length = textures.size();
		
		int[] textureIds = new int[length];
		gl.glGenTextures(length, textureIds, 0);
		glErrorCheck(gl, "addTexture-glGen");
		
		//Iterate over textures
		int i = 0;
		Texture texture;
		Iterator<Texture> it = textures.iterator();
		while(it.hasNext())
		{
			texture = it.next();
			
			// Assign texture id
			texture.setTextureId(textureIds[i++]);
			
			// Load bitmap into openGL
			addTexture(gl, texture);
		}
	}
	
	private void addTexture(GL10 gl, Texture texture){
		
		// Get Bitmap
		Bitmap bitmap = texture.getBitmap();
		
		if(bitmap == null){
			bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
		}
		
		if(bitmap != null && !bitmap.isRecycled()){
		
			// Working with textureId
			gl.glBindTexture(GL10.GL_TEXTURE_2D, texture.getTextureId());
			glErrorCheck(gl, "addTexture-bind");
			
			//SETTINGS
			// Scale up if the texture is smaller.
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			// Scale down if the mesh is smaller.
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			// Clamp to edge behaviour at edge of texture (repeats last pixel)
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
			glErrorCheck(gl, "addTexture-setting");
	
			// resolve
			bitmap = resolveBitmap(bitmap);
			if(bitmap != null && !bitmap.isRecycled()){

				bitmap = resolveBitmap(bitmap);
				if(bitmap != null && !bitmap.isRecycled()){
					
					// Attach bitmap to current texture
					GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
					glErrorCheck(gl, "addTexture-upload");
					
					texture.setBitmap(null);
					// Bitmap Recycle
					bitmap.recycle();
					return;
				}
			}
			
			texture.setVisible(false);
			if(handler != null) handler.glError(new Exception("Out Of Memory "+texture.getName()+" ("+bitmap.getWidth()+"x"+bitmap.getHeight()+") / screen "+getWidth()+"x"+getHeight()));
			textureLoadStatus = TEXTURE_IGNORE;
		}
		
	}
	
	private Bitmap resolveBitmap(Bitmap bitmap)
	{
		Bitmap origin = bitmap;
		Bitmap result = null;
		
		int targetWidth = powerWidth(bitmap.getWidth());
		int targetHeight = powerWidth(bitmap.getHeight());
		
		if(targetWidth==bitmap.getWidth()&&targetHeight==bitmap.getHeight()) return bitmap;
		
		do{
			try{
		
				result = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
				bitmap.recycle();
				return result;
			
			}catch(OutOfMemoryError e){
				targetWidth = targetWidth >> 1;
				targetHeight = targetHeight >> 1;
				if(textureLoadStatus == TEXTURE_SUCCESS) textureLoadStatus = TEXTURE_DOWN_SIZE;
				System.gc();
			}catch(Exception e){
				if(handler != null) handler.glError(e);
				return bitmap;
			}
		}while(targetWidth>0 && targetHeight>0);

		return null;
	}
	public static int powerWidth(int width)
	{
		for(int i=0, targetSize=0;targetSize < support_texture_size;i++){
			targetSize = 2<<i;
			if(targetSize >= width){
				return targetSize;
			}
		}
		return support_texture_size;
	}
	public static int powerWidthUnder(int width)
	{
		for(int i=0, targetSize=support_texture_size;targetSize>2;i++){
			targetSize = support_texture_size>>i;
			if(targetSize <= width){
				return targetSize;
			}
		}
		return support_texture_size;
	}
	public static boolean pleasePowered(int width, int height)
	{
		return powerWidth(width)!=width || powerWidth(height)!=height;
	}
	
	/**************************************************************************
	 * 
	 * @DRAWING
	 * 
	 *************************************************************************/
	
	public void drawBatch(GL10 gl){
		
		for(Sprite sprite:sprites)
		{
			drawSprite(gl, sprite);
		}
	}
	
	private void drawSprite(GL10 gl, Sprite sprite)
	{
		Texture texture;
		if(!sprite.isVisible()) return;

		if(sprite instanceof CustomDraw)
		{
			( (CustomDraw) sprite ).glDraw(gl);
			return;
		}
		
		gl.glPushMatrix();
		try{
//			glErrorCheck(gl, "batch-push");
		
			gl.glTranslatef(sprite.getGlTranslateX(), sprite.getGlTranslateY(), 0f);
			gl.glScalef(sprite.getGlScale(), sprite.getGlScale(), 1f);
//			glErrorCheck(gl, "batch-translate");
	
			if(sprite instanceof SpriteGroup)
			{
				SpriteGroup group = (SpriteGroup) sprite;
				for(int i=0; i<group.size(); i++)
				{
					drawSprite(gl, group.get(i));
				}
			}
			else
			{

				// DRAW COMMAND
				gl.glColor4f(sprite.getR(), sprite.getG(), sprite.getB(), sprite.getA());

				if(sprite.getTexture()!= null){
					texture = sprite.getTexture();
					
					// Tell OpenGL where our texture is located
					gl.glBindTexture(GL10.GL_TEXTURE_2D, texture.getTextureId());
					
					// Telling OpenGL where our textureCoords are.
					gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture.getTextureBuffer());
					
				}
				else
				{
					gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
				}
				
				// Vertex
				gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sprite.getVertexBuffer());
				
				//Index
				gl.glDrawElements(GL10.GL_TRIANGLES, sprite.getIndices().length, GL10.GL_UNSIGNED_SHORT, sprite.getIndexBuffer());
//				glErrorCheck(gl, "batch-draw");
	
			}
		}finally{
			gl.glPopMatrix();
		}
	}
	
	/**************************************************************************
	 * 
	 * @GL
	 * 
	 *************************************************************************/

	@Override
	public void onDrawFrame (GL10 gl) {
		try{
			if (maxFPS != 0) {
				long elapsed = System.currentTimeMillis() - lastTime;
				int minElapsed = 1000 / maxFPS;
				if (elapsed < minElapsed) {
					try {
						Thread.sleep(minElapsed - elapsed);
					} catch (InterruptedException e) {
						Log.e(TAG, "Error sleeping thread, to cap FPS.", e);
					}
				}
				lastTime = System.currentTimeMillis();
			}
			
			while(!drawer.isDraw()){
				Thread.sleep(10);
			}
	
			drawer.onDrawBeforeFrame(gl, this);
	
			// Clears the screen and depth buffer.
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			// Replace the current matrix with the identity matrix
			gl.glLoadIdentity();
			// Rotate world by 180 around x axis so positive y is down (like canvas)
			gl.glRotatef(-180, 1, 0, 0);
	
			drawer.onDrawFrame(gl, this);
			
			drawBatch(gl);
			
			drawer.onDrawAfterFrame(gl, this);

			drawCount++;
			
		}catch(Exception e){
			if(handler != null){
				handler.glError(e);
			}
		}
	}
	
	@Override
	public void onSurfaceCreated (GL10 gl, EGLConfig config) {
		try{
			// SETTINGS
			// Set the background color to black ( rgba ).
			gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	
			// DRAWING SETUP
			// NOTES: As we are always drawing with textures and viewing our
			// elements from the same side all the time we can leave all these
			// settings on the whole time
			// Enable face culling.
			gl.glEnable(GL10.GL_CULL_FACE);
			// What faces to remove with the face culling.
			gl.glCullFace(GL10.GL_BACK);
			// Enabled the vertices buffer for writing and to be used during
			// rendering.
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			glErrorCheck(gl, "surfacecreate-cullFace");
			
			// Telling OpenGL to enable textures.
			gl.glEnable(GL10.GL_TEXTURE_2D);
			// Tell OpenGL to enable the use of UV coordinates.
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			glErrorCheck(gl, "surfacecreate-texture_2d");
			
			// Blending on
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			glErrorCheck(gl, "surfacecreate-blending");
			
			// null gen
			int[] testGenId = new int[1];
			gl.glGenTextures(1, testGenId, 0);
			glErrorCheck(gl, "surfacecreate-null-gen");
			
			initTextureSize(gl);
			
			Texture texture = new Texture(Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888));
			texture.setTextureId(testGenId[0]);
			addTexture(gl, texture);
			glErrorCheck(gl, "surfacecreate-test-texture");

		}catch(Exception e){
			if(handler != null){
				handler.glError(e);
			}
		}
	}

	private void initTextureSize(GL10 gl)
	{
		IntBuffer val = IntBuffer.allocate(1);
		gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, val);
		support_texture_size = val.get();
		System.out.println("Support Texture Size : "+support_texture_size);
	}

	@Override
	public void onSurfaceChanged (GL10 gl, int width, int height) {
		try{
			// Stores width and height
			this.width = width;
			this.height = height;
			// Sets the current view port to the new size.
			gl.glViewport(0, 0, width, height);
			// Select the projection matrix
			gl.glMatrixMode(GL10.GL_PROJECTION);
			// Reset the projection matrix
			gl.glLoadIdentity();
			// Orthographic mode for 2d
			gl.glOrthof(0, width, -height, 0, -1, 8);
			// Select the modelview matrix
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			// Reset the modelview matrix
			gl.glLoadIdentity();
			glErrorCheck(gl, "surfacechange");
		}catch(Exception e){
			if(handler != null){
				handler.glError(e);
			}
		}
	}

	/**************************************************************************
	 * 
	 * @exception
	 * 
	 *************************************************************************/
	
	private void glErrorCheck(GL10 gl, String text){
		glErrorCheck(gl, text, false);
	}
	
	private void glErrorCheck(GL10 gl, String text, boolean send){
		if(handler != null){
			handler.glTracking("glTrack", text, "", drawCount, send);
			
			int error = gl.glGetError();
			if(error != GL10.GL_NO_ERROR) {
				handler.glError(new GLRuntimeException(error, text));
			}
		}
		
	}
	
	public static class GLRuntimeException extends RuntimeException{
		
                private static final long serialVersionUID = 1L;
                
                public GLRuntimeException(int error, String text)
                {
		        super(error+": "+GLRuntimeException.getGLErrorString(error)+" : "+text);
		}
                
		public static String getGLErrorString(int error){
		        String errorString = GLU.gluErrorString(error);
		        if ( errorString == null ) {
		            errorString = "Unknown error 0x" + Integer.toHexString(error);
		        }
		        return errorString;
		}
	}

	/**************************************************************************
	 * 
	 * @GETSET
	 * 
	 *************************************************************************/

	public Drawer getDrawer () {
		return drawer;
	}
	public void setDrawer (Drawer drawer) {
		this.drawer = drawer;
	}
	public int getWidth () {
		return width;
	}
	public void setWidth (int width) {
		this.width = width;
	}
	public int getHeight () {
		return height;
	}
	public void setHeight (int height) {
		this.height = height;
	}
	public int getMaxFPS () {
		return maxFPS;
	}
	public void setMaxFPS (int maxFPS) {
		this.maxFPS = maxFPS;
	}
	public ArrayList<Sprite> getSprites () {
		return sprites;
	}
	public GLErrorListener getHandler () {
		return handler;
	}
	public void setHandler (GLErrorListener handler) {
		this.handler = handler;
	}

	public int getTextureLoadStatus () {
		return textureLoadStatus;
	}

	public void setTextureLoadStatus (int textureLoadStatus) {
		this.textureLoadStatus = textureLoadStatus;
	}


}
