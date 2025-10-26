package sions.android.sionsbeat.interpret;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import sions.android.sionsbeat.template.InterpretNote;

public class InterpretPattern {

	private static final int TYPE_SPLIT = 0;
	private static final int TYPE_MIRROR = 1;
	private static final int TYPE_ANCHOR = 2;
	private static final int TYPE_LINE = 3;

	public InterpretPattern(Random random, float mspf){
		this.random = random;
		this.mspf = mspf;
		
		createPatterns();
	}
	
	private final int PAD = 16;

	private ArrayList<Pattern> patterns = new ArrayList<Pattern>();
	private ArrayList<Pattern> splitPatterns = new ArrayList<Pattern>();	//default
	private ArrayList<Pattern> mirrorPattern = new ArrayList<Pattern>();	//300이하
	private ArrayList<Pattern> anchorPattern = new ArrayList<Pattern>();	//150이하
	private ArrayList<Pattern> linePattern = new ArrayList<Pattern>();	//짧은 구간용 120 이하
	private Random random;
	private float mspf;
	
	
	/***********************************************************************************************************
	 * 
	 * @CREATE_PATTERN
	 * 
	 ***********************************************************************************************************/
	
	private void createPatterns(){
		
		for(int i=0; i<4; i++){
			createPattern(i);
		}
		
		for(int i=0; i<10; i++){
			createPattern(random.nextInt(4));
			
		}

		patterns.addAll(splitPatterns);
		patterns.addAll(mirrorPattern);
		patterns.addAll(anchorPattern);
		patterns.addAll(linePattern);
		
	}
	
	private void createPattern(int type){
		Pattern ptnc = null;
		ArrayList<Pattern> list = null;
		
		switch(type){
			case TYPE_SPLIT:
				ptnc = createSplitPatterns();
				list = splitPatterns;
				break;
			case TYPE_MIRROR:
				ptnc = createMirrorPattern();
				list = mirrorPattern;
				break;
			case TYPE_ANCHOR:
				ptnc = createAnchorPattern();
				list = anchorPattern;
				break;
			case TYPE_LINE:
				ptnc = createLinePattern();
				list = linePattern;
				break;
		}
		
		list.add(ptnc);
		list.add(new Pattern(reverseHorizontalPattern(ptnc.pattern), ptnc.type));
		list.add(new Pattern(reversePattern(ptnc.pattern), ptnc.type));
		list.add(new Pattern(reverseHorizontalPattern(reversePattern(ptnc.pattern)), ptnc.type));
		
	}
	
	private Pattern createSplitPatterns(){
		
		int[] pattern = new int[PAD];
		boolean[] patternButton = new boolean[PAD];
		
		for(int i=0; i<16; i+=2){
			for(int j=0; j<2; j++){
				
				int k=-1;
				while(k == -1){
					int temp = random.nextInt(8);
					k = ((temp/2)*4)+(temp%2) + (j*2);
					if(patternButton[k]){
						k=-1;
					}
				}
				
				patternButton[k] = true;
				pattern[i+j] = k;
				
			}
		}
		
		Pattern result = new Pattern(pattern, TYPE_SPLIT);
		result.width = 4;
		result.height =4;
		
		return result;
	}
	
	private Pattern createMirrorPattern(){

		//-- 근본이 되는 패턴조합
		int[] pattern = new int[PAD]; //오른쪽은 버린다고 보면 된다.
		boolean[] patternButton = new boolean[PAD];

		for(int i=0; i<16; i+=2){
			int k=-1;
			while(k == -1){
				int temp = random.nextInt(8);
				k = ((temp/2)*4)+(temp%2);
				if(patternButton[k]){
					k=-1;
				}
			}

			patternButton[k] = true;
			pattern[i] = k;

			k = ((k/2)*2) + (3-(k%2));
			patternButton[k] = true;
			pattern[i+1] = k;
		}
		
		Pattern result = new Pattern(pattern, TYPE_MIRROR);
		result.width = 4;
		result.height =4;
		
		return result;
	}
	
