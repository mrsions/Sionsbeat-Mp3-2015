package sions.android.sionsbeat.maker;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

import sions.android.sionsbeat.R;
import sions.android.sionsbeat.game.GameModeInterface;
import sions.android.sionsbeat.game.view.CapturedView;
import sions.android.sionsbeat.maker.FFTVisualize.BitmapResult;
import sions.android.sionsbeat.template.Note;
import sions.android.sionsbeat.utils.ErrorController;
import sions.utils.PTC;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.OverScroller;

public class MakerTimelineView extends SurfaceView implements SurfaceHolder.Callback, Runnable, CapturedView{
	
	public static final int ANALYZE_COLOR = 0x00;
	public static final int ANALYZE_GRAYSCALE = 0x01;
	public static final int ANALYZE_NONE = 0x02;
	
	private static final String TAG = "timeline";
	
	private SurfaceHolder holder;
	private MakerGameMode gameMode;
	private GestureDetector mGestureDetector;
	private ScaleGestureDetector mScaleGestureDetector;
	private OverScroller mScroller;
	private FFTVisualize visualize;
	
	private Thread thread;
	
	private int startWidth;
	private int endWidth;
	
	private int width, height;
	private boolean dirty;
	private boolean dirtyNotes;
	
	private Bitmap background;
	private BitmapShader backgroundFill;
	private Paint backgroundPaint;
	private Matrix backgroundMatrix = new Matrix();
	
	private ColorMatrix grayscaleMatrix;
	private ColorMatrixColorFilter grayscaleMatrixFilter;
	private Paint grayscalePaint = new Paint(); 
	
	private Rect timelineRect;
	private Rect stempRect;
	private Rect scrollerRect;
	
	private int anaylzeStyle = ANALYZE_COLOR;

	private float SIZE_SCALE = 1;
	
	private int SIZE_MIN_SECOND = 100;
	private int SIZE_MAX_SECOND = 800;
	private int SIZE_SECOND = SIZE_MAX_SECOND;
	
	private int SIZE_BORDER = 7;

	private int SIZE_TIMELINE_NOTE_WIDTH = 30;
	private int SIZE_TIMELINE_NOTE_ACTIVE_WIDTH = 30;
	private int SIZE_TIMELINE_NOTE_RANGE_WIDTH = 30;
	private int SIZE_TIMELINE_NOTE_MARGIN = 5;
	private int SIZE_TIMELINE_BAR_WIDTH = 7;
	private int SIZE_TIMELINE_BAR2_WIDTH = 3;
	private int SIZE_TIMELINE_STEMP_SIZE = 30;
	private int SIZE_TIMELINE_STEMP_HEIGHT = 60;

	private int SIZE_SCROLL_HEIGHT = 100;
	private int SIZE_SCROLL_BAR_WIDTH = 7;
	private int SIZE_SCROLL_TEXT_MARGIN= 30;
	private int SIZE_SCROLL_TEXT = 30;

	private int SIZE_SCROLLER_BLOCK = 9;
	private int SIZE_SCROLLER_BLOCK_STROKE = 1;
	private int SIZE_SCROLLER_BLOCK_MARGIN = 5;

	private int COLOR_TIMELINE_NOTE = 0xFFFF8a28;
	private int COLOR_TIMELINE_NOTE_ACTIVE = 0x77FF8a28;
	private int COLOR_TIMELINE_NOTE_RANGE = 0x33FF0000;
	private int COLOR_TIMELINE_BAR = 0xFFFFFFFF;
	private int COLOR_TIMELINE_BAR2 = 0xAAFFFFFF;
	private int COLOR_TIMELINE_CURRENT_BAR = 0xFFff3230;
	private int COLOR_TIMELINE_STEMP = 0xAA000000;

	private int COLOR_SCROLL_BAR = 0xFFff3230;
	private int COLOR_SCROLL_BACKGROUND = 0xFF222222;
	private int COLOR_SCROLL_TEXT = 0xFF30c7ff;
	private int COLOR_SCROLLER_BLOCK = 0xAAFFFFFF;
	
