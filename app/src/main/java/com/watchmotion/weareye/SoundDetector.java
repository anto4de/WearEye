package com.watchmotion.weareye;

import android.os.Handler;
import android.util.Log;

/**
 * Created by Antonello on 11/04/2015.
 */
public class SoundDetector {

    /* constants */
    private static final int POLL_INTERVAL = 300;

    /**
     * running state *
     */
    private boolean mRunning = false;

    /**
     * config state *
     */
    private int mThreshold = 8;

    private Handler mHandler = new Handler();

    /* data source */
    private SoundMeter mSensor;

    private CrySensorAdapter crySensorAdapter;

    public SoundDetector(CrySensorAdapter crySensorAdapter) {
        this.crySensorAdapter = crySensorAdapter;
        mSensor = new SoundMeter();
    }

    /**
     * Define runnable thread again and again detect noise
     */
    private Runnable mSleepTask = new Runnable() {
        public void run() {
            Log.i("Noise", "runnable mSleepTask");

            start();
        }
    };

    // Create runnable thread to Monitor Voice
    private Runnable mPollTask = new Runnable() {
        public void run() {

            double amp = mSensor.getAmplitude();
            Log.i("Noise", "runnable mPollTask amp = " +  amp);

            if ((amp > mThreshold)) {
                crySensorAdapter.onSoundTriggered();
            }

            // Runnable(mPollTask) will again execute after POLL_INTERVAL
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);

        }
    };

    public void start() {
        //Log.i("Noise", "==== start ===");

        mSensor.start();

        //Noise monitoring start
        // Runnable(mPollTask) will execute after POLL_INTERVAL
        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }

    public void stop() {
        Log.i("Noise", "==== Stop Noise Monitoring===");

        mHandler.removeCallbacks(mSleepTask);
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