	private Pattern createAnchorPattern(){

		//-- 근본이 되는 패턴조합
		int[] pattern = new int[PAD]; //오른쪽은 버린다고 보면 된다.
		boolean[] patternButton = new boolean[PAD];

		int key = random.nextInt(16);
		pattern[0] = key;
		patternButton[key] = true;

		int adx=0, ady=0, i=1, x, y;
		GLOB:
		for(; i<16; i++){
			x = key%4;
			y = key/4;
			
			for(int kk=0; kk<100; kk++){
				
				do{
					adx = random.nextInt(3)-1;
					ady = random.nextInt(3)-1;
				}while( adx==0&&ady==0 );
				
				if(adx+x>=4) continue;
				else if(adx+x<0) continue;
				else if(ady+y>=4) continue;
				else if(ady+y<0) continue;
				
				key = ((adx+x)+((ady+y)*4));
				if(patternButton[key]){
					continue;
				}

				pattern[i] = key;
				patternButton[key] = true;
				
				continue GLOB;
			}
			
			i--;
			break GLOB;
		}

		
		Pattern rst = new Pattern(pattern, TYPE_ANCHOR);
		rst.width = 4;
		rst.height =4;
		
		if(i != 16){
			rst.pattern = new int[i+1];
			System.arraycopy(pattern, 0, rst.pattern, 0, i+1);
		}
		
		return rst;
		
	}
	
	private Pattern createLinePattern(){

		//-- 근본이 되는 패턴조합
		int[] pattern = new int[PAD]; //오른쪽은 버린다고 보면 된다.
		boolean[] patternButton = new boolean[PAD];

		boolean type = random.nextBoolean(); // true: x랜덤 / false : y랜덤
		
		for(int i=0; i<4; i++){
			
			int baseX = type? random.nextInt(4): random.nextInt(2)*3;
			int baseY = type? random.nextInt(2)*3: random.nextInt(4);
			
			if(patternButton[baseX+(baseY*4)]){
				i--;
				continue;
			}

			for(int j=0; j<4; j++){
				int x = Math.abs( type? baseX 				: Math.max(baseX,0)-j );
				int y = Math.abs( type? Math.max(baseY,0)-j	: baseY );

				int key = x+(y*4);
				patternButton[key] = true;
				pattern[i*4 + j] = key;
			}
		}
		
		Pattern rst = new Pattern(pattern, TYPE_LINE);
		rst.width = 4;
		rst.height =4;
		
		return rst;
		
	}

	private int[] reversePattern(int[] pattern){
		
		int[] result = new int[pattern.length];
		System.arraycopy(pattern, 0, result, 0, result.length);
		
		for(int i=0; i<result.length; i++)
		{
			result[i] = 15 -result[i];
		}
		
		return result;
	}
	
	private int[] reverseHorizontalPattern(int[] pattern){
		
		int[] result = new int[pattern.length];
		System.arraycopy(pattern, 0, result, 0, result.length);
		
		for(int i=0; i<result.length; i++)
		{
			int btn = result[i];
			result[i] = ((btn/4)*4) + (3-(btn%4));
		}
		
		return result;
	}
	

