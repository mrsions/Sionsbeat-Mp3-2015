package sions.android.sionsbeat.interpret;

import sions.android.sionsbeat.template.SongInfo;

public interface InterpretListener 
{
	public void onInterpretProgress(SongInfo song, int progress);
	public void onInterpretSuccess(SongInfo song);
	public void onInterpretFailed(SongInfo song, Throwable e);
}
