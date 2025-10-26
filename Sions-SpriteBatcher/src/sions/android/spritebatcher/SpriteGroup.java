package sions.android.spritebatcher;

import java.util.ArrayList;

import android.graphics.RectF;

public class SpriteGroup extends Sprite {

	public SpriteGroup ()
        {
	        super(null, null);
        }

	public SpriteGroup (Texture texture, RectF bounds, int argb)
        {
	        super(texture, bounds, argb);
        }

	public SpriteGroup (Texture texture, RectF bounds)
        {
	        super(texture, bounds);
        }
	
	private ArrayList<Sprite> childrens = new ArrayList();

	public void add(Sprite sprite)
	{
		childrens.add(sprite);
	}
	public void add(int idx, Sprite sprite)
	{
		childrens.add(idx, sprite);
	}

	public void remove(int idx)
	{
		childrens.remove(idx);
	}
	
	public Sprite get(int idx)
	{
		return childrens.get(idx);
	}
	
	public int size(){
		return childrens.size();
	}
}
