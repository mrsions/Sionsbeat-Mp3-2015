package sions.android.tooltip;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class ToolTipView extends View{

	private ArrayList<ArrayList<ToolTip>> list;
	private DisplayMetrics displayMetrics;
	
	public ToolTipView (Context context, AttributeSet attrs){
	        super(context, attrs);
	        initialize();
        }

	public ToolTipView (Context context){
	        super(context);
	        initialize();
        }
	private void initialize(){
		this.list = new ArrayList<ArrayList<ToolTip>>();
		this.displayMetrics = getContext().getResources().getDisplayMetrics();
	}
	
	public void add(ArrayList<ToolTip> tooltip)
	{
		this.list.add(tooltip);
	}
	
	public void remove(ArrayList<ToolTip> tooltip)
	{
		this.list.remove(tooltip);
	}
	
	@Override
	protected void onDraw (Canvas canvas) {
	        super.onDraw(canvas);

	        int width = getMeasuredWidth();
	        int strokeWidth = Math.max(1, width/250);
	        int ovalWidth = Math.max(3, width/125);
	        
	        canvas.drawARGB(200, 0, 0, 0);

	        Paint stroke = new Paint();
	        stroke.setStyle(Paint.Style.STROKE);
	        stroke.setStrokeWidth(strokeWidth);
	        stroke.setColor(0xFFFFFFFF);
	        stroke.setAntiAlias(true);
	        stroke.setDither(false);
	        stroke.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
	        stroke.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
	        stroke.setPathEffect(new CornerPathEffect(10) );   // set the path effect when they join.
	        
	        Paint fill = new Paint();
	        fill.setStyle(Paint.Style.FILL);
	        fill.setColor(0xFFFFFFFF);
	        fill.setAntiAlias(true);
	        fill.setTextSize(width/30);

	        for(ArrayList<ToolTip> tooltips:list)
	        {
	        	for(ToolTip tip:tooltips)
	        	{
		        	float x = tip.getTarget().getLeft() + ( tip.getTarget().getWidth() * tip.getTargetX() );
		        	float y = tip.getTarget().getTop() + ( tip.getTarget().getHeight() * tip.getTargetY() );
		        	float tx = displayMetrics.widthPixels * tip.getTextX();
		        	float ty = displayMetrics.heightPixels * tip.getTextY();
	
		        	canvas.drawLine(x, y, tx, ty, stroke);
		        	canvas.drawOval(new RectF(x-ovalWidth,y-ovalWidth,x+ovalWidth,y+ovalWidth), fill);
		        	
		        	float size = fill.measureText(tip.getText());
		        	canvas.drawText(tip.getText(), tx-(size*0.5f), ty+(fill.getTextSize()*1.2f), fill);
	        	}
	        }

	}
	
	public int getDipToPx(float dip){
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics);
	}

}
