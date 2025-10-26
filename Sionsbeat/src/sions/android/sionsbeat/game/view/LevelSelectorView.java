package sions.android.sionsbeat.game.view;

import java.util.logging.ErrorManager;

import sions.android.sionsbeat.R;
import sions.android.sionsbeat.game.GameData;
import sions.android.sionsbeat.game.GameMode;
import sions.android.sionsbeat.game.listener.GameModeListener;
import sions.android.sionsbeat.template.Note;
import sions.android.sionsbeat.utils.ErrorController;
import android.content.Context;
import android.content.res.TypedArray;
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
import android.view.MotionEvent;
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
public class LevelSelectorView extends View {

	private static final String TAG = "levelSelector";

	private DisplayMetrics displayMetrics;

	private int categoryTextSize;
	private int categoryTextColor;
	private int categorySplit;
	
	private int levelTextSize;
	private int levelTextColor;
	private int levelOvalWidth;
	private int levelOvalColor;

	private int lineWidth;
	private int lineColor;
	private int noneColor;
	
	private int progress;
	private int progressEnd;
	private int progressStart;
	private int minProgress;
	private int maxProgress;
	
	private float draw_start;
	private float draw_width;
	private float draw_column;
	
	private OnChangeListener changeListener;
	
	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/
	public LevelSelectorView(Context context) {
		super(context);
	}

	public LevelSelectorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	
	public LevelSelectorView ( Context context, AttributeSet attrs, int defStyle )
	{
		super( context, attrs, defStyle );
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs){
		TypedArray array = context.obtainStyledAttributes( attrs, R.styleable.LevelSelectorView );
		if ( array != null )
		{

			categoryTextSize = array.getDimensionPixelOffset( R.styleable.LevelSelectorView_categoryTextSize, 0 );
			categoryTextColor = array.getColor( R.styleable.LevelSelectorView_categoryTextColor, 0 );
			categorySplit = array.getInt( R.styleable.LevelSelectorView_categorySplit, 0 );
			
			levelTextSize = array.getDimensionPixelOffset( R.styleable.LevelSelectorView_levelTextSize, 0 );
			levelTextColor = array.getColor( R.styleable.LevelSelectorView_levelTextColor, 0 );
			levelOvalWidth = array.getDimensionPixelOffset( R.styleable.LevelSelectorView_levelOvalWidth, 0 );
			levelOvalColor = array.getColor( R.styleable.LevelSelectorView_levelOvalColor, 0 );

			lineWidth = array.getDimensionPixelOffset( R.styleable.LevelSelectorView_lineWidth, 0 );
			lineColor = array.getColor( R.styleable.LevelSelectorView_lineColor, 0 );
			noneColor = array.getColor( R.styleable.LevelSelectorView_noneColor, 0 );

			progress = array.getInt( R.styleable.LevelSelectorView_progress, 0 );
			progressStart = array.getInt( R.styleable.LevelSelectorView_start, 0 );
			progressEnd = array.getInt( R.styleable.LevelSelectorView_end, 0 );
			
			minProgress = progressStart;
			maxProgress = progressEnd;
			
			array.recycle();
		}
		
		if(isInEditMode())
		{
			maxProgress = 13;
		}
	}

	/**************************************************************************
	 * 
	 * @View
	 * 
	 *************************************************************************/
	
	
	/**************************************************************************
	 * 
	 * @LiefCycle
	 * 
	 *************************************************************************/

	@Override
	public boolean onTouchEvent ( MotionEvent event )
	{
		switch ( event.getActionMasked() )
		{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				{
					if(this.changeListener != null){
						this.changeListener.onStartTouch( this );
					}
				}
			case MotionEvent.ACTION_MOVE:
				{
					float x = event.getX();
					float y = event.getY();
					
					float draw_column = draw_width / (progressEnd-progressStart);
					
					int idx = (int) ( ( x-draw_start ) / draw_column );
					idx += minProgress;
					
					if(idx < minProgress) idx = minProgress;
					else if(idx > maxProgress) idx = maxProgress;
					
					setProgress( idx ); 

					if(this.changeListener != null){
						this.changeListener.onChange( this, progress );
					}
				}
				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				{
					if(this.changeListener != null){
						this.changeListener.onStopTouch( this );
					}
				}
				return true;
		}
		
		return true;
	}
	
