package com.watchmotion.weareye;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class MyWearEyeService extends WearableListenerService {
    GoogleApiClient mClient;
    public boolean connected = false;
    private final String TAG = this.getClass().getSimpleName();
    private final static String CANTO = "path/canto";

    private LocalBroadcastManager lManager;

    public MyWearEyeService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mClient.connect();
        lManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
            if (messageEvent.getPath().equals(CANTO)) {

                Intent startIntent = new Intent(this, BuonanotteActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startIntent);

        }
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        connected = true;
        Log.d(TAG, "peer connected");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        connected = false;
        Log.d(TAG, "peer NOT connected");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClient.disconnect();
    }
}
