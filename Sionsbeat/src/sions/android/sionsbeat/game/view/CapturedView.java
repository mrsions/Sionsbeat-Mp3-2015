package sions.android.sionsbeat.game.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public interface CapturedView {

	public Bitmap onCapturBitmap (Canvas canvas);
	public float getX();
	public float getY();
	
}
