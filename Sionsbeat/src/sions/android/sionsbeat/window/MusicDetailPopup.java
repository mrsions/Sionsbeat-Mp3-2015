package sions.android.sionsbeat.window;

import java.util.ArrayList;

import sions.android.sionsbeat.MusicSelectActivity;
import sions.android.sionsbeat.NoteMakerActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.template.NoteSet;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.FileUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MusicDetailPopup extends AbsPopup
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
	
	public MusicDetailPopup ( Activity context )
	{
		super( context );
	}
	
	/************************************************************************************************************
	 * 
	 * 				@MEMBER_FIELDS
	 * 
	 ************************************************************************************************************/

	private View view;
	private AbsPopup popup;
	
	/************************************************************************************************************
	 * 
	 * 				@OVERRIDE_METHODS
	 * 
	 ************************************************************************************************************/

	public boolean show ( AbsPopup popup, SongInfo song)
	{
		/**@TRACKING**/ErrorController.tracking(getContext(), this, "show", "", 0, true);
		
		this.popup = popup;
		this.view = LayoutInflater.from(getContext()).inflate( R.layout.window_music_detail, null );
		
		view.findViewById(R.id.window_container_2).setOnClickListener(this); // 종료 이벤트
		
		TextView tv = (TextView) view.findViewById(R.id.description);
		tv.setText(getSongText(song));
		
		return super.show( this.view );
	}
	
	
	/************************************************************************************************************
	 * 
	 * 				@ACTIVE_METHODS
	 * 
	 ************************************************************************************************************/
		
	private String getSongText(SongInfo song)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("Path : "+song.getPath()).append("\n");
		sb.append("Contact : "+song.getContact()).append("\n");
		sb.append("CCL : "+song.isCCL()).append("\n");
		
		if(song.getDetails()!= null){
			sb.append(song.getDetails());
		}
		
		return sb.toString();
	}
	
	
	/************************************************************************************************************
	 * 
	 * 				@EVENT
	 * 
	 ************************************************************************************************************/

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
	
}
