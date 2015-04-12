package com.watchmotion.weareye;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class MyWearListener extends WearableListenerService {
    private final static String commandPath = "/path/camera/start";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals(commandPath)) {
            Toast.makeText(this, "Start received!", Toast.LENGTH_SHORT).show();
            Intent startIntent = new Intent(this, MainWearActivity.class);
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
