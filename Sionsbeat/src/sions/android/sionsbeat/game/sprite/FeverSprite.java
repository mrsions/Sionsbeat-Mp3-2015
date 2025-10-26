package sions.android.sionsbeat.game.sprite;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;
import sions.android.spritebatcher.CustomDraw;
import sions.android.spritebatcher.Sprite;
import sions.android.spritebatcher.SpriteGroup;
import sions.android.spritebatcher.Texture;

public class FeverSprite extends Sprite implements CustomDraw {


	class Particle {
		float rotate;
		float scale = 1f;
		float speed;
		float g;
	}
	
	private float[] vertices; // Positions of vertices
	private short[] indices; // Which verts go together to form Ele's
	
	private ShortBuffer indexBuffer;
	private FloatBuffer vertexBuffer;
	
	private Particle[] particle;
	
	private long drawTime = 0;
	
	public FeverSprite (Texture texture, int width, int height)
        {
	        super(texture, new RectF(-(width/20f), height*3, (width/20f), height*1.5f));
	        
	        float sw = width/80f;
	        float sh = height*0.75f;
	        setBounds(-sw, -sh*5, sw, -sh);

	        setGlTranslateX(width*0.5f);
	        setGlTranslateY(height*0.5f);
	        
		particle = new Particle[20];
        }
	
	public void start()
	{
		for(int i=0; i< particle.length; i++){
			
			Particle pt = new Particle();
			clearParticle(pt);
			particle[i] = pt;
			
		}
		
		drawTime = System.currentTimeMillis();
	}
	
	private void clearParticle(Particle pt){
		pt.scale = 1f;
		pt.rotate = (float) ( Math.random()*360 ); 
		pt.speed = (float) (Math.random()*0.1+0.6);
		pt.g = (float) ( (Math.random()*0.3) +0.7 );
	}
	
	public void stop(){
		synchronized(this){
			drawTime = 0; 
		}
	}

	@Override
        public void glDraw (GL10 gl) {
		
		float power = 1;
		synchronized(this){
			if(drawTime == 0) return;
			
			long time = System.currentTimeMillis();
			power = (time-drawTime) /30f;
			drawTime = time;
		}
		
		int textureId = getTexture().getTextureId();
		int indicesSize = getIndices().length;
		
		FloatBuffer textureBuffer = getTexture().getTextureBuffer();
		FloatBuffer vertexBuffer = getVertexBuffer();
		ShortBuffer indexBuffer = getIndexBuffer();
		
		
		gl.glPushMatrix();
		
		gl.glTranslatef(getGlTranslateX(), getGlTranslateY(), 0f);
		for(Particle pt : particle){

			pt.scale *= Math.pow(pt.speed, power);
			if(pt.scale < 0.01){
				
				clearParticle(pt);;
				
			}
			else
			{
				gl.glPushMatrix();

				gl.glRotatef(pt.rotate, 0, 0, 1f);
				gl.glScalef(1f, pt.scale, 1f);
				
				gl.glColor4f(1f, pt.g, 0f, Math.min(1f, pt.scale*10));
				gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
				gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
				gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
				gl.glDrawElements(GL10.GL_TRIANGLES, indicesSize, GL10.GL_UNSIGNED_SHORT, indexBuffer);
				
				gl.glPopMatrix();
			}
			
		}
		gl.glPopMatrix();
		
        }
	
}
