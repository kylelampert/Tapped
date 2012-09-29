package com.enavigo.tapped.activities;

import java.util.List;
import java.util.UUID;

import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.enavigo.tapped.R;
import com.enavigo.tapped.TappedApplication;
import com.enavigo.tapped.entities.BeamReceivedEntity;
import com.enavigo.tapped.entities.TagReadEntity;
import com.enavigo.tapped.fragments.BeamFragment;
import com.enavigo.tapped.fragments.HistoryListFragment;
import com.enavigo.tapped.fragments.SharingFragment;
import com.enavigo.tapped.fragments.SimpleLayoutFragment;
import com.enavigo.tapped.fragments.TagFragment;
import com.enavigo.tapped.services.FacebookService;
import com.enavigo.tapped.services.KinveyService;
import com.enavigo.tapped.utils.Constants;
import com.enavigo.tapped.utils.NfcUtils;
import com.kinvey.KCSClient;
import com.kinvey.util.ScalarCallback;

public class HomeActivity extends FragmentActivity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {
	private static final String LOG_TAG = "Tapped-" + HomeActivity.class.getSimpleName();

	private static final int MESSAGE_SENT = 1;
	
	// Index of each fragment within the viewpager
	private static final int BEAM_FRAGMENT_INDEX = 1;
	private static final int TAG_FRAGMENT_INDEX = 2;
	private static final int SHARE_FRAGMENT_INDEX = 3;

	// Provides access to Android's NFC service layer
	private NfcAdapter nfcAdapter;
	
	// Kinvey shared client
	private KCSClient sharedClient;
	
	// For tag writing
	private String tagWriteMessage;
	private boolean isWriteReady = false;

	// Fragments in this view
	private SharingFragment sharingFragment;
	private BeamFragment beamFragment;
	private TagFragment tagFragment;
	private HistoryListFragment historyFragment;

	// Facebook-related data to be read / written
	private String beamRecievedFacebookUsername;
	private String beamRecievedFacebookUserId;
	private String tagRecievedFacebookPlaceId;

	// Callback interfaces
	public interface OnTagReadWriteListener {
		public abstract void onTagWritten(String newValue, String previousValue);
		public abstract void onTagRead(String readValue);
	}

	public interface OnBeamRecievedListener {
		public abstract void onBeamRecieved(String username, String userId);
	}

	public interface OnPlaceChangedListener {
		public abstract void onPlaceChanged(String placeName, String facebookPlaceId);
	}

