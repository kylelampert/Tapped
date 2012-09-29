package com.tapped.nfc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class FacebookLoginActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_login);
        
        Button loginButton = (Button) findViewById(R.id.facebook_login_button);
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// The FB SDK has a bit of a delay in response
				final ProgressDialog progressDialog = ProgressDialog.show(
						FacebookLoginActivity.this, "Connecting to Facebook",
						"Logging in with Facebook - just a moment");

				doFacebookSso(progressDialog);
			}
		});
		
		Button skipButton = (Button) findViewById(R.id.skip_button);
		skipButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Start the main activity
				Intent intent = new Intent(FacebookLoginActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
    }
    
    private void doFacebookSso(final ProgressDialog progressDialog){
    	
    	FacebookService.facebook.authorize(FacebookLoginActivity.this, 
				new String[] { "publish_stream," , "publish_checkins" },
				new DialogListener() {
					@Override
					public void onComplete(Bundle values) {
						// Close the progress dialog and toast success to the user
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						Toast.makeText(FacebookLoginActivity.this, "Logged in with Facebook.", 
								Toast.LENGTH_LONG).show();
						
						// Start the main activity
						Intent intent = new Intent(FacebookLoginActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
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
						Toast.makeText(FacebookLoginActivity.this, "FB login cancelled", 
								Toast.LENGTH_LONG).show();
					}
				});
    }
    
    private void showFacebookError(final ProgressDialog progressDialog){
    	if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
    	Toast.makeText(FacebookLoginActivity.this, "Error logging in to Facebook. " +
    			"Please try again.", Toast.LENGTH_LONG).show();
    }
}
