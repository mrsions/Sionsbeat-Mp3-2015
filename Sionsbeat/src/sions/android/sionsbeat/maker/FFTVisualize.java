package sions.android.sionsbeat.maker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;

import sions.android.sionsbeat.R;
import sions.android.sionsbeat.game.GameModeInterface;
import sions.android.sionsbeat.interpret.decoder.AudioDecoder;
import sions.android.sionsbeat.interpret.decoder.MP3Decoder;
import sions.android.sionsbeat.interpret.decoder.Sample;
import sions.android.sionsbeat.template.Note;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.fourier.FFT;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.audiofx.Visualizer;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.OverScroller;

public class FFTVisualize extends Thread{

        public static final int SAMPLE_SIZE = 1024;
        public static final int IMAGE_WIDTH = 100;
        
        private boolean run = true;
        
	private File file;
	private File root;
	
	private MP3Decoder decoder;
	
	private float singlePerSecond = -1;
	
	private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
	private int startIdx;
	private int endIdx;
	
	private MakerTimelineView timelineView;

        /*********************************************************************************************************
         * 
         * @CONSTRUCTOR
         * 
         *********************************************************************************************************/
	
	public FFTVisualize(File file) throws Exception
	{
		super("FFTVisualize");
		setPriority(Thread.MIN_PRIORITY);
		this.file = file;
		
		root = new File(GameOptions.getRootFile(), "fftTemp");
		if(root.exists())
		{
			if(!root.isDirectory()){
				root.delete();
			}else{
				for(File mfile:root.listFiles())
				{
					mfile.delete();
				}
			}
		}
		root.mkdirs();

                decoder = new MP3Decoder(new FileInputStream(file), SAMPLE_SIZE);
                
                singlePerSecond = IMAGE_WIDTH * decoder.getMSPF();

		start();
	}
	
	

        /*********************************************************************************************************
         * 
         * @RUNNING
         * 
         *********************************************************************************************************/
	
	public void dispose()
	{
		run = false;
	}
	
	public void run()
	{
		
		try{
	                
	                // first Sample
	                Sample sample = decoder.nextSamples();
	                if(sample == null) throw new NullPointerException("First sample is null");
	                
	                // create FFT
	                FFT fft = new FFT(1024, sample.getFrequency());

	                int idx = 0;
	                ArrayList<float[]> spectrums = new ArrayList<float[]>();
	                do {

	                        fft.forward(sample.getSamples());
	                        
	                        float[] spectrum = new float[SAMPLE_SIZE / 2 + 1];
	                        System.arraycopy(fft.getSpectrum(), 0, spectrum, 0, spectrum.length);
	                        
				float min = 0;
				float max = 0;
				for( int i=0; i< spectrum.length; i++)
				{
					min = Math.min( spectrum[i], min );
					max = Math.max( spectrum[i], max );
				}
				float scalingFactor = max - min;
				
				for(int i=0;i<spectrum.length; i++){
					spectrum[i] = spectrum[i] / scalingFactor;
				}
				
				spectrums.add(spectrum);
				if(spectrums.size() == IMAGE_WIDTH)
				{
					Bitmap bitmap = createBitmap(spectrums);
					storeBitmap(bitmap, idx++);
					bitmaps.add(null); //공간확보
					bitmap.recycle();
					
					spectrums.clear();
				}
				
				checkBitmaps();
	                        
	                } while (run && ( sample = decoder.nextSamples() ) != null);

			try{
				decoder.close();
			}catch(Throwable e){}
	                
	                while(run)
	                {
	                	checkBitmaps();
	                }
	                
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			try{
				decoder.close();
			}catch(Throwable e){}
		}
                
	}

