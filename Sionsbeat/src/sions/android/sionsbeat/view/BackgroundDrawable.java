package sions.android.sionsbeat.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import sions.android.sionsbeat.R;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;

public class BackgroundDrawable extends AnimationDrawable {

	public static void setup (Activity activity) {
		setup(activity, 0);
	}
	public static void setup (Activity activity, int top) {
		
		BackgroundDrawable bgd = getBackgroundDrawable(activity, top, R.drawable.background);
		setupView(activity, bgd);
	}
	public static void setup (Activity activity, int top, int resID) {
		
		BackgroundDrawable bgd = getBackgroundDrawable(activity, top, resID);
		setupView(activity, bgd);
	}

	public static BackgroundDrawable getBackgroundDrawable(Activity activity, int top, int resID){
		
		Drawable drawable = activity.getResources().getDrawable(resID);
		if (drawable == null) {
			Log.d("test","Background Drawable : not found Drawable");
			return null;
		}

		final BackgroundDrawable bgd = new BackgroundDrawable(1080, 1920, top);
		bgd.addFrame(drawable, 40);
		bgd.addFrame(drawable, 40);
		bgd.setOneShot(false);

		return bgd;
	}

	public static void setupView(Activity activity, final BackgroundDrawable bgd){

		View view = activity.findViewById(R.id.activity_container);
		if (view == null) {
			Log.d("test","Background Drawable : not View : " + activity.getClass().getName());
			return;
		}

		view.setBackgroundDrawable(bgd);
		view.post(new Runnable() {
			public void run () {
				bgd.start();
			}
		});
	}
	
	
	

	public static double getDistance (float mx, float my, float tx, float ty) {
		float dx = tx - mx;
		float dy = ty - my;
		return Math.sqrt(dx * dx + dy * dy);
	}

	private static Random rnd;
	private static Vertex[] vertexs;
	private static Edge[] edges;
	private static ArrayList<Polygon> polys;
	
	private int top;

