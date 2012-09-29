package com.tapped.nfc;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kinvey.KCSClient;
import com.kinvey.KinveySettings;
import com.kinvey.MappedAppdata;
import com.kinvey.util.ListCallback;

public class TagHistoryActivity extends Activity {  
    private static final String KINVEY_KEY = "kid_VPBToYGel";
	private static final String KINVEY_SECRET_KEY = "ed9e2e51121f442694ef74709513ed80";
	
	private KCSClient kinveyClient;
	private ListView tagsList;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_history);
        
        tagsList = (ListView) findViewById(R.id.tags_listview);
        
		// Initialize Kinvey
        KinveySettings settings = new KinveySettings(KINVEY_KEY, KINVEY_SECRET_KEY);
        kinveyClient = KCSClient.getInstance(this.getApplicationContext(), settings);
        
        refreshList();
    }

    
    private void refreshList(){
    	final ProgressDialog pd = ProgressDialog.show(TagHistoryActivity.this, 
    			"", "Loading...", true);
    	
        // Fetch tag history
        kinveyClient.mappeddata("tags").fetch(TagReadEntity.class, new ListCallback<TagReadEntity>() {

			@Override
			public void onSuccess(final List<TagReadEntity> results) {
				
				// Update the list
				tagsList.setAdapter(new ArrayAdapter<TagReadEntity>(TagHistoryActivity.this,
                        android.R.layout.simple_list_item_1, results){
					@Override
					public View getView(int pos, View convertview, ViewGroup parent){
						TextView text = new TextView(TagHistoryActivity.this);
						text.setText(results.get(pos).getTagMessage());
						return text;
					}
				});
				pd.dismiss();
			}
			
			@Override
            public void onFailure(Throwable error) {
                Log.e("Tapped", "Received error response", error);
                pd.dismiss();
            }
		});
    }


}
