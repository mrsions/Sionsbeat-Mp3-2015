package sions.android.sionsbeat.maker;

import sions.android.sionsbeat.template.Note;

public class SingleNote extends Note{

	private byte button;

	public SingleNote(int timing, int button){
		super(timing, button);
		setButton(button);
	}

	@Override
	public boolean isButton(int num){
		return button == num;
	}
	@Override
	public void setButton (int button) {
		for(int i=0; i<16; i++){
			if(((button>>i)&1) == 1){
				this.button = (byte)i;
			}
		}
	        super.setButton(button);
	}
	
	public int getSingleButton() {
		return button;
	}
	public void setSingleButton(byte button) {
		this.button = button;
		super.setButton(1<<button);
	}

}
