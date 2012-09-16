package com.enavigo.tapped.fragments;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.enavigo.tapped.R;
import com.enavigo.tapped.activities.HomeActivity;
import com.enavigo.tapped.activities.HomeActivity.OnTagReadWriteListener;
import com.enavigo.tapped.utils.Constants;

public class TagFragment extends Fragment implements OnTagReadWriteListener {
	private static final String LOG_TAG = "Tapped-TagsFragment";

	private TextView lastTagRead;
	private TextView lastTagWritten;
	private Button tagWriteEnableButton;
	private ProgressBar tagProgressBar;

	// Map the resource ID of the view to the actual name of the place
	private static Map<Integer, String> placeViewIdToName = new HashMap<Integer, String>(3) {
		{
			put(R.id.nyc, Constants.PLACE_NYC);
			put(R.id.sf, Constants.PLACE_SAN_FRAN);
			put(R.id.boston, Constants.PLACE_BOSTON);
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// The parent activity contains the infrastructure for tag read/write
		// Set the default tag message to be Boston
		// TODO don't hardcode this
		((HomeActivity) getActivity()).setTagWriteMessage(Constants.PLACE_BOSTON);
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_tag_writer, container, false);
		return initializeView(view);
	}

	/**
	 * Create references to view items, set listeners for UI elements
	 * 
	 * @param view
	 * @return
	 */
	private View initializeView(View view) {

		// Radio-button options to select a different tag message
		final RadioGroup placeOptions = (RadioGroup) view.findViewById(R.id.places_options);

		// Progress indicator to be shown when in tag-write mode
		tagProgressBar = (ProgressBar) view.findViewById(R.id.tag_writer_progress);

		tagWriteEnableButton = (Button) view.findViewById(R.id.enable_tag_write_button);
		tagWriteEnableButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Tag writing mode must be enabled with this button. Tag
				// writing will be disabled after a tag has been written
				// or when this fragment is navigated away from
				((HomeActivity) getActivity()).setTagWriteMessage(placeViewIdToName.get(placeOptions
						.getCheckedRadioButtonId()));
				((HomeActivity) getActivity()).setTagWriteReady(true);
				updateUi();
			}
		});

		// Display a tag's previous and new value when it's written
		lastTagWritten = (TextView) view.findViewById(R.id.last_tag_written);
		lastTagRead = (TextView) view.findViewById(R.id.last_tag_read);

		return view;
	}

	/**
	 * Refresh the state of the UI based on write mode
	 */
	public void updateUi() {
		// Ensure the fragment has been created and the UI has been drawn
		if (getActivity() != null && tagProgressBar != null && tagWriteEnableButton != null) {
			boolean isWriteReady = ((HomeActivity) getActivity()).getTagWriteReady();
			tagProgressBar.setVisibility(isWriteReady ? View.VISIBLE : View.GONE);
			tagWriteEnableButton.setEnabled(!isWriteReady);
		}
	}

	/**
	 * This listener will get set by the parent activity (HomeActivity) when a
	 * tag is read or written.
	 * 
	 * After a tag has been written, disable write-mode and update the UI with
	 * the result
	 * 
	 * @param lastTagRead
	 */
	@Override
	public void onTagWritten(String newValue, String previousValue) {
		((HomeActivity) getActivity()).setTagWriteReady(false);
		updateUi();
		lastTagWritten.setText(newValue);
		lastTagRead.setText(previousValue);
	}

	@Override
	public void onTagRead(String message) {
	}

}
