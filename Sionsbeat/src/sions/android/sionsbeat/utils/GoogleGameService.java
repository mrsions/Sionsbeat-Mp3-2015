package sions.android.sionsbeat.utils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

import sions.android.sionsbeat.R;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class GoogleGameService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private GoogleApiClient mGoogleApiClient;
	private Activity context;
	
	public GoogleGameService (Activity context){

		this.context = context;
		
		GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context);
		builder.addConnectionCallbacks(this);
		builder.addOnConnectionFailedListener(this);
		builder.addApi(Games.API);
		builder.addScope(Games.SCOPE_GAMES);
		
		mGoogleApiClient = builder.build();
		
	}

	private static final int RC_SIGN_IN = 9001;
	private static final int REQUEST_LEADERBOARD = 8001;
	private static final int REQUEST_ACHIEVEMENTS = 8002;

	private boolean mResolvingConnectionFailure = false;
	private boolean mAutoStartSignInFlow = true;
	private boolean mSignInClicked = false;
	private boolean connected = false;

	/**
	 * The player is signed in. Hide the sign-in button and allow the player to proceed.
	 */
	@Override
	public void onConnected (Bundle connectionHint) {
		Log.d("test", "onConnected " + connectionHint);
		connected = true;
	}

	@Override
	public void onConnectionFailed (ConnectionResult connectionResult) {
		Log.d("test", "onConnectionFailed " + connectionResult);

		if (mResolvingConnectionFailure) {
			// already resolving
			Log.d("test", "onConnectionFailed 1");
			return;
		}

		// if the sign-in button was clicked or if auto sign-in is enabled,
		// launch the sign-in flow
		if (mSignInClicked || mAutoStartSignInFlow) {
			Log.d("test", "onConnectionFailed 2");
			mAutoStartSignInFlow = false;
			mSignInClicked = false;
			mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(context, mGoogleApiClient, connectionResult, RC_SIGN_IN, context.getString(R.string.google_initialize_failed));
		}

		// Put code here to display the sign-in button
		Log.d("test", "onConnectionFailed 4");
	}

	@Override
	public void onConnectionSuspended (int i) {
		Log.d("test", "onConnectionSuspended " + i);
		mGoogleApiClient.connect();
	}

	public void onActivityResult (int requestCode, int resultCode, Intent intent) {
		Log.d("test", "onActivityResult ");
		if (requestCode == RC_SIGN_IN) {
			mSignInClicked = false;
			mResolvingConnectionFailure = false;
			if (resultCode == Activity.RESULT_OK) {
				mGoogleApiClient.connect();
			} else {
				// Bring up an error dialog to alert the user that sign-in
				// failed. The R.string.signin_failure should reference an error
				// string in your strings.xml file that tells the user they
				// could not be signed in, such as "Unable to sign in."
				BaseGameUtils.showActivityResultError(context, requestCode, resultCode, R.string.google_initialize_failed);
			}
		}
	}
	
	public void onStart(){
		mGoogleApiClient.connect();	
	}
	
	public void onStop(){
		mGoogleApiClient.disconnect();
	}
	
	// Call when the sign-in button is clicked
	public void signInClicked () {
		mSignInClicked = true;
		mGoogleApiClient.connect();
	}

	// Call when the sign-out button is clicked
	public void signOutclicked () {
		mSignInClicked = false;
		Games.signOut(mGoogleApiClient);
	}
	
	public void achivementsIncrement(int resid, int add){
		if(connected){
			Games.Achievements.increment(mGoogleApiClient, context.getString(resid), add);
		}
	}
	
	public void leaderboardSubmit(int resid, int add){
		if(connected){
			Games.Leaderboards.submitScore(mGoogleApiClient, context.getString(resid), add);
		}
	}

	public void showLeaderboard(Activity activity, int resid){
		if(connected){
			Intent intent = Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, activity.getString(resid));
			activity.startActivityForResult(intent, REQUEST_LEADERBOARD);
		}
	}
	public void showAchievements(Activity activity){
		if(connected){
			Intent intent = Games.Achievements.getAchievementsIntent(mGoogleApiClient);
			activity.startActivityForResult(intent, REQUEST_ACHIEVEMENTS);
		}
	}
}
