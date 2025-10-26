package sions.android.sionsbeat.game.view;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.microedition.khronos.opengles.GL10;

import sions.android.sionsbeat.GameActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.game.GameModeInterface;
import sions.android.sionsbeat.game.GameModeInterface.TouchEvent;
import sions.android.sionsbeat.game.sprite.FeverSprite;
import sions.android.sionsbeat.game.sprite.NumberSprite;
import sions.android.sionsbeat.template.GameNote;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.zresource.ZAnimation;
import sions.android.sionsbeat.zresource.ZResource;
import sions.android.spritebatcher.Drawer;
import sions.android.spritebatcher.GLErrorListener;
import sions.android.spritebatcher.Sprite;
import sions.android.spritebatcher.SpriteBatcher;
import sions.android.spritebatcher.SpriteGroup;
import sions.android.spritebatcher.Texture;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * 게임을 진행할 때, 사용자와 소통하는 기능을 담당한다.
 * 1. 사용자가 패드를 터치 했을 때 이벤트에 대해서 GameMode에 알린다.
 * 2. GameMode에서 터치에 대한 반응 및 애니메이션 표현에 대해서 알려주면 그것을 이행한다.
 * 
 * @author sunulo
 */
public class GameGLPadView extends GLSurfaceView implements Drawer, GamePad, CapturedView {

	private static final String TAG = "pad";

	public static final int PRESS_MODE_PRESS = 0;
	public static final int PRESS_MODE_SIZE = 1;
	
	private SpriteBatcher sb;
	protected GameModeInterface gameMode;

	protected int width;
	protected int height;
	
	private int updateWidth;
	private int updateHeight;
	
	private int comboCount;
	private long comboCountTime;

	private DisplayMetrics displayMetrics;
	private SurfaceHolder holder;
	
	protected int MARGIN_WIDTH;
	protected int BLOCK_WIDTH;
	protected int COMBO_TEXT_SIZE;
	protected int COMBO_TEXT_COLOR;
	protected int COMBO_STROKE_WIDTH;
	protected int COMBO_STROKE_COLOR;

	private boolean fever;
	private float gameTouchPadding = 1f;
	
	private RectF[] touchRange = new RectF[16];
	
	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/
	public GameGLPadView(Context context) {
		super(context);
		initialize(context, null);
	}