	/***********************************************************************************************************
	 * 
	 * @COMPILE_NOTE
	 * 
	 ***********************************************************************************************************/
	
	
	public void compilePattern(ArrayList<InterpretNote> notes) throws Exception {

		inputPatterns(notes);
		
		boolean[] gamePad = new boolean[16];
		ArrayList<Integer> tempList = new ArrayList<Integer>();

		//-- 패턴으로 입력된것 중 1개만 있는 것은 +모양내에 남는게 있다면 추가한다. 
		for(int i=0; i<notes.size(); i++){
			InterpretNote note = notes.get(i);
			if(Integer.bitCount(note.getButton()) == 1){

				Arrays.fill(gamePad, true);
				for(int st = i-1; st>=0; st--){
					InterpretNote tnote = notes.get(st);
					
					if(note.getTiming()-tnote.getTiming() > 1500){
						break;
					}

					for(int b=0; b<16; b++){
						if(((tnote.getButton() >> b) & 1) == 1){
							gamePad[b] = false;
						}
					}
				}
				for(int st = i+1; st<notes.size(); st++){
					InterpretNote tnote = notes.get(st);
					
					if(tnote.getTiming()-note.getTiming() > 1500){
						break;
					}

					for(int b=0; b<16; b++){
						if(((tnote.getButton() >> b) & 1) == 1){
							gamePad[b] = false;
						}
					}
				}
				
				int key = -1;
				for(int b=0; b<16; b++){
					if(((note.getButton() >> b)&1)==1){
						key = b;
						break;
					}
				}
				
				if(key != -1){

					for(int j=0; j<4; j++){

						int x = key%4;
						int y = key/4;
						
						switch(j){
							case 0: x--; break;
							case 1: x++; break;
							case 2: y--; break;
							case 3: y++; break;
						}

						x = Math.min(0, Math.max(3, x));
						y = Math.min(0, Math.max(3, y));
						
						if(gamePad[x+(y*4)]){
							tempList.add(x+(y*4));
						}
						
					}
					
					if(tempList.size() > 0){
		
						int btn = tempList.get(random.nextInt(tempList.size()));
						
						note.setButton(note.getButton() | (1 << btn));
						note.setLower(true);
						
						tempList.clear();
						
					}
				
				}
			
			}
		}

		InterpretCollector.sleep();
		
		//-- 입력되지 않은 노트들을 입력한다.
		ArrayList<int[]> doubleList = new ArrayList<int[]>();
		for(int i=0; i<notes.size(); i++){
			InterpretNote note = notes.get(i);
			if(note.getButton() != 0) continue;

			Arrays.fill(gamePad, true);
			for(int st = i-1; st>=0; st--){
				InterpretNote tnote = notes.get(st);
				
				if(note.getTiming()-tnote.getTiming() > 1500){
					break;
				}

				for(int b=0; b<16; b++){
					if(((tnote.getButton() >> b) & 1) == 1){
						gamePad[b] = false;
					}
				}
			}
			for(int st = i+1; st<notes.size(); st++){
				InterpretNote tnote = notes.get(st);
				
				if(tnote.getTiming()-note.getTiming() > 1500){
					break;
				}

				for(int b=0; b<16; b++){
					if(((tnote.getButton() >> b) & 1) == 1){
						gamePad[b] = false;
					}
				}
			}

			try{
				for(int b=0; b<16; b++){
					if(gamePad[b]){
						tempList.add(b);
					}
				}
				
				if(tempList.size() > 1){
					
					for(int j=0; j<tempList.size(); j++){
						int jidx = tempList.get(j);
						int jx = Math.min(0, Math.max(3,jidx%4));
						int jy = Math.min(0, Math.max(3,jidx/4));
						for(int k=j+1; k<tempList.size(); k++){
							int kidx = tempList.get(k);
							int kx = Math.min(0, Math.max(3,kidx%4));
							int ky = Math.min(0, Math.max(3,kidx/4));
							
							if((kx == jx && Math.abs(ky-jy)==1) ||
									(ky == jy && Math.abs(kx-jx)==1)){
								doubleList.add(new int[]{jidx, kidx});
							}
						}
					}
					
					if(doubleList.size() > 0){
						int[] btn = doubleList.get(random.nextInt(doubleList.size()));
						
						note.setButton( (1<<btn[0]) | (1<<btn[1]));
						continue;
					}
					
				}
				
				if(tempList.size() == 1){
	
					int btn = tempList.get(0);
					note.setButton(1 << btn);
					continue;
					
				}else{
					
					note.setPeak(null);
					
				}
			}finally{
				tempList.clear();
				doubleList.clear();
			}
		}
		
		for(int i=0; i<notes.size(); i++){

			InterpretNote note = notes.get(i);
			
			int bitCount = Integer.bitCount(note.getButton());
			if(bitCount == 2){
				
				int firstBits = -1, secondBits = -1;
				for(int b=0; b<16; b++){
					if(((note.getButton()>>b)&1)==1){
						if(firstBits == -1){
							firstBits = b;
						}else{
							secondBits = b;
						}
					}
				}
				
				InterpretNote nextNote = new InterpretNote(note.getTiming(), 0);
				
				Peak peak = new Peak();
				peak.count = note.getPeak().count;
				peak.index = note.getPeak().index;
				peak.value = note.isLower() ? 0.0001f : note.getPeak().value*0.3f;
				nextNote.setPeak(peak);
				
				if(random.nextBoolean()){
					note.setButton(1<<firstBits);
					nextNote.setButton(1<<secondBits);
				}else{
					note.setButton(1<<secondBits);
					nextNote.setButton(1<<firstBits);
				}
				
				notes.add(++i, nextNote);
				
			}else if(bitCount == 0 || note.getPeak() == null){
				notes.remove(i--);
			}
			
		}

	}

