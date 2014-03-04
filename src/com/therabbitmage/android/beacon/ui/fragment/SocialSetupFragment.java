package com.therabbitmage.android.beacon.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.service.TwitterIntentService;

public class SocialSetupFragment extends Fragment implements OnClickListener,
		PlusClient.ConnectionCallbacks, PlusClient.OnConnectionFailedListener,
		PlusClient.OnAccessRevokedListener {
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

	private PlusClient mPlusClient;

	/*
	 * A flag indicating that a PendingIntent is in progress and prevents us
	 * from starting further intents.
	 */
	private boolean mIntentInProgress;
	private boolean mSignInClicked;
	private final Session.StatusCallback mStatusCallback = new SessionStatusCallback();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPlusClient = new PlusClient.Builder(getActivity(), this, this).build();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRoot = inflater.inflate(R.layout.social_setup_activity, container,
				false);
		mFacebookLoginButton = (Button) mRoot
				.findViewById(R.id.facebook_login_btn);
		mTwitterLoginButton = (Button) mRoot
				.findViewById(R.id.twitter_login_btn);

		mFacebookLoginButton.setOnClickListener(this);
		mTwitterLoginButton.setOnClickListener(this);

		// Facebook setup
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(getActivity(), null,
						mStatusCallback, savedInstanceState);
			}

			if (session == null) {
				session = new Session(getActivity());
			}

			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(getActivity())
						.setCallback(mStatusCallback));
			}
		}

		// TODO find out what to do here.

		return mRoot;
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(mStatusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(mStatusCallback);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Facebook setup
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.facebook_login_btn) {
			Session session = Session.getActiveSession();
			if (!session.isOpened() && !session.isClosed()) {
				session.openForRead(new Session.OpenRequest(this)
						.setCallback(mStatusCallback));
			} else {
				Session.openActiveSession(getActivity(), this, true,
						mStatusCallback);
			}
		}

		if (v.getId() == R.id.twitter_login_btn) {
			Log.d(TAG, "Twitter button pressed");
			Intent intent = new Intent(getActivity(),
					TwitterIntentService.class);
			intent.setAction(TwitterIntentService.ACTION_AUTH);
			getActivity().startService(intent);
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(getActivity(), requestCode,
				resultCode, data);
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (session.isOpened()) {
				Log.d(TAG, "Application ID: " + session.getApplicationId());
				Log.d(TAG, "Access Token: " + session.getAccessToken());
				Log.d(TAG, "Expiration Date: "
						+ session.getExpirationDate().toString());
				session.closeAndClearTokenInformation();
			} else if (exception != null) {
				Log.e(TAG + exception.getStackTrace()[0].getLineNumber(),
						exception.toString());
			}
		}
	}

	@Override
	public void onAccessRevoked(ConnectionResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

}
