package sions.android.sionsbeat.game.view;

import sions.android.sionsbeat.utils.ErrorController;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class GameCounterView extends TextView implements Runnable{

	private int count;
	private int currentCount;
	private long start_time;
	private Thread thread;
	
	public GameCounterView(Context context) {
		super(context);
	}

	public GameCounterView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		if(thread == null){
			thread = new Thread(this, "CounterThread");
			thread.start();
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		
		Thread bthread = thread;
		thread = null;
	}


	public void setCount(int count) {
		this.count = count;
		this.start_time = System.currentTimeMillis();
	}
	
	public void setDirectCount(int count){
		synchronized(this){
			this.count = count;
			this.currentCount = count;
			this.start_time = System.currentTimeMillis();
			setText(String.valueOf(currentCount));
		}
	}

	@Override
	public void run() {
		Runnable ui = new Runnable(){
			public void run(){
				setText(String.valueOf(currentCount));
			}
		};
		
		Activity activity = (Activity) getContext();
		while(thread != null){
			try{
				synchronized(this){
					int befCurrentCount = this.currentCount;
					
					int up = (int) (( count - currentCount ) * 0.2);
					if(up == 0){
						this.currentCount = this.count;
					}else{
						this.currentCount += up;
					}
					
					if(this.currentCount != befCurrentCount){
						activity.runOnUiThread(ui);
					}
				}
				Thread.sleep(40);
				
			}catch(Throwable e){
				ErrorController.error(1, e);
			}
		}
	}
	
}