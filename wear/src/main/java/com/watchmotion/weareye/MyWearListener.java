package com.watchmotion.weareye;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.List;

public class MyWearListener extends WearableListenerService {

    private final static String commandPath = "/path/camera/start";
    private final static String cameraPreviewPath = "/path/camera/preview";

    private boolean receiving = true;

    private Handler mHandler = new Handler();
    private Runnable vibrationTask = new Runnable() {
        public void run() {
            receiving = true;
        }
    };

    private LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if (receiving) {
            if (messageEvent.getPath().equals(commandPath)) {
                //Toast.makeText(this, "Start received!", Toast.LENGTH_SHORT).show();
                receiving = false;
                mHandler.postDelayed(vibrationTask, 60000);
                Intent startIntent = new Intent(this, WakeActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startIntent);
            }
        }
        if (messageEvent.getPath().equals(cameraPreviewPath)) {
            byte[] data = messageEvent.getData();
           // final Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Intent intent = new Intent("Bitmap");
            intent.putExtra("IMAGE",data);

            localBroadcastManager.sendBroadcast(intent);

           // notifyListeners(tempBitmap);
        }

    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        Log.e("WEAR_LISTENER", peer.getDisplayName()+" CONNECTED");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        Log.e("WEAR_LISTENER", peer.getDisplayName() +" disconnected");
    }

    private GoogleApiClient gClient;

    public MyWearListener() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        gClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        gClient.connect();


    }

}
