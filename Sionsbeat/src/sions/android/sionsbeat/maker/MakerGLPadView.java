package sions.android.sionsbeat.maker;

import javax.microedition.khronos.opengles.GL10;

import sions.android.sionsbeat.game.GameModeInterface;
import sions.android.sionsbeat.game.sprite.NumberSprite;
import sions.android.sionsbeat.game.view.GameGLPadView;
import sions.android.spritebatcher.Sprite;
import sions.android.spritebatcher.SpriteBatcher;
import sions.android.spritebatcher.SpriteGroup;
import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;

/**
 * 게임을 진행할 때, 사용자와 소통하는 기능을 담당한다.
 * 1. 사용자가 패드를 터치 했을 때 이벤트에 대해서 GameMode에 알린다.
 * 2. GameMode에서 터치에 대한 반응 및 애니메이션 표현에 대해서 알려주면 그것을 이행한다.
 * 
 * @author sunulo
 */
public class MakerGLPadView extends GameGLPadView {

	private MakerGameMode gameMode;
	private boolean dirty = true;
	private boolean allDraw = false;
	
	/**************************************************************************
	 * 
	 * @Constructor
	 * 
	 *************************************************************************/
	public MakerGLPadView(Context context) {
		super(context);
	}

	public MakerGLPadView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	
	/**************************************************************************
	 * 
	 * @View
	 * 
	 *************************************************************************/

	@Override
	public boolean isDraw(){
//		return allDraw || dirty;
		return true;
	}
	
	private SpriteGroup bgSprites = new SpriteGroup(); 
	private SpriteGroup textSprites = new SpriteGroup(); 
	private String[] texts = new String[16];
	
	@Override
	protected void initializeSprite (GL10 gl, SpriteBatcher sb) {
	        super.initializeSprite(gl, sb);
	        
	        sb.getSprites().add(1, bgSprites);
	        sb.getSprites().add(2, textSprites);
	        
	        NumberSprite temp;
	        for(int i=0; i<16; i++)
	        {
	        	bgSprites.add(new Sprite(null, new RectF(0,0,0,0), 0x77FF0000 ));
	        	textSprites.add(temp = new NumberSprite(numberTextures));
	        	temp.setTextSize(COMBO_TEXT_SIZE);
	        	
	        	texts[i] = i+"";
	        }
	}
	
	@Override
	protected boolean updateBounds () {
		dirty = false;
		
		NumberSprite number;
	        if(super.updateBounds()){
		        
			// 게임터치
			for(int i=0; i<bgSprites.size(); i++){
	
				int x = ( i % gameMode.getNoteWidth() ) * BLOCK_WIDTH;
				int y = ( i / gameMode.getNoteWidth() ) * BLOCK_WIDTH + MARGIN_WIDTH;
	
				bgSprites.get(i).setBounds(x, y, x+BLOCK_WIDTH, y+BLOCK_WIDTH);
				
				number = (NumberSprite) this.textSprites.get(i);
				number.setGlTranslateX(x+(BLOCK_WIDTH*0.5f));
				number.setGlTranslateY(y+((BLOCK_WIDTH)*0.5f));
			}
		
	        }
	        
	        if(gameMode.getRunStatus() == MakerGameMode.STATUS_PREVIEW){

		        for(int i=0; i<16; i++)
		        {
	        		bgSprites.get(i).setVisible(false);
	        		textSprites.get(i).setVisible(false);	
		        }
	        	
	        }else{
		        
	        	int tempo = Math.min(32, gameMode.getTempo());
		        float tempoTiming = 1000f / tempo;
		        int maxTime = (int) Math.ceil(1500 / tempoTiming);
		        
		        for(int i=0; i<16; i++)
		        {
		        	SingleNote note = gameMode.getCloserNote(i);
		        	if(note != null)
		        	{
		        		bgSprites.get(i).setVisible(true);
					number = (NumberSprite) this.textSprites.get(i);
					
					int time = (int) Math.ceil( Math.abs(note.getTiming()-gameMode.getSysTime()) / tempoTiming );
					number.setNumber(Math.max(1, maxTime - time));
					number.setVisible(true);
		        	}
		        	else
		        	{
		        		bgSprites.get(i).setVisible(false);
		        		textSprites.get(i).setVisible(false);	
		        	}
		        }
		        
	        }
	        
	        return true;
	}
	


	/**************************************************************************
	 * 
	 * @View
	 * 
	 *************************************************************************/
	
	@Override
	public void setGameMode (GameModeInterface gameMode) {
	        super.setGameMode(gameMode);
	        this.gameMode = (MakerGameMode) gameMode;
	}

	public boolean isDirty () {
		return dirty;
	}

	public void setDirty (boolean dirty) {
		this.dirty = dirty;
	}

	public boolean getFinish () {
		return allDraw;
	}

	public void setFinish (boolean allDraw) {
		this.allDraw = allDraw;
	}

	@Override
	public void onPause () {
		this.allDraw = true;
	        super.onPause();
	}
	
	@Override
	public void onResume () {
		this.allDraw = false;
	        super.onResume();
	}
	
}