	// Supporting elements of the ViewPager
	private ViewPager mViewPager;
	private SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * A FragmentPagerAdapter that returns a fragment corresponding to
	 * one of the primary sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0:
				return new SimpleLayoutFragment(R.layout.fragment_intro);
			case 1:
				return beamFragment;
			case 2:
				return tagFragment;
			case 3:
				return sharingFragment;
			case 4:
				return historyFragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "Welcome";
			case 1:
				return "Beam";
			case 2:
				return "Tags";
			case 3:
				return "Share";
			case 4:
				return "Session History";
			}
			return null;
		}
	}

	/**
	 * Called when this activity is first created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_activity);

		// Create the adapter that will return a fragment for page
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Create the fragments that will be displayed as pages in the viewpager
		beamFragment = new BeamFragment(R.layout.fragment_beam);
		tagFragment = new TagFragment();
		sharingFragment = new SharingFragment();
		historyFragment = new HistoryListFragment();

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int page) {
				// Disable tag writing if the user navigates away from the tag-writer fragment
				if (isWriteReady && page != TAG_FRAGMENT_INDEX) {
					setTagWriteReady(false);
				}
				tagFragment.updateUi();
			}
		});
		
		// Init Kinvey
		sharedClient = getTappedApplication().
        		getKinveyService();

		// Check for available NFC Adapter
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		
		if (nfcAdapter == null) {
			Toast.makeText(this, "Sorry, NFC is not available on this device", Toast.LENGTH_SHORT).show();
		} else{
			// Register callback to set NDEF message
			nfcAdapter.setNdefPushMessageCallback(this, this);
			// Register callback to listen for message-sent success
			nfcAdapter.setOnNdefPushCompleteCallback(this, this);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		// set the intent here to update mIntent
		setIntent(intent);
	}

	/**
	 * Based on the intent's action, read/write a tag or read a beam message
	 */
	@Override
	public void onResume() {
		super.onResume();

		final Intent intent = getIntent();
		
		// NDEF Discovered (Beam or Tag)
		if (!isWriteReady && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			List<String> messages = NfcUtils.getStringsFromNfcIntent(intent);

			if (messages.size() >= 2) { // Beam
				
				// The username will always be in the first message
				beamRecievedFacebookUsername = messages.get(0);
				beamRecievedFacebookUserId = messages.get(1);

				// Notify the classes that care about this
				mViewPager.setCurrentItem(SHARE_FRAGMENT_INDEX);
				sharingFragment.onBeamRecieved(beamRecievedFacebookUsername, beamRecievedFacebookUserId);
				saveBeamToHistory(messages.get(0));

			} else { // It might be a tag
				readTag(intent);
			}
		}
		// Tag reading
		else if (!isWriteReady && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			readTag(intent);
		}
		// Tag writing
		else if (isWriteReady && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			writeTag(intent);
		}
	}
	
	private void readTag(Intent intent){
		List<String> messages = NfcUtils.getStringsFromNfcIntent(intent);
		for (String message : messages) {
			if (Constants.VALID_PLACES.contains(message)) {
				tagRecievedFacebookPlaceId = Constants.PLACE_TO_FB_ID.get(message);
				mViewPager.setCurrentItem(SHARE_FRAGMENT_INDEX);
				sharingFragment.onPlaceChanged(message, tagRecievedFacebookPlaceId);
				saveTagToHistory(message);
				return;
			} else {
				Log.w(LOG_TAG, "Unknown place : " + message);
			}
		}
	}
	
	private void writeTag(Intent intent){
		// Try to read anything on the tag before writing
		String previousValue = "";
		List<NdefMessage> messages = NfcUtils.getMessagesFromIntent(intent);
		for (NdefMessage message : messages) {
			byte[] payload = message.getRecords()[0].getPayload();
			previousValue = new String(payload);
			saveTagToHistory(previousValue);
			Log.i(LOG_TAG, "Read NDEF Message: " + previousValue);
		}

		if (TextUtils.isEmpty(tagWriteMessage)) {
			Toast.makeText(this, "Tag detected with no message ready to write.", Toast.LENGTH_LONG).show();
			return;
		}

		Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		byte[] payload = new String(tagWriteMessage).getBytes();
		if (detectedTag != null
				&& NfcUtils.writeTag(NfcUtils.createMessage(Constants.MIME_TYPE, payload), detectedTag)) {
			// Successful write has occurred
			tagFragment.onTagWritten(tagWriteMessage, previousValue);
		} else {
			Toast.makeText(this, "Write failed. Please try again.", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * (BEAM) Implementation for the OnNdefPushCompleteCallback interface
	 */
	@Override
	public void onNdefPushComplete(NfcEvent arg0) {
		// A handler is needed to send messages to the activity when this
		// callback occurs, because it happens from a binder thread
		mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}

	/**
	 * This handler receives a message from onNdefPushComplete At this point the
	 * ndef message has been successful pushed
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SENT:
				Toast.makeText(getApplicationContext(), "Beam message sent!", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	/**
	 * (BEAM) Implementation for the CreateNdefMessageCallback interface. The
	 * NDEF message is not created until it is needed.
	 */
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		String userName = FacebookService.getUserDataType(FacebookService.USER_DATA_NAME);
		String userId = FacebookService.getUserDataType(FacebookService.USER_DATA_ID);

		// In the event that this data is not available, send an empty message
		if (userName == null || userId == null) {
			userName = "";
			userId = "";
		}

		/**
		 * The order of the NDEF messages is crucial; the AAR
		 * (createApplicationRecord) must be *last* to ensure that the preceding
		 * payload is delivered. Any messages after the AAR are ignored.
		 */
		NdefMessage msg = new NdefMessage(new NdefRecord[] { 
				NfcUtils.createRecord(Constants.MIME_TYPE, userName.getBytes()),
				NfcUtils.createRecord(Constants.MIME_TYPE, userId.getBytes()), 
				NdefRecord.createApplicationRecord("com.enavigo.tapped") });
		return msg;
	}

	/**
	 * Save a read tag to history using Kinvey's cloud-based backend
	 * @param tagMessage
	 */
	private void saveTagToHistory(String tagMessage){
		if (TextUtils.isEmpty(tagMessage)){
			tagMessage = "(Empty)";
		}
		TagReadEntity tag = new TagReadEntity(sharedClient.getCurrentUser().getId(), tagMessage, System.currentTimeMillis());
		KinveyService.saveTag((TappedApplication) getApplication(), tag, new ScalarCallback<TagReadEntity>() {

			@Override
			public void onSuccess(TagReadEntity tag) {
				Log.i(LOG_TAG, "Saved tag!");
			}
			
			@Override
			public void onFailure(Throwable e) {
				Log.e(LOG_TAG, "Error saving tag", e);
			}
		});
	}

	/**
	 * Save a beam message to history using Kinvey's cloud-based backend
	 * @param beamMessage
	 */
	private void saveBeamToHistory(String beamMessage){
		if (TextUtils.isEmpty(beamMessage)){
			beamMessage = "(Empty)";
		}
		BeamReceivedEntity beam = new BeamReceivedEntity(sharedClient.getCurrentUser().getId(), beamMessage, System.currentTimeMillis());
		KinveyService.saveBeam((TappedApplication) getApplication(), beam, new ScalarCallback<BeamReceivedEntity>() {

			@Override
			public void onSuccess(BeamReceivedEntity beam) {
				Log.i(LOG_TAG, "Saved beam!");
			}
			
			@Override
			public void onFailure(Throwable e) {
				Log.e(LOG_TAG, "Error saving beam", e);
			}
		});
	}

	/**
	 * Is the application ready to write to a tag?
	 * @return
	 */
	public boolean getTagWriteReady() {
		return isWriteReady;
	}

	/**
	 * (TAG) Enable this activity to write to a tag (as opposed to any other
	 * action, such as read)
	 * 
	 * @param isWriteReady
	 */
	public void setTagWriteReady(boolean isWriteReady) {
		this.isWriteReady = isWriteReady;
		if (isWriteReady) {
			IntentFilter[] writeTagFilters = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) };
			nfcAdapter.enableForegroundDispatch(HomeActivity.this, NfcUtils.getPendingIntent(HomeActivity.this),
					writeTagFilters, null);
		} else {
			// Disable dispatch if not writing tags - otherwise this will
			// interfere with Android BEAM implementation
			nfcAdapter.disableForegroundDispatch(HomeActivity.this);
		}

	}

	/**
	 * (TAG) The message to be written
	 * 
	 * @param message
	 */
	public void setTagWriteMessage(String message) {
		tagWriteMessage = message;
	}
	
	/**
	 * Return a reference to the TappedApplication instance - for Kinvey
	 * @return
	 */
	public TappedApplication getTappedApplication(){
		return ((TappedApplication) getApplication());
	}
	
}
