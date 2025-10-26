package sions.android.sionsbeat.window;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.analytics.tracking.android.MapBuilder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Picasso.LoadedFrom;

import sions.android.SQ;
import sions.android.sionsbeat.MusicSelectActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.interpret.InterpretCollector;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.GoogleGameService;
import sions.android.sionsbeat.utils.JsonType;
import sions.android.sionsbeat.zresource.ZAnimate;
import sions.android.sionsbeat.zresource.ZResource;
import sions.json.JSONArray;
import sions.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ImportMusicsPopup extends AbsPopup implements OnCheckedChangeListener, AdapterView.OnItemClickListener
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
	
	public ImportMusicsPopup ( MusicSelectActivity context )
	{
		super( context );
		this.activity = context;
	}
	
	/************************************************************************************************************
	 * 
	 * 				@MEMBER_FIELDS
	 * 
	 ************************************************************************************************************/
	private MusicSelectActivity activity;
	private View view;
	private LinearLayout progress;
	private ListView folderList;
	
	private FolderListAdapter adapter;
	private ArrayList<Folder> list;
	private HashMap<String, Folder> map;

	private JSONObject imported;

	private int changeStatus = 0; // 1
	
	/************************************************************************************************************
	 * 
	 * 				@OVERRIDE_METHODS
	 * 
	 ************************************************************************************************************/

	public boolean show ()
	{
		this.view = LayoutInflater.from(getContext()).inflate( R.layout.window_import_music, null );

		/**@TRACKING**/ErrorController.tracking(getContext(), this, "show", "", 0, true);
		
		setupDefaults();
		setupEvent();
		if(!setupPref()){
			return false;
		}
		return super.show( this.view );
	}
	
	@Override
	public boolean dispose () {
		
		/**@TRACKING**/ErrorController.tracking(getContext(), this, "dispose", "", 0, true);
		
		switch(changeStatus){
			case 1:
				changeStatus=2;
				progress.setVisibility(View.VISIBLE);
				InterpretCollector.get(getContext()).callRecollect(new Runnable(){
					public void run(){
						activity.runOnUiThread(new Runnable(){
							public void run(){
								activity.doReloadList();
								changeStatus = 0;
								dispose();
							}
						});
					}
				});
			case 2:
				return true;
		}
	        return super.dispose();
	}
	
	/************************************************************************************************************
	 * 
	 * 				@ACTIVE_METHODS
	 * 
	 ************************************************************************************************************/
	
	private void setupDefaults()
	{
		this.folderList = (ListView) view.findViewById(R.id.folderList);
		this.progress = (LinearLayout) view.findViewById(R.id.progress);
	}
	
	private void setupEvent()
	{
		this.folderList.setOnItemClickListener(this);
		this.progress.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch (View v, MotionEvent event) {
				return true;
			}
		});
	}
	
	private boolean setupPref()
	{
		
		imported = SQ.getJSONObject(JsonType.IMPORTED_MUSIC);
		if(imported == null) imported = new JSONObject();
		
		map = new HashMap<String, Folder>();
		list = new ArrayList<Folder>();

		Folder folder;
		File folderFile;
		List<SongInfo> songList = InterpretCollector.getMusicList(getContext(), null);
		for(SongInfo song:songList){
			folderFile = new File(song.getPath()).getParentFile();
			folder = map.get(folderFile.getAbsolutePath());
			
			if(folder == null){
				folder = new Folder();
				folder.file = folderFile;
				folder.path = folder.file.getAbsolutePath();
				folder.fileName = folder.file.getName();
				
				if(imported.is(folder.path)){
					folder.imported = imported.getBoolean(folder.path);
				}else{
					imported.put(folder.path, true);
					folder.imported = true;
				}
				
				map.put(folder.path, folder);
				list.add(folder);
			}
			
			folder.count++;
		}
		
		Collections.sort(list, new Comparator<Folder>() {
			@Override
                        public int compare (Folder lhs, Folder rhs) {
	                        return lhs.path.compareTo(rhs.path);
                        }
		});
		
		this.folderList.setAdapter(adapter = new FolderListAdapter());
		
		return true;
	}
	
	/************************************************************************************************************
	 * 
	 * 				@EVENT
	 * 
	 ************************************************************************************************************/

	@Override
        public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
	        Holder holder = (Holder) view.getTag();
	        if(holder != null){
	        	toggled(holder, !holder.folder.imported);
	        }
        }
	
	@Override
        public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
	        Holder holder = (Holder) buttonView.getTag();
	        if(holder != null){
	        	toggled(holder, isChecked);
	        }
        }
	
	private void toggled(Holder holder, boolean checked){
		
		if(checked == holder.folder.imported) return;
		
		holder.folder.imported = checked;
		holder.checkbox.setChecked(checked);
		
		imported.put(holder.folder.path, checked);
		SQ.setJSON(JsonType.IMPORTED_MUSIC, imported);
		
		changeStatus = 1;
	}
	
	/************************************************************************************************************
	 * 
	 * 				@ACCESS_METHODS
	 * 
	 ************************************************************************************************************/
	
	class FolderListAdapter extends BaseAdapter {

		@Override
                public int getCount () {
	                return list.size();
                }

		@Override
                public Object getItem (int position) {
	                return list.get(position);
                }

		@Override
                public long getItemId (int position) {
	                return position;
                }

		@Override
                public View getView (int position, View convertView, ViewGroup parent) {
			
			Folder folder = list.get(position);
			Holder holder = null;
			
			if(convertView == null || convertView.getTag() == null){
				
				LayoutInflater inflater = LayoutInflater.from(getContext());
				convertView = inflater.inflate(R.layout.layout_import_music_item, null);
				
				holder = new Holder();
				holder.folder = folder;
				holder.container = (LinearLayout) convertView;
				
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.path = (TextView) convertView.findViewById(R.id.path);
				holder.checkbox = (CheckBox) convertView.findViewById(R.id.checked);
				holder.checkbox.setTag(holder);
				holder.checkbox.setOnCheckedChangeListener(ImportMusicsPopup.this);
				
				convertView.setTag(holder);
				
			}else{
				
				holder = (Holder) convertView.getTag();
				holder.folder = folder;
				
			}
			
			holder.name.setText(folder.fileName+" ("+folder.count+")");
			holder.path.setText(folder.path);
			holder.checkbox.setChecked(folder.imported);
			
	                return convertView;
                }
	}
	
	class Folder {
		private File file;
		private String path;
		private String fileName;
		private int count;
		private boolean imported;
	}
	
	class Holder {
		Folder folder;
		LinearLayout container;
		TextView name, path;
		CheckBox checkbox;
	}
}
