package sions.android.sionsbeat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.util.Log;
import sions.android.SQ;
import sions.android.sionsbeat.fragment.AdsBanner;

public class Base 
{
	public static final boolean FREE = true;
	
	
	private static InterstitialAd immersiveAds;
	public static void InitializeImmersiveAds(Context context)
	{
		if(immersiveAds == null)
		{	
			immersiveAds = new InterstitialAd(context);
			immersiveAds.setAdUnitId("ca-app-pub-1093695024710196/2474188065");
		}
		immersiveAds.loadAd(AdsBanner.getAdRequest());
		Log.d("test", "Initialized");
	}
	public static boolean ShowImmersiveAds(final Context context, final Runnable listener)
	{
		if(immersiveAds == null) InitializeImmersiveAds(context);
		if(immersiveAds != null && immersiveAds.isLoaded())
		{	
			immersiveAds.setAdListener(new AdListener()
			{
				@Override public void onAdClosed()
				{
					InitializeImmersiveAds(context);
					if(listener != null)
					{
						listener.run();
					}
				}
			});
			immersiveAds.show();
			return true;
		}
		return false;
	}
	
	public static final String URI_PURCHASE_APPLICATION = "market://details?id=sions.android.nsionsbeat";
	static AlertDialog msgDialog;
	public static void ShowPleasePurchaseFunction(final Context context)
	{
		ShowPleasePurchase(R.string.purchase_function, context);
	}
	public static void ShowPleasePurchaseNew(final Context context)
	{
		ShowPleasePurchase(R.string.purchase_new_application, context);
	}
	public static void ShowPleasePurchase(int messageID, final Context context)
	{
		if(msgDialog != null && msgDialog.isShowing()) return;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(messageID); // 이 기능을 사용하시려면 정식 어플을 다운받아주세요. 정식 어플에서는 광고도 표시되지 않습니다. 
		builder.setPositiveButton(R.string.purchase_download, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URI_PURCHASE_APPLICATION));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				msgDialog = null;
			}
		});
		builder.setNegativeButton(R.string.purchase_cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				msgDialog = null;
			}
		});
		msgDialog = builder.show();
	}
}
