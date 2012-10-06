package com.tapped.nfc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kinvey.KCSClient;
import com.kinvey.KinveySettings;
import com.kinvey.util.ScalarCallback;

public class TagsActivity extends Activity {
	private static final String KINVEY_KEY = "YOUR_APP_KEY";
	private static final String KINVEY_SECRET_KEY = "YOUR_APP_SECRET_KEY";
	
	private KCSClient kinveyClient;
	private NfcAdapter mNfcAdapter;

	private Button mEnableWriteButton;
	private EditText mTextField;
	private ProgressBar mProgressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tags);

		mTextField = (EditText) findViewById(R.id.text_field);

		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mProgressBar.setVisibility(View.GONE);

		mEnableWriteButton = (Button) findViewById(R.id.enable_write_button);
		mEnableWriteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setTagWriteReady(!isWriteReady);
				mProgressBar.setVisibility(isWriteReady ? View.VISIBLE : View.GONE);
			}
		});

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, "Sorry, NFC is not available on this device", Toast.LENGTH_SHORT).show();
			finish();
		}
		
		// Initialize Kinvey
        KinveySettings settings = new KinveySettings(KINVEY_KEY, KINVEY_SECRET_KEY);
        kinveyClient = KCSClient.getInstance(this.getApplicationContext(), settings);
	}

	private boolean isWriteReady = false;

	/**
	 * Enable this activity to write to a tag
	 * 
	 * @param isWriteReady
	 */
	public void setTagWriteReady(boolean isWriteReady) {
		this.isWriteReady = isWriteReady;
		if (isWriteReady) {
			IntentFilter[] writeTagFilters = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) };
			mNfcAdapter.enableForegroundDispatch(TagsActivity.this, NfcUtils.getPendingIntent(TagsActivity.this),
					writeTagFilters, null);
		} else {
			// Disable dispatch if not writing tags
			mNfcAdapter.disableForegroundDispatch(TagsActivity.this);
		}
		mProgressBar.setVisibility(isWriteReady ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isWriteReady && NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {
			processWriteIntent(getIntent());
		} else if (!isWriteReady
				&& (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction()) || NfcAdapter.ACTION_NDEF_DISCOVERED
						.equals(getIntent().getAction()))) {
			processReadIntent(getIntent());
		}
	}

	private static final String MIME_TYPE = "application/com.tapped.nfc.tag";

	/**
	 * Write to an NFC tag; reacting to an intent generated from foreground
	 * dispatch requesting a write
	 * 
	 * @param intent
	 */
	public void processWriteIntent(Intent intent) {
		if (isWriteReady && NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {

			Tag detectedTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);

			String tagWriteMessage = mTextField.getText().toString();
			byte[] payload = new String(tagWriteMessage).getBytes();

			if (detectedTag != null && NfcUtils.writeTag(
					NfcUtils.createMessage(MIME_TYPE, payload), detectedTag)) {
				
				Toast.makeText(this, "Wrote '" + tagWriteMessage + "' to a tag!", 
						Toast.LENGTH_LONG).show();
				setTagWriteReady(false);
			} else {
				Toast.makeText(this, "Write failed. Please try again.", Toast.LENGTH_LONG).show();
			}
		}
	}

	public void processReadIntent(Intent intent) {
		List<NdefMessage> intentMessages = NfcUtils.getMessagesFromIntent(intent);
		List<String> payloadStrings = new ArrayList<String>(intentMessages.size());

		for (NdefMessage message : intentMessages) {
			for (NdefRecord record : message.getRecords()) {
				byte[] payload = record.getPayload();
				String payloadString = new String(payload);

				if (!TextUtils.isEmpty(payloadString))
					payloadStrings.add(payloadString);
			}
		}

		if (!payloadStrings.isEmpty()) {
			String content =  TextUtils.join(",", payloadStrings);
			Toast.makeText(TagsActivity.this, "Read from tag: " + content,
					Toast.LENGTH_LONG).show();
			saveTag(content);
		}
	}
	
	private void saveTag(String tagMessage){
		TagReadEntity tag = new TagReadEntity(UUID.randomUUID().toString(), 
				tagMessage, System.currentTimeMillis());
		
		kinveyClient.mappeddata("tags").save(tag, new ScalarCallback<TagReadEntity>() {

			@Override
			public void onSuccess(TagReadEntity tag) {
				Log.i("NFC Demo", "Saved tag!");
			}
			
			@Override
			public void onFailure(Throwable e) {
				Log.e("NFC Demo", "Error saving tag", e);
			}
		});
	}
}
