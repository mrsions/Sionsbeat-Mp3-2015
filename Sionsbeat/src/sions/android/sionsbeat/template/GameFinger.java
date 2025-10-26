package sions.android.sionsbeat.template;

import java.util.ArrayList;
import java.util.Random;

public class GameFinger
{

	/************************************************************************************************************
	 * 
	 * 				@STATIC_FIELDS
	 * 
	 ************************************************************************************************************/

	public static final int NOTE_TIME_LAG = 1500;
	
	/************************************************************************************************************
	 * 
	 *				 @CONSTRUCTOR
	 * 
	 ************************************************************************************************************/

	public GameFinger(int[] notes, int noteWidth, Random rnd){
		possibleTouch = new ArrayList<Integer>(notes.length);
		
		this.noteWidth = noteWidth;
		this.currentNotes = notes;
		this.rnd = rnd;
	}
	
	/************************************************************************************************************
	 * 
	 * 				@MEMBER_FIELDS
	 * 
	 ************************************************************************************************************/

	ArrayList<Integer> possibleTouch;
	
	private Random rnd;
	
	/**
	 * �� �հ����� �ٸ� �հ������� �� �� �մ� �ӵ�. (�������� ������ �ȴ�)
	 */
	private int speed = 100;
	private int fullSpeed = 400;
	
	/**
	 * ������ �հ����� ������ ��Ʈ
	 */
	private int prevX;
	private int prevY;
	private int prevNoteTime = -5000;

	private int noteWidth;
	
	/**
	 * ������ �����Ȳ
	 */
	private int[] currentNotes;
	
	
	/************************************************************************************************************
	 * 
	 * 				@ACTIVE_METHODS
	 * 
	 ************************************************************************************************************/

	public int touch(int curTime){
		
		possibleTouch.clear();
		int timeGab = curTime - prevNoteTime;
		
		if(timeGab > fullSpeed){
			for(int i=0; i<currentNotes.length; i++){
				addPossible( curTime, i );
			}
		}else if(timeGab < speed){
			addPossible( curTime, prevX+1, prevY );
			addPossible( curTime, prevX-1, prevY );
			addPossible( curTime, prevX, prevY+1 );
			addPossible( curTime, prevX, prevY-1 );
		}else{
			
			int count = (timeGab / speed);
			for(int x=-count; x<=count; x++){
				for(int y=-count; y<=count; y++){
					addPossible( curTime, prevX+x, prevY+y );
				}
			}
		}
		
		if(possibleTouch.size() > 0){
			int idx = possibleTouch.get( rnd.nextInt( possibleTouch.size() ) );
			
			prevX = idx % noteWidth;
			prevY = idx / noteWidth;
			prevNoteTime = curTime;
			currentNotes[idx] = curTime;
			
			return idx;
		}else{
			return -1;
		}
	}
	
	private void addPossible(int curTime, int x, int y){
		if(x < 0 || x >= noteWidth || y < 0 || y >= noteWidth) return;
		
		int idx = x + ( y * noteWidth );
		addPossible(curTime, idx);
	}
	private void addPossible(int curTime, int idx){
		if(idx <0 || idx >= currentNotes.length) return;
		
		if(currentNotes[idx]+NOTE_TIME_LAG < curTime){
			possibleTouch.add(idx);
		}
	}

	/************************************************************************************************************
	 * 
	 * 				@ACCESS_METHODS
	 * 
	 ************************************************************************************************************/

	public int getSpeed ()
	{
		return speed;
	}

	public void setSpeed ( int speed )
	{
		this.speed = speed;
	}

	public int getFullSpeed ()
	{
		return fullSpeed;
	}

	public void setFullSpeed ( int fullSpeed )
	{
		this.fullSpeed = fullSpeed;
	}

	public int getPrevX ()
	{
		return prevX;
	}

	public void setPrevX ( int prevX )
	{
		this.prevX = prevX;
	}

	public int getPrevY ()
	{
		return prevY;
	}

	public void setPrevY ( int prevY )
	{
		this.prevY = prevY;
	}

	public int getPrevNoteTime ()
	{
		return prevNoteTime;
	}

	public void setPrevNoteTime ( int prevNoteTime )
	{
		this.prevNoteTime = prevNoteTime;
	}
}
