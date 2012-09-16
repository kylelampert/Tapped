package com.enavigo.tapped.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.enavigo.tapped.R;
import com.enavigo.tapped.services.FacebookService;
import com.enavigo.tapped.services.FacebookService.OnRequestResultListener;

public class BeamFragment extends SimpleLayoutFragment {
	
	TextView mBeamStatus;
	TextView mBeamMessage;

	public BeamFragment(int layoutResourceId) {
		super(layoutResourceId);
		
		//TODO crash if you try to beam when on the intro screen. we need to kick off the async task somewhere else
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (FacebookService.userData != null) {
			updateUi();
		} else {
			FacebookService.requestUserData(new OnRequestResultListener() {

				@Override
				public void onRequestResult(String result) {
					updateUi();
				}
			},null);
		}
	}
	
	@Override
	protected View initializeView(View view){
		mBeamStatus = (TextView) view.findViewById(R.id.beam_status);
		mBeamMessage = (TextView) view.findViewById(R.id.beam_message);
		return view;
	}
	
	private void updateUi(){
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				String name = FacebookService.getUserDataType(FacebookService.USER_DATA_USERNAME);
				mBeamStatus.setText("Ready");
				mBeamMessage.setText(name);
			}
		});
	}

}
