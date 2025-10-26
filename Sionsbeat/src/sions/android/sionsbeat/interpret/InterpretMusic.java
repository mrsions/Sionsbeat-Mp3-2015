package sions.android.sionsbeat.interpret;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import android.util.Log;
import sions.android.sionsbeat.game.GameData;
import sions.android.sionsbeat.game.GameOption;
import sions.android.sionsbeat.interpret.bpm.BeatDetector;
import sions.android.sionsbeat.interpret.decoder.AudioDecoder;
import sions.android.sionsbeat.interpret.decoder.MP3Decoder;
import sions.android.sionsbeat.interpret.decoder.Sample;
import sions.android.sionsbeat.template.GameFinger;
import sions.android.sionsbeat.template.InterpretNote;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.NumericTools;
import sions.fourier.FFT;
import sions.json.JSONArray;
import sions.json.JSONObject;
import sions.utils.FloatBuffer;
import sions.utils.PTC;

public class InterpretMusic {
        private static final int SAMPLE_SIZE = 1024;
        private static final int VERSION = 1;

        public static final String TAG = "interpret";
        private static final File root = GameOptions.getRootFile();

	private static void log (String tag, Object text) {
//                 Log.d(tag, (String)text);
        }
        
        public static File getInterpretFile(SongInfo song){
                return new File(root, "analyze/" + song.getIdentity() + ".analyze");
        }

        // -- 노래 데이터
        private SongInfo song;

        // -- 분석 데이터
        private Peak[] peaks;
        private float duration;
        private float mspf;
        private float bpm;
        private int touchField = 16;

        // -- 처리 필드
        private InterpretListener listener;
        private int beforeProgress;
        private String resultPath;

        private File analyzeFile;
        private int maxLevel;
        private int maxCount;
        private boolean firstInterpret;

        /*********************************************************************************************************
         * 
         * @LifeCycle
         * 
         *********************************************************************************************************/

        public InterpretMusic (SongInfo song, InterpretListener listener)
        {

                this.listener = listener;

                this.song = song;
        }

        public void onProgress (int progress) 
        {
                if (progress != beforeProgress) {
                        log(TAG, progress+"%");
                        listener.onInterpretProgress(song, beforeProgress = progress);
                }
        }

        /*********************************************************************************************************
         * 
         * @SPECTRUM_ANALYZE
         * 
         *********************************************************************************************************/

        public static final int THRESHOLD_WINDOW_SIZE = 100;
        public static final float MULTIPLIER = 1.5f;

        /**
         * @throws Exception
         * @throws FileNotFoundException
         */
        public boolean prepareSpectrum () throws Exception {
                /**** @START_TIME_CHECK *****/
                int tpid = PTC.start();
                /**** @START_TIME_CHECK *****/
                int pid = PTC.start();

                /******************************* @ANALYZE_FILE_READ *********************************/
                analyzeFile = getInterpretFile(song);

                log(TAG, "analyze File " + analyzeFile.exists());
                // -- break를 위해서 while로 처리
                READ_ANALYZE:
                while (analyzeFile.exists()) {
                	try{
	                        FileInputStream fis = new FileInputStream(analyzeFile);
	                        int totalAvailable = fis.available();
	
	                        DataInputStream dis = new DataInputStream(fis);
	
	                        if (!( dis.readChar() == 'A' && dis.readChar() == 'N' && dis.readChar() == 'A' && dis.readChar() == 'L' && dis.readChar() == 'Y' && dis.readChar() == 'Z' && dis.readChar() == 'E' && dis.readChar() == '0' )) {
	
	                                log(TAG, "not Analaze File");
	                                break READ_ANALYZE;
	                        }
	
	                        int version = dis.readInt();
	                        if (version != VERSION) {
	                                log(TAG, "different version " + version + " != " + VERSION);
	                                break READ_ANALYZE;
	                        }
	
	                        bpm = dis.readFloat();
	                        duration = dis.readFloat();
	                        mspf = dis.readFloat();
	
	                        this.peaks = new Peak[dis.readInt()];
	                        for (int k = 0; k < this.peaks.length; k++) {
	                                Peak p = new Peak();
	
	                                p.index = dis.readInt();
	                                p.value = dis.readFloat();
	                                p.count = dis.readShort();
	
	                                this.peaks[k] = p;
	                        }
	
	                        /**** @END_TIME_CHECK *****/
	                        PTC.end(tpid, TAG, "load_analyze");
	                        return true;
                	}catch(Throwable e){
                		break;
                	}
                }

		InterpretCollector.sleep();
		
                firstInterpret = true;

                /******************************* @ANALYZE *********************************/
                File file = new File(song.getPath());
                if (!file.exists() || file.isDirectory()) {
                	throw new InterpretNotSupportException(song.getPath());
                }

                FileInputStream fis = new FileInputStream(file);
                double totalFileLength = fis.available();

//                AudioDecoder decoder = new JniMP3Decoder(fis, SAMPLE_SIZE);
                AudioDecoder decoder = new MP3Decoder(fis, SAMPLE_SIZE);

                /**** @END_TIME_CHECK *****/
                pid = PTC.restart(pid, TAG, "create MP3 Decoder to target File " + file);

                boolean result = prepareAnalyzeFFT(pid, decoder, fis, totalFileLength);

                /**** @END_TIME_CHECK *****/
                PTC.end(tpid, TAG, "complete prepareSpectrum");
                return result;
        }