	private void inputPatterns(ArrayList<InterpretNote> notes) throws Exception {
		SplitNote beforeNote = null, note = null, nextNote = null;
		ArrayList<Pattern> targetList = null;
		Pattern ptn;
		
		ArrayList<SplitNote> list = getSplitNotes(notes);
		InterpretCollector.sleep();

		//-- 개별 패턴을 입력한다.
		beforeNote = null;
		for(int i=0; i<list.size(); i++){
			note = list.get(i);

			if(beforeNote == null || note.getStarterTiming() - beforeNote.getEnderTiming() > 1500){
				
				int noteSpace = (note.getEnderTiming() - note.getStarterTiming()) / note.notes.length;
				
				if(noteSpace < 120){
					targetList = linePattern;
				}else if(noteSpace < 150){
					targetList = anchorPattern;
				}else if(noteSpace < 300){
					targetList = random.nextBoolean() ? mirrorPattern : splitPatterns;
				}else{
					targetList = random.nextBoolean() ? mirrorPattern : splitPatterns;
				}
				
				Pattern pattern = targetList.get(random.nextInt(targetList.size()));
				(beforeNote = note).pattern = pattern;

				
			}
			
		}
		
		//-- 패턴 적용
		for(SplitNote nt:list){
			if((ptn = nt.pattern) == null) continue;
			
			int noteSpace = (nt.getEnderTiming() - nt.getStarterTiming()) / nt.notes.length;
			
			int[] gamePad = new int[PAD];
			Arrays.fill(gamePad, -6000);
			
			//-- 일반적이게 2개씩 넣는 라인
			if((nt.notes.length <= 8 && ptn.pattern.length>=16) ||  noteSpace > (2000/(ptn.pattern.length/2))){
				
				//넣기
				for(int i=0; i<nt.notes.length; i++){
					note = nt.notes[i];
					
					int btn1 = ptn.pattern[(i*2)%ptn.pattern.length];
					int btn2 = ptn.pattern[((i*2)+1)%ptn.pattern.length];

					if(gamePad[btn1]+1500 > note.inote.getTiming()) note.inote.setTiming(gamePad[btn1]+1500);
					if(gamePad[btn2]+1500 > note.inote.getTiming()) note.inote.setTiming(gamePad[btn2]+1500);
					
					gamePad[btn1] = note.inote.getTiming();
					gamePad[btn2] = note.inote.getTiming();
					
					int btn = (1<<btn1) | (1<<btn2);
					note.inote.setButton(btn);

				}
				
			}else{

				int doubleCount = 0;
				int singleCount = 0;
				int rndCount = 0;
				int ttlCount = 0;
				long rndSeed = 0;
				
				//-- 전체를 2개씩 해서 다 들어가는지를 검사한다.
				Arrays.fill(gamePad, -6000);
				for(int i=0, pi=0; i<nt.notes.length; i++){
					note = nt.notes[i];

					int btn1 = ptn.pattern[(i*2)%ptn.pattern.length];
					int btn2 = ptn.pattern[((i*2)+1)%ptn.pattern.length];
					
					if(gamePad[btn1]+1500 <= note.inote.getTiming()){
						doubleCount++;
						gamePad[btn1] = note.inote.getTiming();
					}
					if(gamePad[btn2]+1500 <= note.inote.getTiming()){
						doubleCount++;
						gamePad[btn2] = note.inote.getTiming();
					}

				}
				
				//-- 더블이 다 들어가지 않는 녀석들은 싱글과 랜덤 검사를 통해서 더 높은 쪽을 선택하게된다.
				if(doubleCount < nt.notes.length*2){

					//-- 한개씩은 다 들어가는지 검사한다.
					Arrays.fill(gamePad, -6000);
					for(int i=0, pi=0; i<nt.notes.length; i++){
						note = nt.notes[i];
	
						int btn = ptn.pattern[(pi++)%ptn.pattern.length];
	
						if(gamePad[btn]+1500 <= note.inote.getTiming()){
							gamePad[btn] = note.inote.getTiming();
							singleCount++;
						}
						
					}
	
					//렌덤으로 넣어보자 +ㅁ+
					for(int ll=0; ll<1000; ll++){
						
						long randomSeed = random.nextLong();
						Random rnd = new Random(randomSeed);
						
						int count = 0;
						int totalCount = 0;
						
						Arrays.fill(gamePad, -6000);
						
						for(int i=0, pi=0; i<nt.notes.length; i++){
							note = nt.notes[i];
							
							if(rnd.nextBoolean()){
								
								totalCount += 2;
	
								int btn1 = ptn.pattern[pi++%ptn.pattern.length];
								int btn2 = ptn.pattern[pi++%ptn.pattern.length];
	
								if(gamePad[btn1]+1500 <= note.inote.getTiming()){
									count++;
									gamePad[btn1] = note.inote.getTiming();
									
								}
								if(gamePad[btn2]+1500 <= note.inote.getTiming()){
									count++;
									gamePad[btn2] = note.inote.getTiming();
									
								}
								
								
							}else{
								
								totalCount += 1;
	
								int btn = ptn.pattern[(pi++)%ptn.pattern.length];
								
								if(gamePad[btn]+1500 <= note.inote.getTiming()){
									gamePad[btn] = note.inote.getTiming();
									count++;
								}
								
							}
		
						}
						
						if(totalCount == count){
							randomSeed = rndSeed;
							rndCount = count;
							ttlCount = totalCount;
							break;
						}else if(rndCount < count){
							rndCount = count;
							randomSeed = rndSeed;
							ttlCount = totalCount;
						}
					
					}
				
				}

				/************
				 * 
				 * 실제 검사 영역이 적용되는 구간.
				 * 
				 ************/
				
				Arrays.fill(gamePad, -6000);
				
				//-- 싱글
				if(singleCount>=doubleCount && singleCount>=rndCount){

					for(int i=0, pi=0; i<nt.notes.length; i++){
						note = nt.notes[i];

						int btn = ptn.pattern[(pi++)%ptn.pattern.length];

						if(gamePad[btn]+1500 <= note.inote.getTiming()){
							gamePad[btn] = note.inote.getTiming();
							note.inote.setButton(1<<btn);
						}
						
					}
					
				}
				//-- 더블
				else if(doubleCount>=singleCount && doubleCount>=rndCount){

					for(int i=0, pi=0; i<nt.notes.length; i++){
						note = nt.notes[i];

						int btn1 = ptn.pattern[(i*2)%ptn.pattern.length];
						int btn2 = ptn.pattern[((i*2)+1)%ptn.pattern.length];
						int btn = 0;
						
						if(gamePad[btn1]+1500 <= note.inote.getTiming()){
							gamePad[btn1] = note.inote.getTiming();
							btn |= 1 << btn1;
						}
						if(gamePad[btn2]+1500 <= note.inote.getTiming()){
							gamePad[btn2] = note.inote.getTiming();
							btn |= 1 << btn2;
						}
						
						note.inote.setButton(btn);

					}
					
				}
				//-- 랜덤
				else{
					
					Random rnd = new Random(rndSeed);
					for(int i=0, pi=0; i<nt.notes.length; i++){
						note = nt.notes[i];
						
						if(rnd.nextBoolean()){
							
							int btn1 = ptn.pattern[pi++%ptn.pattern.length];
							int btn2 = ptn.pattern[pi++%ptn.pattern.length];
							int btn = 0;

							if(gamePad[btn1]+1500 <= note.inote.getTiming()){
								gamePad[btn1] = note.inote.getTiming();
								btn |= 1 << btn1;
							}
							if(gamePad[btn2]+1500 <= note.inote.getTiming()){
								gamePad[btn2] = note.inote.getTiming();
								btn |= 1 << btn2;
							}
							
							note.inote.setButton(btn);
							
							
						}else{
							
							int btn = ptn.pattern[(pi++)%ptn.pattern.length];

							if(gamePad[btn]+1500 <= note.inote.getTiming()){
								gamePad[btn] = note.inote.getTiming();
								note.inote.setButton(1<<btn);
							}
						}
					}
				}

				InterpretCollector.sleep();
			}
		}

		//-- 패턴 없는 놈들에게 현실을 반영한 패턴을 하사하도록 하겠음돠.
		for(int k=0; k<list.size(); k++){
			SplitNote nt = list.get(k);
			if(nt.pattern != null) continue;
			
			int rndCount = 0;
			int rndSeed = 0;
			Pattern rndPattern = null;

			int[] beforeGamePad = new int[16];
			int[] nextGamePad = new int[16];
			int[] gamePad = new int[16];
			Arrays.fill(beforeGamePad, -5000);
			Arrays.fill(nextGamePad, Integer.MAX_VALUE-5000);

			//-- beforeNote의 정보 입력
			try{
				int kk=k-1;
				for(int duration=3000; kk>=0; kk--){
					SplitNote bnt = list.get(kk);
					duration -= bnt.duration+bnt.end;
					if(duration < 0){
						break;
					}
				}
				
				for(; kk<k; kk++){
					SplitNote bnt = list.get(kk);
					for(SplitNote nnt:bnt.notes){
						for(int b=0; b<16; b++){
							if(((nnt.inote.getButton()>>b)&1) == 1){
								beforeGamePad[b] = nnt.inote.getTiming();
							}
						}
					}
				}
			}catch(ArrayIndexOutOfBoundsException e){}
			
			//-- nextNote의 정보 입력
			try{
				int kk=k+1;
				for(int duration=3000; kk<list.size(); kk++){
					SplitNote bnt = list.get(kk);
					duration -= bnt.duration+bnt.end;
					if(duration < 0){
						break;
					}
				}
				
				kk = Math.min(list.size()-1, kk);
				for(; kk>k; kk--){
				
					SplitNote bnt = list.get(kk);
					for(int z=bnt.notes.length-1; z>=0; z--){
						SplitNote nnt = bnt.notes[z];
						for(int b=0; b<16; b++){
							if(((nnt.inote.getButton()>>b)&1) == 1){
								nextGamePad[b] = nnt.inote.getTiming();
							}
						}
					}
					
				}
			}catch(ArrayIndexOutOfBoundsException e){}

			for(int ii=0; ii<20; ii++){
				
				ptn = patterns.get(random.nextInt(patterns.size()));

				for (int ll = 0; ll < 1000; ll++) {

					long randomSeed = random.nextLong();
					Random rnd = new Random(randomSeed);

					int count = 0;
					int totalCount = 0;

					System.arraycopy(beforeGamePad, 0, gamePad, 0, gamePad.length);

					for (int i = 0, pi = 0; i < nt.notes.length; i++) {
						note = nt.notes[i];

						if (rnd.nextBoolean()) {

							totalCount += 2;

							int btn1 = ptn.pattern[pi++ % ptn.pattern.length];
							int btn2 = ptn.pattern[pi++ % ptn.pattern.length];

							if (gamePad[btn1] + 1500 <= note.inote.getTiming() && note.inote.getTiming()+1500 <= nextGamePad[btn1]) {
								count++;
								gamePad[btn1] = note.inote.getTiming();

							}
							if (gamePad[btn2] + 1500 <= note.inote.getTiming() && note.inote.getTiming()+1500 <= nextGamePad[btn2]) {
								count++;
								gamePad[btn2] = note.inote.getTiming();

							}

						} else {

							totalCount += 1;

							int btn = ptn.pattern[( pi++ ) % ptn.pattern.length];

							if (gamePad[btn] + 1500 <= note.inote.getTiming() && note.inote.getTiming()+1500 <= nextGamePad[btn]) {
								gamePad[btn] = note.inote.getTiming();
								count++;
							}

						}

					}

					if (totalCount == count) {
						randomSeed = rndSeed;
						rndPattern = ptn;
						rndCount = count;
						break;
					} else if (rndCount < count) {
						rndCount = count;
						rndPattern = ptn;
						randomSeed = rndSeed;
					}

				}
				
			}
			
			if(rndPattern == null){
				continue;
			}
			
			System.arraycopy(beforeGamePad, 0, gamePad, 0, gamePad.length);

			nt.pattern = ptn = rndPattern;
			Random rnd = new Random(rndSeed);
			for(int i=0, pi=0; i<nt.notes.length; i++){
				note = nt.notes[i];
				
				if(rnd.nextBoolean()){
					
					int btn1 = ptn.pattern[pi++%ptn.pattern.length];
					int btn2 = ptn.pattern[pi++%ptn.pattern.length];
					int btn = 0;

					if(gamePad[btn1]+1500 <= note.inote.getTiming() && note.inote.getTiming()+1500 <= nextGamePad[btn1]){
						gamePad[btn1] = note.inote.getTiming();
						btn |= 1 << btn1;
					}
					if(gamePad[btn2]+1500 <= note.inote.getTiming() && note.inote.getTiming()+1500 <= nextGamePad[btn2]){
						gamePad[btn2] = note.inote.getTiming();
						btn |= 1 << btn2;
					}
					
					note.inote.setButton(btn);
					
					
				}else{
					
					int btn = ptn.pattern[(pi++)%ptn.pattern.length];

					if(gamePad[btn]+1500 <= note.inote.getTiming() && note.inote.getTiming()+1500 <= nextGamePad[btn]){
						gamePad[btn] = note.inote.getTiming();
						note.inote.setButton(1<<btn);
					}
				}
			}

			InterpretCollector.sleep();
			
		}
	}
	

