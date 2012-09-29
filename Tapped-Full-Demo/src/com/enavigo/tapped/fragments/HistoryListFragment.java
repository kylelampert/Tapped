package com.enavigo.tapped.fragments;

import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.enavigo.tapped.R;
import com.enavigo.tapped.TappedApplication;
import com.enavigo.tapped.activities.HomeActivity;
import com.enavigo.tapped.entities.BeamReceivedEntity;
import com.enavigo.tapped.entities.TagReadEntity;
import com.enavigo.tapped.utils.Constants;
import com.kinvey.KCSClient;
import com.kinvey.MappedAppdata;
import com.kinvey.util.ListCallback;

public class HistoryListFragment extends Fragment {
	
	private ListView beamsList;
	private ListView tagsList;
	private ProgressDialog progressDialog;
	
	private KCSClient sharedClient;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		sharedClient = ((HomeActivity) getActivity()).getTappedApplication().
        		getKinveyService();
		
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_history, container, false);
		return initializeView(view);
	}
	
	private View initializeView(View view){
		beamsList = (ListView) view.findViewById(R.id.beams_history_list);
		tagsList = (ListView) view.findViewById(R.id.tags_history_list);
		
		Button refresh = (Button) view.findViewById(R.id.refresh_history_button);
		refresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				refreshLists(true);
			}
		});
		
		refreshLists(false);
		return view;
	}
	
	/**
	 * Fetch beam and tag history and update UI
	 */
	public void refreshLists(final boolean showProgressDialog){
		
		if(showProgressDialog){
			progressDialog = ProgressDialog.show(getActivity(), "", "Loading...", true);
		}
        
        // Fetch tag history
        MappedAppdata data = sharedClient.mappeddata(Constants.KINVEY_COLLECTION_TAGS);
        
        data.addFilterCriteria("userName", "==", sharedClient.getCurrentUser().getId());
        data.fetchByFilterCriteria(TagReadEntity.class, new ListCallback<TagReadEntity>() {

			@Override
			public void onSuccess(final List<TagReadEntity> results) {
				tagsList.setAdapter(new ArrayAdapter<TagReadEntity>(getActivity(),
                        android.R.layout.simple_list_item_1, results){
					@Override
					public View getView(int pos, View convertview, ViewGroup parent){
						TextView text = new TextView(getActivity());
						text.setText(results.get(pos).getTagMessage());
						return text;
					}
				});
				if (showProgressDialog)
					progressDialog.dismiss();
			}
			
			@Override
            public void onFailure(Throwable error) {
				showError(progressDialog, error);
            }
		});
        
        // Fetch beam history
        data = sharedClient.mappeddata(Constants.KINVEY_COLLECTION_BEAMS);
        
        data.addFilterCriteria("userName", "==", sharedClient.getCurrentUser().getId());
        data.fetchByFilterCriteria(BeamReceivedEntity.class, new ListCallback<BeamReceivedEntity>() {

			@Override
			public void onSuccess(final List<BeamReceivedEntity> results) {
				beamsList.setAdapter(new ArrayAdapter<BeamReceivedEntity>(getActivity(),
                        android.R.layout.simple_list_item_1, results){
					@Override
					public View getView(int pos, View convertview, ViewGroup parent){
						TextView text = new TextView(getActivity());
						text.setText(results.get(pos).getBeamMessage());
						return text;
					}
				});
				if (showProgressDialog)
					progressDialog.dismiss();
			}
			
			@Override
            public void onFailure(Throwable error) {
				showError(progressDialog, error);
            }
		});

	}
	
	private void showError(ProgressDialog dialog, Throwable error){
		Toast.makeText(getActivity(), "Error fetching data", Toast.LENGTH_SHORT).show();
        Log.e("Tapped", "Received error response", error);
        if (dialog!=null && dialog.isShowing())
        	dialog.dismiss();
	}

}
