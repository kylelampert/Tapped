package com.tapped.nfc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

/**
 * A collection of utility methods for NFC data
 */
public class NfcUtils {
	private static final String LOG_TAG = "NfcUtils";

	/**
	 * Creates a custom MIME type encapsulated in an NDEF record for a given
	 * payload
	 * 
	 * @param mimeType
	 */
	public static NdefRecord createRecord(String mimeType, byte[] payload) {
		byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
		NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
		return mimeRecord;
	}

	/**
	 * Creates an Ndef message
	 * 
	 * @param payload
	 * @return
	 */
	public static NdefMessage createMessage(String mimeType, byte[] payload) {
		// Min API Level of 14 requires an array as the argument
		return new NdefMessage(new NdefRecord[] { createRecord(mimeType, payload) });
	}

	/**
	 * Write an NDEF message to a Tag
	 * 
	 * @param message
	 * @param tag
	 * @return true if successful, false if not written to
	 */
	public static boolean writeTag(NdefMessage message, Tag tag) {
		int size = message.toByteArray().length;
		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();
				if (!ndef.isWritable()) {
					Log.e(LOG_TAG, "Not writing to tag- tag is not writable");
					return false;
				}
				if (ndef.getMaxSize() < size) {
					Log.e(LOG_TAG, "Not writing to tag- message exceeds the max tag size of " + ndef.getMaxSize());
					return false;
				}
				ndef.writeNdefMessage(message);
				return true;
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						return true;
					} catch (IOException e) {
						Log.e(LOG_TAG, "Not writing to tag", e);
						return false;
					}
				} else {
					Log.e(LOG_TAG, "Not writing to tag- undefined format");
					return false;
				}
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Not writing to tag", e);
			return false;
		}
	}

	/**
	 * Parse an intent for non-empty strings within an NDEF message
	 * 
	 * @param intent
	 * @return an empty list if the payload is empty
	 */
	public static List<String> getStringsFromNfcIntent(Intent intent) {
		List<String> payloadStrings = new ArrayList<String>();

		for (NdefMessage message : getMessagesFromIntent(intent)) {
			for (NdefRecord record : message.getRecords()) {
				byte[] payload = record.getPayload();
				String payloadString = new String(payload);

				if (!TextUtils.isEmpty(payloadString))
					payloadStrings.add(payloadString);
			}
		}

		return payloadStrings;
	}

	/**
	 * Parses an intent for NDEF messages, returns all that are found
	 * 
	 * @param intent
	 * @return an empty list if there are no NDEF messages found
	 */
	public static List<NdefMessage> getMessagesFromIntent(Intent intent) {
		List<NdefMessage> intentMessages = new ArrayList<NdefMessage>();
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Log.i(LOG_TAG, "Reading from NFC " + action);
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				for (Parcelable msg : rawMsgs) {
					if (msg instanceof NdefMessage) {
						intentMessages.add((NdefMessage) msg);
					}
				}
			} else {
				// Unknown tag type
				byte[] empty = new byte[] {};
				final NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
				final NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
				intentMessages = new ArrayList<NdefMessage>() {
					{
						add(msg);
					}
				};
			}
		}
		return intentMessages;
	}

	/**
	 * A pending intent is required to enable foreground NDEF dispatch
	 * 
	 * @param context
	 * @return
	 */
	public static PendingIntent getPendingIntent(Activity context) {
		return PendingIntent.getActivity(context, 0,
				new Intent(context, context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	}

}