	public BackgroundDrawable (int width, int height, int top)
	{
		this.top = top;
		
		if(rnd == null){
			
			rnd = new Random();
	
			double[] points = {-0.693356, -0.166666666666667, -0.454685, -0.166666666666667, -0.216014, -0.166666666666667, 0.022658, -0.166666666666667, 0.261329, -0.166666666666667, 0.5, -0.166666666666667, 0.738671, -0.166666666666667, 0.977342, -0.166666666666667, 1.259583, -0.164786, 1.454685, -0.166666666666667, 1.693356, -0.166666666666667, -0.693356, -0.0333333333333333, -0.501091, -0.0745836666666667, -0.162132, -0.088888, -0.027875, -0.0476376666666667, 0.350275, -0.117123, 0.5, -0.0333333333333333, 0.780952, -0.0257236666666667, 1.042941, -0.0566613333333333, 1.258553, 0.0233856666666666, 1.462419, -0.025599, 1.693356, -0.0333333333333333, -0.693356, 0.1, -0.512693, 0.0574606666666667, -0.320342, 0.10023, 0.00718900000000002, 0.0548826666666666, 0.17625, 0.080664, 0.496133, 0.080664, 0.649725, 0.0716403333333333, 1.004413, 0.061328, 1.33203, 0.141250333333333, 1.504959, 0.0896873333333333, 1.693356, 0.1, -0.693356, 0.233333333333333, -0.558668, 0.189763666666667,
			                -0.366835, 0.199817333333333, -0.185884, 0.246272, 0.199453, 0.229466, 0.515469, 0.167590666666667, 0.723634, 0.192342, 0.923633, 0.243904666666667, 1.24317, 0.251610333333333, 1.562967, 0.22431, 1.693356, 0.233333333333333, -0.693356, 0.366666666666667, -0.512175, 0.301413, -0.397773, 0.352487, -0.092927, 0.297315666666667, 0.261329, 0.366666666666667, 0.550274, 0.356354, 0.75414, 0.409206, 1.031483, 0.388581, 1.38216, 0.429706666666667, 1.613241, 0.32026, 1.693356, 0.366666666666667, -0.693356, 0.5, -0.551365, 0.534805, -0.208279, 0.443281, 0.103869, 0.429101, 0.261329, 0.5, 0.538528, 0.458625, 0.746406, 0.533516, 1.015813, 0.546569666666667, 1.312694, 0.506445333333333, 1.527961, 0.504030333333333, 1.693356, 0.5, -0.693356, 0.633333333333333, -0.551365, 0.619153666666667, -0.126981, 0.592313, 0.022658, 0.633333333333333, 0.284532, 0.589505, 0.488398, 0.650091333333333, 0.827157, 0.606301333333333, 1.093359, 0.713255666666667, 1.351165,
			                0.623183666666667, 1.508826, 0.657825666666667, 1.693356, 0.633333333333333, -0.693356, 0.766666666666667, -0.465855, 0.688292333333333, -0.266287, 0.755065, 0.003322, 0.752487, 0.238212, 0.705021333333333, 0.577057, 0.792352333333333, 0.738671, 0.766666666666667, 0.973475, 0.806628, 1.285365, 0.800058, 1.5705, 0.753939, 1.693356, 0.766666666666667, -0.693356, 0.9, -0.477802, 0.838354666666667, -0.26242, 0.927070333333333, -0.050388, 0.861586666666667, 0.327071, 0.921914333333333, 0.5, 0.9, 0.808022, 0.933391333333333, 1.120429, 0.956719, 1.328163, 0.933516, 1.535896, 0.876796666666667, 1.693356, 0.9, -0.693356, 1.03333333333333, -0.470413, 1.06930266666667, -0.200545, 1.06040366666667, 0.011056, 1.07200533333333, 0.261329, 1.06040366666667, 0.500431, 1.06323133333333, 0.769609, 1.07071633333333, 1.019882, 1.06040366666667, 1.281843, 1.042587, 1.501091, 1.07458366666667, 1.693356, 1.03333333333333, -0.693356, 1.16666666666667, -0.454685,
			                1.16666666666667, -0.216014, 1.16666666666667, 0.022658, 1.16666666666667, 0.261329, 1.16666666666667, 0.5, 1.16666666666667, 0.738671, 1.16666666666667, 0.977342, 1.16666666666667, 1.216014, 1.16666666666667, 1.462506, 1.16560766666667, 1.693356, 1.16666666666667,};
	
			int[] idx = {13, 12, 2, 13, 12, 2, 14, 13, 3, 14, 13, 3, 15, 14, 4, 15, 14, 4, 16, 15, 5, 16, 15, 5, 6, 16, 6, 5, 18, 17, 7, 18, 17, 7, 8, 18, 8, 7, 20, 19, 9, 20, 19, 9, 10, 20, 10, 9, 21, 10, 11, 21, 11, 10, 12, 23, 13, 23, 24, 13, 14, 24, 25, 14, 15, 25, 26, 15, 16, 26, 27, 16, 17, 27, 16, 17, 28, 17, 18, 28, 29, 18, 19, 29, 18, 19, 30, 19, 20, 30, 31, 20, 21, 31, 20, 21, 32, 21, 22, 32, 21, 22, 23, 34, 24, 34, 23, 24, 36, 35, 25, 36, 35, 25, 37, 36, 26, 37, 36, 26, 38, 37, 27, 38, 37, 27, 39, 38, 28, 39, 38, 28, 29, 39, 28, 29, 41, 40, 30, 41, 40, 30, 42, 41, 31, 42, 41, 31, 32, 42, 31, 32, 43, 32, 33, 43, 32, 33, 34, 45, 35, 45, 34, 35, 46, 35, 36, 46, 47, 36, 37, 47, 48, 37, 38, 48, 50, 49, 39, 50, 49, 39, 40, 50, 39, 40, 51, 40, 41, 51, 52, 41, 42, 52, 53, 42, 43, 53, 42, 43, 54, 43, 44, 54, 43, 44, 45, 56, 46, 56, 45, 46, 58, 57, 47, 58, 57, 47, 48, 58, 47, 48, 59, 48, 49, 59, 48, 49, 60, 49, 50, 60, 61, 50, 51, 61, 50, 51, 62, 51, 52, 62, 51, 52, 63, 52, 53,
			                63, 52, 53, 64, 53, 54, 64, 53, 54, 65, 54, 55, 65, 54, 55, 56, 67, 57, 67, 56, 57, 69, 68, 58, 69, 68, 58, 70, 69, 59, 70, 69, 59, 71, 70, 60, 71, 70, 60, 72, 71, 61, 72, 71, 61, 62, 72, 61, 62, 73, 62, 63, 73, 62, 63, 74, 63, 64, 74, 63, 64, 75, 64, 65, 75, 64, 65, 76, 65, 66, 76, 65, 66, 67, 78, 68, 78, 67, 68, 80, 79, 69, 80, 79, 69, 70, 80, 81, 70, 71, 81, 83, 82, 72, 83, 82, 72, 73, 83, 72, 73, 84, 73, 74, 84, 73, 74, 85, 74, 75, 85, 74, 75, 86, 75, 76, 86, 75, 76, 87, 76, 77, 87, 76, 77, 78, 89, 79, 89, 78, 79, 91, 90, 80, 91, 90, 80, 92, 91, 81, 92, 91, 81, 82, 92, 81, 82, 94, 93, 83, 94, 93, 83, 84, 94, 83, 84, 95, 84, 85, 95, 84, 85, 96, 85, 86, 96, 85, 86, 97, 86, 87, 97, 86, 87, 98, 87, 88, 98, 87, 88, 89, 100, 90, 100, 89, 90, 102, 101, 91, 102, 101, 91, 92, 102, 103, 92, 93, 103, 92, 93, 104, 93, 94, 104, 105, 94, 95, 105, 94, 95, 106, 95, 96, 106, 95, 96, 107, 96, 97, 107, 96, 97, 108, 97, 98, 108, 97, 98, 109, 98, 99, 109, 98, 99, 100,
			                111, 101, 111, 100, 101, 112, 101, 102, 112, 113, 114, 103, 114, 113, 103, 104, 114, 103, 104, 115, 116, 105, 116, 115, 105, 106, 116, 105, 106, 117, 118, 107, 118, 117, 107, 108, 118, 107, 108, 119, 120, 109, 120, 119, 109, 110, 120, 109, 110, 22, 11, 30, 31, 60, 61, 71, 82, 80, 81, 101, 90, 111, 112, 33, 22, 82, 93, 103, 102, 112, 113, 102, 113, 44, 33, 55, 44, 105, 104, 114, 115, 104, 115, 66, 55, 77, 66, 107, 106, 116, 117, 106, 117, 88, 77, 99, 88, 109, 108, 118, 119, 108, 119, 110, 99, 120, 121, 121, 110, 29, 30, 40, 29, 59, 60, 90, 79, 9, 8, 19, 8, 49, 38, 58, 59, 79, 68, 27, 28, 68, 57, 7, 6, 17, 6, 26, 27, 46, 47, 57, 46, 25, 26, 5, 4, 24, 25, 35, 24, 4, 3, 3, 2, 2, 1, 1, 12};
	
			vertexs = new Vertex[points.length / 2];
			for (int i = 0; i < points.length; i += 2) {
				vertexs[i / 2] = new Vertex((float) ( points[i] * width ), (float) ( points[i + 1] * height ));
			}
	
			edges = new Edge[idx.length / 2];
			for (int i = 0; i < idx.length; i += 2) {
				Edge edge = new Edge();
				edge.a = vertexs[idx[i] - 1];
				edge.b = vertexs[idx[i + 1] - 1];
				
				edges[i / 2] = edge;
			}
	
			polys = new ArrayList<Polygon>();
			for(Edge e: edges){
				Polygon poly = null;
				
				BREAK:
				for(Edge e1: edges){
					
					if(e.a == e1.a){
	
						for(Edge e2: edges){
							
							if((e2.a == e1.b && e2.b == e.b)
									|| (e2.b == e1.b && e2.a == e.b)){
								poly = new Polygon();
								poly.a = e.b;
								poly.b = e.a;
								poly.c = e1.b;
								break BREAK;
							}
							
						}
						
					}else if( e.a == e1.b){
	
						for(Edge e2: edges){
							
							if((e2.a == e1.a && e2.b == e.b)
									|| (e2.b == e1.a && e2.a == e.b)){
								poly = new Polygon();
								poly.a = e.b;
								poly.b = e.a;
								poly.c = e1.a;
								break BREAK;
							}
							
						}
						
					}else if(e.b == e1.a){
	
						for(Edge e2: edges){
							
							if((e2.a == e1.a && e2.b == e.a)
									|| (e2.b == e1.a && e2.a == e.a)){
								poly = new Polygon();
								poly.a = e.a;
								poly.b = e.b;
								poly.c = e1.b;
								break BREAK;
							}
							
						}
						
					}else if(e.b == e1.b){
	
						for(Edge e2: edges){
							
							if((e2.a == e1.a && e2.b == e.a)
									|| (e2.b == e1.a && e2.a == e.a)){
								poly = new Polygon();
								poly.a = e.a;
								poly.b = e.b;
								poly.c = e1.a;
								break BREAK;
							}
							
						}
						
					}
					
				}
				
				if(poly != null){
					if(rnd.nextInt(10)==0){
						polys.add(poly);
					}
				}
			}
		
		}
		
	}

