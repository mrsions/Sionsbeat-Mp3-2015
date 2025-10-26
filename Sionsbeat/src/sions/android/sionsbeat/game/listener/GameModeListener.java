package sions.android.sionsbeat.game.listener;

public interface GameModeListener {
	
	public void onPerfect(int time, int pos);

	public void onGreat(int time, int pos);
	
	public void onGood(int time, int pos);

	public void onEarly(int time, int pos);

	public void onFailed(int time, int pos);

	public void onCombo(int gameTime, int pos);
	
	public int getTime();

	
}
