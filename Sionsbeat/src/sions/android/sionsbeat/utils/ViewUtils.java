package sions.android.sionsbeat.utils;

import android.os.Build.VERSION;
import android.view.View;
import android.view.animation.AlphaAnimation;

public class ViewUtils {

	public static void setAlpha(View view, float alpha)
	{
		if (VERSION.SDK_INT >= 11) {
			view.setAlpha(alpha);
		}
	}

}
