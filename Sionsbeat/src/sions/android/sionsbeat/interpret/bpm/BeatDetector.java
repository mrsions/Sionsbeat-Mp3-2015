package sions.android.sionsbeat.interpret.bpm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BeatDetector
{
	
	public static final float BD_DETECTION_RATE = 12.0F;
	public static final float BD_DETECTION_FACTOR = 0.925F;

	public static final float BD_QUALITY_TOLERANCE = 0.96F;
	public static final float BD_QUALITY_DECAY = 0.95F;
	public static final float BD_QUALITY_REWARD = 7.0F;
	public static final float BD_QUALITY_STEP = 0.1F;
	public static final float BD_FINISH_LINE = 60.0F;
	public static final int BD_MINIMUM_CONTRIBUTIONS = 6;

	float BPM_MIN;
	float BPM_MAX;

	float current_bpm; 
	float winning_bpm; 
	float winning_bpm_lo; 
	float win_val;
	int win_bpm_int;
	float win_val_lo;
	int win_bpm_int_lo;
	
	float bpm_predict;
	
	boolean is_erratic;
	float bpm_offset;
	float last_timer;
	float last_update;
	float total_time;
	
	float bpm_timer;
	int beat_counter;
	int half_counter;
	int quarter_counter;
	float detection_factor;
	
	//	float quality_minimum,
	float quality_reward,
			quality_decay,
			detection_rate,
			finish_line;
	
	int minimum_contributions;
	float quality_total, 
			quality_avg, 
			ma_quality_lo, 
			ma_quality_total, 
			ma_quality_avg, 
			maa_quality_avg;
	
	// current average (this sample) for range n
	float[] a_freq_range;
	// moving average of frequency range n
	float[] ma_freq_range;
	// moving average of moving average of frequency range n
	float[] maa_freq_range;
	// timestamp of last detection for frequecy range n
	float[] last_detection;
	
	// moving average of gap lengths
	float[] ma_bpm_range;
	// moving average of moving average of gap lengths
	float[] maa_bpm_range;
	
	// range n quality attribute, good match = quality+, bad match = quality-, min = 0
	float[] detection_quality;
	
	// current trigger state for range n
	boolean[] detection; 
	
	public int BD_DETECTION_RANGES;

	HashMap < Integer, Float > bpm_contest;
	HashMap < Integer, Float > bpm_contest_lo;
	

	public BeatDetector(){
		this(85f, 200f, 128);
	}
	public BeatDetector(int range){
		this(85f, 200f, range);
	}
	public BeatDetector( float BPM_MIN_in, float BPM_MAX_in, int range ){

		bpm_contest = new HashMap< Integer, Float >();
		bpm_contest_lo = new HashMap< Integer, Float >();
		
		current_bpm = 0.0f;
		winning_bpm = 0.0f; 
		win_val = 0.0f;
		win_bpm_int = 0;
		win_val_lo = 0.0f;
		win_bpm_int_lo = 0;
		
		bpm_predict = 0;
		
		is_erratic = false;
		bpm_offset = 0.0f;
		last_timer = 0.0f;
		last_update = 0.0f;
		total_time = 0.0f;
		
		bpm_timer = 0.0f;
		beat_counter = 0;
		half_counter = 0;
		quarter_counter = 0;
		
		//	quality_minimum = BD_QUALITY_MINIMUM;
		quality_reward = BD_QUALITY_REWARD;
		detection_rate = BD_DETECTION_RATE;
		finish_line = BD_FINISH_LINE;
		minimum_contributions = BD_MINIMUM_CONTRIBUTIONS;
		detection_factor = BD_DETECTION_FACTOR;
		quality_total = 1.0f;
		quality_avg = 1.0f;
		quality_decay = BD_QUALITY_DECAY;
		ma_quality_avg = 0.001f;
		ma_quality_lo = 1.0f;
		ma_quality_total = 1.0f;
		
		BPM_MIN = BPM_MIN_in;
		BPM_MAX = BPM_MAX_in;
		
		BD_DETECTION_RANGES = range;
		a_freq_range = new float[BD_DETECTION_RANGES];
		ma_freq_range = new float[BD_DETECTION_RANGES];
		maa_freq_range = new float[BD_DETECTION_RANGES];
		last_detection = new float[BD_DETECTION_RANGES];

		ma_bpm_range = new float[BD_DETECTION_RANGES];
		maa_bpm_range = new float[BD_DETECTION_RANGES];
		detection_quality = new float[BD_DETECTION_RANGES];
		detection = new boolean[BD_DETECTION_RANGES];

		reset();
	}
	

	public void reset()
	{
		reset(true);
	}
	public void reset(boolean reset_freq)
	{
		for (int i = 0; i < BD_DETECTION_RANGES; i++) 
		{
			//			ma_bpm_range[i] = maa_bpm_range[i] = 60.0/(float)(BPM_MIN + (1.0+sin(8.0*M_PI*((float)i/(float)BD_DETECTION_RANGES))/2.0)*((BPM_MAX-BPM_MIN)/2));			
			ma_bpm_range[i] = maa_bpm_range[i] = (float) ( 60.0/(float)(BPM_MIN+5)+ ((60.0/(float)(BPM_MAX-5)-60.0/(float)(BPM_MIN+5)) * ((float)i/(float)BD_DETECTION_RANGES)) );
			if (reset_freq) 
			{
				a_freq_range[i] = ma_freq_range[i] = maa_freq_range[i] = 0;
			}
			last_detection[i] = 0;
			detection_quality[i] = 0;
			detection[i] = false;
			
		}
		
		total_time = 0;
		maa_quality_avg = 500.0f;
		bpm_offset = bpm_timer = last_update = last_timer = winning_bpm = current_bpm = win_val = win_bpm_int = 0;
		bpm_contest.clear();
		bpm_contest_lo.clear();
	}
	
	public void process(float timer_seconds, float[] fft_data)
	{
		if (last_timer == 0) { last_timer = timer_seconds; return; }	// ignore 0 start time
		
		if (timer_seconds < last_timer) { reset(); return; }
		
		float timestamp = timer_seconds;
		
		last_update = timer_seconds - last_timer;
		last_timer = timer_seconds;
		
		total_time+=last_update;
		
		int range_step = (fft_data.length/BD_DETECTION_RANGES);
		int range = 0;
		int i,x;
		float v;
		
		float bpm_floor = 60.0f /BPM_MAX;
		float bpm_ceil = 60.0f /BPM_MIN;
		
		if (current_bpm != current_bpm) current_bpm = 0;
		
		for (x=0; x<fft_data.length && range < BD_DETECTION_RANGES; x+=range_step)
		{
			a_freq_range[range] = 0;
			
			// accumulate frequency values for this range
			for (i = x; i<x+range_step; i++)
			{
				v = Math.abs(fft_data[i]);
				a_freq_range[range] += v;
			}
			
			// average for range
			a_freq_range[range] /= range_step;
			
			// two sets of averages chase this one at a 
			
			// moving average, increment closer to a_freq_range at a rate of 1.0 / detection_rate seconds
			ma_freq_range[range] -= (ma_freq_range[range]-a_freq_range[range])*last_update*detection_rate;
			// moving average of moving average, increment closer to ma_freq_range at a rate of 1.0 / detection_rate seconds
			maa_freq_range[range] -= (maa_freq_range[range]-ma_freq_range[range])*last_update*detection_rate;
			
			
			// if closest moving average peaks above trailing (with a tolerance of BD_DETECTION_FACTOR) then trigger a detection for this range 
			boolean det = (ma_freq_range[range]*detection_factor >= maa_freq_range[range]);
			
			// compute bpm clamps for comparison to gap lengths
			
			// clamp detection averages to input ranges
			if (ma_bpm_range[range] > bpm_ceil) ma_bpm_range[range] = bpm_ceil;
			if (ma_bpm_range[range] < bpm_floor) ma_bpm_range[range] = bpm_floor;
			if (maa_bpm_range[range] > bpm_ceil) maa_bpm_range[range] = bpm_ceil;
			if (maa_bpm_range[range] < bpm_floor) maa_bpm_range[range] = bpm_floor;
			
			boolean rewarded = false;
			
			// new detection since last, test it's quality
			if (!detection[range] && det)
			{
				// calculate length of gap (since start of last trigger)
				float trigger_gap = timestamp-last_detection[range];
				
				final int REWARD_VALS = 7;
				float[] reward_tolerances = new float[]{ 0.001f, 0.005f, 0.01f, 0.02f, 0.04f, 0.08f, 0.10f };  
				float[] reward_multipliers = new float[]{ 20.0f, 10.0f, 8.0f, 1.0f, 1.0f/2.0f, 1.0f/4.0f, 1.0f/8.0f };
				
				// trigger falls within acceptable range, 
				if (trigger_gap < bpm_ceil && trigger_gap > (bpm_floor))
				{		
					// compute gap and award quality
					
					for (i = 0; i < REWARD_VALS; i++)
					{
						if (Math.abs(ma_bpm_range[range]-trigger_gap) < ma_bpm_range[range]*reward_tolerances[i])
						{
							detection_quality[range] += quality_reward * reward_multipliers[i]; 
							rewarded = true;
							
						}
					}
					
					
					if (rewarded) 
					{
						last_detection[range] = timestamp;
					}
				}
				else if (trigger_gap >= bpm_ceil) // low quality, gap exceeds maximum time
				{
					// test for 2* beat
					trigger_gap /= 2.0f;
					// && Math.abs((60.0/trigger_gap)-(60.0/ma_bpm_range[range])) < 50.0
					if (trigger_gap < bpm_ceil && trigger_gap > (bpm_floor)) for (i = 0; i < REWARD_VALS; i++)
					{
						if (Math.abs(ma_bpm_range[range]-trigger_gap) < ma_bpm_range[range]*reward_tolerances[i])
						{
							detection_quality[range] += quality_reward * reward_multipliers[i]; 
							rewarded = true;
						}
					}
					
					if (!rewarded) trigger_gap *= 2.0f;
					
					// start a new gap test, next gap is guaranteed to be longer
					last_detection[range] = timestamp;					
				}
				
				
				float qmp = (detection_quality[range]/quality_avg)*BD_QUALITY_STEP;
				if (qmp > 1.0)
				{
					qmp = 1.0f;
				}
				
				if (rewarded)
				{
					ma_bpm_range[range] -= (ma_bpm_range[range]-trigger_gap) * qmp;
					maa_bpm_range[range] -= (maa_bpm_range[range]-ma_bpm_range[range]) * qmp;
				}
				else if (trigger_gap >= bpm_floor && trigger_gap <= bpm_ceil)
				{
					if (detection_quality[range] < quality_avg*BD_QUALITY_TOLERANCE && current_bpm!=0)
					{
						ma_bpm_range[range] -= (ma_bpm_range[range]-trigger_gap) * BD_QUALITY_STEP;
						maa_bpm_range[range] -= (maa_bpm_range[range]-ma_bpm_range[range]) * BD_QUALITY_STEP;
					}
					detection_quality[range] -= BD_QUALITY_STEP;
				}
				else if (trigger_gap >= bpm_ceil)
				{
					if (detection_quality[range] < quality_avg*BD_QUALITY_TOLERANCE && current_bpm!=0)
					{
						ma_bpm_range[range] -= (ma_bpm_range[range]-current_bpm) * 0.5;
						maa_bpm_range[range] -= (maa_bpm_range[range]-ma_bpm_range[range]) * 0.5;
					}
					detection_quality[range] -= quality_reward*BD_QUALITY_STEP;
				}
				
			}
			
			if ((!rewarded && timestamp-last_detection[range] > bpm_ceil) || ((det && Math.abs(ma_bpm_range[range]-current_bpm) > bpm_offset))) 
				detection_quality[range] -= detection_quality[range]*BD_QUALITY_STEP*quality_decay*last_update;
			
			// quality bottomed out, set to 0
			if (detection_quality[range] <= 0) detection_quality[range]=0.001f;
			
			
			detection[range] = det;		
			
			range++;
		}
		
		
		// total contribution weight
		quality_total = 0;
		
		// total of bpm values
		float bpm_total = 0;
		// number of bpm ranges that contributed to this test
		int bpm_contributions = 0;
		
		
		// accumulate quality weight total
		for (x=0; x<BD_DETECTION_RANGES; x++)
		{
			quality_total += detection_quality[x];
		}
		
		// determine the average weight of each quality range
		quality_avg = quality_total / (float)BD_DETECTION_RANGES;
		
		
		ma_quality_avg += (quality_avg - ma_quality_avg) * last_update * detection_rate/2.0f;
		maa_quality_avg += (ma_quality_avg - maa_quality_avg) * last_update;
		ma_quality_total += (quality_total - ma_quality_total) * last_update * detection_rate/2.0f;
		
		ma_quality_avg -= 0.98*ma_quality_avg*last_update*3.0f;
		
		if (ma_quality_total <= 0) ma_quality_total = 1.0f;
		if (ma_quality_avg <= 0) ma_quality_avg = 1.0f;
		
		float avg_bpm_offset = 0.0f;
		float offset_test_bpm = current_bpm;
		HashMap<Integer, Float> draft = new HashMap<Integer, Float>();
		HashMap<Integer, Float> fract_draft = new HashMap<Integer, Float>();
		
		{
			for (x=0; x<BD_DETECTION_RANGES; x++)
			{
				// if this detection range weight*tolerance is higher than the average weight then add it's moving average contribution 
				if (detection_quality[x]*BD_QUALITY_TOLERANCE >= ma_quality_avg)
				{
					if (maa_bpm_range[x] < bpm_ceil && maa_bpm_range[x] > bpm_floor)
					{
						bpm_total += maa_bpm_range[x];
						
						float draft_float = Math.round((60.0/maa_bpm_range[x])*1000.0);
						
						draft_float = (float) ( (Math.abs(Math.ceil(draft_float)-(60.0/current_bpm)*1000.0)<(Math.abs(Math.floor(draft_float)-(60.0/current_bpm)*1000.0)))?Math.ceil(draft_float/10.0):Math.floor(draft_float/10.0) );
						
						int draft_int = (int)(draft_float/10.0);
						
						float add_draft_value = (detection_quality[x]/quality_avg);
						if(draft.containsKey( draft_int )){
							add_draft_value = draft.get( draft_int ) + add_draft_value;
						}
						draft.put( draft_int, add_draft_value );
						
						bpm_contributions++;
						if (offset_test_bpm == 0.0) offset_test_bpm = maa_bpm_range[x];
						else 
						{
							avg_bpm_offset += Math.abs(offset_test_bpm-maa_bpm_range[x]);
						}
						
					}
				}
			}
		}
		
		// if we have one or more contributions that pass criteria then attempt to display a guess
		boolean has_prediction = (bpm_contributions>=minimum_contributions)?true:false;
		
		
		Iterator < Map.Entry < Integer, Float > > draft_it = draft.entrySet().iterator(), 
				contest_it;
		Map.Entry < Integer, Float > draft_i;
		
		if (has_prediction) 
		{

			int draft_winner=0;
			float win_max = 0;
			
			while(draft_it.hasNext()){
				draft_i = draft_it.next();
				
				if (draft_i.getValue() > win_max)
				{
					win_max = draft_i.getValue();
					draft_winner = draft_i.getKey();
				}
			}
			
			bpm_predict = (float) (60.0/(float)(draft_winner/10.0));
			
			avg_bpm_offset /= (float)bpm_contributions;
			bpm_offset = avg_bpm_offset;
			
			if (current_bpm == 0)  
			{
				current_bpm = bpm_predict; 
			}
			
			
			if (current_bpm!=0 && bpm_predict!=0) current_bpm -= (current_bpm-bpm_predict)*last_update; //*avg_bpm_offset*200.0f;	
			if (current_bpm != current_bpm || current_bpm < 0) current_bpm = 0;
			
			
			// hold a contest for bpm to find the current mode
			contest_it = bpm_contest.entrySet().iterator();
			Map.Entry < Integer, Float > contest_i;
			
			float contest_max=0;

			while(contest_it.hasNext()){
				contest_i = contest_it.next();
				
				if (contest_max < contest_i.getValue()) contest_max =contest_i.getValue(); 
				if ((contest_i.getValue()) > BD_FINISH_LINE/2.0)
				{
					int contest_lo_int = (int) Math.round((float)(contest_i.getKey())/10.0);
					
					float add_value = ((contest_i.getValue())/10.0f)*last_update;
					if(bpm_contest_lo.containsKey( contest_lo_int )){
						add_value = bpm_contest_lo.get( contest_lo_int ) + add_value;
					}
					bpm_contest_lo.put(contest_lo_int, add_value);
				}
			}
			
			// normalize to a finish line of BD_FINISH_LINE
			if (contest_max > finish_line) 
			{
				contest_it = bpm_contest.entrySet().iterator();
				while(contest_it.hasNext()){
					contest_i = contest_it.next();
					
					contest_i.setValue( (contest_i.getValue()/contest_max)*finish_line);
				}
			}
			
			contest_max = 0;

			contest_it = bpm_contest_lo.entrySet().iterator();
			while(contest_it.hasNext()){
				contest_i = contest_it.next();
				if (contest_max < contest_i.getValue()) contest_max =contest_i.getValue(); 
			}
			
			if (contest_max > finish_line) 
			{
				contest_it = bpm_contest_lo.entrySet().iterator();
				while(contest_it.hasNext()){
					contest_i = contest_it.next();
					
					contest_i.setValue( (contest_i.getValue()/contest_max)*finish_line );
				}
			}
			
			
			// decay contest values from last loop
			contest_it = bpm_contest.entrySet().iterator();
			while(contest_it.hasNext()){
				contest_i = contest_it.next();
				contest_i.setValue( contest_i.getValue() - ( contest_i.getValue()*(last_update/detection_rate) ) );
			}
			
			// decay contest values from last loop
			contest_it = bpm_contest_lo.entrySet().iterator();
			while(contest_it.hasNext()){
				contest_i = contest_it.next();
				contest_i.setValue( contest_i.getValue()- ( contest_i.getValue()*(last_update/detection_rate) ));
			}
			
			
			bpm_timer+=last_update;
			
			int winner = 0;
			int winner_lo = 0;				
			
			// attempt to display the beat at the beat interval ;)
			if (bpm_timer > winning_bpm/4.0 && current_bpm != 0)
			{		
				if (winning_bpm != 0) while (bpm_timer > winning_bpm/4.0) bpm_timer -= winning_bpm/4.0f;
				
				// increment beat counter
				
				quarter_counter++;		
				half_counter= quarter_counter/2;
				beat_counter = quarter_counter/4;
				
				// award the winner of this iteration
				int bpm_contest_int = (int)Math.round((60.0/current_bpm)*10.0);
				float bpm_contest_add_value = quality_reward;
				if( bpm_contest.containsKey( bpm_contest_int )){
					bpm_contest_add_value = bpm_contest.get( bpm_contest_int ) + bpm_contest_add_value;
				}
				bpm_contest.put( bpm_contest_int, bpm_contest_add_value );
				
				win_val = 0;
				
				// find the overall winner so far
				contest_it = bpm_contest.entrySet().iterator();
				while(contest_it.hasNext()){
					contest_i = contest_it.next();
					if (win_val < contest_i.getValue())
					{
						winner = contest_i.getKey();
						win_val = contest_i.getValue();
					}
				}
				
				if (winner != 0)
				{
					win_bpm_int = winner;
					winning_bpm = 60.0f/(float)(winner/10.0f);
				}
				
				
				win_val_lo = 0;		
				
				// find the overall winner so far
				contest_it = bpm_contest_lo.entrySet().iterator();
				while(contest_it.hasNext()){
					contest_i = contest_it.next();
					if (win_val_lo < contest_i.getValue())
					{
						winner_lo = contest_i.getKey();
						win_val_lo = contest_i.getValue();
					}
				}
				
				if (winner_lo != 0)
				{
					win_bpm_int_lo = winner_lo;
					winning_bpm_lo = 60.0f/(float)(winner_lo);
				}
			}
		}
	}


	public float getBPM ()
	{
		return win_bpm_int*0.1f;
	}
	
	

































}