        private boolean prepareAnalyzeFFT (int pid, AudioDecoder decoder, FileInputStream fis, double totalFileLength) throws Exception {
        	
                bpm = decoder.getBPM();
                mspf = decoder.getMSPF();

                Sample sample = decoder.nextSamples(); // 첫 샘플
                if(sample == null) throw new NullPointerException("First sample is null");

//                BeatDetector bd = new BeatDetector(80, 300f, 513);
                FFT fft = new FFT(1024, sample.getFrequency());

                // -- 분석용
                float[] spectrum = new float[SAMPLE_SIZE / 2 + 1];
                float[] lastSpectrum = new float[SAMPLE_SIZE / 2 + 1];

                // -- 분석용 리스트 생성
                FloatBuffer[] spectralFlux = new FloatBuffer[touchField]; // 여러 밴드를 저장하기 위함
                for (int i = 0; i < spectralFlux.length; i++) {
                        spectralFlux[i] = new FloatBuffer(2000, 1000); // 초기버퍼 2000 그 후 1000씩 추가
                }

                // -- 밴드 생성
                int bandWidth = sample.getFrequency();
                int[] bands = new int[touchField + 1];
                for (int i = 0; i < bands.length - 1; i++) {
                        int freq = (int) ( ( bandWidth * 0.362 ) * ( i / ( bands.length - 2.0 ) ) ) + 80; // 1:80 ~ 15:16000
                        bands[i] = freq;
                }
                bands[bands.length - 1] = bandWidth;

                // ----- FFT 분석
//                boolean bpmCheck = bpm <= 0;
                float flux;
                int frame_index = 0;
                do {

        		InterpretCollector.sleep();
        		
                        fft.forward(sample.getSamples());
                        System.arraycopy(spectrum, 0, lastSpectrum, 0, spectrum.length);
                        System.arraycopy(fft.getSpectrum(), 0, spectrum, 0, spectrum.length);

                        // -- 밴드별 flux 입력
                        for (int i = 0; i < touchField; i++) {

                                int startFreq = fft.freqToIndex(bands[i]);
                                int endFreq = fft.freqToIndex(bands[i + 1]);

                                flux = 0;
                                for (int j = startFreq; j <= endFreq; j++) {
                                        flux += Math.max(0, spectrum[j] - lastSpectrum[j]);
                                }
                                spectralFlux[i].add(flux);
                        }

                        // -- Bpm Flux
//                        if (bpmCheck) {
//                                bd.process(decoder.getFrameTime(frame_index++) / 1000f, spectrum);
//                        }

                        // -- Progress
                        onProgress((int) ( ( 1 - ( fis.available() / totalFileLength ) ) * 100 ));
//
//                        Thread.sleep(10);
                } while (( sample = decoder.nextSamples() ) != null);

//                if (bpmCheck) {
//                        bpm = bd.getBPM();
//                        Log.d("test",TAG + "  bpm detect : " + bpm);
//                }

                duration = decoder.getDuration();
                
                /**** @END_TIME_CHECK *****/
                pid = PTC.restart(pid, TAG, "analyze FFT");
                
//                return false;
                return prepareAnalyzeOnset(pid, spectralFlux);
        }

