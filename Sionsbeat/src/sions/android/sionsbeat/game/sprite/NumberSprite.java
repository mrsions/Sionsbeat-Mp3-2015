package sions.android.sionsbeat.game.sprite;

import android.content.Context;
import android.graphics.RectF;
import sions.android.spritebatcher.Sprite;
import sions.android.spritebatcher.SpriteGroup;
import sions.android.spritebatcher.Texture;

public class NumberSprite extends SpriteGroup {

	private int number;
	private int textSize;
	
	private Texture[] numbers;
	private int textureSingleWidth;
	private int textureSingleHeight;
	
	public NumberSprite(Texture[] numbers){
		this.numbers = numbers;
		this.textureSingleWidth = numbers[0].getBounds().right;
		this.textureSingleHeight = numbers[0].getBounds().bottom;
	}
	
	private void update(){
		
		String text = String.valueOf(number);
		char[] chars = text.toCharArray();
		
		int charSize = chars.length;
		int originSize = size();
		
		if(originSize < charSize){
			for(int i=originSize; i<charSize; i++){
				add(new Sprite(null, new RectF()));
			}
		}else{
			for(int i=charSize; i<originSize; i++){
				get(i).setVisible(false);
			}
		}
		
		float scale = textSize / textureSingleHeight;
		float scaleWidth = (textureSingleWidth*scale);
		float scaleHeight = (textureSingleHeight*scale);
		
		float cutWidth = (charSize*scaleWidth) * -0.5f;
		float cutHeight = scaleHeight * - 0.5f;
		
		
		for(int i=0; i<charSize; i++){
			Sprite sprite = get(i);
			int idx = chars[i]-0x30;
			
			if(idx <0 || idx>9){
				sprite.setVisible(false);
			}else{
				sprite.setVisible(true);
				sprite.setTexture(numbers[idx]);
				sprite.setBounds(cutWidth+(i*scaleWidth), cutHeight, cutWidth+((i+1)*scaleWidth), cutHeight+scaleHeight);
			}
		}
	}
	
	public void setNumber(int number){
		if(this.number != number){
			this.number = number;
			this.update();
		}
	}
	public int getNumber(){
		return number;
	}

	public int getTextSize () {
		return textSize;
	}

	public void setTextSize (int textSize) {
		this.textSize = textSize;
	}

	public Texture[] getTextures() {
		return numbers;
	}
	
	
	
}
