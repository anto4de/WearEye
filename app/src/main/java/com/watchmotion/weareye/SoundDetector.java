package com.watchmotion.weareye;

import android.os.Handler;
import android.util.Log;

/**
 * Created by Antonello on 11/04/2015.
 */
public class SoundDetector {

    private static final int POLL_INTERVAL = 500;

    private boolean mRunning = false;

    private Handler mHandler = new Handler();

    private SoundMeter mSensor;

    private CrySensorAdapter crySensorAdapter;

    private int mThreshold;

    public SoundDetector(CrySensorAdapter crySensorAdapter, int threshold) {
        this.crySensorAdapter = crySensorAdapter;
        this.mThreshold = threshold;
        mSensor = new SoundMeter();
    }

    // Create runnable thread to Monitor Voice
    private Runnable mPollTask = new Runnable() {
        public void run() {

            double amp = mSensor.getAmplitude();
            Log.i("SoundDetector", "runnable mPollTask amp = " + amp);

            if ((amp > mThreshold)) {
                crySensorAdapter.onSoundTriggered();
            }

            // Runnable(mPollTask) will again execute after POLL_INTERVAL
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);

        }
    };

    public void start() {
        mSensor.start();

        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }

    public void stop() {
        mHandler.removeCallbacks(mPollTask);
        mSensor.stop();
        mRunning = false;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public void setRunning(boolean mRunning) {
        this.mRunning = mRunning;
    }
}
