package com.watchmotion.weareye;

import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class MyWearListener extends WearableListenerService {
    private final static String commandPath = "/path/camera/start";

    private boolean receiving = true;

    private Handler mHandler = new Handler();
    private Runnable vibrationTask = new Runnable() {
        public void run() {
            receiving = true;
        }
    };

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (receiving)
            if (messageEvent.getPath().equals(commandPath)) {
                //Toast.makeText(this, "Start received!", Toast.LENGTH_SHORT).show();
                receiving = false;
                mHandler.postDelayed(vibrationTask, 60000);
                Intent startIntent = new Intent(this, WakeActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startIntent);
            }
        super.onMessageReceived(messageEvent);
    }

    /*
    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mApiClient.blockingConnect(5000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mApiClient, asset).await().getInputStream();
        //gClient.disconnect();

        if (assetInputStream == null) {
            Log.w("WeaActivityMain", "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

    private final static String cameraPreviewPAth = "/path/camera/preview";

    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals(cameraPreviewPAth)) {
            byte[] data = messageEvent.getData();
            final Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraPreview.setImageBitmap(tempBitmap);
                }
            });
        }

    }*/

}