	/***********************************************************************************************************
	 * 
	 * @SPLICE_SPLIT_NOTE
	 * 
	 ***********************************************************************************************************/
	
	/**
	 * InterpretNote를 SplitNote로 변환한다
	 * 
	 * @param notes
	 * @return ArrayList<SplitNote>
	 * @throws InterpretNotSupportException 
	 */
	private ArrayList<SplitNote> getSplitNotes(ArrayList<InterpretNote> notes) throws InterpretNotSupportException{
		
		
		ArrayList<SplitNote> list = new ArrayList<SplitNote>();
		{
			SplitNote bnote = null, snote = null;
			for(InterpretNote in:notes){
				list.add(snote = new SplitNote(in));
				
				if(bnote == null){
					bnote = snote;
				}else{
					bnote.end = snote.start = snote.inote.getTiming() - bnote.inote.getTiming();
					bnote = snote;
				}
			}
		}
		
		if(list.size() == 0) throw new InterpretNotSupportException("intepret peak pattern is not");
		SplitNote master = new SplitNote((SplitNote[]) list.toArray(new SplitNote[list.size()]));
		procSpliting(master);
		
		list = compileArrayNote(master, new ArrayList<SplitNote>());
		for(int i=0; i<list.size()-1; i++){
			SplitNote bnote = list.get(i);
			SplitNote snote = list.get(i+1);

			float av1 = averageNoteEnd(bnote);
			float av2 = averageNoteEnd(snote);
			
			if( ((av1+av2)/2) > bnote.end ) {
				
				SplitNote[] dest = new SplitNote[bnote.notes.length + snote.notes.length];
				System.arraycopy(bnote.notes, 0, dest, 0, bnote.notes.length);
				System.arraycopy(snote.notes, 0, dest, bnote.notes.length, snote.notes.length);
				
				bnote.notes = dest;
				bnote.end = snote.end;
				bnote.duration = bnote.getEnderTiming() - bnote.getStarterTiming();
				list.remove(i+1);
				
			}
		}
		
//		for(SplitNote note:list){
//			Log.d("test",note.start+"\t"+note.end+"\t"+note.duration+"\t"+note.notes.length);
//		}
		
		return list;
		
	}
	
