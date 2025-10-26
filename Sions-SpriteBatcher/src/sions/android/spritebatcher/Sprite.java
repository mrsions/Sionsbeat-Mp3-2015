package sions.android.spritebatcher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.RectF;

public class Sprite {
	
	private static final int DEFAULT_ARGB = 0xFFFFFFFF;

	private float[] vertices; // Positions of vertices
	private short[] indices; // Which verts go together to form Ele's
	
	private ShortBuffer indexBuffer;
	private FloatBuffer vertexBuffer;
	
	private Texture texture;
	
	private RectF bounds;
	
	private float glTranslateX;
	private float glTranslateY;
	private float glScale = 1f;
	
	private int argb;
	private float r;
	private float g;
	private float b;
	private float a;
	
	private boolean visible = true;

	public Sprite(Texture texture, RectF bounds)
	{
		this(texture, bounds, DEFAULT_ARGB);
	}
	
	public Sprite(Texture texture, RectF bounds, int argb)
	{
		this.texture = texture;
		this.bounds = bounds;
		this.setArgb(argb);
		
		if(bounds != null){
			updateTransform();
		}
	}
	
	public void updateTransform(){

		vertices = new float[]{
				bounds.left, bounds.top, 0f,			//0
				bounds.left, bounds.bottom, 0f,		//1
				bounds.right, bounds.bottom, 0f,		//2
				bounds.right, bounds.top, 0f			//3
		};

		indices = new short[]{
			0, 1, 2,
			0, 2, 3
		};

		// Vertex
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		// Index
		bb = ByteBuffer.allocateDirect(indices.length * 2);
		bb.order(ByteOrder.nativeOrder());
		indexBuffer = bb.asShortBuffer();
		indexBuffer.put(indices);
		indexBuffer.position(0);
		
	}

	public Texture getTexture () {
		return texture;
	}
	public void setTexture (Texture texture) {
		this.texture = texture;
	}
	public RectF getBounds () {
		return bounds;
	}
	public void setBounds (RectF bounds) {
		this.bounds = bounds;
		this.updateTransform();
	}
	public void setBounds (float left, float top, float right, float bottom) {
		this.bounds.set(left, top, right, bottom);
		this.updateTransform();
	}
	public int getArgb () {
		return argb;
	}
	public void setArgb (int argb) {
		this.argb = argb;
		this.a = ( ( argb >> 0x18 ) & 0xFF ) / 255f;
		this.r = ( ( argb >> 0x10 ) & 0xFF ) / 255f;
		this.g = ( ( argb >> 0x08 ) & 0xFF ) / 255f;
		this.b = ( argb & 0xFF ) / 255f;
	}
	public float[] getVertices () {
		return vertices;
	}
	public short[] getIndices () {
		return indices;
	}

	public float getR () {
		return r;
	}
	public float getG () {
		return g;
	}
	public float getB () {
		return b;
	}
	public float getA () {
		return a;
	}
	public void setA (float a){
		this.a = a;
	}
	public boolean isVisible () {
		return visible;
	}
	public void setVisible (boolean visible) {
		this.visible = visible;
	}

	public float getGlTranslateX () {
		return glTranslateX;
	}

	public void setGlTranslateX (float glTranslateX) {
		this.glTranslateX = glTranslateX;
	}

	public float getGlTranslateY () {
		return glTranslateY;
	}

	public void setGlTranslateY (float glTranslateY) {
		this.glTranslateY = glTranslateY;
	}

	public float getGlScale () {
		return glScale;
	}

	public void setGlScale (float glScale) {
		this.glScale = glScale;
	}

	public ShortBuffer getIndexBuffer () {
		return indexBuffer;
	}

	public void setIndexBuffer (ShortBuffer indexBuffer) {
		this.indexBuffer = indexBuffer;
	}

	public FloatBuffer getVertexBuffer () {
		return vertexBuffer;
	}

	public void setVertexBuffer (FloatBuffer vertexBuffer) {
		this.vertexBuffer = vertexBuffer;
	}

	
}
