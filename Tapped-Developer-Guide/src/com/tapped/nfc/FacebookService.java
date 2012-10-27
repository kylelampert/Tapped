package com.tapped.nfc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

/**
 * Singleton class that provides access to Facebook SDK
 */
public enum FacebookService {
	INSTANCE; // Singleton

	public enum RequestType {
		POST, GET
	};

	// User Data Types for use in getUserData().get()
	public static final String USER_DATA_USERNAME = "username";
	public static final String USER_DATA_NAME = "name";
	public static final String USER_DATA_ID = "id";

	// Unique identifier used by FB
	public static final String FB_APP_ID = "YOUR_APP_ID_HERE";

	public static Facebook facebook = new Facebook(FB_APP_ID);

	// Cache the user's data after pulling it the first time
	public static JSONObject userData;
	private static Bitmap profileImage;

	// Interfaces
	public interface OnRequestResultListener {
		public abstract void onRequestResult(String result);
	};

	public interface OnRequestErrorListener {
		public abstract void onRequestError();
	};

	/**
	 * Run an asynchronous request against the Graph API for a given request
	 * string
	 * 
	 * @param request
	 * @param resultListener
	 * @param errorListener
	 */
	public static void requestAsync(final String graphPath, final Bundle parameters, final RequestType requestType,
			final OnRequestResultListener resultListener, final OnRequestErrorListener errorListener) {
		AsyncFacebookRunner runner = new AsyncFacebookRunner(facebook);
		RequestListener requestListener = new RequestListener() {

			@Override
			public void onComplete(String response, Object state) {
				resultListener.onRequestResult(response);
			}

			@Override
			public void onMalformedURLException(MalformedURLException e, Object state) {
				onError(e);
			}

			@Override
			public void onIOException(IOException e, Object state) {
				onError(e);
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e, Object state) {
				onError(e);
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
				onError(e);
			}

			public void onError(Exception e) {
				Log.e("NFC-Demo", "Error in FB async request or result", e);
				errorListener.onRequestError();
			}
		};

		if (parameters != null && parameters.size() > 0) {
			runner.request(graphPath, parameters, requestType.toString(), requestListener, null);
		} else {
			runner.request(graphPath, requestListener);
		}
	}

	/**
	 * Get the user's profile image. Cache it in memory for lazy loading
	 * 
	 * @param imageView
	 */
	public static synchronized void setProfileImage(final ImageView imageView) {
		if (profileImage != null) {
			imageView.setImageBitmap(profileImage);
		} else {

			AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

				@Override
				public Bitmap doInBackground(Void... params) {
					try {
						if (userData == null) {
							userData = new JSONObject(facebook.request("me", null,
									FacebookService.RequestType.GET.toString()));
						}
						URL profileUrl = new URL("http://graph.facebook.com/" + userData.getString(USER_DATA_USERNAME)
								+ "/picture?type=normal");
						profileImage = BitmapFactory.decodeStream(profileUrl.openConnection().getInputStream());
					} catch (Exception e) {
						e.printStackTrace();
					}
					return profileImage;
				}

				@Override
				protected void onPostExecute(Bitmap result) {
					if (result != null) {
						imageView.setImageBitmap(result);
					}
				}

			};
			task.execute();
		}
	}

	/**
	 * Get a data type from the current user's data
	 * 
	 * @param dataType
	 * @return
	 */
	public static String getUserDataType(String dataType) {
		try {
			if (userData != null && userData.getString(dataType) != null) {
				return userData.getString(dataType);
			}
		} catch (JSONException e) {
			Log.e("FacebookService", "Error getting property: " + dataType, e);
		}
		return null;
	}

	/**
	 * sample JSON for user data { "id":"0207150", "name":"John Doe",
	 * "first_name":"John", "last_name":"Doe",
	 * "link":"http:\/\/www.facebook.com\/johndoe", "username":"johndoe",
	 * "hometown":{ "id":"106003956105810", "name":"Boston, Massachusetts" },
	 * "location":{ "id":"106003956105810", "name":"Boston, Massachusetts" },
	 * "gender":"male", "timezone":-4, "locale":"en_US", "verified":true,
	 * "updated_time":"2012-08-01T21:33:02+0000" }
	 */

}
