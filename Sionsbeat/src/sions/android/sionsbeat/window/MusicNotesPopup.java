package sions.android.sionsbeat.window;

import java.util.ArrayList;

import sions.android.sionsbeat.MusicSelectActivity;
import sions.android.sionsbeat.NoteMakerActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.template.NoteSet;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.FileUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class MusicNotesPopup extends AbsPopup implements OnItemClickListener, OnItemLongClickListener, OnKeyListener
{

	/************************************************************************************************************
	 * 
	 * 				@STATIC_FIELDS
	 * 
	 ************************************************************************************************************/

	/************************************************************************************************************
	 * 
	 *				 @CONSTRUCTOR
	 * 
	 ************************************************************************************************************/
	
	public MusicNotesPopup ( Activity context )
	{
		super( context );
	}
	
	/************************************************************************************************************
	 * 
	 * 				@MEMBER_FIELDS
	 * 
	 ************************************************************************************************************/

	private ListView listView;
	private NoteAdapter adapter;
	
	private View view;
	private MusicSelectPopup popup;
	private ArrayList<NoteSet> noteSets;
	
	/************************************************************************************************************
	 * 
	 * 				@OVERRIDE_METHODS
	 * 
	 ************************************************************************************************************/

	public boolean show (MusicSelectPopup popup, ArrayList<NoteSet> noteSets)
	{
		/**@TRACKING**/ErrorController.tracking(getContext(), this, "show", "", 0, true);
		
		this.popup = popup;
		this.noteSets = noteSets;
		this.view = LayoutInflater.from(getContext()).inflate( R.layout.window_note_list, null );
		
		setupDefaults();
		setupEvent();
		if(!setupPref()){
			return false;
		}
		return super.show( this.view, true );
	}
	@Override
	protected void onBeforeWindowSetup (PopupWindow window) {
		window.setFocusable(true);
		window.setInputMethodMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		try{	window.getContentView().setFocusable(true);
		}catch(Throwable e){ ErrorController.error(10, e);}
		try{	window.getContentView().setFocusableInTouchMode(true);
		}catch(Throwable e){ ErrorController.error(10, e);}
		
		window.getContentView().setOnKeyListener(this);
	}
	
	/************************************************************************************************************
	 * 
	 * 				@ACTIVE_METHODS
	 * 
	 ************************************************************************************************************/
	
	private void setupDefaults(){
		listView = (ListView) view.findViewById(R.id.noteList);
	}
	
	private void setupEvent(){
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		
		this.view.findViewById(R.id.window_container_2).setOnClickListener(this);
	}
	
	private boolean setupPref()
	{
		listView.setAdapter(adapter = new NoteAdapter());
		return true;
	}

	
	
	/************************************************************************************************************
	 * 
	 * 				@EVENT
	 * 
	 ************************************************************************************************************/

	@Override
        public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
		NoteSet noteSet = noteSets.get(position);
		
		switch(noteSet.getType())
		{
			case NoteSet.TYPE_MAKE:
				{
					Intent intent = new Intent(getContext(), NoteMakerActivity.class);
					intent.putExtra(NoteMakerActivity.INTENT_SONG_TAG, popup.getSong().toJSON().toString());
					getContext().startActivityForResult(intent, MusicSelectActivity.REQUEST_NOTEMAKE);
					popup.dispose();
				}
				break;
			default:
				{
					popup.setupSelectNoteSet(noteSet);
					popup.dispose();
				}
				break;
		}
        }

	@Override
        public boolean onItemLongClick (AdapterView<?> parent, View view, int position, long id) {
		final NoteSet noteSet = noteSets.get(position);
		switch(noteSet.getType())
		{
			case NoteSet.TYPE_MAKE:
			case NoteSet.TYPE_ANALYZE:
				return false;
			default:
				{
					if(noteSet.getNoteName() == null || noteSet.getNoteName().length() < 2){
						return false;
					}
					
					DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick (DialogInterface dialog, int which) {
							switch(which)
							{
								case DialogInterface.BUTTON_NEUTRAL:
									{
										FileUtils.deleteFile(noteSet.getRootFile());
										popup.deleteNoteSet(noteSet);
										popup.dispose();
										popup.showMusicNotePopup();

										if(noteSet == popup.getSelectNoteSet()){
											NoteSet nowNoteSet = popup.getNoteSets().get(1);
											popup.setupSelectNoteSet(nowNoteSet);
										}
										
									}
									break;
								case DialogInterface.BUTTON_POSITIVE:
									{
										Intent intent = new Intent(getContext(), NoteMakerActivity.class);
										intent.putExtra(NoteMakerActivity.INTENT_SONG_TAG, popup.getSong().toJSON().toString());
										intent.putExtra(NoteMakerActivity.INTENT_NOTESET, noteSet.toJson().toString());
										getContext().startActivityForResult(intent, MusicSelectActivity.REQUEST_NOTEMODIFY);
										popup.dispose();
									}
									break;
							}
						}
					};
					
					AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setNegativeButton(R.string.notelist_cancel, listener);
					builder.setNeutralButton(R.string.notelist_delete, listener);
					if(noteSet.isEditable())
					{
						builder.setPositiveButton(R.string.notelist_modify, listener);
					}
					builder.show();
				}
				return true;	
		}
        }
	
	@Override
	public void onClick (View v) {
		switch(v.getId()){
			case R.id.window_container_2:
				popup.dispose();
				break;
		}
	        super.onClick(v);
	}
	
	/************************************************************************************************************
	 * 
	 * 				@ACCESS_METHODS
	 * 
	 ************************************************************************************************************/
	
	class NoteAdapter extends BaseAdapter {

		@Override
                public int getCount () {
	                return noteSets.size();
                }

		@Override
                public Object getItem (int position) {
	                return noteSets.get(position);
                }

		@Override
                public long getItemId (int position) {
	                return position;
                }

		@Override
                public View getView (int position, View convertView, ViewGroup parent) {

			
			NoteSet noteSet = noteSets.get(position);
			TextView textView = (TextView) convertView;
			
			if(textView == null){
				LayoutInflater lp = LayoutInflater.from(getContext());
				textView = (TextView) lp.inflate(R.layout.layout_note_item, null);
			}
			
			switch(noteSet.getType()){
				case NoteSet.TYPE_ANALYZE:
					textView.setText(getContext().getString(R.string.notelist_default));
					break;
				case NoteSet.TYPE_MAKE:
					textView.setText(getContext().getString(R.string.notelist_make));
					break;
				default:
					if(noteSet.getNoteName().length() == 1){
						textView.setText(getContext().getString(R.string.notelist_default));
					}else{
						textView.setText(noteSet.getNoteName());
					}
					break;
			}
			
	                return textView;
                }
		
	}

	@Override
	public boolean onKey (View v, int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK) { 
			dispose();
			return true; 
		}
		return false;
	}
	
}
