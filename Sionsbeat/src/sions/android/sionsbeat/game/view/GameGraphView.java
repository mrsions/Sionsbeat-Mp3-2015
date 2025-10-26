package sions.android.sionsbeat.game.view;

import java.util.Arrays;
import java.util.logging.ErrorManager;

import sions.android.sionsbeat.R;
import sions.android.sionsbeat.game.GameData;
import sions.android.sionsbeat.game.GameMode;
import sions.android.sionsbeat.game.listener.GameModeListener;
import sions.android.sionsbeat.template.Note;
import sions.android.sionsbeat.utils.ErrorController;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * 게임 진행에 대한 내용을 비주얼적으로 표현한다.
 * 1. 게임 노트를 통해서 비주얼 블럭을 만든다.
 * 2. 진행사항을 나타낸다.
 * 3. 진행된 단계 임계치를 나타낸다.
 * 
 * @author sunulo
 */
public class GameGraphView extends View implements Runnable{

	private static final String TAG = "graph";
	private static final int PEAK_DEF = 10;

	private GameMode gameMode;
	private int width;
	private int height;
	
	private int duration = 60000;
	private int blockIndex = -1;
	private int[] blocks = new int[60];
	private float[] blockRates = new float[60];
	
	private DisplayMetrics displayMetrics;
	private SurfaceHolder holder;
	private Thread thread;

	private int		COLOR_NONE = 0xFFFFFFFF,
				COLOR_FAILED = 0xFFF2575F,
				COLOR_GOOD = 0xFF00a3de,
				COLOR_PERFECT = 0xFFddd500,
				COLOR_SEPARATOR = 0xFFFFFFFF;
	
	private int		MARGIN_WIDTH,
				STROKE_WIDTH;
	
	private float	BLOCK_WIDTH,
				PEAK = PEAK_DEF;
	
	private Bitmap graph_time_line;
	private Bitmap bufferBitmap;
	private Canvas bufferCanvas;
	private boolean firstBuild;
	
	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/
	public GameGraphView(Context context) {
		super(context);
		initialize(context, null);
	}