	private String TEXT_START = "START";
	private String TEXT_END = "END";
	
	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/
	
	public MakerTimelineView (Context context)
        {
	        super(context);
	        initialize();
        }
	public MakerTimelineView (Context context, AttributeSet attrs)
        {
	        super(context, attrs);
	        
		if(attrs!=null){
			TypedArray array = context.obtainStyledAttributes( attrs, R.styleable.MakerTimelineView );
			if ( array != null )
			{
				SIZE_MAX_SECOND = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_MAX_SECOND, 0);
				SIZE_BORDER = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_BORDER, 0);
				SIZE_TIMELINE_NOTE_WIDTH = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_TIMELINE_NOTE_WIDTH, 0);
				SIZE_TIMELINE_NOTE_ACTIVE_WIDTH = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_TIMELINE_NOTE_ACTIVE_WIDTH, 0);
				SIZE_TIMELINE_NOTE_RANGE_WIDTH = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_TIMELINE_NOTE_RANGE_WIDTH, 0);
				SIZE_TIMELINE_NOTE_MARGIN = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_TIMELINE_NOTE_MARGIN, 0);
				SIZE_TIMELINE_BAR_WIDTH = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_TIMELINE_BAR_WIDTH, 0);
				SIZE_TIMELINE_BAR2_WIDTH = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_TIMELINE_BAR2_WIDTH, 0);
				SIZE_TIMELINE_STEMP_SIZE = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_TIMELINE_STEMP_SIZE, 0);
				SIZE_TIMELINE_STEMP_HEIGHT = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_TIMELINE_STEMP_HEIGHT, 0);
				SIZE_SCROLL_HEIGHT = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_SCROLL_HEIGHT, 0);
				SIZE_SCROLL_BAR_WIDTH = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_SCROLL_BAR_WIDTH, 0);
				SIZE_SCROLL_TEXT_MARGIN = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_SCROLL_TEXT_MARGIN, 0);
				SIZE_SCROLL_TEXT = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_SCROLL_TEXT, 0);
				SIZE_SCROLLER_BLOCK = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_SCROLLER_BLOCK, 0);
				SIZE_SCROLLER_BLOCK_STROKE = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_SCROLLER_BLOCK_STROKE, 0);
				SIZE_SCROLLER_BLOCK_MARGIN = array.getDimensionPixelOffset( R.styleable.MakerTimelineView_SIZE_SCROLLER_BLOCK_MARGIN, 0);
				array.recycle();
			}
		}
	        
	        initialize();
        }
	private void initialize(){
		this.holder = getHolder();
		this.holder.addCallback(this);
		
		try{
			this.background = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.notemake_background_pattern);
			this.backgroundFill = new BitmapShader(background, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
			this.backgroundPaint = new Paint();
			this.backgroundPaint.setStyle(Paint.Style.FILL);
			this.backgroundPaint.setColor(0xFFFFFFFF);
			this.backgroundPaint.setShader(backgroundFill);
			
			this.grayscaleMatrix = new ColorMatrix();
			this.grayscaleMatrix.setSaturation(0);
			this.grayscaleMatrixFilter = new ColorMatrixColorFilter(grayscaleMatrix);
			this.grayscalePaint.setColorFilter(grayscaleMatrixFilter);
			
		}catch(Throwable e){
			ErrorController.error(10, e);
		}
		
		this.mGestureDetector = new GestureDetector( getContext(), mGestureListener);
		this.mScaleGestureDetector = new ScaleGestureDetector( getContext(), mScaleGestureListener );
		this.mScroller = new OverScroller( getContext() );
		
	}
	


	/**************************************************************************
	 * 
	 * @LIFECYCLE
	 * 
	 *************************************************************************/
	
	@Override
        public void surfaceCreated (SurfaceHolder holder) {
		thread = new Thread(this, TAG+"Thread");
		thread.start();
        }
	@Override
        public void surfaceChanged (SurfaceHolder holder, int format, int width, int height) {
	        this.width = width;
	        this.height = height;
	        
	        this.scrollerRect = new Rect(0, height-SIZE_BORDER-SIZE_SCROLL_HEIGHT, width, height-SIZE_BORDER);
	        this.stempRect = new Rect(0, scrollerRect.top-SIZE_TIMELINE_STEMP_HEIGHT, width, scrollerRect.top);
	        this.timelineRect = new Rect(0,SIZE_BORDER, width, stempRect.top);

	        dirty = true;
        }
	@Override
        public void surfaceDestroyed (SurfaceHolder holder) {
	        Thread temp = thread;
	        thread = null;
	        
	        for(;;){
	        	try{
	        		temp.join();
	        		break;
	        	}catch(Throwable e){}
	        }
        }
	
	@Override
	public void run()
	{
		Bitmap buffer = null;
		Canvas bufferCanvas = null;
		
		int bWidth=0, bHeight=0;
		
		while(thread != null)
		{
			try{
				
				if(dirty)
				{
					dirty = false;
					if(bWidth != width || bHeight != height)
					{
						buffer = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
						bufferCanvas = new Canvas(buffer);
					}
					
					doDraw(bufferCanvas);
					
					synchronized(holder)
					{
						Canvas canvas = holder.lockCanvas();
						
						canvas.drawBitmap(buffer, 0, 0, null);
						
						try{
							holder.unlockCanvasAndPost(canvas);
						}catch(Throwable e){}
					}
				}
				
				Thread.sleep(50);
				
			}catch(Throwable e){
				ErrorController.error(10, e);
			}
		}
	}

	/**************************************************************************
	 * 
	 * @DRAW
	 * 
	 *************************************************************************/

	@Override
        public Bitmap onCapturBitmap (Canvas canvas) {
		doDraw(canvas);
	        return null;
        }

	private float oneTempo, systemTime, screenStartTime, screenEndTime;
	
	private RectF rect = new RectF();
	private RectF rect2 = new RectF();
	private Rect rect3 = new Rect();
	private ArrayList<SingleNote> drawNotes = new ArrayList<SingleNote>();
	private Paint paint = new Paint();
	private Paint stroke = new Paint();
	
	private Bitmap scrollerBitmap; 
	private int pid;
	
	public void doDraw(Canvas canvas)
	{
		
		/** @TIMINGCHECKER **/int pid = PTC.start();
		
		//-- draw Background
		drawBackground(canvas);
		/** @TIMINGCHECKER **/ PTC.restart(pid, "performance", "drawBackground Complete");

		//-- draw String
		drawStartEnd(canvas);
		/** @TIMINGCHECKER **/ PTC.restart(pid, "performance", "drawStartEnd Complete");
		baseCalculate();
		/** @TIMINGCHECKER **/ PTC.restart(pid, "performance", "baseCalculate Complete");

		//-- FFT
		drawFFT(canvas);
		/** @TIMINGCHECKER **/ PTC.restart(pid, "performance", "drawFFT Complete");
		
		//-- draw Line
		drawLine(canvas);
		/** @TIMINGCHECKER **/ PTC.restart(pid, "performance", "drawLine Complete");
		
		//-- draw timelineNotes
		drawNotesTimeline(canvas);
		/** @TIMINGCHECKER **/ PTC.restart(pid, "performance", "drawNotesTimeline Complete");
		drawNotesScroller(canvas);
		/** @TIMINGCHECKER **/ PTC.restart(pid, "performance", "drawNotesScroller Complete");
		
		//-- draw scroll pointer
		drawScrollPointer(canvas);
		/** @TIMINGCHECKER **/ PTC.restart(pid, "performance", "drawScrollPointer Complete");
		
		/** @TIMINGCHECKER **/ PTC.restart(pid, "performance", "end");
		
	}
	
	private void drawBackground(Canvas canvas)
	{
		paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		
		//-- draw Background
		if(backgroundFill != null && backgroundMatrix!= null){
			canvas.getMatrix().invert(backgroundMatrix);
			backgroundFill.setLocalMatrix(backgroundMatrix);
			
			canvas.drawRect(0, 0, width, height, this.backgroundPaint);
		}else{
			paint.setColor(Color.WHITE);
			canvas.drawRect(0, 0, width, height, this.paint);
		}
		
		//-- draw Rect
		paint.setColor(COLOR_SCROLL_BACKGROUND);
		
		canvas.drawRect(scrollerRect, paint);
		canvas.drawRect(timelineRect, paint);
	}
	
	private void drawStartEnd(Canvas canvas)
	{
		paint.setColor(COLOR_SCROLL_TEXT);
		paint.setTextSize(SIZE_SCROLL_TEXT);
		
		startWidth = (int)paint.measureText(TEXT_START);
		endWidth = (int)paint.measureText(TEXT_END);

		int y = height-(((SIZE_SCROLL_HEIGHT-SIZE_SCROLL_TEXT)/2)+SIZE_BORDER);
		canvas.drawText(TEXT_START, SIZE_SCROLL_TEXT_MARGIN, y, paint);
		canvas.drawText(TEXT_END, width-(SIZE_SCROLL_TEXT_MARGIN+endWidth), y, paint);

		startWidth += SIZE_SCROLL_TEXT_MARGIN*2;
		endWidth += SIZE_SCROLL_TEXT_MARGIN*2;
	}
	
	private void baseCalculate(){
		oneTempo = 1f / gameMode.getTempo();
		systemTime = gameMode.getSysTime()/1000f;
		screenStartTime = systemTime - ((float) startWidth / SIZE_SECOND);
		screenEndTime = screenStartTime + ( (float)width / SIZE_SECOND);

		SIZE_TIMELINE_NOTE_ACTIVE_WIDTH = (int)(SIZE_SECOND * 0.3f);
		SIZE_TIMELINE_NOTE_RANGE_WIDTH = (int)(SIZE_SECOND * 3f);
	}
	
	private void drawFFT(Canvas canvas)
	{
		if(anaylzeStyle == ANALYZE_NONE) return;
		
		BitmapResult br = visualize.getBitmap(screenStartTime*1000, screenEndTime*1000);
		if(br == null || br.getBitmap().length == 0) return;
		
		float gabWidth = (br.getGabTime()*0.001f) * SIZE_SECOND;
		float singleWidth = (visualize.getSinglePerSecond()*0.001f) * SIZE_SECOND;
		
		
		rect.top = timelineRect.top;
		rect.bottom = timelineRect.bottom;
		rect3.left = 0;
		rect3.top = 0;
		
		Bitmap bitmap;
		for(int i=0; i<br.getBitmap().length; i++){
			bitmap = br.getBitmap()[i]; 
			if(bitmap != null){
				
				rect3.right = bitmap.getWidth();
				rect3.bottom = bitmap.getHeight();
				
				rect.left = (singleWidth*i)+gabWidth;
				rect.right = rect.left+singleWidth;
				
				if(anaylzeStyle == ANALYZE_COLOR){
					canvas.drawBitmap(bitmap, rect3, rect, null);
				}else{
					canvas.drawBitmap(bitmap, rect3, rect, grayscalePaint);
				}
				
			}
		}
	}
	
	private void drawLine(Canvas canvas)
	{
		int tempo = this.gameMode.getTempo();
		int baseTempo = 4;
		if(tempo == 1000){
			tempo = 1; // free
			baseTempo = 1;
		}
		
		float size_bar = Math.max(1, SIZE_TIMELINE_BAR_WIDTH* SIZE_SCALE);
		float size_bar2 = SIZE_TIMELINE_BAR2_WIDTH* SIZE_SCALE;
		
		int startTime = (int)( Math.floor(Math.max(0, screenStartTime-oneTempo)) );
		int endTime = (int)( Math.ceil(Math.min(gameMode.getDuration(), screenEndTime+oneTempo)) );
		
		int gab = (int) ( (screenStartTime - startTime) * SIZE_SECOND );
		
		int currentTime = startTime;
		rect.top = timelineRect.top;
		rect.bottom = timelineRect.bottom;
		
		float size_second_tempo = (float)SIZE_SECOND/tempo;
		String text;
		for(int i=0, end=((endTime-startTime)*tempo)+1, tmod=tempo/baseTempo; i<=end; i++)
		{
			rect.left = (size_second_tempo*i ) - gab;
			rect2.left = rect.left;
			if(i%tmod == 0)
			{
				paint.setColor(COLOR_TIMELINE_BAR);
				rect.left -= size_bar*0.5f;
				rect.right = rect.left + size_bar;
				
				canvas.drawRect(rect, paint);
			}else if(size_bar2 >= 1){
				
				paint.setColor(COLOR_TIMELINE_BAR2);
				rect.left -= size_bar2*0.5f;
				rect.right = rect.left + size_bar2;
				
				canvas.drawRect(rect, paint);
			}
			
			if(i%tempo == 0)
			{
				paint.setColor(COLOR_TIMELINE_STEMP);
				paint.setTextSize(SIZE_TIMELINE_STEMP_SIZE);
				
				text = Integer.toString(startTime+(i/tempo));
				rect2.left -= (paint.measureText(text)/2);
				rect2.top = stempRect.bottom - (SIZE_TIMELINE_STEMP_HEIGHT-SIZE_TIMELINE_STEMP_SIZE)/2;
						
				canvas.drawText(text, rect2.left, rect2.top, paint);
			}
		}
	}
	
	private void drawNotesTimeline(Canvas canvas){

		float size_block_point = Math.max(1, SIZE_TIMELINE_NOTE_WIDTH* SIZE_SCALE);
		
		//-- draw timelineNotes
		int size_timeline_note_start = timelineRect.top + SIZE_TIMELINE_NOTE_MARGIN;
		int size_timeline_note_height = (timelineRect.bottom-timelineRect.top) - (SIZE_TIMELINE_NOTE_MARGIN*2);
		int size_note_height = size_timeline_note_height/16;
		int size_note_height_height = size_note_height - (SIZE_TIMELINE_NOTE_MARGIN*2);

		// 노트 모음
		drawNotes.clear();
		ArrayList<SingleNote> notes = gameMode.getNotes();
		for(int i=0; i<notes.size(); i++)
		{
			SingleNote note = notes.get(i);
			float time = note.getTiming()/1000f;
			
			if(note != null && (screenStartTime-1.5f) <= time && time <= (screenEndTime+1.5f) ){
				drawNotes.add(note);
			}
		}

		//-- 영역설정
		paint.setColor(COLOR_TIMELINE_NOTE_RANGE);
		for(int i=0; i<drawNotes.size(); i++){
			SingleNote note = drawNotes.get(i);
			
			float time = note.getTiming()/1000f;

			rect.left = (time-screenStartTime)*SIZE_SECOND - (SIZE_TIMELINE_NOTE_RANGE_WIDTH*0.5f);
			rect.right = rect.left + SIZE_TIMELINE_NOTE_RANGE_WIDTH;
			rect.top = size_timeline_note_start + (note.getSingleButton()*size_note_height) + SIZE_TIMELINE_NOTE_MARGIN;
			rect.bottom = rect.top+size_note_height_height;

			canvas.drawRect(rect, paint);
		}
		
		//-- 영역설정
		for(int i=0; i<drawNotes.size(); i++){
			SingleNote note = drawNotes.get(i);
			
			float time = note.getTiming()/1000f;
			
			// 수정영역
			rect.left = (time-screenStartTime)*SIZE_SECOND - (SIZE_TIMELINE_NOTE_ACTIVE_WIDTH*0.5f);
			rect.right = rect.left + SIZE_TIMELINE_NOTE_ACTIVE_WIDTH;
			rect.top = size_timeline_note_start + (note.getSingleButton()*size_note_height) + SIZE_TIMELINE_NOTE_MARGIN;
			rect.bottom = rect.top+size_note_height_height;

			paint.setColor(COLOR_TIMELINE_NOTE_ACTIVE);
			canvas.drawRect(rect, paint);
			
			// 포인트 영역
			rect.left = (time-screenStartTime)*SIZE_SECOND - (size_block_point*0.5f);
			rect.right = rect.left + size_block_point;
			
			paint.setColor(COLOR_TIMELINE_NOTE);
			canvas.drawRect(rect, paint);
		}
		
	}
	
	private void drawNotesScroller(Canvas _canvas)
	{
		if(dirtyNotes || scrollerBitmap == null )
		{
			dirtyNotes = false;

			//-- 그릴 수 있는 영역을 구한다
			int width = scrollerRect.width() - (startWidth+endWidth);
			int height = scrollerRect.height()-SIZE_SCROLLER_BLOCK_MARGIN;

			//-- 그릴 수 있는 갯수를 구한다
			int widthCount = width / SIZE_SCROLLER_BLOCK;
			int heightCount = height / SIZE_SCROLLER_BLOCK;
			
			//-- 그릴 수 있는 영역으로 자른다.
			width = (width/widthCount) * widthCount;
			height = (height/heightCount) * heightCount;
	
			if( scrollerBitmap == null || scrollerBitmap.getWidth()!=width || scrollerBitmap.getHeight()!=height ) {
				if(scrollerBitmap != null) scrollerBitmap.recycle();
				scrollerBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			}else scrollerBitmap.eraseColor(Color.TRANSPARENT);
			Canvas canvas = new Canvas(scrollerBitmap);

			int[] blockRate = new int[widthCount];

			ArrayList<SingleNote> notes = gameMode.getNotes();
			for(int i=0; i<notes.size(); i++)
			{
				SingleNote note = notes.get(i);
				int idx = (int)(((float)note.getTiming()/gameMode.getDuration()) * widthCount);
				blockRate[Math.max(0, Math.min(blockRate.length-1,idx))]++;
			}
			
			paint.setColor(COLOR_SCROLLER_BLOCK);
			
			float blockMaxValue = Math.max(heightCount, 600f/widthCount);
			for(int i=0; i<blockRate.length; i++){
				
				int value = (int) Math.ceil(blockRate[i]/blockMaxValue * heightCount);
				for(int y=0; y<value; y++){
				
					rect.left = (i*SIZE_SCROLLER_BLOCK) + SIZE_SCROLLER_BLOCK_STROKE;
					rect.top = (height-(Math.min(heightCount, y+1)*SIZE_SCROLLER_BLOCK)) +  SIZE_SCROLLER_BLOCK_STROKE;
					rect.right = rect.left + SIZE_SCROLLER_BLOCK  - (SIZE_SCROLLER_BLOCK_STROKE*2);
					rect.bottom = rect.top +  SIZE_SCROLLER_BLOCK  - (SIZE_SCROLLER_BLOCK_STROKE*2);
					
					canvas.drawRect(rect, paint);
				
				}
			}
			
		}
		
		rect.left = (width-scrollerBitmap.getWidth())*0.5f;
		rect.top = scrollerRect.top + (((scrollerRect.height()-scrollerBitmap.getHeight()))*0.5f);
		
		_canvas.drawBitmap(scrollerBitmap, rect.left, rect.top, null);
	}
	
	private void drawScrollPointer(Canvas canvas)
	{
		float percentPosition = (float)gameMode.getSysTime() / gameMode.getDuration();
		int firstPosition = (int)(percentPosition * (width - (startWidth+endWidth)))+startWidth;
		
		//-- scroller rect
		rect.set(firstPosition-(SIZE_SCROLL_BAR_WIDTH*0.5f), scrollerRect.top, firstPosition+(SIZE_SCROLL_BAR_WIDTH*0.5f), scrollerRect.bottom);
		paint.setColor(COLOR_SCROLL_BAR);
		canvas.drawRect(rect, paint);

		//-- timeline rect
		rect.left = startWidth-(SIZE_TIMELINE_BAR_WIDTH*0.5f);
		rect.right = rect.left + SIZE_TIMELINE_BAR_WIDTH;
		rect.top = timelineRect.top;
		rect.bottom = timelineRect.bottom;
		paint.setColor(COLOR_TIMELINE_CURRENT_BAR);
		canvas.drawRect(rect, paint);
	}

	/**************************************************************************
	 * 
	 * @EVENT
	 * 
	 *************************************************************************/

	@Override
	public boolean onTouchEvent ( MotionEvent event )
	{
		int actionmasked = event.getAction() & 0xFF;

		switch ( actionmasked )
		{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				{
					gameMode.soundPlay();
					gameMode.setTargetSystemTime_startTime(0);
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				{
					gameMode.soundStop();
					gameMode.setTargetSystemTime_startTime(System.currentTimeMillis());
				}
				break;
		}
		
		mScaleGestureDetector.onTouchEvent( event );
		return  mGestureDetector.onTouchEvent( event ) || super.onTouchEvent(event);
	}

	private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

		private boolean targetScrollBar;
		
		@Override
		public boolean onDown(MotionEvent e) {
			if(gameMode.getRunStatus() != MakerGameMode.STATUS_MAKE){
				return false;
			}
			
			targetScrollBar = e.getY() > timelineRect.bottom;
			if(targetScrollBar){
				long time = (long) ( ((e.getX()-startWidth) / (width-(startWidth+endWidth))) * gameMode.getDuration() );
				time = Math.max(0, Math.min((gameMode.getDuration()-700), time));
				gameMode.setTargetSystemTime(time);
				gameMode.setTargetSystemTime_startTime(1);
			}
			return true;
		};
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return true;
		};
		
		@Override
		public boolean onSingleTapConfirmed ( MotionEvent e )
		{
			if(!targetScrollBar){
				int type = 1;
				if(e.getX()<startWidth){
					type = -1;
				}
				float tempoDuration = 1000/gameMode.getTempo() * type;

				gameMode.setTargetSystemTime((long) ( gameMode.getSysTime()+tempoDuration ));
			}
			return true;
		}
		
		@Override
		public void onShowPress ( MotionEvent e )
		{
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
		};
		
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

			long time;
			if(targetScrollBar)
			{
				time = (long) ( ((e2.getX()-startWidth) / (width-(startWidth+endWidth))) * gameMode.getDuration() );
				time = Math.max(0, Math.min((gameMode.getDuration()-700), time));
				gameMode.setTargetSystemTime(time);
				gameMode.setTargetSystemTime_startTime(1);
				
			}else{
				time = gameMode.getSysTime() + (int)( (distanceX / SIZE_SECOND) * 1000 );
				time = Math.max(0, Math.min((gameMode.getDuration()-700), time));

				gameMode.setTargetSystemTime(time);
				gameMode.setSysTime(time);
			}
			
			return true;
		};

		@Override
		public boolean onFling ( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY )
		{
			if(targetScrollBar)
			{
				return false;	
			}else{
				fling( (int) -velocityX, (int) -velocityY );
				return true;	
			}
		}
		
	};

	private void fling ( int velocityX, int velocityY )
	{
		int pos = (int)( (gameMode.getSysTime()/1000f) * SIZE_SECOND );
		int size = (int)( ((gameMode.getDuration()-700)/1000f) * SIZE_SECOND );
		mScroller.forceFinished( true );
		mScroller.fling(pos, 0, velocityX, 0, 0, size, 0, 0, SIZE_SECOND/10,0 );
	        ViewCompat.postInvalidateOnAnimation(this);
	}
	
	@Override
	public void computeScroll ()
	{
		super.computeScroll();
		if(mScroller.computeScrollOffset()){

			long time = (long)(( (float)mScroller.getCurrX() / SIZE_SECOND ) * 1000); 
			time = Math.max(0, Math.min(gameMode.getDuration(), time));

//			gameMode.setTargetSystemTime_startTime(System.currentTimeMillis());
			gameMode.setTargetSystemTime(time);
		}
	        ViewCompat.postInvalidateOnAnimation(this);
	}

	

	private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {

	        private float lastSpanX;
	        private float lastSpanY;
	        
		@Override
		public boolean onScaleBegin ( ScaleGestureDetector detector )
		{
			if(gameMode.getRunStatus() != MakerGameMode.STATUS_MAKE){
				return false;
			}
			Log.d("motionEvent", "onScaleBegin");
			lastSpanX = getCurrentSpanX(detector);
			lastSpanY = getCurrentSpanY(detector);
			return true;
		}

		@Override
		public boolean onScale ( ScaleGestureDetector detector )
		{
			Log.d("motionEvent", "onScale");
			float spanX = getCurrentSpanX(detector);
			float spanY = getCurrentSpanY(detector);
			float scale = 1;
			
			if(spanX > spanY){
				scale = lastSpanX / spanX;
			}else{
				scale = lastSpanY / spanY;
			}
			
			int second_width = (int)( SIZE_SECOND / scale );
			
			second_width = Math.min(SIZE_MAX_SECOND, Math.max(SIZE_MIN_SECOND, second_width));
			SIZE_SECOND = second_width;
			MakerTimelineView.this.SIZE_SCALE = (float)SIZE_SECOND / SIZE_MAX_SECOND;
			setDirty(true);

			
			lastSpanX = spanX;
			lastSpanY = spanY;
			return true;
		}
		
		@Override
		public void onScaleEnd ( ScaleGestureDetector detector )
		{
			Log.d("motionEvent", "onEnd");
		}

		/**
		 * @see android.view.ScaleGestureDetector#getCurrentSpanX()
		 */
		@TargetApi ( Build.VERSION_CODES.HONEYCOMB )
		public float getCurrentSpanX ( ScaleGestureDetector scaleGestureDetector )
		{
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
			{
				return scaleGestureDetector.getCurrentSpanX();
			}
			else
			{
				return scaleGestureDetector.getCurrentSpan();
			}
		}

		/**
		 * @see android.view.ScaleGestureDetector#getCurrentSpanY()
		 */
		@TargetApi ( Build.VERSION_CODES.HONEYCOMB )
		public float getCurrentSpanY ( ScaleGestureDetector scaleGestureDetector )
		{
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
			{
				return scaleGestureDetector.getCurrentSpanY();
			}
			else
			{
				return scaleGestureDetector.getCurrentSpan();
			}
		}
	};

	/**************************************************************************
	 * 
	 * @GETSET
	 * 
	 *************************************************************************/
	
	public GameModeInterface getGameMode () {
		return gameMode;
	}
	public void setGameMode (MakerGameMode gameMode) {
		this.gameMode = gameMode;
	}
	public boolean isDirty () {
		return dirty;
	}
	public void setDirty (boolean dirty) {
		this.dirty = dirty;
	}
	public boolean isDirtyNotes () {
		return dirtyNotes;
	}
	public void setDirtyNotes (boolean dirtyNotes) {
		this.dirtyNotes = dirtyNotes;
	}
	public FFTVisualize getVisualize () {
		return visualize;
	}
	public void setVisualize (FFTVisualize visualize) {
		this.visualize = visualize;
	}
	
	public int getAnaylzeStyle () {
		return anaylzeStyle;
	}
	public void setAnaylzeStyle (int anaylzeStyle) {
		this.anaylzeStyle = anaylzeStyle;
	}
}
