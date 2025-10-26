package sions.android.sionsbeat.game;

import android.util.Log;
import sions.android.sionsbeat.template.GameNote;

public class GameScore 
{
	public static final double SCORE_NORMAL = 800001;
	public static final double SCORE_COMBO = 100001;
	public static final double SCORE_LAST = 100001;

	public void setup(int duration){

		this.scoreNormal = 100 * (60000.0/duration);
		
	}
	
	private int noteCount = 0; // 전체 노트의 양
	private int maxCombo = 0; // 최대 콤보 갯수

	//-- status
	private int failedCount = 0;
	private int goodCount = 0;
	private int greatCount = 0;
	private int perfectCount = 0;

	//-- 게임 정보
	private double score;
	private double feverScore;
	private int combo;
	
	//-- 스코어
	private double scoreNormal;

	public double addScore(GameFever fever, int type, boolean comboUp){
		
		double addTotalScore = 0;
		double rate = 0;
		
		noteCount++;
		switch(type){
			case GameNote.TYPE_COMBO:
				rate = 0.1D;
				goodCount++;
				break;
			case GameNote.TYPE_GOOD:
				rate = 0.5D;
				goodCount++;
				break;
			case GameNote.TYPE_GREAT:
				rate = 0.7D;
				greatCount++;
				break;
			case GameNote.TYPE_PERFECT:
				rate = 1D;
				perfectCount++;
				break;
			default:
				failedCount++;
				break;
		}
		
		if(rate != 0){
			addTotalScore = scoreNormal * rate;
		}
		
		if(comboUp){
			if(this.combo < 0) this.combo = 0;
			this.combo ++;
			
			addTotalScore *= (this.combo / 30.0) +1;
		}else{
			if(this.combo > 0){
				this.combo = 0;	
			}else{
				this.combo--;
			}
		}
		
		if(this.combo > maxCombo){
			this.maxCombo = combo;
		}

		double feverScore = fever.onGameScore(addTotalScore);
		addTotalScore += feverScore;
		
		this.feverScore += feverScore;
		
		this.score += addTotalScore;
		Log.d("score", "add Normal: "+(scoreNormal * rate)+"   combo:"+this.combo+"     total:"+this.score);
		
		return addTotalScore;
	}
	
	public void addLastScore(int add){
		this.score += add;
	}
	
	public double getScore(){
		return this.score;
	}
	
	public int getCombo(){
		return this.combo;
	}

	public int getNoteCount ()
	{
		return noteCount;
	}

	public int getGoodCount ()
	{
		return goodCount;
	}

	public int getGreatCount ()
	{
		return greatCount;
	}

	public int getPerfectCount ()
	{
		return perfectCount;
	}

	public int getFailedCount ()
	{
		return failedCount;
	}

	public int getMaxCombo ()
	{
		return maxCombo;
	}

	public double getFeverScore () {
		return feverScore;
	}

	public int getRank () {
		
		if(perfectCount == noteCount){
			return GameMode.RANK_EXCELLENT;
		}else{
			
			double score = 0;

			score += perfectCount;
			score += greatCount * 0.7;
			score += goodCount * 0.5;
			
			score /= noteCount;
			
			switch((int)(score * 20)){
				case 20:
				case 19:
					return GameMode.RANK_SS;
				case 18:
					return GameMode.RANK_S;
				case 17:
					return GameMode.RANK_A;
				case 16:
					return GameMode.RANK_B;
				case 15:
				case 14:
					return GameMode.RANK_C;
				case 13:
				case 12:
					return GameMode.RANK_D;
				case 11:
				case 10:
					return GameMode.RANK_E;
				default:
					return GameMode.RANK_F;
			}
			
		}
        }

}