	@Override
	protected void onMeasure ( int widthMeasureSpec, int heightMeasureSpec )
	{
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
		int height = (int) Math.ceil( (categoryTextSize*1.5f) + levelOvalWidth*1.1 );
		int width = getMeasuredWidth();
		
//		width += getPaddingLeft() + getPaddingRight();
		height += getPaddingTop() + getPaddingBottom();
		
		setMeasuredDimension( width, height );
	}
	
	@Override
	protected void onDraw ( Canvas canvas )
	{
		super.onDraw( canvas );
		
		canvas.translate(getPaddingLeft(), getPaddingTop());
		
		int width = getMeasuredWidth() - ( getPaddingLeft() + getPaddingRight() );
		int height = getMeasuredHeight() - ( getPaddingTop() + getPaddingBottom());

		Paint paint = new Paint();
		paint.setColor( categoryTextColor );
		paint.setTextSize( categoryTextSize );

		Paint stroke = new Paint();
		stroke.setStyle( Paint.Style.STROKE );
		stroke.setStrokeWidth( 3 );

//		float startWidth = getStringWidth(String.valueOf(progressStart), paint);
//		float endWidth = getStringWidth(String.valueOf(progressEnd), paint);
		
		draw_start = levelOvalWidth*0.5f;
		draw_width = width - (draw_start*2);
		draw_column = draw_width / (progressEnd-progressStart);
		
		drawStringCenter( String.valueOf(progressStart), draw_start, categoryTextSize, canvas, paint );
		drawStringCenter( String.valueOf(progressEnd), draw_start+draw_width, categoryTextSize, canvas, paint );
		
		for(int i=1; i<categorySplit+1; i++){
			int label = (int) Math.round( ( i / (categorySplit+1f)) * (progressEnd-progressStart) );
			float position = (label*draw_column)+draw_start;
			
			drawStringCenter(String.valueOf( label+1 ), position, categoryTextSize, canvas, paint);
		}
		
		float top = categoryTextSize*1.5f;
		float lineMargin = ( height - top - lineWidth ) * 0.5f;

		paint.setColor( noneColor );
		canvas.drawRect( draw_start, top+lineMargin, draw_start+draw_width, height-lineMargin, paint );
		
//		minProgress = 2;
//		maxProgress = 14;

		float left = 0;
		float right = 0;
		
		if(minProgress != 0) left = (minProgress-progressStart) * draw_column;
		if(maxProgress != 0) right = (progressEnd-maxProgress) * draw_column;
		
		paint.setColor( lineColor );
		canvas.drawRect( draw_start+left, top+lineMargin, draw_start+draw_width-right, height-lineMargin, paint );

		
		
		left = (progress-progressStart) * draw_column;
		paint.setColor( levelOvalColor );
		stroke.setColor( levelOvalColor | 0xFF000000 );
		stroke.setStrokeWidth(1);
		RectF ovalRect = new RectF( left, top, left+levelOvalWidth, top+levelOvalWidth );
		canvas.drawOval( ovalRect, paint );
		canvas.drawOval( ovalRect, stroke );
		
		float addColumn = (levelOvalWidth-draw_column)*0.5f;
		left += addColumn;
		top += addColumn;
		ovalRect.set( left, top, left+draw_column, top+draw_column );
		paint.setColor( levelOvalColor | 0xFF000000 );
		canvas.drawOval( ovalRect, paint ); 
		
//		
//		left = (progress-progressStart) * draw_column;
//		paint.setColor( levelOvalColor );
//		stroke.setColor( levelOvalColor | 0xFF000000 );
//		RectF ovalRect = new RectF( left, top, left+levelOvalWidth, top+levelOvalWidth );
//		canvas.drawOval( ovalRect, paint );
//		canvas.drawOval( ovalRect, stroke );
//		
//		
//		
//		
//		float addColumn = (levelOvalWidth-draw_column)*0.5f;
//		left += addColumn;
//		top += addColumn;
//		ovalRect.set( left, top, left+draw_column, top+draw_column );
//		paint.setColor( levelOvalColor | 0xFF000000 );
//		canvas.drawOval( ovalRect, paint ); 
		

		paint.setColor( levelTextColor );
		paint.setTextSize( levelTextSize );
		stroke.setColor( 0xFFFFFFFF );
		stroke.setTextSize( levelTextSize );

		ovalRect.left = left + (draw_column*0.5f);
		ovalRect.top = top + (draw_column*0.5f) + (levelTextSize*0.3f);
		drawStringCenter( String.valueOf( progress ), ovalRect.left, ovalRect.top, canvas, stroke );
		drawStringCenter( String.valueOf( progress ), ovalRect.left, ovalRect.top, canvas, paint );
		
		
	}
	
