package sions.android.sionsbeat.template;

import sions.android.sionsbeat.zresource.ZAnimation;

public class GameNote {

	public static final int TYPE_UNNOTE = 0x00;	
	public static final int TYPE_NOTING = 0x01; // 터치할 수 있는 상태
	public static final int TYPE_COMBO = 0x03;
	public static final int TYPE_GOOD = 0x04;
	public static final int TYPE_GREAT = 0x05;
	public static final int TYPE_PERFECT = 0x06;
	public static final int TYPE_FAILED = 0x07;

	private long startTime;
	private long touchTime = -10000;
	
	private Note note;
	private ZAnimation anim;
	private int type	= TYPE_UNNOTE;

	/**************************************************************************
	 * 
	 * @Action
	 * 
	 *************************************************************************/
	
	public boolean isTouch(){
		return this.type == TYPE_NOTING;
	}

	/**************************************************************************
	 * 
	 * @GETSET
	 * 
	 *************************************************************************/
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long start) {
		this.startTime = start;
	}

	public long getTouchTime() {
		return touchTime;
	}

	public void setTouchTime(long touchTime) {
		this.touchTime = touchTime;
	}

	public ZAnimation getAnim() {
		return anim;
	}

	public void setAnim(ZAnimation anim) {
		this.anim = anim;
	}

	public Note getNote ()
	{
		return note;
	}

	public void setNote ( Note note )
	{
		this.note = note;
	}
	

}
