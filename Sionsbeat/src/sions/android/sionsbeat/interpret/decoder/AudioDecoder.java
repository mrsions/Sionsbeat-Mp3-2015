package sions.android.sionsbeat.interpret.decoder;

import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;

public interface AudioDecoder
{
	public Sample nextSamples() throws Exception;
	public int getFrameTime ( int frame );
	public void close() throws Exception;
	public float getBPM();
	public float getDuration();
	public int getTimeToFrame(float time);
	
	public float getMSPF();
}