        /**
         * Onset
         * 
         * @param pid
         * @param spectralBuffers
         * @return
         * @throws Exception
         */
        private boolean prepareAnalyzeOnset (int pid, FloatBuffer[] spectralBuffers) throws Exception {
                int bandLength = spectralBuffers.length;
                int spectralLength = spectralBuffers[0].size();

                // -- default setup
                float[][] spectralFlux = new float[bandLength][];
                for (int i = 0; i < bandLength; i++) {
                        spectralFlux[i] = spectralBuffers[i].toArray();
                }

                float[] spectral, peak, threshold;

                // -- threshold
                float[][] thresholds = new float[bandLength][spectralLength];
                for (int k = 0; k < bandLength; k++) {
                        threshold = thresholds[k];
                        spectral = spectralFlux[k];
                        for (int i = 0; i < spectralLength; i++) {
                                int start = Math.max(0, i - THRESHOLD_WINDOW_SIZE);
                                int end = Math.min(spectralLength - 1, i + THRESHOLD_WINDOW_SIZE);
                                float mean = 0;
                                for (int j = start; j <= end; j++)
                                        mean += spectral[j];
                                mean /= ( end - start );

                                threshold[i] = mean * MULTIPLIER;
                        }
                }

                // -- prunnedSpectralFlux
                float[][] peaks = new float[bandLength][spectralLength];
                Peak[] totalPeaks = new Peak[spectralLength];
                for (int k = 0; k < bandLength; k++) {
                        peak = peaks[k];
                        threshold = thresholds[k];
                        spectral = spectralFlux[k];
                        for (int i = 0; i < spectralLength; i++) {
                                float flux = spectral[i];
                                if (threshold[i] <= flux) {
                                        // peak[i] = ( flux - threshold[i] ) / threshold[i];
                                        peak[i] = flux - threshold[i];
                                }
                        }

                        for (int i = 0; i < spectralLength; i++) {
                                Peak p = totalPeaks[i];

                                if (peak[i] != 0) {
                                        if (p == null) {
                                                totalPeaks[i] = p = new Peak();
                                                p.index = i;
                                        }
                                        p.value += peak[i];
                                        p.count++;
                                }
                        }
                }

                ArrayList<Peak> peakFlux = new ArrayList<Peak>();
                ArrayList<Peak> tempFlux = new ArrayList<Peak>();
                for (int i = 0; i < spectralLength + 1; i++) {
                        // 임의로 넣은 1개 증가된 마지막이거나 peak가 없을 때
                        if (i == spectralLength || totalPeaks[i] == null) {
                                if (tempFlux.size() > 0) {
                                        Peak temp = new Peak();
                                        Peak max = null;
                                        for (Peak p : tempFlux) {
                                                if (max == null || max.value < p.value) {
                                                        max = p;
                                                        temp.index = p.index;
                                                }
                                                temp.value += p.value;
                                                temp.count += p.count;
                                        }
                                        peakFlux.add(temp);
                                        tempFlux.clear();
                                }
                        } else {
                                tempFlux.add(totalPeaks[i]);
                        }
                }

                // BufferedImage bi = new BufferedImage( spectralLength, 513, BufferedImage.TYPE_INT_ARGB );
                // doSave( "peak.png", doVisualize( bi, peakFlux, 10, Color.red, Color.blue ) );


		InterpretCollector.sleep();
                /**** @END_TIME_CHECK *****/
                pid = PTC.restart(pid, TAG, "analyze onset");

                this.peaks = (Peak[]) peakFlux.toArray(new Peak[peakFlux.size()]);

                /**** @END_TIME_CHECK *****/
                pid = PTC.restart(pid, TAG, "save visualize");
                /******************************* @SAVE_ANALYZE_FILE *********************************/
                analyzeFile.getParentFile().mkdirs();

                DataOutputStream dos = new DataOutputStream(new FileOutputStream(analyzeFile));
                dos.writeChars("ANALYZE0");
                dos.writeInt(VERSION);

                dos.writeFloat(bpm);
                dos.writeFloat(duration);
                dos.writeFloat(mspf);

                dos.writeInt(this.peaks.length);
                for (int k = 0; k < this.peaks.length; k++) {
                        Peak p = this.peaks[k];

                        dos.writeInt(p.index);
                        dos.writeFloat(p.value);
                        dos.writeShort(p.count);

                }

                dos.close();

                /**** @END_TIME_CHECK *****/
                pid = PTC.restart(pid, TAG, "save_analyzeFile");

                return true;
        }

        /*********************************************************************************************************
         * 
         * @throws Exception
         * @DO_INTERPRET_GAME_NOTE_DATA
         * 
         *********************************************************************************************************/

