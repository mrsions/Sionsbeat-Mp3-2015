package sions.android.tooltip;

import android.app.Activity;
import android.view.View;

public class ToolTip {

	public ToolTip (Activity context, int resId, String text, float targetX, float targetY, float textX, float textY)
        {
		this(context.findViewById(resId), text, targetX, targetY, textX, textY);
        }
	public ToolTip (View target, String text, float targetX, float targetY, float textX, float textY)
        {
	        super();
	        this.target = target;
	        this.text = text;
	        this.targetX = targetX;
	        this.targetY = targetY;
	        this.textX = textX;
	        this.textY = textY;
        }
	
	private View target;
	private String text;

	//-- 타겟 View에서의 시작 위치
	private float targetX = 0.5f;
	private float targetY = 0.5f;
	
	//-- 텍스트 위치, 화면 좌표
	private float textX;
	private float textY;
	
	public View getTarget () {
		return target;
	}
	public void setTarget (View target) {
		this.target = target;
	}
	public void setTarget (Activity context, int resId)
	{
		this.target = context.findViewById(resId);
	}
	public String getText () {
		return text;
	}
	public void setText (String text) {
		this.text = text;
	}
	public float getTargetX () {
		return targetX;
	}
	public void setTargetX (float targetX) {
		this.targetX = targetX;
	}
	public float getTargetY () {
		return targetY;
	}
	public void setTargetY (float targetY) {
		this.targetY = targetY;
	}
	public float getTextX () {
		return textX;
	}
	public void setTextX (float textX) {
		this.textX = textX;
	}
	public float getTextY () {
		return textY;
	}
	public void setTextY (float textY) {
		this.textY = textY;
	}
	
	
	
}
