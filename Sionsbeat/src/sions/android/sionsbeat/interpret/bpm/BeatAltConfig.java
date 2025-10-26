package sions.android.sionsbeat.interpret.bpm;

public class BeatAltConfig
{
	public int BD_DETECTION_RANGES = 128;
	public float BD_DETECTION_RATE = 12.0f;   // Rate in 1.0 / BD_DETECTION_RATE seconds
	public float BD_DETECTION_FACTOR = 0.915f; // Trigger ratio
	public float BD_QUALITY_DECAY = 0.6f;     // range and contest decay
	public float BD_QUALITY_TOLERANCE = 0.96f;// Use the top x % of contest results
	public float BD_QUALITY_REWARD = 10.0f;    // Award weight
	public float BD_QUALITY_STEP = 0.1f;     // Award step (roaming speed)
	public float BD_MINIMUM_CONTRIBUTIONS = 6f;   // At least x ranges must agree to process a result
	public float BD_FINISH_LINE = 60.0f;          // Contest values wil be normalized to this finish line
	
	public float[] BD_REWARD_TOLERANCES = new float[]{ 0.001f, 0.005f, 0.01f, 0.02f, 0.04f, 0.08f, 0.10f, 0.15f, 0.30f };
	public float[] BD_REWARD_MULTIPLIERS = new float[]{ 20.0f, 10.0f, 8.0f, 1.0f, 1.0f/2.0f, 1.0f/4.0f, 1.0f/8.0f, 1/16.0f, 1/32.0f };
}
