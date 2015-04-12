package com.watchmotion.weareye;

import io.snapback.sdk.gesture.sequence.pulse.PulseGestureEvent;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureHandler;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureListener;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class MainActivity extends ActionBarActivity {

    private final static long DEFAULT_VIBRATION_DURATION = 1000;
    private final static long ANIM_INTERVAL = 600;

    private PulseGestureHandler pulseCry;
    private Vibrator v;
    private Handler mHandler = new Handler();
    private SoundMeter mSoundMeter = new SoundMeter();
    private int mThreshold = 7;

    private ImageView micButton;
    private TextView calibrateTextView;

    // Create runnable thread to calibrate the threshold
    private Runnable calibrateThreshold = new Runnable() {
        public void run() {
            YoYo.with(Techniques.Pulse).duration(ANIM_INTERVAL).playOn(micButton);
            double amp = mSoundMeter.getAmplitude();
            if ((amp > mThreshold)) {
                mThreshold = (int) amp;
            }
            mHandler.postDelayed(calibrateThreshold, ANIM_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Set screen brightness to minimum for this activity.
        //WindowManager.LayoutParams lp = getWindow().getAttributes();
        //lp.screenBrightness = 0.0f;
        //getWindow().setAttributes(lp);

        micButton = (ImageView) findViewById(R.id.calibrate_button);
        calibrateTextView = (TextView) findViewById(R.id.calibrating_textview);

        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        calibrateTextView.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeIn).duration(ANIM_INTERVAL).playOn(calibrateTextView);
                        if (pulseCry.isStarted()) {
                            pulseCry.stop();
                        }
                        mThreshold = 3;
                        mSoundMeter.start();
                        mHandler.postDelayed(calibrateThreshold, ANIM_INTERVAL);
                        break;
                    case MotionEvent.ACTION_UP:
                        YoYo.with(Techniques.FadeOut).duration(ANIM_INTERVAL).playOn(calibrateTextView);
                        mHandler.removeCallbacks(calibrateThreshold);
                        mSoundMeter.stop();
                        CrySensorAdapter cry = new CrySensorAdapter(pulseCry, mThreshold);
                        pulseCry.setSensorAdapters(cry);
                        if (!pulseCry.isStarted()) {
                            pulseCry.start();
                        }
                        break;
                }
                return true;
            }
        });

        pulseCry = new PulseGestureHandler(this);
        pulseCry.setUsageTrackingDetails("m3rcur14l@hotmail.com", "WearEye");
        CrySensorAdapter cry = new CrySensorAdapter(pulseCry, mThreshold);
        pulseCry.setSensorAdapters(cry);
        pulseCry.register(new CryListener());

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!pulseCry.isStarted()) {
            pulseCry.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (pulseCry.isStarted()) {
            pulseCry.stop();
        }
    }

    private class CryListener implements PulseGestureListener {

        @Override
        public void onEvent(PulseGestureEvent event) {

            if (event.getType() == PulseGestureEvent.PULSE_START_EVENT_TYPE) {
                v.vibrate(DEFAULT_VIBRATION_DURATION);
            }
        }
    }

}
