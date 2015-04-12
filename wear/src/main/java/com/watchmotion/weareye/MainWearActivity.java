package com.watchmotion.weareye;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;


public class MainWearActivity extends Activity implements  GoogleApiClient.ConnectionCallbacks,NodeApi.NodeListener,MessageApi.MessageListener, DataApi.DataListener, GoogleApiClient.OnConnectionFailedListener{


    ImageView cameraPreview;
    TextView testo;



    GoogleApiClient gClient;
    public boolean googleConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wear);
        cameraPreview = (ImageView)findViewById(R.id.cameraPreview);
        testo = (TextView)findViewById(R.id.testo);

        gClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Wearable.API).build();
        gClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onStop() {
        super.onStop();
        gClient.disconnect();
        Wearable.MessageApi.removeListener(gClient,this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        googleConnected = true;
        Wearable.DataApi.addListener(gClient,this);
        Wearable.MessageApi.addListener(gClient,this);
        testo.setText("gP connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleConnected = false;
        testo.setText("gP disconnect");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        testo.setText("receviving data");
        for(DataEvent event: dataEvents){
            if(event.getType() == DataEvent.TYPE_CHANGED){
                testo.setText("data changed");
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                DataMap dataMap = dataMapItem.getDataMap();
                Asset asset = dataMap.getAsset("ASSET_BITMAP");
                final Bitmap mAssetBitmap = loadBitmapFromAsset(asset);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(mAssetBitmap != null){
                            //mDataItemSyncMessageTextView.setVisibility(View.GONE);
                            cameraPreview.setImageBitmap(mAssetBitmap);
                            cameraPreview.setVisibility(View.VISIBLE);

                        }else{

                            cameraPreview.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
    }
    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                gClient.blockingConnect(5000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                gClient, asset).await().getInputStream();
        //gClient.disconnect();

        if (assetInputStream == null) {
            Log.w("WeaActivityMain", "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        testo.setText("con. failed +\n"+connectionResult.getErrorCode());
    }

    @Override
    public void onPeerConnected(Node node) {

    }

    @Override
    public void onPeerDisconnected(Node node) {

    }
    private final static String cameraPreviewPAth = "/path/camera/preview";
    @Override
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

    }
}
