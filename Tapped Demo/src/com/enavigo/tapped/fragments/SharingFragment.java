package com.enavigo.tapped.fragments;

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
				
				// TODO check-in

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
		});

		return view;
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
		// TODO enable share if I have both a person and place
	}

	@Override
	public void onPlaceChanged(String placeName, String facebookPlaceId) {
		this.tagReadPlaceName = placeName;
		this.tagReadPlaceId = facebookPlaceId;
		updateUi();
		// TODO enable share if I have both a person and place
	}
	
	private void resetFields(){
		beamRecievedUserId = null;
		beamReceivedUsername = null;
		tagReadPlaceId = null;
		tagReadPlaceName = null;
		updateUi();
	}

}
