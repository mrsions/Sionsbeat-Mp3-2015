package sions.android.sionsbeat.maker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import com.google.analytics.tracking.android.MapBuilder;

import sions.android.sionsbeat.MusicSelectActivity;
import sions.android.sionsbeat.NoteMakerActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.SplashActivity;
import sions.android.sionsbeat.template.Note;
import sions.android.sionsbeat.template.NoteSet;
import sions.android.sionsbeat.template.NoteSet.NoteFile;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.JsonType;
import sions.android.sionsbeat.window.AbsPopup;
import sions.android.sionsbeat.zresource.ZAnimate;
import sions.android.sionsbeat.zresource.ZResource;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.GridLayout;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class NoteModifyPopup extends AbsPopup implements View.OnKeyListener
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
	
	public NoteModifyPopup ( NoteMakerActivity context )
	{
		super( context );
		this.activity = context;
	}
	
	/************************************************************************************************************
	 * 
	 * 				@MEMBER_FIELDS
	 * 
	 ************************************************************************************************************/
	
	private NoteMakerActivity activity;
	
	
	private View view;
	
	private EditText title, author, contact;
	private Button applyBtn, cancelBtn;
	
	private NoteSet noteSet;
	
	/************************************************************************************************************
	 * 
	 * 				@OVERRIDE_METHODS
	 * 
	 ************************************************************************************************************/

	public boolean show ( NoteSet noteSet )
	{
		this.noteSet = noteSet;
		this.view = LayoutInflater.from(getContext()).inflate( R.layout.window_note_modify, null );

		/**@TRACKING**/ErrorController.tracking(getContext(), this, "show", "", 0, true);
		
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
	
	private void setupDefaults()
	{
		title = (EditText) view.findViewById( R.id.title );
		author = (EditText) view.findViewById( R.id.author );
		contact = (EditText) view.findViewById( R.id.contact );
		
		applyBtn = (Button)view.findViewById( R.id.apply );
		cancelBtn = (Button)view.findViewById( R.id.cancel );
	}
	
	private void setupEvent()
	{
		applyBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		
		title.setOnKeyListener(this);
		author.setOnKeyListener(this);
		contact.setOnKeyListener(this);
		applyBtn.setOnKeyListener(this);
		cancelBtn.setOnKeyListener(this);
	}
	
	private boolean setupPref()
	{
		title.setText(noteSet.getNoteName());
		author.setText(noteSet.getAuthorName());
		contact.setText(noteSet.getContact());
		
		if(noteSet.getAuthorName() == null || noteSet.getAuthorName().length()==0){
			author.setText(GameOptions.get(getContext()).getString(JsonType.NOTE_MAKER_AUTHOR, ""));	
		}
		if(noteSet.getContact() == null || noteSet.getContact().length()==0){
			contact.setText(GameOptions.get(getContext()).getString(JsonType.NOTE_MAKER_CONTACT, ""));	
		}
		return true;
	}

	
	/************************************************************************************************************
	 * 
	 * 				@EVENT
	 * 
	 ************************************************************************************************************/

	@Override
	public boolean onKey (View v, int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK) { 
			dispose();
			return true; 
		}
		return false;
	}
	
	@Override
	public void onClick (View v) {
		switch(v.getId()){
			case R.id.apply:
				{
					if(title.getText() == null || title.getText().length()<2 || title.getText().length()>100){
						Toast.makeText(getContext(), R.string.note_maker_save_message_short_name, Toast.LENGTH_SHORT).show();
						return;
					}
					if(author.getText() == null || author.getText().length()<2 || author.getText().length()>30){
						Toast.makeText(getContext(), R.string.note_maker_save_message_short_author, Toast.LENGTH_SHORT).show();
						return;
					}
					if(contact.getText() == null || contact.getText().length()<2 || contact.getText().length()>250){
						Toast.makeText(getContext(), R.string.note_maker_save_message_short_contact, Toast.LENGTH_SHORT).show();
						return;
					}
					
					activity.doSaveBefore();
					
					noteSet.setNoteName(title.getText().toString());
					noteSet.setAuthorName(author.getText().toString());
					noteSet.setContact(contact.getText().toString());
					noteSet.setLastUpdate(System.currentTimeMillis());
					
					noteSet.doCreateNoteFolder();	//폴더가 없을경우 생성해냄
					noteSet.doStoreNotes();
					noteSet.doStoreProperty(true);

					GameOptions.get(getContext()).put(JsonType.NOTE_MAKER_AUTHOR, noteSet.getAuthorName());
					GameOptions.get(getContext()).put(JsonType.NOTE_MAKER_CONTACT, noteSet.getContact());
					
					Toast.makeText(getContext(), R.string.note_maker_save_message, Toast.LENGTH_SHORT).show();
					dispose();
					
					if(activity.isSaveAfterExit())
					{
						activity.doFinish();
					}
				}
				break;
				
			case R.id.cancel:
				dispose();
				break;
		}
		super.onClick(v);
	}
	
	/************************************************************************************************************
	 * 
	 * 				@ACCESS_METHODS
	 * 
	 ************************************************************************************************************/
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