	public GameGLPadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context, attrs);
	}

	private void initialize(Context context, AttributeSet attrs){
		
		this.holder = getHolder();
		this.holder.addCallback( this );
		
		this.displayMetrics = context.getResources().getDisplayMetrics();

		if(attrs!=null){
			TypedArray array = context.obtainStyledAttributes( attrs, R.styleable.GamePadView );
			if ( array != null )
			{
				COMBO_TEXT_SIZE = array.getDimensionPixelOffset( R.styleable.GamePadView_comboTextSize, 0 );
				COMBO_TEXT_COLOR = array.getColor( R.styleable.GamePadView_comboTextColor, 0 );
				COMBO_STROKE_WIDTH = array.getDimensionPixelOffset( R.styleable.GamePadView_comboStrokeWidth, 0 );
				COMBO_STROKE_COLOR = array.getColor( R.styleable.GamePadView_comboStrokeColor, 0 );
				array.recycle();
			}
		}
		
		setRenderer(sb = new SpriteBatcher(context, this));
		sb.setMaxFPS(GameOptions.get(context).getSettingInt(GameOptions.OPTION_GAME_FPS));
		sb.setHandler(new GLErrorListener() {
			
			@Override
			public void glTracking(String category, String action, String action2, int value, boolean send) {
				ErrorController.tracking(getContext(), category, action, action2, value, send);
			}
			
			@Override
			public void glError (Exception e) {
				ErrorController.error(10, e);
			}
		});
		
	}


	/**************************************************************************
	 * 
	 * @View
	 * 
	 *************************************************************************/

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int width = getMeasuredWidth();
		int height = (int)(getMeasuredWidth()*1.05F);
		
		MARGIN_WIDTH = (height - width) /2;
		BLOCK_WIDTH = width / 4;
		
		onTouchrangeSetup(width);
		setMeasuredDimension(width, height);
	}
	
	private void onTouchrangeSetup(int width){
	        float blockSize = width * 0.25f;
	        float halfSize = blockSize * 0.5f;
	        float padSize = halfSize * gameTouchPadding;
		
	        RectF rect;
		for(int i=0,x,y; i<16; i++){
			touchRange[i] = rect = new RectF();
			
			x = i % 4;
			y = i / 4;
			
	        	rect.top = (blockSize*y) + halfSize - padSize + MARGIN_WIDTH;
	        	rect.bottom = (blockSize*y) + halfSize + padSize + MARGIN_WIDTH;
	        	rect.left = (blockSize*x) + halfSize - padSize;
	        	rect.right = (blockSize*x) + halfSize + padSize;
		}
	}
	
	/**************************************************************************
	 * 
	 * @GL_Capture
	 * 
	 *************************************************************************/

	private Bitmap capturedBitmap;
	private boolean capturePlease;
	@Override
        public Bitmap onCapturBitmap (Canvas canvas) {
		capturedBitmap = null;
		capturePlease = true;
		setDirty(true);
		
		while(capturedBitmap == null && capturePlease){
			try{
				Thread.sleep(10);
			}catch(Throwable e){}
		}
	        return capturedBitmap;
        }

	@Override
        public void onDrawAfterFrame (GL10 gl, SpriteBatcher spriteBatcher) {
		if(capturePlease)
		{
			try {
				int bitmapBuffer[] = new int[width * height];
				int bitmapSource[] = new int[width * height];
				IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
				intBuffer.position(0);
				
				gl.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
				int offset1, offset2;
				for (int i = 0; i < height; i++) {
					offset1 = i * width;
					offset2 = ( height - i - 1 ) * width;
					for (int j = 0; j < width; j++) {
						int texturePixel = bitmapBuffer[offset1 + j];
						int blue = ( texturePixel >> 16 ) & 0xff;
						int red = ( texturePixel << 16 ) & 0x00ff0000;
						int pixel = ( texturePixel & 0xff00ff00 ) | red | blue;
						bitmapSource[offset2 + j] = pixel;
					}
				}

				capturedBitmap = Bitmap.createBitmap(bitmapSource, width, height, Bitmap.Config.ARGB_8888);
				
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (GLException e) {
				ErrorController.error(10, e);
			}

			capturePlease = false;
		}
        }

	/**************************************************************************
	 * 
	 * @GL
	 * 
	 *************************************************************************/

	protected Texture[] numberTextures;

	private Texture touchTexture;
	private Texture albumCover;
	private Texture feverTexture;
	
	private SpriteGroup gamePad = new SpriteGroup();
	private SpriteGroup gamePadTouch = new SpriteGroup();
	private NumberSprite spriteCombos;
	private FeverSprite spriteFever;
	private Sprite spriteBackgroundFever;
	private Sprite spriteBackground;
	private SpriteGroup spriteOverlay = new SpriteGroup();
	
	private HashMap<Object, Texture> bitmapIds;

	public void onDrawBeforeFrame(GL10 gl, SpriteBatcher sb)
	{
		if(this.gameMode == null || this.gameMode.getResource() == null) return;
		
		this.width = sb.getWidth();
		this.height = sb.getHeight();

		if(bitmapIds == null){
			/**@TRACKING**/ErrorController.tracking(getContext(), this, "initializeTexture", "", 0);
			initializeTexture(gl, sb);
			/**@TRACKING**/ErrorController.tracking(getContext(), this, "initializeSprite", "", 0);
			initializeSprite(gl, sb);
		}
	}
	
	private StringBuilder textureTracking;
	protected void initializeTexture(GL10 gl, SpriteBatcher sb){
		
		ZResource res = getGameMode().getResource();
		
		bitmapIds = new HashMap<Object, Texture>();

		//-- 배경
		{
			Bitmap bgBitmap = createBackground();
			albumCover = createTexture(bgBitmap, "background");
		}
		
		//-- 피버
		{
			int hh = SpriteBatcher.powerWidth(height/2);
			Bitmap bitmap = Bitmap.createBitmap(2, hh, Bitmap.Config.ARGB_8888);
			
			int[] pixels = new int[hh*2];
			for(int i=0, hi=0; i<hh; i++){
				float percent = Math.min(1f, (i * 1.2f) / hh); //반절정도만 opacity처리
				int color = (((int)(percent*255) << 0x18) &0xFF000000) | 0xFFFFFF;
				pixels[hi++] = color;
				pixels[hi++] = color;
			}
			
			bitmap.setPixels(pixels, 0, 2, 0, 0, 2, hh);
			feverTexture = createTexture(bitmap, "background");
		}
		
		//-- 콤보용
		{
			numberTextures = new Texture[10];
			numberTextures[0] = createTexture(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.number_0), "n0");
			numberTextures[1] = createTexture(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.number_1), "n1");
			numberTextures[2] = createTexture(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.number_2), "n2");
			numberTextures[3] = createTexture(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.number_3), "n3");
			numberTextures[4] = createTexture(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.number_4), "n4");
			numberTextures[5] = createTexture(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.number_5), "n5");
			numberTextures[6] = createTexture(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.number_6), "n6");
			numberTextures[7] = createTexture(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.number_7), "n7");
			numberTextures[8] = createTexture(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.number_8), "n8");
			numberTextures[9] = createTexture(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.number_9), "n9");
		}
		
		//-- 노트
		Entry<String, Object>[] entrys = res.getMap().entrySet().toArray(new Entry[res.getMap().size()]);
		for(Entry<String, Object> e : entrys){
			if(e.getValue() instanceof ZAnimation){
				ZAnimation anim = (ZAnimation) e.getValue();
				anim.prepare(0, 0);
				
				Bitmap bitmap;
				String name;
				try{
					Bitmap[] bitmaps = anim.getBitmaps();
					if(bitmaps == null) throw new NullPointerException(anim.getKey()+" / "+GameOptions.get(getContext()).getMarkerFile());
					for(int i=0, len=bitmaps.length; i<len; i++)
					{
						bitmap = bitmaps[i];
						createTexture(bitmap, anim.getKey()+":"+i);
					}
				}catch(Throwable ex){
					ErrorController.error(10, ex);
				}
			}
		}

		//-- 터치
		{
			Bitmap bitmap = res.getBitmap("touch");
			if(bitmap == null){
				bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.touch);
			}
			touchTexture = createTexture(bitmap, "touch");
		}
		
		if(textureTracking!=null){
			ErrorController.tracking(getContext(), this, "initializeBitmaps", textureTracking.toString(), 0, true);
		}

		sb.addTextures(gl, bitmapIds.values());
		
	}
	
	protected Texture createTexture(Bitmap bitmap, String name){
		if(bitmap == null){
			if(textureTracking == null){
				textureTracking = new StringBuilder().append(name);
			}else{
				textureTracking.append("/").append(name);
			}
			return null;
		}
		
		Texture texture = bitmapIds.get(bitmap);
		if(texture == null){
			bitmapIds.put(bitmap, texture = new Texture(bitmap));
			texture.setName(name);
		}
		return texture;
	}
	
	protected void initializeSprite(GL10 gl, SpriteBatcher sb)
	{
		Sprite temp;
		
		// 배경
		sb.getSprites().add( spriteBackground = new Sprite(albumCover, new RectF(0,0, width,height)) );

		sb.getSprites().add( spriteBackgroundFever= new Sprite(null, new RectF(0,0,width,height), 0x77FF7F00));
		sb.getSprites().add( spriteFever= new FeverSprite(feverTexture, width, height ));
		spriteBackgroundFever.setVisible(false);
		spriteFever.setVisible(false);
		
		// 콤보
		sb.getSprites().add( spriteCombos = new NumberSprite(numberTextures) );
		spriteCombos.setTextSize(COMBO_TEXT_SIZE);
		
		// 게임터치
		sb.getSprites().add(gamePad);
		sb.getSprites().add(gamePadTouch);
		for(int i=0; i<16; i++){
			gamePad.add( temp = new Sprite(null, new RectF(0,0,0,0) ));
			gamePadTouch.add( temp = new Sprite(touchTexture, new RectF(0,0,0,0) ));
		}

		// Overlay
		sb.getSprites().add(spriteOverlay);
		for(int i=0; i<8; i++){
			spriteOverlay.add( temp = new Sprite(null, new RectF(0, 0, 0, 0), 0xAA000000 ));
		}
	}
	

	/**************************************************************************
	 * 
	 * @Draw
	 * 
	 *************************************************************************/

	@Override
	public boolean isDraw(){
		return true;
	}

	@Override
	public void onDrawFrame(GL10 gl, SpriteBatcher sb)
	{
		if(bitmapIds == null){
			return;
		}

		long time = gameMode.getSysTime();
		
		updateBounds();
		
		updateNotes(time);
		updateCombo(time);
		
	}
	
	/**
	 * 위치를 지정한다.
	 */
	protected boolean updateBounds(){
		if(updateWidth == width && updateHeight == height) return false;
		
		updateWidth = width;
		updateHeight = height;

		// 배경
		spriteBackground.setBounds(0,0,width,height);
		spriteBackgroundFever.setBounds(0,0,width,height);
		spriteFever.setGlTranslateX(width*0.5f);
		spriteFever.setGlTranslateY(height*0.5f);
		
		// 게임터치
		for(int i=0; i<gamePad.size(); i++){

			int x = ( i % gameMode.getNoteWidth() ) * BLOCK_WIDTH;
			int y = ( i / gameMode.getNoteWidth() ) * BLOCK_WIDTH + MARGIN_WIDTH;

			gamePad.get(i).setBounds(x, y, x+BLOCK_WIDTH, y+BLOCK_WIDTH);
			gamePadTouch.get(i).setBounds(x, y, x+BLOCK_WIDTH, y+BLOCK_WIDTH);
//			gamePad.get(i).setBounds(touchRange[i]);
//			gamePadTouch.get(i).setBounds(touchRange[i]);
		}

		// Overlay
		int idx = 0;
		spriteOverlay.get(idx++).setBounds(0, 0, width, MARGIN_WIDTH); // 위쪽
		spriteOverlay.get(idx++).setBounds(0, height-MARGIN_WIDTH, width, height); //아래쪽
		
		float halfWidth = COMBO_STROKE_WIDTH *0.5f;
		for(int y=1; y<4; y++){
			int yy = y*BLOCK_WIDTH + MARGIN_WIDTH;
			spriteOverlay.get(idx++).setBounds(0, yy-halfWidth, width, yy+halfWidth);
		}

		for(int x=1; x<4; x++){
			int xx = x*BLOCK_WIDTH;
			spriteOverlay.get(idx++).setBounds(xx-halfWidth, MARGIN_WIDTH, xx+halfWidth, height-MARGIN_WIDTH);
		}
		
		return true;
	}
	
	/**
	 * 노트 타이밍을 조정한다
	 */
	private void updateNotes(long time){

		GameNote[] notes = gameMode.getGameNotes();
		GameNote note;
		Sprite sprite, touch;
		
		for(int i=0; i<notes.length; i++)
		{
			note = notes[i];
			sprite = gamePad.get(i);
			touch = gamePadTouch.get(i);
			
			if(note.getAnim() != null && note.getAnim().isPlay(time)){
				Bitmap bm = note.getAnim().getBitmap(time);
				if(bm != null){
					sprite.setTexture(bitmapIds.get(bm));
					sprite.setVisible(true);
				}else{
					sprite.setVisible(false);
				}
			}else{
				sprite.setVisible(false);
			}
			
			if((time - note.getTouchTime()) < gameMode.getOption().ANIMATION_TOUCH){
				float alpha = 1f - (float)(time - note.getTouchTime()) / gameMode.getOption().ANIMATION_TOUCH ;
				touch.setA(Math.min(1f, Math.max(0f, alpha)));
				touch.setVisible(true);
			}else{
				touch.setVisible(false);
			}
		}
		
	}
	
	private void updateCombo(long time){

		if(comboCount < 5){
			spriteCombos.setVisible(false);
			return;
		}else{
			spriteCombos.setVisible(true);	
		}
		
		float size = 1.5f - Math.min( 0.5f, (time-comboCountTime)*0.005f);

		spriteCombos.setNumber(comboCount);
		spriteCombos.setGlScale(size);
		spriteCombos.setGlTranslateX( width/2);
		spriteCombos.setGlTranslateY( (height*0.5f) - COMBO_TEXT_SIZE );
		
		
//		if(comboCount < 5){
//			spriteCombo.setVisible(false);
//			return;
//		}else{
//			spriteCombo.setVisible(true);	
//		}
//		
//		float size = 1.5f - Math.min( 0.5f, (time-comboCountTime)*0.005f);
//		
//		spriteCombo.setText(String.valueOf(comboCount));
//		spriteCombo.setGlScale(size);
//		spriteCombo.setGlTranslateX( width/2);
//		spriteCombo.setGlTranslateY( (height*0.5f) - COMBO_TEXT_SIZE );
		
	}
	
	
	
	/**************************************************************************
	 * 
	 * @LiefCycle
	 * 
	 *************************************************************************/
	
	
	private Bitmap createBackground(){
		Bitmap src = null;
		try{
			src = BitmapFactory.decodeFile(gameMode.getGameData().getSong().getArt());
			if(src == null) throw new NullPointerException();
		}catch(Throwable e){
			src = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.empty_album);
		}

		int w = width;
		int h = height;
		
		do{
			try{
				Bitmap result = Bitmap.createScaledBitmap(src, width, height, true);
				Canvas canvas = new Canvas(result);
		
				Paint paint = new Paint();
				paint.setStyle(Paint.Style.FILL);
				paint.setColor(0xAA000000);
				
				canvas.drawRect(0, 0, width, height, paint);
				return result;
				
			}catch(OutOfMemoryError e){
				w *= 0.7f;
				h *= 0.7f;
			}
		}while(w>10 && h>10);
		
		return null;
	}

	/**************************************************************************
	 * 
	 * @EVENTs
	 * 
	 *************************************************************************/
	
	private TouchEvent touchEvent = new TouchEvent(0, 0, 0);
	
	@Override
	public boolean onTouchEvent ( MotionEvent event )
	{
		if(gameMode == null) return false;
		
		try
		{
			int pointcount = event.getPointerCount();
			int pointid, index;
			float x, y;

			int actionindex = (event.getAction() & 0xff00) >> 8;
			int actionmasked = event.getAction() & 0xFF;

			switch ( actionmasked )
			{
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					{
						index = actionindex;
						pointid = event.getPointerId( index );
						x = event.getX( index );
						y = event.getY( index );

						touchEvent.set((int)x, (int)y, index, true);
						touch( touchEvent );
					}
					return true;
				case MotionEvent.ACTION_MOVE:
					{
						for ( int i = 0; i < pointcount; i++ )
						{
							pointid = event.getPointerId( i );
							x = event.getX( i );
							y = event.getY( i );

							touchEvent.set((int)x, (int)y, i, true);
							touch( touchEvent );
						}
					}
					return true;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					{
						index = actionindex;
						pointid = event.getPointerId( index );
						x = event.getX( index );
						y = event.getY( index );

						touchEvent.set((int)x, (int)y, index, false);
						touch( touchEvent );
					}
					return true;
			}

		} catch ( Exception e )
		{
			ErrorController.error( 8, e );
		}
		return false;
	}

	/**************************************************************************
	 * 
	 * @ACTION
	 * 
	 *************************************************************************/
	
	private void touch(TouchEvent event)
	{
		Log.d("test",event.getX()+" / "+event.getY());
		float x = event.getX();
		float y = event.getY();
		for(int i=0; i<touchRange.length; i++){
			if(touchRange[i].contains(x, y)){
				event.setX(i);
				gameMode.onAction( event );
			}
		}
	}
	
	public void doCombo(int combo, long currTime){
		comboCount = combo;
		comboCountTime = currTime;
	}
	
	
	/**************************************************************************
	 * 
	 * @GETSET
	 * 
	 *************************************************************************/
	
	public int getDipToPx(float dip){
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics);
	}
	
	public GameModeInterface getGameMode ()
	{
		return gameMode;
	}

	public void setGameMode ( GameModeInterface gameMode )
	{
		this.gameMode = gameMode;
	}

	public boolean isFever () {
		return fever;
	}

	public float getGameTouchPadding () {
		return gameTouchPadding;
	}

	@Override
        public void setGameTouchPadding (float gamePadTouch) {
		this.gameTouchPadding = gamePadTouch;
        }

	public void setFever (boolean fever) {
		this.fever = fever;
		if(this.spriteFever != null)
		{
			this.spriteBackgroundFever.setVisible(fever);
			if(getContext() instanceof GameActivity && ((GameActivity)getContext()).isGraphicFeverParticle()){
				this.spriteFever.setVisible(fever);
				if(fever){
					this.spriteFever.start();
				}else{
					this.spriteFever.stop();
				}	
			}
		}
	}
	
	public void clear () {
		comboCount = 0;
        }
	
	public void setDirty(boolean dirty){
		
	}

	class TouchInfo {
		
		int x, y;
		int half;
		long time = -3000;
		
	}


	
}
