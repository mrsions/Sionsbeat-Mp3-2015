package sions.android.sionsbeat.template;

public class Note {

	private int timing;
	private int button;
	private int grade = GameNote.TYPE_UNNOTE;

	public Note(int timing, int button){
		this.timing = timing;
		this.button = button;
	}
	
	public boolean isButton(int num){
		return ( ( this.button >> num ) & 0x01 ) == 1;
	}
	
	
	public int getTiming() {
		return timing;
	}
	public void setTiming(int timing) {
		this.timing = timing;
	}
	public int getButton() {
		return button;
	}
	public void setButton(int button) {
		this.button = button;
	}

	public int getGrade ()
	{
		return grade;
	}
	public void setGrade ( int grade )
	{
		this.grade = grade;
	}
	
	
}
