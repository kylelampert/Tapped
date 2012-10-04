package com.enavigo.tapped.fragments;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.enavigo.tapped.R;
import com.enavigo.tapped.activities.HomeActivity.OnBeamRecievedListener;
import com.enavigo.tapped.activities.HomeActivity.OnPlaceChangedListener;
import com.enavigo.tapped.services.FacebookService;
import com.enavigo.tapped.services.FacebookService.OnRequestErrorListener;
import com.enavigo.tapped.services.FacebookService.OnRequestResultListener;
import com.enavigo.tapped.utils.Constants;

public class SharingFragment extends Fragment implements OnBeamRecievedListener, OnPlaceChangedListener {

	// Layout assets for showing my FB profile
	private ProgressBar profileProgress;
	private TextView profileName;

	private TextView beamReceivedUsernameView;
	private TextView beamReceivedFacebookPlaceView;

	private String beamRecievedUserId;
	private String beamReceivedUsername;
	private String tagReadPlaceId;
	private String tagReadPlaceName;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Get FB info for current user if it's not ready
		if (FacebookService.userData != null) {
			updateUi();
		} else {
			FacebookService.requestUserData(new OnRequestResultListener() {

				@Override
				public void onRequestResult(String result) {
					if (FacebookService.userData == null) {
						showProfileError();
					}
					updateUi();
				}
			}, new OnRequestErrorListener() {

				@Override
				public void onRequestError() {
					showProfileError();
				}
			});
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_sharing, container, false);
		return initializeView(view);
	}

	private View initializeView(View view) {
		profileProgress = (ProgressBar) view.findViewById(R.id.fb_profile_progressbar);
		profileName = (TextView) view.findViewById(R.id.fb_profile_fullname);

		beamReceivedFacebookPlaceView = (TextView) view.findViewById(R.id.beam_place_id);
		beamReceivedUsernameView = (TextView) view.findViewById(R.id.beam_username);

		ImageView profileImage = (ImageView) view.findViewById(R.id.fb_profile_image);
		FacebookService.setProfileImage(profileImage);

		Button resetFields = (Button) view.findViewById(R.id.reset_fields_button);
		resetFields.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				resetFields();
			}
		});

		Button shareToFacebook = (Button) view.findViewById(R.id.share_to_facebook_button);
		shareToFacebook.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Check for valid state before posting
				if (TextUtils.isEmpty(tagReadPlaceId) || TextUtils.isEmpty(tagReadPlaceName)) {
					Toast.makeText(getActivity(), "Can't share; invalid place.", Toast.LENGTH_SHORT).show();
					return;
				}
				if (TextUtils.isEmpty(beamRecievedUserId) || TextUtils.isEmpty(beamReceivedUsername)) {
					Toast.makeText(getActivity(), "Can't share; invalid person.", Toast.LENGTH_SHORT).show();
					return;
				}

				doFbPost();
				doFbCheckin();
			}
		});

		return view;
	}

	private void doFbPost() {
		Bundle params = new Bundle();
		params.putString("message", "I'm hacking at Tapped (www.tappednfc.com) with " + beamReceivedUsername + ".");
		params.putString("place", tagReadPlaceId);

		FacebookService.requestAsync("me/feed", params, FacebookService.RequestType.POST,
				new OnRequestResultListener() {

					@Override
					public void onRequestResult(String result) {
						if (result.contains("id")) {
							showFacebookSuccess();
							resetFields();
							Log.i("Tapped", "Sucess posting! Result is: " + result);
						} else {
							showFacebookError(result);
						}
					}
				}, new OnRequestErrorListener() {

					@Override
					public void onRequestError() {
						showFacebookError(null);
					}
				});
	}

	private void doFbCheckin() {
		Bundle params = new Bundle();
		params.putString("access_token", FacebookService.facebook.getAccessToken());
		params.putString("place", Constants.TAPPED_FB_PLACE); 
		params.putString("Message", "I'm hacking at Tapped!");

		JSONObject coordinates = new JSONObject();
		try {
			double[] coords = getGPS();
			coordinates.put("latitude", coords[0]);
			coordinates.put("longitude", coords[1]);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		params.putString("coordinates", coordinates.toString());

		FacebookService.requestAsync("me/checkins", params, FacebookService.RequestType.POST,
				new OnRequestResultListener() {

					@Override
					public void onRequestResult(String result) {
						Log.i("Tapped", "Checked in with result: " + result);
					}
				}, new OnRequestErrorListener() {

					@Override
					public void onRequestError() {
						Log.i("Tapped", "Error checking in");
					}
				});
	}

	private double[] getGPS() {
		LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);

		/*
		 * Loop over the array backwards, and if you get an accurate location,
		 * then break out the loop
		 */
		Location l = null;

		for (int i = providers.size() - 1; i >= 0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null)
				break;
		}

		double[] gps = new double[2];
		if (l != null) {
			gps[0] = l.getLatitude();
			gps[1] = l.getLongitude();
		} else {
			gps[0] = 40.739326;
			gps[1] = -73.989357;
		}
		return gps;
	}

	private void showFacebookSuccess() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getActivity(), "Posted to Facebook!", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void showFacebookError(final String result) {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Log.e("Tapped", "Error posting to FB: " + result);
				Toast.makeText(getActivity(), "Error posting to Facebook. Please try again.", Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	/**
	 * Refresh the user's facebook data and the current state of tags read /
	 * person received over beam
	 */
	private void updateUi() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				String name = FacebookService.getUserDataType(FacebookService.USER_DATA_NAME);
				profileName.setText(name != null ? name : "Data not found");
				profileProgress.setVisibility(View.GONE);

				if (beamReceivedUsername != null) {
					beamReceivedUsernameView.setText(beamReceivedUsername);
					beamReceivedUsernameView.setTextColor(getActivity().getResources().getColor(android.R.color.black));
				} else {
					beamReceivedUsernameView.setText("Who are you hacking with? Tap to another NFC-Enabled phone!");
					beamReceivedUsernameView.setTextColor(getActivity().getResources().getColor(
							android.R.color.darker_gray));
				}
				if (tagReadPlaceName != null) {
					beamReceivedFacebookPlaceView.setText(tagReadPlaceName);
					beamReceivedFacebookPlaceView.setTextColor(getActivity().getResources().getColor(
							android.R.color.black));
				} else {
					beamReceivedFacebookPlaceView.setText("Where are you? Tap your phone to a Tapped NFC Tag!");
					beamReceivedFacebookPlaceView.setTextColor(getActivity().getResources().getColor(
							android.R.color.darker_gray));
				}
			}
		});
	}

	private void showProfileError() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// Hide view and show error in profile
				profileName.setText("Error getting your Facebook profile. Please log out and log back in.");
				profileProgress.setVisibility(View.GONE);
			}
		});
	}

	@Override
	public void onBeamRecieved(String username, String userId) {
		this.beamReceivedUsername = username;
		this.beamRecievedUserId = userId;
		updateUi();
	}

	@Override
	public void onPlaceChanged(String placeName, String facebookPlaceId) {
		this.tagReadPlaceName = placeName;
		this.tagReadPlaceId = facebookPlaceId;
		updateUi();
	}

	private void resetFields() {
		beamRecievedUserId = null;
		beamReceivedUsername = null;
		tagReadPlaceId = null;
		tagReadPlaceName = null;
		updateUi();
	}

}
