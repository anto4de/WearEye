package com.watchmotion.weareye;

import io.snapback.sdk.gesture.sequence.pulse.PulseGestureEvent;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureHandler;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureListener;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.WindowManager;

public class MainActivity extends ActionBarActivity {

    private final static long DEFAULT_VIBRATION_DURATION = 1000;

    private PulseGestureHandler pulseCry;
    private Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pulseCry = new PulseGestureHandler(this);
        pulseCry.setUsageTrackingDetails("m3rcur14l@hotmail.com", "WearEye");
        CrySensorAdapter cry = new CrySensorAdapter(pulseCry);
        pulseCry.setSensorAdapters(cry);
        pulseCry.register(new CryListener());

        v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!pulseCry.isStarted()) {
            pulseCry.start();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(pulseCry.isStarted()) {
            pulseCry.stop();
        }
    }

    private class CryListener implements PulseGestureListener {

        @Override
        public void onEvent(PulseGestureEvent event) {

            if(event.getType() == PulseGestureEvent.PULSE_START_EVENT_TYPE) {
                v.vibrate(DEFAULT_VIBRATION_DURATION);
            }
        }

    }
}
