package sions.android.sionsbeat.maker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import sions.android.sionsbeat.NoteMakerActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.template.NoteSet.NoteFile;
import sions.android.sionsbeat.utils.ErrorController;
import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MakerPopupMenu implements PopupMenu.OnMenuItemClickListener {

	private PopupMenu popup;
	private NoteMakerActivity context;

	public MakerPopupMenu (NoteMakerActivity context, View anchor)
	{
		this.context = context;
		
		popup = new PopupMenu(context, anchor);
		setForceShowIcon(popup);

		MenuInflater inflater =popup.getMenuInflater();
		inflater.inflate(R.menu.maker_popup_menu, popup.getMenu());
		onUpdateCount();
		
		popup.setOnMenuItemClickListener(this);
	}
	
	public void show(){
		popup.show();
	}
	
	public void dismiss(){
		popup.dismiss();
	}

	private void setForceShowIcon (PopupMenu popup) {
		try {
			Class<?> classPopupMenu = Class.forName(popup.getClass().getName());
			Field field = classPopupMenu.getDeclaredField("mPopup");
			field.setAccessible(true);
			Object menuPopupHelper = field.get(popup);
			Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
			Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
			setForceIcons.setAccessible(true);
			setForceIcons.invoke(menuPopupHelper, true);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		try {
			Field[] fields = popup.getClass().getDeclaredFields();
			for (Field field : fields) {
				if ("mPopup".equals(field.getName())) {
					field.setAccessible(true);
					Object menuPopupHelper = field.get(popup);
					Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
					Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
					setForceIcons.setAccessible(true);
					setForceIcons.invoke(menuPopupHelper, true);
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ErrorController.error(10, new Exception("NotFoundPopupMenu"));
	}

	@Override
	public boolean onMenuItemClick (MenuItem menu) {
		switch ( menu.getItemId() )
		{
			case R.id.action_new:
				context.getMode().doClear();
				break;
			case R.id.action_save:
				{
					NoteModifyPopup npopup = new NoteModifyPopup(context);
					context.onPopupShow(npopup);
					npopup.show( context.getNoteSet() );
				}
				break;
			case R.id.action_upload:
				Toast.makeText(context, R.string.not_support_now, Toast.LENGTH_SHORT).show();
				break;
			case R.id.action_exit:
				context.doFinish();
				break;
			case R.id.action_basic:
				context.doChangeNoteFile(context.getNoteSet().getNotes().get(NoteFile.TYPE_BASIC), true);
				break;
			case R.id.action_advance:
				context.doChangeNoteFile(context.getNoteSet().getNotes().get(NoteFile.TYPE_ADVANCE), true);
				break;
			case R.id.action_extreme:
				context.doChangeNoteFile(context.getNoteSet().getNotes().get(NoteFile.TYPE_EXTREME), true);
				break;
		}
		return false;
	}

	private void onUpdateCount(){
		
		Menu menu = popup.getMenu();
		for(int i=0; i<3; i++){
			NoteFile nf = context.getNoteSet().getNotes().get(i);
			MenuItem item = null;
			int resId = 0;
			switch(i){
				case NoteFile.TYPE_BASIC:
					item = menu.findItem(R.id.action_basic); 
					resId = R.string.start_game_mode_basic;
					break;
				case NoteFile.TYPE_ADVANCE: 
					item = menu.findItem(R.id.action_advance);  
					resId = R.string.start_game_mode_advance;
					break;
				case NoteFile.TYPE_EXTREME: 
					item = menu.findItem(R.id.action_extreme);  
					resId = R.string.start_game_mode_extreme;
					break;
			}
			
			if(nf == context.getCurrNoteFile()){
				item.setIcon(R.drawable.maker_menu_selected);
			}
			item.setTitle(context.getString(resId)+" ("+nf.getNotes().length+")");
		}
		
	}
}