	/**
	 * 내부 노트의 평균 간격을 구한다.
	 * @param note
	 * @return
	 */
	private float averageNoteEnd(SplitNote note){
		float total = 0;
		int count = 0;
		for(SplitNote n:note.notes){
			if(n.end != Integer.MAX_VALUE){
				total += n.end;
				count++;
			}
		}
		
		return total / count;
	}
	
	/**
	 * 조각된 노트를 한개의 list에 모은다.
	 * 
	 * @param note
	 * @param list
	 * @return
	 */
	private ArrayList<SplitNote> compileArrayNote(SplitNote note, ArrayList<SplitNote> list){
		if(note.notes != null){
			if(note.notes[0].notes == null){
				list.add(note);
			}else{
				for(SplitNote n:note.notes){
					compileArrayNote(n, list);
				}
			}
		}
		return list;
	}
	
	
	/**
	 * 내부 노트를 가장 큰 end를 가진 녀석으로부터 나눈다.
	 * 
	 * @param master
	 */
	private void procSpliting(SplitNote master){
		
		SplitNote[] mnotes = master.notes;
		
		int max = 0;
		int maxIndex = -1;
		
		for(int i=5; i<mnotes.length-5; i++){
			SplitNote note = mnotes[i];
			
			if(note.end < 100);
			else if(note.end > max){
				max = note.end;
				maxIndex = i;
			}
		}
		
		if(maxIndex == -1) return;

		SplitNote[] front = new SplitNote[maxIndex];
		SplitNote[] back = new SplitNote[mnotes.length-maxIndex];
		
		System.arraycopy(mnotes, 0, front, 0, front.length);
		System.arraycopy(mnotes, maxIndex, back, 0, back.length);
		
		
		master.notes = new SplitNote[]{ new SplitNote(front), new SplitNote(back) };
		for(SplitNote note:master.notes){
			procSpliting(note);
		}
		
	}
	
	
	/***********************************************************************************************************
	 * 
	 * @INNER_CLASS
	 * 
	 ***********************************************************************************************************/
	
