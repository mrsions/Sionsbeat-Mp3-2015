package sions.android.spritebatcher;

import javax.microedition.khronos.opengles.GL10;

public interface Drawer {

	public boolean isDraw();

	public void onDrawFrame(GL10 gl, SpriteBatcher spriteBatcher);

	public void onDrawBeforeFrame(GL10 gl, SpriteBatcher spriteBatcher);
	
	public void onDrawAfterFrame(GL10 gl, SpriteBatcher spriteBatcher);

}
