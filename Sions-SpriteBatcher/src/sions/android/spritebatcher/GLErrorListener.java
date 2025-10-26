package sions.android.spritebatcher;

public interface GLErrorListener {
	
	public void glError(Exception e);
	
	public void glTracking(String category, String action, String action2, int value, boolean send);
	
}
