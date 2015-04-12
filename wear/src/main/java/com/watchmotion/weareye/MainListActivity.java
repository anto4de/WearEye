package com.watchmotion.weareye;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;


public class MainListActivity extends Activity  implements WearableListView.ClickListener{

    WearableListView mListView;
    private float mDefaultCircleRadius;
    private float mSelectedCircleRadius;
    MyListAdapter myAdapter;
    String[] elements = { "musica", "camera", "setting"};
    int[] resources = { R.drawable.ic_action_play, R.drawable.ic_action_picture, R.drawable.ic_action_settings};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        // Get the list component from the layout of the activity
        WearableListView listView =
                (WearableListView) findViewById(R.id.wearable_list);

        // Assign an adapter to the list
        listView.setAdapter(new MyListAdapter(this, elements,resources));

        // Set a click listener
        listView.setClickListener(this);
    }

    // WearableListView click listener
    @Override
    public void onClick(WearableListView.ViewHolder v) {
        Integer tag = (Integer) v.itemView.getTag();
        // use this data to complete some action ...
    }

    @Override
    public void onTopEmptyRegionClick() {
    }
}
