package com.watchmotion.weareye;

import io.snapback.sdk.gesture.sequence.pulse.PulseGestureEvent;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureHandler;
import io.snapback.sdk.gesture.sequence.pulse.PulseGestureListener;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Handler;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static long DEFAULT_VIBRATION_DURATION = 1000;
    private final static long ANIM_INTERVAL = 600;

    private PulseGestureHandler pulseCry;
    private Vibrator v;
    private Handler mHandler = new Handler();
    private SoundMeter mSoundMeter = new SoundMeter();
    private int mThreshold = 7;

    private Camera mCamera;
    private Preview mPreview;
    private SurfaceView cameraPreview;

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

    private GoogleApiClient gClient;
    private boolean googleConnected = false;
    private Node mWearableNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Setting screen brightness.
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 0.0f;
        getWindow().setAttributes(lp);

        cameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
        safeCameraOpen(Camera.CameraInfo.CAMERA_FACING_BACK);
        mPreview = new Preview(this, cameraPreview, mCamera);

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

        final ImageView camButton = (ImageView) findViewById(R.id.cam_button);
        camButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cameraPreview.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.Pulse).duration(ANIM_INTERVAL).playOn(camButton);
                        YoYo.with(Techniques.FadeIn).duration(ANIM_INTERVAL).playOn(cameraPreview);
                        break;
                    case MotionEvent.ACTION_UP:
                        YoYo.with(Techniques.FadeOut).duration(ANIM_INTERVAL).playOn(cameraPreview);
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
        gClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Wearable.API).build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //connect google client
        gClient.connect();
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

    @Override
    protected void onStop() {
        super.onStop();
        releaseCameraAndPreview();
        //disconnect googleClient
        gClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mPreview.setGoogleClient(gClient);
        mPreview.setGoogleConnected(true);
        googleConnected = true;
        findWearableNode();

        Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPreview.setGoogleClient(null);
        mPreview.setGoogleConnected(false);

        Toast.makeText(getApplicationContext(), "disconnected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private class CryListener implements PulseGestureListener {

        @Override
        public void onEvent(PulseGestureEvent event) {

            if (event.getType() == PulseGestureEvent.PULSE_START_EVENT_TYPE) {
                v.vibrate(DEFAULT_VIBRATION_DURATION);
                if (googleConnected && gClient != null && mWearableNode != null) {
                    Wearable.MessageApi.sendMessage(
                            gClient, mWearableNode.getId(), "/path/camera/start", "Cry Event".getBytes());
                }
            }
        }
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);

            qOpened = true;
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }


    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mPreview != null) {
            mPreview.destroyDrawingCache();
        }
    }

    void findWearableNode() {
        PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(gClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                if (result.getNodes().size() > 0) {
                    mWearableNode = result.getNodes().get(0);
                    //if(D) Log.d(TAG, "Found wearable: name=" + mWearableNode.getDisplayName() + ", id=" + mWearableNode.getId());
                } else {
                    mWearableNode = null;
                }
            }
        });
    }

}