        public void doInterpretation () throws Exception {
                
                /**** @START_TIME_CHECK *****/
                int pid = PTC.start();

                // BufferedImage bi = new BufferedImage( (int)(duration/mspf)+1, 500, BufferedImage.TYPE_INT_ARGB );
                // doSave("starter.png", doVisualize( bi, peaks, 1, Color.red ));

                Arrays.sort(peaks, new Comparator<Peak>() {
                        @Override
                        public int compare (Peak lhs, Peak rhs) {
                                return  NumericTools.Float.compare(rhs.value, lhs.value);
                        }
                });

                // -- find max;
                double total = 0;
                for (Peak peak : peaks) {
                        total += peak.value / peak.count;
                }

                double cut = ( total / peaks.length ) * 0.5f;
                // -- cut minimum
                int cutting = 0;
                for (int i = 0; i < peaks.length; i++) {
                        if (peaks[i].value / peaks[i].count < cut) {
                                peaks[i] = null;
                                cutting++;
                        }
                }

                //-- seed;
                long seed = 0;
                for (Peak peak : peaks) {
                        if (peak != null) {
                                seed += peak.value * peak.count;
                        }
                }


                ArrayList<InterpretNote> noteList = new ArrayList<InterpretNote>();
                for(Peak peak:peaks){
                	if(peak == null) continue;
                	
                	InterpretNote inote = new InterpretNote((int)(peak.index*mspf), 0);
                	inote.setPeak(peak);
                	noteList.add(inote);
                }
                
                Collections.sort(noteList, new Comparator<InterpretNote>() {
			@Override
                        public int compare (InterpretNote a, InterpretNote b) {
	                        return NumericTools.Integer.compare(a.getTiming(), b.getTiming());
                        }
                });


		InterpretCollector.sleep();
		
                Random random = new Random(seed);
                InterpretPattern ip = new InterpretPattern(random, mspf);
		InterpretCollector.sleep();
		
                ip.compilePattern(noteList);
                
                int size = noteList.size();
                Iterator<InterpretNote> it = noteList.iterator();
                while(it.hasNext()){
                	if(it.next().getPeak() == null){
                		it.remove();
                		
                	}
                }
                
                log(TAG, "delete "+(noteList.size()-size));

                InterpretNote[] notes = (InterpretNote[]) noteList.toArray(new InterpretNote[noteList.size()]);
                
                for (maxLevel = 1; maxLevel <= 15; maxLevel++) {
                        int storeLevel = doInterpretationLevel(random, maxLevel, notes);
                        log(TAG, "-> store "+maxLevel+" = "+storeLevel);
                        if (storeLevel == -1){
                        	maxLevel--;
                                break;
                        }else if(storeLevel != maxLevel){
                		maxLevel = storeLevel;
                		break;
                	}else{
                        	maxLevel = storeLevel;
                        }
                }
                log(TAG, "max Level : "+maxLevel);

                /**** @END_TIME_CHECK *****/
                pid = PTC.restart(pid, TAG, "complete interpret");
        }

        /*********************************************************************************************************
         * 
         * @STORE_DATA
         * 
         *********************************************************************************************************/

        public int doInterpretationLevel (Random random, int level, InterpretNote[] totalNote) throws Exception {
        	
        	int levelCount = GameOption.LEVEL[level]+((GameOption.LEVEL[level+1]-GameOption.LEVEL[level])/2);
        	levelCount *= duration / 60000;

        	log(TAG, level+" = "+totalNote.length+" / "+levelCount);
        	
        	// copy
        	InterpretNote[] notes = new InterpretNote[totalNote.length];
        	for(int i=0; i<notes.length; i++){
        		notes[i] = totalNote[i].copy();
        	}
        	
        	Arrays.sort(notes, new Comparator<InterpretNote>(){
			@Override
                        public int compare (InterpretNote a, InterpretNote b) {
				float bf = b.getPeak().value;
				float af = a.getPeak().value;
	                        return  NumericTools.Float.compare(bf, af);
                        }
        	});

        	ArrayList<InterpretNote> storeNotes = new ArrayList<InterpretNote>(totalNote.length);
        	for(InterpretNote note : notes){
        		
        		if((levelCount -= Integer.bitCount(note.getButton())) < 0) break;
        		
        		storeNotes.add(note);
        		
        	}

        	if(maxCount == storeNotes.size()){
                      return -1;
        	}
        	maxCount = storeNotes.size();

		// -- Interpret Note
		InterpretNote[] inotes = storeNotes.toArray(new InterpretNote[storeNotes.size()]);
		Arrays.sort(inotes, new Comparator<InterpretNote>() {
			@Override
			public int compare (InterpretNote lhs, InterpretNote rhs) {
				return NumericTools.Integer.compare(lhs.getTiming(), rhs.getTiming());
			}
		});
		
		level = GameData.getLevel(inotes, song.getDuration());
                store(level, inotes);

                return level;

        }

        public void store (int level, InterpretNote[] inotes) throws Exception {

                // -- 노트 기록
                File file = GameData.getNoteFile(song, level);
                GameData gm = new GameData(song, inotes, -1, -1);
                gm.store(file);

                this.resultPath = file.getAbsolutePath();
                Thread.sleep(10);

                // /****@END_TIME_CHECK*****/ pid = PTC.restart(pid, TAG, "complete store");

        }

        /*********************************************************************************************************
         * 
         * @GETSET
         * 
         *********************************************************************************************************/

        public SongInfo getSong () {
                return song;
        }

        public void setSong (SongInfo song) {
                this.song = song;
        }

        public String getPath () {
                return resultPath;
        }

        public void setPath (String path) {
                this.resultPath = path;
        }

        public InterpretListener getListener () {
                return listener;
        }

        public void setListener (InterpretListener listener) {
                this.listener = listener;
        }

        public int getMaxLevel () {
                return maxLevel;
        }

        public void setMaxLevel (int maxLevel) {
                this.maxLevel = maxLevel;
        }

}
