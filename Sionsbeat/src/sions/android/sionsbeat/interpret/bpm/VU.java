package sions.android.sionsbeat.interpret.bpm;


public class VU
{

	float[] vu_levels;
	
	public VU(){
		
	}

	public void process(BeatDetector det){
		process(det, det.last_update);
	}
	public void process(BeatDetector det, float lus){

		if(vu_levels == null) vu_levels = new float[det.BD_DETECTION_RANGES];
		
		int i;
		float det_val;
		float det_max = 0f;

		for (i = 0; i < det.BD_DETECTION_RANGES; i++)
		{
			det_val = ( det.ma_freq_range[i] / det.maa_freq_range[i]);	
			if (det_val > det_max) det_max = det_val;
		}		

		if (det_max <= 0) det_max = 1f;

		for (i = 0; i < det.BD_DETECTION_RANGES; i++)
		{
			det_val = (det.ma_freq_range[i]/det.maa_freq_range[i]); //fabs(fftData[i*2]/2.0);

			if (det_val != det_val) det_val = 0;

			//&& (det_val > this.vu_levels[i])
			if (det_val>1.0)
			{
				det_val -= 1.0;
				if (det_val>1.0) det_val = 1f;

				if (det_val > this.vu_levels[i]){
					this.vu_levels[i] = det_val;	
				}
				else if (det.current_bpm != 0){
					this.vu_levels[i] -= (this.vu_levels[i]-det_val)*lus*(1.0/det.current_bpm)*3.0;
				}
			}
			else 
			{
				if (det.current_bpm != 0){
					this.vu_levels[i] -= (lus/det.current_bpm)*2.0;
				}
			}

			if (this.vu_levels[i] < 0 || this.vu_levels[i] != this.vu_levels[i]){
				this.vu_levels[i] = 0;
			}
		}
	}

	// returns vu level for BD_DETECTION_RANGES range[x]
	public float getLevel (int x)
	{
		return this.vu_levels[x];
	}

}
