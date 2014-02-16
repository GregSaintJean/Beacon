package com.therabbitmage.android.beacon.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.therabbitmage.android.beacon.R;

public class SocialSetupFragment extends Fragment implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener{
	private static final String TAG = SocialSetupFragment.class.getSimpleName();
	
	private View mRoot;
	private SignInButton mGoogleLoginButton;
	private Button mFacebookLoginButton;
	private Button mTwitterLoginButton;
	
	private ConnectionResult mConnectionResult;
	
	private static final int STATE_DEFAULT = 0;
	private static final int STATE_SIGN_IN = 1;
	private static final int STATE_IN_PROGRESS = 2;
	
	/* Request code used to invoke sign in user interactions. */
	  private static final int RC_SIGN_IN = 0;

	  /* Client used to interact with Google APIs. */
	  private GoogleApiClient mGoogleApiClient;

	  /* A flag indicating that a PendingIntent is in progress and prevents
	   * us from starting further intents.
	   */
	  private boolean mIntentInProgress;
	  private boolean mSignInClicked;
	  private Session.StatusCallback mStatusCallback = new SessionStatusCallback();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Google Plus setup
		mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(Plus.API, null)
        .addScope(Plus.SCOPE_PLUS_LOGIN)
        .build();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRoot = inflater.inflate(R.layout.social_setup_fragment, container, false);
		mGoogleLoginButton = (SignInButton)mRoot.findViewById(R.id.google_login_btn);
		mFacebookLoginButton = (Button)mRoot.findViewById(R.id.facebook_login_btn);
		mTwitterLoginButton = (Button)mRoot.findViewById(R.id.twitter_login_btn);
		
		mGoogleLoginButton.setOnClickListener(this);
		mFacebookLoginButton.setOnClickListener(this);
		mTwitterLoginButton.setOnClickListener(this);

		//Facebook setup
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		Session session = Session.getActiveSession();
		if(session == null){
			if(savedInstanceState != null){
				session = Session.restoreSession(getActivity(), null, mStatusCallback, savedInstanceState);
			}
			
			if(session == null){
				session = new Session(getActivity());
			}
			
			Session.setActiveSession(session);
			if(session.getState().equals(SessionState.CREATED_TOKEN_LOADED)){
				session.openForRead(new Session.OpenRequest(getActivity()).setCallback(mStatusCallback));
			}
		}
		
		//TODO find out what to do here.
		
		return mRoot;
	}

	@Override
	public void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
		Session.getActiveSession().addCallback(mStatusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
		      mGoogleApiClient.disconnect();
		}
		Session.getActiveSession().removeCallback(mStatusCallback);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//Facebook setup
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
		
		//Google Plus setup
	}

	@Override
	public void onClick(View v) {
		
		if(v.getId() == R.id.google_login_btn
				&& !mGoogleApiClient.isConnecting()){
			mSignInClicked = true;
			resolveSignInError();
		}
		
		if(v.getId() == R.id.facebook_login_btn){
			Session session = Session.getActiveSession();
			if(!session.isOpened() && !session.isClosed()){
				session.openForRead(new Session.OpenRequest(this).setCallback(mStatusCallback));
			} else {
				Session.openActiveSession(getActivity(), this, true, mStatusCallback);
			}
		}
		
		if(v.getId() == R.id.twitter_login_btn){
			
		}
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Google Plus setup
		Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult is called.");
		if (requestCode == RC_SIGN_IN) {
			if (resultCode != Activity.RESULT_OK) {
			      mSignInClicked = false;
			} else{
				Log.d(TAG, "onActivityResult is reporting a resultCode of \"canceled\"");
			}
			
			mIntentInProgress = false;

		    if (!mGoogleApiClient.isConnecting()) {
		      mGoogleApiClient.connect();
		    }
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.d(TAG, "onConnected called");
		//Google Plus setup
		mSignInClicked = false;
		Toast.makeText(getActivity(), "User is connected!", Toast.LENGTH_LONG).show();
		
		for(String s : bundle.keySet()){
			Log.d(TAG, s);
		}
	}
	
	/* A helper method to resolve the current ConnectionResult error. */
	private void resolveSignInError() {
		Log.d(TAG, "resolveSignInError called");
	  if (mConnectionResult != null 
			  && mConnectionResult.hasResolution()) {
	    try {
	      mIntentInProgress = true;
	      getActivity().startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
	          RC_SIGN_IN, null, 0, 0, 0);
	    } catch (SendIntentException e) {
	      // The intent was canceled before it was sent.  Return to the default
	      // state and attempt to connect to get an updated ConnectionResult.
	      mIntentInProgress = false;
	      mGoogleApiClient.connect();
	    }
	  }
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		//Google Plus setup
		Log.d(TAG, "onConnectionFailed called");
		mConnectionResult = result;
		if (!mIntentInProgress) {
		    // Store the ConnectionResult so that we can use it later when the user clicks
		    // 'sign-in'.
		    mConnectionResult = result;

		    if (mSignInClicked) {
		      // The user has already clicked 'sign-in' so we attempt to resolve all
		      // errors until the user is signed in, or they cancel.
		      resolveSignInError();
		    }
		  }
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		//Google Plus setup
		Log.d(TAG, "onConnectionSuspended called");
		mGoogleApiClient.connect();
	}
	
	private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        	Session active = Session.getActiveSession();
        	if(active.isOpened()){
        		Log.d(TAG, "Application ID: " + active.getApplicationId());
        		Log.d(TAG, "Access Token: " + active.getAccessToken());
        		Log.d(TAG, "Expiration Date: " + active.getExpirationDate().toString());
        		active.closeAndClearTokenInformation();
        	}
        }
    }

}
