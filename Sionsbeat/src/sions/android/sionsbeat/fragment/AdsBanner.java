package sions.android.sionsbeat.fragment;

import sions.android.sionsbeat.R;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AdsBanner extends AdListener{
	
	public static AdRequest getAdRequest(){
		return new AdRequest.Builder()
			.addTestDevice("6EBC217330944DE8DEA6A08971221E6D")
			.addTestDevice("58D31E04F65F6908F0A5BACC8050CE4D")
			.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
			.build();
	}

	private AdView adView;
	private AdRequest request;

	public AdsBanner (AdView adview)
	{
		this.adView = adview;
		this.adView.setAdListener(this);
		
		request = getAdRequest();
		adView.loadAd(request);
	}
	

	public void onPause () {
		if (adView != null) {
			adView.pause();
		}
	}

	public void onResume () {
		if (adView != null) {
			adView.resume();
		}
	}

	public void onDestroy () {
		if (adView != null) {
			adView.destroy();
		}
	}

	@Override
	public void onAdClosed () {
		Log.d("test", "onAdClosed");
	        super.onAdClosed();
	}
	
	@Override
	public void onAdFailedToLoad (int errorCode) {
		Log.d("test", "onAdFailedToLoad");
//		adView.loadAd(request);
	        super.onAdFailedToLoad(errorCode);
	}
	
	@Override
	public void onAdLeftApplication () {
		Log.d("test", "onAdLeftApplication");
	        super.onAdLeftApplication();
	}

	@Override
	public void onAdOpened () {
		Log.d("test", "onAdOpened");
	        super.onAdOpened();
	}
	
	@Override
	public void onAdLoaded () {
		Log.d("test", "onAdLoaded");
	        super.onAdLoaded();
	}
	
}
