package sions.android.sionsbeat.game.view;

import sions.android.sionsbeat.game.GameMode;
import sions.android.sionsbeat.game.GameModeInterface;

public interface GamePad {

	void setGameMode (GameModeInterface gameMode);

	void clear ();

	void doCombo (int combo, long sysTime);

	void setFever (boolean b);

	public void onPause();
	
	public void onResume();
	
	public void setDirty(boolean dirty);
	
	public void setGameTouchPadding(float padding);

}
