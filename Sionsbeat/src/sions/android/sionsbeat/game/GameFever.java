package sions.android.sionsbeat.game;

import java.util.Arrays;

import sions.android.sionsbeat.GameActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.template.GameNote;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GameFever implements SensorEventListener {

	private static final int SHAKE_MULTIPLYER = 4;
	private static final int SHAKE_THRESHOLD = 13;
	private static final double MAX_FEVER_POINT = 100;

	private static final int DATA_X = SensorManager.DATA_X;
	private static final int DATA_Y = SensorManager.DATA_Y;
	private static final int DATA_Z = SensorManager.DATA_Z;

	private GameMode mode;
	private GameActivity context;
	
	private SensorManager sensorManager;
	private Sensor accelerormeterSensor;
	private Sensor gyroscopeSensor;
	private ProgressBar feverBar;
	private TextView feverText;

	private float[][] speed = new float[30][3];
	private float[] avgSpeed = new float[3];
	private int speedOffset = 0;

	private double feverPoint;

	private boolean isFever;
	private int feverLevel;
	private long feverStartTime;
	private long feverEndTime;

	private float optionMultiply = 1;
	private boolean optionDownPoint = true;
	private int optionFeverDuration = 10000;

	private boolean feverMessage;
	
	public GameFever (GameActivity context, GameMode mode)
	{
		this.context = context;
		this.mode = mode;

		sensorManager = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
		accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		feverBar = (ProgressBar) context.findViewById(R.id.fever_bar);
		feverText = (TextView) context.findViewById(R.id.feverText);
	}

	public void onStart () {
		if (accelerormeterSensor != null) sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_GAME);
	}

	public void onStop () {
		if (sensorManager != null) sensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged (SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			float[] bacc = calculateAverageSpeed();
			float totalBacc = Math.abs(bacc[0]) + Math.abs(bacc[1]) + Math.abs(bacc[2]);

			float[] acc = speed[speedOffset = ( speedOffset + 1 ) % speed.length];
			acc[0] = event.values[SensorManager.DATA_X];
			acc[1] = event.values[SensorManager.DATA_Y];
			acc[2] = event.values[SensorManager.DATA_Z];
			float totalAcc = Math.abs(acc[0]) + Math.abs(acc[1]) + Math.abs(acc[2]);

			if (totalBacc * SHAKE_MULTIPLYER < totalAcc) {
				onShake();
			}
			if (Math.abs(acc[0] - bacc[0]) > SHAKE_THRESHOLD || Math.abs(acc[1] - bacc[1]) > SHAKE_THRESHOLD || Math.abs(acc[2] - bacc[2]) > SHAKE_THRESHOLD) {
				onShake();
			}

		}
	}

	@Override
	public void onAccuracyChanged (Sensor sensor, int accuracy) {}

	private float[] calculateAverageSpeed () {
		Arrays.fill(avgSpeed, 0);
		for (int i = 0, len = speed.length; i < len; i++) {
			avgSpeed[0] += speed[i][0] / len;
			avgSpeed[1] += speed[i][1] / len;
			avgSpeed[2] += speed[i][2] / len;
		}

		return avgSpeed;
	}

	public void onShake () {
		if (mode.getPlayType() != GameMode.PLAYTYPE_PLAY || mode.getGameStatus() != GameMode.STATUS_PLAY){
			return;
		}
		if (feverPoint >= MAX_FEVER_POINT) {
			
			isFever = true;
			feverPoint = 0;
			feverStartTime = mode.getSysTime();
			feverEndTime = feverStartTime + optionFeverDuration;
			
			feverLevel++;
			
			mode.onActionAll();
			context.doFever(true);
			
			context.runOnUiThread(new Runnable(){
				public void run(){
					context.animView(feverText, context.getString(R.string.game_fever_message, feverLevel+1));
				}
			});
			
		}
	}
	
	public void run(){
		if(isFever && mode.getSysTime() > feverEndTime ){

			isFever = false;
			context.doFever(false);
				
		}
	}

	public void addFever (boolean up) {

		if(up){
			feverPoint += 1*optionMultiply;
		}else{
			feverPoint --;
			feverLevel = 0;
			feverEndTime = 0;
		}
		context.runOnUiThread(new Runnable(){
			public void run(){
				feverBar.setProgress((int)feverPoint);
				
				if(feverPoint == MAX_FEVER_POINT){
					context.animView(feverText, R.string.game_fever_intro);
				}
			}
		});

	}

	public void clear () {
		
		feverPoint = 0;
		isFever = false;
		context.doFever(false);
		context.runOnUiThread(new Runnable(){
			public void run(){
				feverBar.setProgress(0);
			}
		});
	        
        }

	public long onGameTime(GameOption option, GameNote note, long time){
		return time;
	}

	public double onGameScore(double addScore){
		if(isFever){
			return addScore * feverLevel;
		}
		return 0;
	}
}
