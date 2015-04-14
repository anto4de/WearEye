package com.watchmotion.weareye;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;


public class MainListActivity extends Activity implements WearableListView.ClickListener,GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    GoogleApiClient gClient = null;

    WearableListView mListView;
    private float mDefaultCircleRadius;
    private float mSelectedCircleRadius;
    MyListAdapter myAdapter;
    String[] elements = {"musica", "camera", "setting"};
    int[] resources = {R.drawable.ic_action_play, R.drawable.ic_action_picture, R.drawable.ic_action_settings};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        // Get the list component from the layout of the activity
        WearableListView listView =
                (WearableListView) findViewById(R.id.wearable_list);

        // Assign an adapter to the list
        listView.setAdapter(new MyListAdapter(this, elements, resources));

        // Set a click listener
        listView.setClickListener(this);
        gClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Wearable.API).build();
        gClient.connect();
    }

    // WearableListView click listener
    @Override
    public void onClick(WearableListView.ViewHolder v) {
        Integer tag = (Integer) v.itemView.getTag();
        //click on camera
        if (tag == 1) {
            startActivity(new Intent(MainListActivity.this, StartActivity.class));
        }
    }

    @Override
    public void onTopEmptyRegionClick() {
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_SHORT).show();

            }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(), "connected lost", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "connected failed:"+connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
    }
}
