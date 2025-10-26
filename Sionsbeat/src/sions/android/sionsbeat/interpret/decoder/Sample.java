package sions.android.sionsbeat.interpret.decoder;

public class Sample
{
	public Sample(int frequency, float[] samples)
	{
		this.frequency = frequency;
		this.samples = samples;
	}
	
	private int frequency;
	private float[] samples;
	
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	public float[] getSamples() {
		return samples;
	}
	public void setSamples(float[] samples) {
		this.samples = samples;
	}
	
	
}