	/**************************************************************************
	 * 
	 * @ACTION
	 * 
	 *************************************************************************/

	private float getStringWidth(String text, Paint paint){
		
		float[] widths = new float[text.length()];
		paint.getTextWidths( text, widths );
		
		float swidth = 0f;
		for (float w:widths){
			swidth += w;
		}
		
		return swidth;
	}
	
	private void drawStringCenter(String text, float x, float y, Canvas canvas, Paint paint)
	{
		float width = getStringWidth( text, paint );
		canvas.drawText( text, x-(width*0.5f), y, paint );
	}
	
	
	/**************************************************************************
	 * 
	 * @GETSET
	 * 
	 *************************************************************************/
	
	public int getDipToPx(float dip){
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics);
	}

	public DisplayMetrics getDisplayMetrics ()
	{
		return displayMetrics;
	}

	public void setDisplayMetrics ( DisplayMetrics displayMetrics )
	{
		this.displayMetrics = displayMetrics;
	}

	public int getCategoryTextSize ()
	{
		return categoryTextSize;
	}

	public void setCategoryTextSize ( int categoryTextSize )
	{
		this.categoryTextSize = categoryTextSize;
	}

	public int getCategoryTextColor ()
	{
		return categoryTextColor;
	}

	public void setCategoryTextColor ( int categoryTextColor )
	{
		this.categoryTextColor = categoryTextColor;
	}

	public int getCategorySplit ()
	{
		return categorySplit;
	}

	public void setCategorySplit ( int categorySplit )
	{
		this.categorySplit = categorySplit;
	}

	public int getLevelTextSize ()
	{
		return levelTextSize;
	}

	public void setLevelTextSize ( int levelTextSize )
	{
		this.levelTextSize = levelTextSize;
	}

	public int getLevelTextColor ()
	{
		return levelTextColor;
	}

	public void setLevelTextColor ( int levelTextColor )
	{
		this.levelTextColor = levelTextColor;
	}

	public int getLevelWidth ()
	{
		return levelOvalWidth;
	}

	public void setLevelWidth ( int levelWidth )
	{
		this.levelOvalWidth = levelWidth;
	}

	public int getLineColor ()
	{
		return lineColor;
	}

	public void setLineColor ( int lineColor )
	{
		this.lineColor = lineColor;
	}

	public int getNoneColor ()
	{
		return noneColor;
	}

	public void setNoneColor ( int noneColor )
	{
		this.noneColor = noneColor;
	}

	public int getProgress ()
	{
		return progress;
	}

	public void setProgress ( int progress )
	{
		if(progress < minProgress) progress = minProgress;
		else if(progress > maxProgress) progress = maxProgress;
		
		if(this.progress != progress){
			this.progress = progress;
			invalidate();
		}
	}

	public int getProgressEnd ()
	{
		return progressEnd;
	}

	public void setProgressEnd ( int progressEnd )
	{
		this.progressEnd = progressEnd;
	}

	public int getProgressStart ()
	{
		return progressStart;
	}

	public void setProgressStart ( int progressStart )
	{
		this.progressStart = progressStart;
	}

	public int getMinProgress ()
	{
		return minProgress;
	}

	public void setMinProgress ( int minProgress )
	{
		this.minProgress = minProgress;
	}

	public int getMaxProgress ()
	{
		return maxProgress;
	}

	public void setMaxProgress ( int maxProgress )
	{
		this.maxProgress = maxProgress;
	}

	public OnChangeListener getOnChangeListener ()
	{
		return changeListener;
	}

	public void setOnChangeListener ( OnChangeListener changeListener )
	{
		this.changeListener = changeListener;
	}



	public interface OnChangeListener {

		public void onStartTouch(LevelSelectorView view);
		public void onStopTouch(LevelSelectorView view);
		public void onChange(LevelSelectorView view, int progress);
		
	}
}
