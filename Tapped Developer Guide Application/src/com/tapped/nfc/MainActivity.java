package com.tapped.nfc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tapped.nfc.FacebookService.OnRequestErrorListener;
import com.tapped.nfc.FacebookService.OnRequestResultListener;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button beamButton = (Button) findViewById(R.id.beam_button);
		beamButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, BeamActivity.class));
			}
		});

		Button tagsButton = (Button) findViewById(R.id.tags_button);
		tagsButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, TagsActivity.class));
			}
		});
		
		Button tagHistoryButton = (Button) findViewById(R.id.tag_history_button);
		tagHistoryButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, TagHistoryActivity.class));
			}
		});

		final EditText facebookPostContent = (EditText) findViewById(R.id.facebook_post_content);

		Button facebookPostButton = (Button) findViewById(R.id.post_to_facebook_button);
		facebookPostButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				postToFacebook(facebookPostContent.getText().toString());
			}
		});
	}

	private void postToFacebook(String content) {

		Bundle params = new Bundle();
		params.putString("message", content);

		FacebookService.requestAsync("me/feed", params, FacebookService.RequestType.POST,
				new OnRequestResultListener() {

					@Override
					public void onRequestResult(String result) {
						facebookPostResult(result);
					}
				}, new OnRequestErrorListener() {

					@Override
					public void onRequestError() {
						facebookPostResult("Error");
					}
				});
	}

	private void facebookPostResult(final String result) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(MainActivity.this, "Facebook posted with result: " + result, 
						Toast.LENGTH_SHORT).show();
			}
		});
	}
}