	@Override
	public void draw (Canvas canvas) {
		canvas.translate(0, top);
		
		super.draw(canvas);

		Paint stroke = new Paint();
		stroke.setStyle(Style.STROKE);
		stroke.setColor(0xFF0a5c5e);
		stroke.setStrokeWidth(1);

		Paint circle = new Paint();
		circle.setStyle(Style.FILL);
		circle.setColor(0xFF0a5c5e);

		Paint fill = new Paint();
		fill.setStyle(Style.FILL);
		fill.setColor(0x10FFFFFF);

		for (Vertex v : vertexs) {

			v.x += v.speedX;
			v.y += v.speedY;

			if (Math.abs(v.x - v.targetX) < 3) {
				v.x = v.targetX;
			}
			if (Math.abs(v.y - v.targetY) < 3) {
				v.y = v.targetY;
			}

			if (v.targetX == v.x && v.targetY == v.y) {
				// v.targetX = rnd.nextInt(1500)-250;
				// v.targetY = rnd.nextInt(2500)-250;
				v.targetX = v.firstX + rnd.nextInt(400) - 200;
				v.targetY = v.firstY + rnd.nextInt(400) - 200;

				// int spd = rnd.nextInt(100)+ 60;

				int spd = (int) ( getDistance(v.x, v.y, v.targetX, v.targetY) / 1 );

				v.speedX = ( v.targetX - v.x ) / spd;
				v.speedY = ( v.targetY - v.y ) / spd;
			}

		}

		for (Polygon p : polys) {
			
			Path path = new Path();
			path.moveTo(p.a.x, p.a.y);
			path.lineTo(p.b.x, p.b.y);
			path.lineTo(p.c.x, p.c.y);
			path.lineTo(p.a.x, p.a.y);
			
			canvas.drawPath(path, fill);
			
		}

		for (Vertex v : vertexs) {

			canvas.drawCircle(v.x, v.y, 5, circle);

		}

		for (Edge e : edges) {
			canvas.drawLine(e.a.x, e.a.y, e.b.x, e.b.y, stroke);
		}

		canvas.translate(0, -top);
	}
	
	private static class Polygon {
		Vertex a, b, c;
	}

	private static class Edge {

		Vertex a, b;

	}

	private static class Vertex {

		public Vertex (float x, float y)
		{
			this.x = targetX = firstX = x;
			this.y = targetY = firstY = y;
		}

		float x, y;
		float targetX, targetY;
		float speedX, speedY;

		float firstX, firstY;
		
	}
}
