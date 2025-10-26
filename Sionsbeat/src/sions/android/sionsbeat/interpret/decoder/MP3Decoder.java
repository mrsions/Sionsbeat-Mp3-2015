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
 * JLayer 1.01�� �̿��� MP3 Decoder
 * �ִ� 2ü�α����� �����͸� �����մϴ�.
 * 
 * @author sunulo
 *
 */
public class MP3Decoder implements AudioDecoder
{
	//������ 1024!! FFT�� ���� ������ �ִ³��̶�� ������!! Ȥ�� 2�� ��� 1024 �̻�
	private static final int PCM_SIZE = 1024;
	private static final double MAX_VALUE = (double) Short.MAX_VALUE;
	
	
	//-- Header�� ���� ������ ������
	private int version;			//1		mp3 ���� ���� 
	private int layer;				//3		���̾�
	private int frequency;			//44100	���ļ�
	private int bitrate;			//128000	��Ʈ����Ʈ
	private int mode;			//1		ä�� ���
	private int slots;				//381		??
	private int vbr_scale;			//false	??
	private int number_of_subbands;	//27		??
	private int syncHeader;		//-290716	??
	private int frameSize;			//413		??
	
	private float mspf;			//26.122	�����Ӵ� �и���(ms)
	private float fps;			//dd?		�ʴ� ������ ��
	private float duration;			//60708.5	��� ����
	
	private boolean vbr;			//false	??
	private boolean checksums;		//false	??
	private boolean copyright;		//false	���۱�
	private boolean original;		//true		�������� ������
	private boolean padding;		//false	??
	
	//-- ���� ������
	private int channel;			//2		ä�� ����
	
	//-- ���� ������
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
