package com.watchmotion.weareye;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ImageView;

public class StartActivity extends Activity implements BitmapListener {

    private ImageView cameraView;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        cameraView = (ImageView) findViewById(R.id.cameraView);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("Bitmap")) {
                    byte[] data = intent.getExtras().getByteArray("IMAGE");
                    Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    setBipmap(tempBitmap);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("Bitmap"));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    public void setBipmap(final Bitmap b) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cameraView.setImageBitmap(b);
            }
        });
    }

    @Override
    public void callback(Bitmap b) {
        setBipmap(b);
    }
}
