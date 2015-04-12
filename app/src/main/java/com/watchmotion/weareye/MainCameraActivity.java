package com.watchmotion.weareye;

import android.app.Activity;
import android.hardware.Camera;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;


public class MainCameraActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static String cameraPreviewPAth = "/path/camera/preview";
    ImageView preview;
    SurfaceView mSurface;
    Camera mCamera;
    Preview mPreview;
    TextView tv;

    private GoogleApiClient gClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_camera);
        mSurface = (SurfaceView) findViewById(R.id.msurface);
        preview = (ImageView) findViewById(R.id.preview);
        tv = (TextView) findViewById(R.id.bitmapInfo);

        safeCameraOpen(Camera.CameraInfo.CAMERA_FACING_BACK);

        mPreview = new Preview(this, mSurface, mCamera, preview, tv);
        gClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Wearable.API).build();


    }



    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);

            qOpened = (mCamera != null);
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

    @Override
    protected void onStart() {
        super.onStart();
        //connect google client
        gClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCameraAndPreview();
        //disconnect googleClient
        gClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mPreview.setGoogleClient(gClient);
        mPreview.setGoogleConnected(true);

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
}
