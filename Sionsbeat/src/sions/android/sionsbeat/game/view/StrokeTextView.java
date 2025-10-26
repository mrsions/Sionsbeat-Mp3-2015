package sions.android.sionsbeat.game.view;

import sions.android.sionsbeat.R;
import android.R.anim;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class StrokeTextView extends View
{
	
	private Paint stroke;
	private Paint fill;
	
	private String text="text";
	private int textSize = 30;
	private int textColor = 0xFF000000;
	
	private int strokeSize = 30;
	private int strokeColor = 0xFFFFFFFF;

	public StrokeTextView ( Context context )
	{
		super( context );
		init(context, null);
	}

	public StrokeTextView ( Context context, AttributeSet attrs )
	{
		super( context, attrs );
		init(context, attrs);
	}
	
	public StrokeTextView ( Context context, AttributeSet attrs, int defStyleAttr )
	{
		super( context, attrs, defStyleAttr );
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs){

		stroke = new Paint();
		stroke.setAntiAlias( true );
		stroke.setStyle( Paint.Style.STROKE );
		stroke.setFakeBoldText( true );

		fill = new Paint();
		fill.setAntiAlias( true );
		fill.setStyle( Paint.Style.FILL );
		fill.setFakeBoldText( true );
		
		if(attrs != null){
			TypedArray array = context.obtainStyledAttributes( attrs, R.styleable.StrokeTextView );
			if ( array != null )
			{
				setText( array.getString( R.styleable.StrokeTextView_text ) );
				
				setTextSize( array.getDimensionPixelOffset( R.styleable.StrokeTextView_textSize, textSize ) );
				setTextColor( array.getColor( R.styleable.StrokeTextView_textColor, textColor ) );
				
				setStrokeSize( array.getDimensionPixelOffset( R.styleable.StrokeTextView_strokeSize, strokeSize ) );
				setStrokeColor( array.getColor( R.styleable.StrokeTextView_strokeColor, strokeColor ) );
				
				array.recycle();
			}
		}
		
	}
	
	private int parseInt(String value, int def){
		try{
			return Integer.parseInt(value);
		}catch(Throwable e){
			return def;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
		
		if(text != null)
		{
			int width = (int)Math.ceil( getStringWidth( text, fill ) ) +(strokeSize*2);
			int height = textSize+(strokeSize*2);
			
			if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY){
				width = getMeasuredWidth();
			}
			if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY){
				height = getMeasuredHeight();
			}
			setMeasuredDimension( width, height );
		}
		else
		{
			int width = 10;
			int height = 10;
			
			if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY){
				width = getMeasuredWidth();
			}
			if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY){
				height = getMeasuredHeight();
			}
			setMeasuredDimension( width, height );
		}
	}
	
	private float getStringWidth(String text, Paint paint){
		
		float[] widths = new float[text.length()];
		paint.getTextWidths( text, widths );
		
		float swidth = 0f;
		for (float w:widths){
			swidth += w;
		}
		
		return swidth;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw( canvas );

		if(text != null){
			
			int left = strokeSize;
			int top = (int) ( (textSize+strokeSize) * 0.9 );
			
			if(strokeSize > 0){
				canvas.drawText( text, left, top, stroke );
			}
			canvas.drawText( text, left, top, fill );
		}
	}
	
	public String getText ()
	{
		return text;
	}

	public void setText ( String text )
	{
		this.text = text;
		invalidate();
	}

	public int getTextSize ()
	{
		return textSize;
	}

	public void setTextSize ( int textSize )
	{
		this.textSize = textSize;
		stroke.setTextSize( textSize );
		fill.setTextSize( textSize );
		invalidate();
	}

	public int getTextColor ()
	{
		return textColor;
	}

	public void setTextColor ( int textColor )
	{
		this.textColor = textColor;
		fill.setColor( textColor );
		invalidate();
	}

	public int getStrokeSize ()
	{
		return strokeSize;
	}

	public void setStrokeSize ( int strokeSize )
	{
		this.strokeSize = strokeSize;
		stroke.setStrokeWidth( strokeSize );
		invalidate();
	}

	public int getStrokeColor ()
	{
		return strokeColor;
	}

	public void setStrokeColor ( int strokeColor )
	{
		this.strokeColor = strokeColor;
		stroke.setColor( strokeColor );
		invalidate();
	}

}
