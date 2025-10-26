package sions.android.sionsbeat.game;

import sions.android.sionsbeat.template.GameNote;
import sions.android.sionsbeat.zresource.ZResource;

public interface GameModeInterface {

	public ZResource getResource();
	
	public long getSysTime();
	
	public int getNoteWidth();
	
	public GameNote[] getGameNotes();
	
	public GameOption getOption();
	
	public GameData getGameData();

	public boolean onAction( TouchEvent touch );
	
	public int getDuration();
	
	public static class TouchEvent {
		
		private int x;
		private int y;
		private int touchId;
		private boolean press;

		public TouchEvent (int x, int y, int touchId)
                {
	                this.x = x;
	                this.y = y;
	                this.touchId = touchId;
                }

		public TouchEvent (int x, int y, int touchId, boolean press)
                {
	                this.x = x;
	                this.y = y;
	                this.touchId = touchId;
	                this.press = press;
                }

		public void set(int x, int y, int touchId, boolean press)
                {
	                this.x = x;
	                this.y = y;
	                this.touchId = touchId;
	                this.press = press;
                }
		
		
		public int getX () {
			return x;
		}
		public void setX (int x) {
			this.x = x;
		}
		public int getY () {
			return y;
		}
		public void setY (int y) {
			this.y = y;
		}
		public int getTouchId () {
			return touchId;
		}
		public void setTouchId (int touchId) {
			this.touchId = touchId;
		}
		public boolean isPress () {
			return press;
		}
		public void setPress (boolean press) {
			this.press = press;
		}
		
	}
}
