package sions.android.sionsbeat.template;

import sions.android.sionsbeat.interpret.Peak;

public class InterpretNote extends Note{

	public InterpretNote(int timing, int button){
		super(timing, button);
	}
	
	private Peak peak;
	private boolean isLower;

	public Peak getPeak ()
	{
		return peak;
	}

	public void setPeak ( Peak peak )
	{
		this.peak = peak;
	}
	
	public boolean isLower () {
		return isLower;
	}

	public void setLower (boolean isLower) {
		this.isLower = isLower;
	}

	public InterpretNote copy(){
		InterpretNote note = new InterpretNote(getTiming(), getButton());
		
		Peak peak = new Peak();
		peak.index = this.peak.index;
		peak.value = this.peak.value;
		peak.count = this.peak.count;
		
		note.peak = peak;
		
		return note;
	}

}
