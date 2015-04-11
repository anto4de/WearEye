package com.watchmotion.weareye;

import android.util.Log;

import io.snapback.sdk.gesture.sequence.SequenceGestureHandler;
import io.snapback.sdk.gesture.sequence.SequenceSensorAdapter;

/**
 * Created by Antonello on 11/04/2015.
 */
public class CrySensorAdapter extends SequenceSensorAdapter {

    private SoundDetector soundDetector;

    public CrySensorAdapter(SequenceGestureHandler<?, ?> handler) {
        super(handler);
        this.soundDetector = new SoundDetector(this);
    }

    @Override
    public boolean sensorIsAvailable() {
        return soundDetector != null;
    }

    @Override
    public void start() {
        Log.i("CrySensorAdapter", "==== Start Called===");

        if (!soundDetector.isRunning()) {
            soundDetector.setRunning(true);
            soundDetector.start();
        }
    }

    @Override
    public void stop() {
        Log.i("CrySensorAdapter", "==== Stop Called===");
        soundDetector.stop();
    }

    @Override
    public boolean isHoldEventSupported() {
        return false;
    }

    public void onSoundTriggered() {
        Log.i("CrySensorAdapter", "==== onSoundTriggered Called===");
        sequenceStartDetected();
    }
}
