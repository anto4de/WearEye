package com.watchmotion.weareye;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;


public class WakeActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {

    private Handler mHandler = new Handler();

    private Runnable vibrationTask = new Runnable() {
        public void run() {
            vibrate();
            mHandler.postDelayed(vibrationTask, 3000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Button awakeButton = (Button) findViewById(R.id.awake_button);
        awakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(vibrationTask);
                finish();
            }
        });
        mHandler.post(vibrationTask);
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(vibrationTask);
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(2000);
    }


}
