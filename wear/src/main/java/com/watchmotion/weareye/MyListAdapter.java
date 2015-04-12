package com.watchmotion.weareye;

import android.app.Activity;
import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.jar.Attributes;

/**
 * Created by Chai on 12/04/2015.
 */
public class MyListAdapter extends WearableListView.Adapter {
    private String[] mDataset;
    private int[] res;
    private final Context mContext;
    private final LayoutInflater mInflater;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyListAdapter(Context context, String[] dataset, int[] res) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDataset = dataset;
        this.res = res;
    }

    // Provide a reference to the type of views you're using
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView textView;
        private ImageView imageView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            // find the text view within the custom item's layout
            textView = (TextView) itemView.findViewById(R.id.name);
            imageView = (ImageView)itemView.findViewById(R.id.circle);
        }
    }

    // Create new views for list items
    // (invoked by the WearableListView's layout manager)
    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // Inflate our custom layout for list items
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item, null));
    }

    // Replace the contents of a list item
    // Instead of creating new views, the list tries to recycle existing ones
    // (invoked by the WearableListView's layout manager)
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {
        // retrieve the text view
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        TextView view = itemHolder.textView;
        ImageView img = itemHolder.imageView;
        // replace text contents
        view.setText(mDataset[position]);
        img.setImageResource(res[position]);
        //img.setImageResource(R.drawable.heart);
        // replace list item's metadata
        holder.itemView.setTag(position);
    }

    // Return the size of your dataset
    // (invoked by the WearableListView's layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}