	public GameGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context, attrs);
	}
	
	public GameGraphView ( Context context, AttributeSet attrs, int defStyle )
	{
		super( context, attrs, defStyle );
		initialize(context, attrs);
	}

	private void initialize(Context context, AttributeSet attrs){
		
		this.displayMetrics = context.getResources().getDisplayMetrics();
		
		TypedArray array;
		if(attrs != null && (array= context.obtainStyledAttributes( attrs, R.styleable.GameGraphView )) != null){
			this.MARGIN_WIDTH = array.getDimensionPixelOffset( R.styleable.GameGraphView_marginWidth, 10 );
			this.STROKE_WIDTH = array.getDimensionPixelOffset( R.styleable.GameGraphView_strokeWidth, 1 );
		}
		
		if(isInEditMode())
		{
			for(int i=0; i<blocks.length; i++)
			{
				blockRates[i] = (float) Math.abs( Math.sin( i/4.6f ) );
				blocks[i] = (int)( blockRates[i]*PEAK_DEF );
			}
			
			blocks[0] = 25;
			blocks[1] = 1;
			blocks[30] = 0;
			blocks[59] = PEAK_DEF;
			
		}

	}

	/**************************************************************************
	 * 
	 * @View
	 * 
	 *************************************************************************/

	private void threadStart(){
		if(gameMode == null) return;
		
		if(thread == null){
		        thread = new Thread(this, "GameGraphView");
		        thread.setDaemon(true);
		        thread.setPriority(Thread.MIN_PRIORITY);
		        thread.start();
		}
	}
	
	private void threadStop(){
		if(thread != null){
			Thread temp = thread;
			thread = null;
			while(true){
				try{
					temp.join();
					break;
				}catch(Throwable e){}
			}
		}
	}
	
	/**************************************************************************
	 * 
	 * @LiefCycle
	 * 
	 *************************************************************************/
	
	@Override
	protected void onAttachedToWindow () {
	        super.onAttachedToWindow();
        	threadStart();
	}
	
	@Override
	protected void onDetachedFromWindow () {
	        super.onDetachedFromWindow();
	        threadStop();
	}
	
	@Override
	public void run(){
		
		Activity context = (Activity) getContext();
		
//		if(graph_time_line == null){
//			graph_time_line = BitmapFactory.decodeResource(context.getResources(), R.drawable.graph_time_line);
//		}
		
		Runnable run = new Runnable(){
			public void run(){
				invalidate();
			}
		};

//		int len = 3;
//		
//		Bitmap buffer[] = new Bitmap[len];
//		Canvas canvas[] = new Canvas[len];
//		int index = 0;

		while(thread != null){
			try{
				
//				if(width == 0 || height == 0){
//					Thread.sleep(100);
//					continue;
//				}
//
//				if(buffer[0] == null || width != buffer[0].getWidth() || height != buffer[0].getHeight()){
//					synchronized(this){
//						bufferBitmap = null;
//						for(int i=0; i<len; i++){
//							if(buffer[i] != null) buffer[i].recycle();
//							buffer[i] = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//							canvas[i] = new Canvas(buffer[i]);
//						}
//					}
//				}
//				
//				//-- build
//				int idx = index%len;
//				
//				buffer[idx].eraseColor(Color.TRANSPARENT);
//				doDraw(canvas[idx]);
//				
//				bufferBitmap = buffer[idx];

				context.runOnUiThread(run);
				
				Thread.sleep(100);
			}catch(Throwable e){
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onMeasure ( int widthMeasureSpec, int heightMeasureSpec )
	{
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
		this.width = getMeasuredWidth();
		this.height = getMeasuredHeight();
		
		if(this.width > 5000) this.width = 0;
		if(this.height > 5000) this.height = 0;
		
	}

	@Override
	protected void onDraw ( Canvas canvas )
	{
		super.onDraw( canvas );
		try{
//			synchronized(this){
//				if(bufferBitmap != null){
//					canvas.drawBitmap(bufferBitmap, 0, 0, null);
//				}else{
					doDraw(canvas);
//				}
//			}
		}catch(Throwable e){
			ErrorController.error(10, e);
		}
	}
	
	private void doDraw ( Canvas canvas ){
		
		if(width == 0 || height == 0) return;
		
		if(graph_time_line == null){
			graph_time_line = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.graph_time_line);
		}

		int usableWidth = this.width - ( MARGIN_WIDTH* 2 ); // 마진을 제외하고 사용할 수 있는 공간
		int usableHeight = this.height - ( MARGIN_WIDTH* 2); // 마진을 제외하고 사용할 수 있는 공간
		
		if(usableHeight < 1) return;
		
		BLOCK_WIDTH = usableWidth/60f;
		
		float marginSize = ( ( usableWidth % BLOCK_WIDTH ) /2 ) + MARGIN_WIDTH; // 실제 사용 될 마진
		int blockHeightLength = (int) ( usableHeight / BLOCK_WIDTH ); // 블럭의 높이 갯수
		
		Paint strokeP = new Paint(Paint.ANTI_ALIAS_FLAG);
		strokeP.setStyle( Paint.Style.STROKE );
		strokeP.setColor( Color.BLACK );
		strokeP.setStrokeWidth( STROKE_WIDTH );
		
		Paint fillP = new Paint(Paint.ANTI_ALIAS_FLAG);
		fillP.setStyle( Paint.Style.FILL );
		
		RectF rect = new RectF();
		for(int i=0; i<this.blocks.length; i++)
		{
			int level = (int)( Math.ceil( Math.min(1, this.blocks[i] / PEAK) * blockHeightLength ) );
			float rate = blockRates[i] / blocks[i];

			if (rate < 0.1) {
				fillP.setColor(COLOR_NONE);
			} else if (rate < 0.5) {
				fillP.setColor(COLOR_FAILED);

			} else if (rate < 0.9) {
				fillP.setColor(COLOR_GOOD);
			} else {
				fillP.setColor(COLOR_PERFECT);
			}

			rect.left = marginSize + ( i * BLOCK_WIDTH );
			rect.right = rect.left + BLOCK_WIDTH;
			for(int j=0; j<level; j++)
			{
				rect.bottom = this.height - ( marginSize + ( j*BLOCK_WIDTH ) );
				rect.top = rect.bottom-BLOCK_WIDTH;
				
				canvas.drawRect( rect, fillP );
				canvas.drawRect( rect, strokeP );
			}
		}
		
		if(gameMode != null && graph_time_line != null){
			
			//-- resizing
			if(usableHeight != graph_time_line.getHeight()){
				int width = (int) ( ( (double) usableHeight / graph_time_line.getHeight() ) * graph_time_line.getWidth() );
				graph_time_line = Bitmap.createScaledBitmap(graph_time_line, Math.max(1, width), Math.max(1, usableHeight), true);
			}
			
			float time = Math.max(0, gameMode.getCurrentTime()-gameMode.getGameData().getStartOffset());
			
			//-- Separator
			
			float progressX = ( (time / gameMode.getGameData().getDuration()) * usableWidth );
			progressX += marginSize;
			progressX -= graph_time_line.getWidth()*0.5f;

			canvas.drawBitmap(graph_time_line, progressX, (this.height - marginSize - graph_time_line.getHeight() + 10  ), null);
//			canvas.drawLine( progressX, marginSize, progressX, marginSize + usableHeight, strokeP );
			
		}
		
	}
	
	/**************************************************************************
	 * 
	 * @ACTION
	 * 
	 *************************************************************************/

	public void setupBlocks(GameData gameData)
	{
		this.duration = gameData.getDuration();

		Arrays.fill( blocks, 0 );
		Arrays.fill( blockRates, 0 );
		
		PEAK = PEAK_DEF * ( duration/60000f );

		int idx;
		Note note;
		for(int i=0; i<gameData.getNotes().length; i++){
			note = gameData.getNotes()[i];
			idx =  (int) Math.min( blocks.length-1, ((double)(note.getTiming()-gameData.getStartOffset()) / duration ) * blocks.length );
			
			blocks[idx] += Integer.bitCount( note.getButton() );
		}
		
	}
	
	/**************************************************************************
	 * 
	 * @GETSET
	 * 
	 *************************************************************************/
	
	public int getDipToPx(float dip){
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics);
	}

	public GameMode getGameMode ()
	{
		return gameMode;
	}

	public void setGameMode ( GameMode gameMode )
	{
		this.gameMode = gameMode;
		if(gameMode != null){
			threadStart();	
		}else{
			threadStop();
		}
	}

	public float getBlockRates (int idx)
	{
		return blockRates[idx];
	}

	public void setBlockRates (int idx, float blockRates )
	{
		this.blockRates[idx] = blockRates;
	}

	public void addBlockRates (int idx, float blockRates )
	{
		this.blockRates[idx] += blockRates;
	}

	public float[] getBlockRates ()
	{
		return blockRates;
	}

	public void setBlockRates ( float[] blockRates )
	{
		this.blockRates = blockRates;
	}

	public void clear () {
	        // TODO Auto-generated method stub
	        
        }

}
