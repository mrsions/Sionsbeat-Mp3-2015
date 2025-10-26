package sions.android.sionsbeat.interpret.bpm;


public class BassKick
{

	boolean is_kick;
	
	public void process( BeatDetector det ){

		this.is_kick = (( det.detection[0] && det.detection[1]) || 
				( det.ma_freq_range[0] / det.maa_freq_range[0] )>1.4);
		
	}

	public boolean isKick ()
	{
		return is_kick;
	}

}
