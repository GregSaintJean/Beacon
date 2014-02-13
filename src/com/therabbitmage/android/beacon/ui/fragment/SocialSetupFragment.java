package com.therabbitmage.android.beacon.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.therabbitmage.android.beacon.R;

public class SocialSetupFragment extends Fragment implements OnClickListener{
	
	private View mRoot;
	private Button mGoogleLoginButton;
	private Button mFacebookLoginButton;
	private Button mTwitterLoginButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRoot = inflater.inflate(R.layout.social_setup_fragment, container, false);
		mGoogleLoginButton = (Button)mRoot.findViewById(R.id.google_login_btn);
		mFacebookLoginButton = (Button)mRoot.findViewById(R.id.facebook_login_btn);
		mTwitterLoginButton = (Button)mRoot.findViewById(R.id.twitter_login_btn);
		
		mGoogleLoginButton.setOnClickListener(this);
		mFacebookLoginButton.setOnClickListener(this);
		mTwitterLoginButton.setOnClickListener(this);
		return mRoot;
	}

	@Override
	public void onClick(View v) {
		
		if(v == mGoogleLoginButton){
			
		}
		
		if(v == mFacebookLoginButton){
					
		}
		
		if(v == mTwitterLoginButton){
			
		}
		
	}

}
