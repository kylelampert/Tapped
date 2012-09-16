package com.enavigo.tapped.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.enavigo.tapped.R;
import com.enavigo.tapped.TappedApplication;
import com.enavigo.tapped.services.FacebookService;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.kinvey.KinveyUser;
import com.kinvey.util.KinveyCallback;

public class LoginActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		if (FacebookService.facebook.isSessionValid()){
			startHomeActivity();
		}

		ImageView loginButton = (ImageView) findViewById(R.id.fb_login);
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loginWithFacebook();
			}
		});
		
		// TODO don't login during onCreate - that's redundant - need to check
		// if the session is valid

		// TODO global logout ?
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		FacebookService.facebook.authorizeCallback(requestCode, resultCode, data);
	}
	
	private void loginWithFacebook(){
		// The FB SDK has a bit of a delay in response
		final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Connecting to Facebook",
				"Logging in with Facebook - just a moment");

		FacebookService.facebook.authorize(LoginActivity.this, new String[] { "publish_stream," , "publish_checkins" },
				new DialogListener() {
					@Override
					public void onComplete(Bundle values) {
						// Close the progress dialog and toast success to the user
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						Toast.makeText(LoginActivity.this, "Logged in with Facebook.", Toast.LENGTH_LONG)
								.show();
						
						// Login to Kinvey
						 ((TappedApplication) getApplication()).getKinveyService().loginWithFacebookAccessToken(
								 FacebookService.facebook.getAccessToken(),
								 new KinveyCallback<KinveyUser>() {
									
									@Override
									public void onSuccess(KinveyUser arg0) {
										Log.i("Tapped", "Logged in to Kinvey");
									}
								});
						
						startHomeActivity();
					}

					@Override
					public void onFacebookError(FacebookError error) {
						showFacebookError(progressDialog);
					}

					@Override
					public void onError(DialogError e) {
						showFacebookError(progressDialog);
					}

					@Override
					public void onCancel() {
						Toast.makeText(LoginActivity.this, "FB auth cancelled", Toast.LENGTH_LONG).show();
					}
				});
	}
	
	private void startHomeActivity(){
		Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
		startActivity(intent);
		finish();
	}
	
	private void showFacebookError(final ProgressDialog progressDialog){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				Toast.makeText(LoginActivity.this, "Error logging in to Facebook. Please try again.",
						Toast.LENGTH_LONG).show();
			}
		});
	}

}
