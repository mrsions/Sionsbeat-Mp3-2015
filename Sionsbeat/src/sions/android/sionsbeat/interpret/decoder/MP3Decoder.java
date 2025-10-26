package sions.android.sionsbeat.interpret.decoder;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.androidquery.util.AQUtility;

import sions.android.sionsbeat.interpret.InterpretNotSupportException;
import sions.utils.PTC;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

/**
 * JLayer 1.01을 이용한 MP3 Decoder
 * 최대 2체널까지의 데이터를 지원합니다.
 * 
 * @author sunulo
 *
 */
public class MP3Decoder implements AudioDecoder
{
	//무조건 1024!! FFT를 돌릴 생각이 있는놈이라면 무조건!! 혹은 2의 배수 1024 이상
	private static final int PCM_SIZE = 1024;
	private static final double MAX_VALUE = (double) Short.MAX_VALUE;
	
	
	//-- Header를 통한 정량적 데이터
	private int version;			//1		mp3 버전 정보 
	private int layer;				//3		레이어
	private int frequency;			//44100	주파수
	private int bitrate;			//128000	비트레이트
	private int mode;			//1		채널 모드
	private int slots;				//381		??
	private int vbr_scale;			//false	??
	private int number_of_subbands;	//27		??
	private int syncHeader;		//-290716	??
	private int frameSize;			//413		??
	
	private float mspf;			//26.122	프레임당 밀리초(ms)
	private float fps;			//dd?		초당 프레임 수
	private float duration;			//60708.5	재생 길이
	
	private boolean vbr;			//false	??
	private boolean checksums;		//false	??
	private boolean copyright;		//false	저작권
	private boolean original;		//true		오리지널 데이터
	private boolean padding;		//false	??
	
	//-- 유추 데이터
	private int channel;			//2		채널 갯수
	
	//-- 내부 데이터
	private Decoder decoder;
	private Bitstream bs = null;
	private int sampleSize;

	private Header nextHeader;
	private short[] nextBuffer;
	private int nextBufferOffset;

	public MP3Decoder ( InputStream is, int sampleSize ) throws Exception
	{
		try{
			this.sampleSize = sampleSize;
			bs = new Bitstream( is );
			
			decoder = new Decoder();
			
			nextHeader = bs.readFrame();
			SampleBuffer frame = (SampleBuffer) decoder.decodeFrame(nextHeader, bs);
			nextBuffer = frame.getBuffer();
			nextBufferOffset = 0;
			bs.closeFrame();
			
			setupHeader(nextHeader, is);
		}catch(Throwable e){
			e.printStackTrace();
			throw new InterpretNotSupportException(e.getMessage());
		}
	}
	
	private void setupHeader( Header header, InputStream is ) throws IOException
	{
		version = header.version();
		layer = header.layer();
		frequency = header.frequency();
		bitrate = header.bitrate();
		mode = header.mode();
		slots = header.slots();
		vbr_scale = header.vbr_scale();
		number_of_subbands = header.number_of_subbands();
		syncHeader = header.getSyncHeader();
		frameSize = header.calculate_framesize();
		
		//-- Channel
		channel = ( mode == Header.SINGLE_CHANNEL ) ? 1 : 2;
		duration = header.total_ms( is.available() );

		mspf = (float)( ( (double)sampleSize / ( nextBuffer.length / channel ) ) * header.ms_per_frame() );
		fps = (float) ( ( 1.0 / ( header.ms_per_frame() ) ) * 1000.0 );

		checksums = header.checksums();
		copyright = header.copyright();
		original = header.original();
		padding = header.padding();

	}
	
	int combineIndex = 0;

	@Override
	public Sample nextSamples() throws Exception
	{
		if(nextHeader == null){
			return null;
		}

		float[] samples = new float[sampleSize];
		int i;
		for(i=0; i<sampleSize; i++)
		{
			if( nextBufferOffset >= nextBuffer.length )
			{
				nextHeader = bs.readFrame();
				if(nextHeader == null) break;
				
				SampleBuffer frame = (SampleBuffer) decoder.decodeFrame(nextHeader, bs);
				nextBuffer = frame.getBuffer();
				nextBufferOffset = 0;
				bs.closeFrame();
			}

			int combineValue = 0;
			for(int c=0; c<channel; c++){
				combineValue += nextBuffer[nextBufferOffset++];
			}
			samples[i] = (float)( combineValue / MAX_VALUE / channel );
		}
		
		if( i==0 ) return null;
		
		return new Sample(frequency, samples);
	}

	@Override
	public int getFrameTime(int frame)
	{
		return (int)(frame * mspf);
	}
	
	@Override
	public void close() throws BitstreamException
	{
		bs.close();
	}
	
	@Override
	public float getBPM() {
		return -1;
	}

	@Override
	public float getDuration() {
		return duration;
	}
	
	@Override
	public int getTimeToFrame(float time)
	{
		return (int)(time/mspf);
	}

	@Override
	public float getMSPF() {
		return mspf;
	}
}
