package sions.android.sionsbeat.adapter;

import java.io.File;
import java.util.List;

import com.squareup.picasso.Picasso;

import sions.android.SQ;
import sions.android.sionsbeat.MusicSelectActivity;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.game.GameMode;
import sions.android.sionsbeat.interpret.InterpretCollector;
import sions.android.sionsbeat.template.SongInfo;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.JsonType;
import sions.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MusicListAdapter extends BaseAdapter {
	
	public static void setupProgress (final Activity context, final SongInfo song, final int progress) {
		if (song.getTag() != null) {
			context.runOnUiThread(new Runnable() {
				public void run () {
					if (song.getTag() != null) {
						View view = (View) song.getTag();

						View record = view.findViewById(R.id.record);
						View interpret = view.findViewById(R.id.interpret);

						record.setVisibility(View.GONE);
						interpret.setVisibility(View.VISIBLE);

						ProgressBar pb = (ProgressBar) view.findViewById(R.id.interpret_progress);
						TextView tv = (TextView) view.findViewById(R.id.interpret_text);

						pb.setProgress(progress);
						pb.setVisibility(View.VISIBLE);

						tv.setText(context.getString(R.string.select_list_interpret_item, progress)); // 분석 중 %d%

					}
				}
			});
		}
	}

	public static void setupSuccess (final Activity context, final SongInfo song) {
		if (song.getTag() != null) {
			context.runOnUiThread(new Runnable() {
				public void run () {
					if (song.getTag() != null) {
						View view = (View) song.getTag();

						View recordv = view.findViewById(R.id.record);
						View interpret = view.findViewById(R.id.interpret);

						recordv.setVisibility(View.VISIBLE);
						interpret.setVisibility(View.GONE);
						
						TextView recordLevel = (TextView) recordv.findViewById(R.id.record_level);
						TextView recordRank = (TextView) recordv.findViewById(R.id.record_rank);
						TextView recordScore = (TextView) recordv.findViewById(R.id.record_score);
						
						JSONObject musics = SQ.getJSONObject(JsonType.MUSIC);
						if(musics != null){
							JSONObject record = musics.getJSONObject(song.getIdentity());
							if(record != null){
								recordLevel.setText(String.valueOf(record.getInt("level")));
								recordRank.setText(GameMode.RANK_TEXT[record.getInt("rank")]);
								recordScore.setText(context.getString(R.string.select_list_record,record.getInt("score"))); //기록 %,3d 
							}else{
								musics = null; // 뷰를 비우기 위함
							}
						}
						
						if(musics == null){
							recordLevel.setText("");
							recordRank.setText("");
							recordScore.setText(R.string.select_list_record_none); //기록 없음
						}
					}
				}
			});
		}
	}

	public static void setupFailed (Activity context, final SongInfo song, Throwable e) {
		if(e != null){
//			ErrorController.error(1, e);
		}

		if (song.getTag() != null) {
			context.runOnUiThread(new Runnable() {
				public void run () {
					if (song.getTag() != null) {
						View view = (View) song.getTag();

						View record = view.findViewById(R.id.record);
						View interpret = view.findViewById(R.id.interpret);

						record.setVisibility(View.GONE);
						interpret.setVisibility(View.VISIBLE);

						ProgressBar pb = (ProgressBar) view.findViewById(R.id.interpret_progress);
						TextView tv = (TextView) view.findViewById(R.id.interpret_text);

						tv.setText(R.string.select_list_interpret_failed); //분석 실패

						pb.setVisibility(View.GONE);
					}
				}
			});
		}
	}

	public static void setupNone (Activity context, final SongInfo song) {
		Log.d(MusicSelectActivity.TAG, "setup None " + song.getID() + ":" + song.getTitle());

		if (song.getTag() != null) {
			context.runOnUiThread(new Runnable() {
				public void run () {
					if (song.getTag() != null) {
						View view = (View) song.getTag();

						View record = view.findViewById(R.id.record);
						View interpret = view.findViewById(R.id.interpret);

						record.setVisibility(View.GONE);
						interpret.setVisibility(View.VISIBLE);

						ProgressBar pb = (ProgressBar) view.findViewById(R.id.interpret_progress);
						TextView tv = (TextView) view.findViewById(R.id.interpret_text);

						tv.setText(R.string.select_list_interpret_wait); //분석 대기 중

						pb.setVisibility(View.GONE);
					}
				}
			});
		}
	}

	private List<SongInfo> list;
	private Activity context;
	private LayoutInflater inflater;
	
	public static boolean load_album_cover;

	public MusicListAdapter (Activity context, List<SongInfo> list)
	{
		this.context = context;
		this.list = list;

		this.inflater = LayoutInflater.from(context);
		
		this.load_album_cover = GameOptions.get(context).getSettingBoolean(GameOptions.OPTION_GRAPHIC_LOAD_THUMBNAIL);
	}
	
	public void setList(List<SongInfo> list){
		this.list = list;
	}

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

		SongInfo song = list.get(position);

		ViewHolder holder = null;
		if (convertView == null || convertView.getTag() == null) {

			holder = new ViewHolder();

			convertView = inflater.inflate(R.layout.layout_music_item, null);
			holder.master = convertView;
			holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
			holder.artist = (TextView) convertView.findViewById(R.id.artist);
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.duration = (TextView) convertView.findViewById(R.id.duration);

			convertView.setTag(holder);

		} else {

			holder = (ViewHolder) convertView.getTag();

		}

		if (holder.song != null) {
			holder.song.setTag(null);
		}
		holder.song = song;
		holder.song.setTag(holder.master);

		if(load_album_cover){
			song.parseAlbumView50(context, holder.thumbnail, R.drawable.list_empty);
		}else{
			holder.thumbnail.setImageResource(R.drawable.list_empty);
		}
		
		holder.artist.setText(song.getArtist());
		holder.title.setText(song.getTitle());
		holder.duration.setText(song.getDurationString());
		
		onStatusSetup(holder.song);

		return convertView;
	}
	
	public void onStatusSetup(SongInfo song){
		
		if (song.isCompatibility()){
			setupSuccess(context, song);
		}else if (song == InterpretCollector.get(context).getInterpretSong()) {
			setupProgress(context, InterpretCollector.get(context).getInterpretSong(), InterpretCollector.get(context).getInterpretProgress());
		} else {
			JSONObject obj = SQ.getJSONObject(JsonType.INTERPRET);
			if (obj != null) {
				int type = obj.getInt(song.getIdentity(), InterpretCollector.INTERPRET_NONE);
				switch ( type )
				{
					case InterpretCollector.INTERPRET_FAILED:
						setupFailed(context, song, null);
						break;
					case InterpretCollector.INTERPRET_NONE:
						setupNone(context, song);
						break;
					default:
						setupSuccess(context, song);
						break;
				}
			} else {
				setupNone(context, song);
			}
		}
		
	}
	
	class ViewHolder {
		SongInfo song;
		View master;
		ImageView thumbnail;
		TextView artist, title, duration;
	}

}