	private Bitmap createBitmap (ArrayList<float[]> spectrums) {
		
		int width = spectrums.size();
		int height = spectrums.get(0).length;
		
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		int[] pixels = new int[width*height];
		
		for(int x=0, y, value, idx; x<width; x++)
		{
			float[] spectrum = spectrums.get(x);
			for(int i=0; i<spectrum.length; i++ )
			{
				y = spectrum.length-1-i;
				idx = x+(y*width);
				
				pixels[idx] = visualizeValue_Color(Math.max(0, Math.min(255, (int)( spectrum[i] * 255 ))));

			}
		}
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		
	        return bitmap;
        }

	private int visualizeValue_Color(int value){
		int r=0, g=0, b=0;
		
		if(value == 0){
			return 0;
		}
		
		if(value < 10){
			b = (int) Math.max(0, 255-(value*25.5));
		}
		
		if(value > 25){
			r = 255;
		}else if(value >2){
			r = (int)( Math.max(0, Math.min(255, value*10)));
		}
		
		if(value > 25){
			g = (int)( Math.max(0, Math.min(255, value-25)) );
		}
		
		return 0xFF000000 | ((r<<0x10)&0xFF0000) | ((g<<0x08)&0xFF00) | (b&0xFF);
	}
	private int visualizeValue_blackWhite(int value)
	{
		return 0xFF000000 | ((value<<0x10)&0xFF0000) | ((value<<0x08)&0xFF00) | (value&0xFF);
	}
	
	private void storeBitmap ( Bitmap bitmap, int idx )
	{
		FileOutputStream fos = null;
		try{
			fos = new FileOutputStream(new File(root, idx+".png"));
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			try{fos.close();}catch(Throwable e){}
		}
	}

	private void checkBitmaps()
	{
		boolean dirty = false;
		
		for(int i=0; i<bitmaps.size(); i++)
		{
			Bitmap bm = bitmaps.get(i);
			
			if( startIdx<= i && i<= endIdx)
			{
				if(bm == null)
				{
					File file = new File(root, i+".png");
					if(file.exists()){
						bitmaps.set(i, BitmapFactory.decodeFile(file.getAbsolutePath()));
						dirty = true;
					}
				}
			}
			else if(bm != null)
			{
				bm.recycle();
				bitmaps.set(i, null);
			}
		}
		
		if(dirty && timelineView != null){
			timelineView.setDirty(true);
		}		
	}

	
        /*********************************************************************************************************
         * 
         * @ETC
         * 
         *********************************************************************************************************/
	
	public BitmapResult getBitmap(float startTime, float endTime) {
		
		startIdx = Math.max(0, (int)(startTime/singlePerSecond)-1);
		endIdx = (int)Math.ceil(endTime/singlePerSecond)+1;

		if(endIdx<startIdx) return null;

		BitmapResult br = new BitmapResult();
		br.gabTime = (startIdx*singlePerSecond) - startTime;
		br.bitmap = new Bitmap[endIdx-startIdx+1];

		for(int i=0; i<br.bitmap.length; i++){
			if(i+startIdx<bitmaps.size()){
				Bitmap bm = bitmaps.get(i+startIdx);
				if(bm != null){
					br.bitmap[i] = bm;
				}
			}
		}
			
		return br;
	}


        /*********************************************************************************************************
         * 
         * @GETSET
         * 
         *********************************************************************************************************/

	public float getSinglePerSecond () {
		return singlePerSecond;
	}
	public void setSinglePerSecond (float singlePerSecond) {
		this.singlePerSecond = singlePerSecond;
	}
	public MakerTimelineView getTimelineView () {
		return timelineView;
	}
	public void setTimelineView (MakerTimelineView timelineView) {
		this.timelineView = timelineView;
	}

	public static class BitmapResult {
		
		private float gabTime;
		private Bitmap[] bitmap;
		
		
		public float getGabTime () {
			return gabTime;
		}
		public void setGabTime (float gabTime) {
			this.gabTime = gabTime;
		}
		public Bitmap[] getBitmap () {
			return bitmap;
		}
		public void setBitmap (Bitmap[] bitmap) {
			this.bitmap = bitmap;
		}
		
	}
}