	/**
	 * 내부 클레스. SplitNote
	 * 
	 * @author sunulo
	 *
	 */
	private class SplitNote{

		public SplitNote(InterpretNote note){
			this.inote = note;
		}
		public SplitNote(SplitNote[] notes){
			this.notes = notes;
			this.start = notes[0].start;
			this.end = notes[notes.length-1].end;
			this.duration = getEnderTiming() - getStarterTiming();
		}
		
		private InterpretNote inote;
		private SplitNote[] notes;
		private int duration;
		private int start = Integer.MIN_VALUE;
		private int end = Integer.MAX_VALUE;
		private Pattern pattern;

		
		public int getStarterTiming(){
			if(inote != null){
				return inote.getTiming();
			}else{
				if(notes != null && notes.length> 0){
					return notes[0].getStarterTiming();
				}
			}
			return Integer.MIN_VALUE;
		}
		public int getEnderTiming(){
			if(inote != null){
				return inote.getTiming();
			}else{
				if(notes != null && notes.length> 0){
					return notes[notes.length-1].getEnderTiming();
				}
			}
			return Integer.MIN_VALUE;
		}
	}
	
	/**
	 * 패턴정보
	 * @author sunulo
	 *
	 */
	private class Pattern {
		
		public Pattern(int[] pattern, int type){
			this.pattern = pattern;
			this.type = type;
		}
		int type;
		int width;
		int height;
		int[] pattern;
		int count = 1;
	}
	
}