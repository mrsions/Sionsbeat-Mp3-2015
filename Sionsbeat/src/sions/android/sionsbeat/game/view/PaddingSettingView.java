package sions.android.sionsbeat.game.view;

import sions.android.sionsbeat.utils.ErrorController;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

/**
 * 게임을 진행할 때, 사용자와 소통하는 기능을 담당한다.
 * 1. 사용자가 패드를 터치 했을 때 이벤트에 대해서 GameMode에 알린다.
 * 2. GameMode에서 터치에 대한 반응 및 애니메이션 표현에 대해서 알려주면 그것을 이행한다.
 * 
 * @author sunulo
 */
public class PaddingSettingView extends View {

	private int width;
	private float padding=0.99f;
	
	private SeekBar seekBar;
	
	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/
	public PaddingSettingView(Context context) {
		super(context);
	}

	public PaddingSettingView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	

	/**************************************************************************
	 * 
	 * @View
	 * 
	 *************************************************************************/

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		width = getMeasuredWidth();
		setMeasuredDimension(width, width);
	}
	
	/**************************************************************************
	 * 
	 * @draw
	 * 
	 *************************************************************************/

	@Override
	protected void onDraw (Canvas canvas) {
	        super.onDraw(canvas);

	        float blockSize = width * 0.25f;
	        float halfSize = blockSize * 0.5f;
	        float padSize = halfSize * padding;
	        
	        Paint stroke = new Paint();
	        
	        Paint paint = new Paint();
	        paint.setStyle(Paint.Style.FILL);
	        paint.setColor(0xAAFFFFFF);
	        
	        RectF rect = new RectF();
	        for(int y=0; y<4; y++){
	        	rect.top = (blockSize*y) + halfSize - padSize;
	        	rect.bottom = (blockSize*y) + halfSize + padSize;
	        	for(int x=0; x<4; x++)
	        	{
		        	rect.left = (blockSize*x) + halfSize - padSize;
		        	rect.right = (blockSize*x) + halfSize + padSize;
		        	
		        	canvas.drawRect(rect, paint);
	        	}
	        }
	        
	}

	private int beforeX;
	private int beforeY;
	@Override
	public boolean onTouchEvent ( MotionEvent event )
	{
		try
		{
			int x = (int) event.getX();
			int y = (int) event.getY();

			int actionmasked = event.getAction() & 0xFF;
			switch ( actionmasked )
			{
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					{
						beforeX = x;
						beforeY = y;
					}
					return true;
				case MotionEvent.ACTION_MOVE:
					{
						int count = 0;
						Log.d("test",x+","+y+"    "+beforeX+","+beforeY);
						
						if(Math.abs(x-beforeX)>=10){
							count += (x-beforeX)/10;
							beforeX = x;
						}if(Math.abs(y-beforeY)>=10){
							count += (y-beforeY)/10;
							beforeY = y;
						}
						Log.d("test",count+"    "+Math.max(0, Math.min(seekBar.getMax(), seekBar.getProgress()+count)));
						
						if(count != 0 && seekBar != null){
							seekBar.setProgress(Math.max(0, Math.min(seekBar.getMax(), seekBar.getProgress()+count)));
						}
					}
					return true;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					return true;
			}

		} catch ( Exception e )
		{
			ErrorController.error( 8, e );
		}
		return false;
	}

	public float getBlockPadding () {
		return padding;
	}

	public void setBlockPadding (float padding) {
		this.padding = padding;
	}

	public SeekBar getSeekBar () {
		return seekBar;
	}

	public void setSeekBar (SeekBar seekBar) {
		this.seekBar = seekBar;
	}

	
}
