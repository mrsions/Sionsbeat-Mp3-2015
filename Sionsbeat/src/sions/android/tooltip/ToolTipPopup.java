package sions.android.tooltip;

import java.util.ArrayList;

import sions.android.sionsbeat.R;
import android.content.Context;
import android.view.Gravity;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public class ToolTipPopup {
	
	private Context context;
	private PopupWindow popup;
	private ToolTipView view;
	
	public ToolTipPopup(Context context){
		this.context = context;

		this.view = new ToolTipView(context);
		this.popup = new PopupWindow(this.view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
	}
	
	public void add(ArrayList<ToolTip> tooltip){
		view.add(tooltip);
	}
	
	public void show()
	{
		popup.showAtLocation(view, Gravity.FILL, 0, 0);
	}
	
}
