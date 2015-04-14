package com.watchmotion.weareye;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.service.carrier.CarrierMessagingService;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_180;
import static android.view.Surface.ROTATION_270;
import static android.view.Surface.ROTATION_90;


/**
 * Created by Chai on 10/04/2015.
 */
public class Preview extends ViewGroup implements SurfaceHolder.Callback {


    private final static String cameraPreviewPAth = "/path/camera/preview";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Size mPreviewSize;
    // List of supported preview sizes
    private List<Camera.Size> mSupportedPreviewSizes;

    // Flash modes supported by this camera
    private List<String> mSupportedFlashModes;

    // View holding this camera.
    private Context mContext;
    int currentCamera;
    public int mCameraOrientation;

    public boolean googleConnected = false;
    public GoogleApiClient gClient = null;
    private Node mWearableNode = null;

    public void setGoogleConnected(boolean con) {
        googleConnected = con;
        findWearableNode();
    }

    Preview(Context context, SurfaceView surfaceView, Camera camera) {
        super(context);
        mContext = context;
        mSurfaceView = surfaceView;
        //mSurfaceView.setLayerType(View.LAYER_TYPE_HARDWARE,null);
        setCamera(camera);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setKeepScreenOn(true);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_HARDWARE);

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

    private void setCamera(Camera camera) {
        // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
        mCamera = camera;
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();

        // Set the camera to Auto Flash mode.
        if (mSupportedFlashModes != null && mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);

            mCamera.setParameters(parameters);
        }

        currentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
        requestLayout();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mCamera.startPreview();
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {

        if (mHolder.getSurface() == null) return;
        try {

            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // Important: Call startPreview() to start updating the preview surface.
        // Preview must be started before you can take a picture.
        try {

            Camera.Parameters p = mCamera.getParameters();
            List<String> focusModes = p.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            mCamera.setParameters(p);
            // Preview size must exist.
            if (mPreviewSize != null) {
                Camera.Size previewSize = mPreviewSize;
                p.setPreviewSize(previewSize.width, previewSize.height);
            }
            mCamera.setParameters(p);
            setCameraDisplayOrientation();
            //requestLayout();


            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                public void onPreviewFrame(byte[] data, Camera arg1) {
                 /*
                    if (mWearableNode != null && readyToProcessImage && mPreviewRunning && displayFrameLag<6 && displayTimeLag<2000
                            && System.currentTimeMillis() - lastMessageTime < 4000) {
                        readyToProcessImage = false;
                        */
                    Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                    int[] rgb = decodeYUV420SP(data, previewSize.width, previewSize.height);
                    Bitmap bmp = Bitmap.createBitmap(rgb, previewSize.width, previewSize.height, Bitmap.Config.RGB_565);

                    //get the image rotate properly
                    Matrix matrix = new Matrix();
                    matrix.postRotate(mCameraOrientation);
                    int smallWidth, smallHeight;
                    int dimension = 150;

                    if (previewSize.width > previewSize.height) {
                        smallWidth = dimension;
                        smallHeight = dimension * previewSize.height / previewSize.width;
                    } else {
                        smallHeight = dimension;
                        smallWidth = dimension * previewSize.width / previewSize.height;
                    }

                    Bitmap bmpSmall = Bitmap.createScaledBitmap(bmp, smallWidth, smallHeight, false);
                    //Bitmap bmpSmallRotated = Bitmap.createBitmap(bmpSmall, 0, 0, smallWidth, smallHeight, matrix, false);
                    // String tempString = "previus: " + bmpSmall.getByteCount();

                    if (googleConnected && gClient != null && mWearableNode != null) {

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bmpSmall.compress(Bitmap.CompressFormat.WEBP, 30, baos);

                        Wearable.MessageApi.sendMessage(
                                gClient, mWearableNode.getId(), cameraPreviewPAth, baos.toByteArray()).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if(sendMessageResult.getStatus().isSuccess())
                                Toast.makeText(mContext,"data sent",Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(mContext,"data not sent",Toast.LENGTH_SHORT).show();
                            }
                        });
                        /*
                        Asset asset = createAssetFromBitmap(bmpSmall);
                        PutDataMapRequest dataMap = PutDataMapRequest.create(cameraPreviewPAth);
                        dataMap.getDataMap().putAsset("ASSET_BITMAP",asset);
                        PutDataRequest request = dataMap.asPutDataRequest();
                        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(gClient,request);
                        DataApi.DataItemResult result = pendingResult.await();*/
                    } else {

                    }


                    // Bitmap tempBitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);
/*
                    tempString = tempString + "\n later: " + tempBitmap.getByteCount();
                    bitmapInfo.setText(tempString);*/
                       /* displayFrameLag++;
                        sendToWearable(String.format("show %d", System.currentTimeMillis()), baos.toByteArray(), new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult result) {
                                if(displayFrameLag>0) displayFrameLag--;
                            }
                        });*/
                    bmp.recycle();
                    //bmpSmall.recycle();
                    // bmpSmallRotated.recycle();
                    // readyToProcessImage = true;
                }
                //}
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        mCamera.setDisplayOrientation(mCameraOrientation);
    }

    public void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        mCamera.getCameraInfo(currentCamera, info);
        int rotation = ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();
        Log.e("Preview", "screen rotation: " + rotation);
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int resultB = 0;
        if (currentCamera == Camera.CameraInfo.CAMERA_FACING_BACK) {
            resultB = (info.orientation - degrees + 360) % 360;

        }

        mCameraOrientation = resultB;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {

            // mCamera.stopPreview();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            final int width = right - left;
            final int height = bottom - top;

            int previewWidth = width;
            int previewHeight = height;

            if (mPreviewSize != null) {
                Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

                switch (display.getRotation()) {
                    case ROTATION_0:
                        previewWidth = mPreviewSize.height;
                        previewHeight = mPreviewSize.width;
                        mCamera.setDisplayOrientation(90);
                        break;
                    case ROTATION_90:
                        previewWidth = mPreviewSize.width;
                        previewHeight = mPreviewSize.height;
                        break;
                    case ROTATION_180:
                        previewWidth = mPreviewSize.height;
                        previewHeight = mPreviewSize.width;
                        break;
                    case ROTATION_270:
                        previewWidth = mPreviewSize.width;
                        previewHeight = mPreviewSize.height;
                        mCamera.setDisplayOrientation(180);
                        break;
                }
            }

            final int scaledChildHeight = previewHeight * width / previewWidth;
            //mCameraView.layout(0, height - scaledChildHeight, width, height);
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
        // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
        Camera.Size optimalSize = null;

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;

        // Try to find a size match which suits the whole screen minus the menu on the left.
        for (Camera.Size size : sizes) {

            if (size.height != width) continue;
            double ratio = (double) size.width / size.height;
            if (ratio <= targetRatio + ASPECT_TOLERANCE && ratio >= targetRatio - ASPECT_TOLERANCE) {
                optimalSize = size;
            }
        }

        // If we cannot find the one that matches the aspect ratio, ignore the requirement.
        if (optimalSize == null) {
            // TODO : Backup in case we don't get a size.
        }

        return optimalSize;
    }

    public int[] decodeYUV420SP(byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;
        int rgb[] = new int[width * height];
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
                        | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, 30, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());

    }

    public void setGoogleClient(GoogleApiClient client) {
        this.gClient = client;
    }

    ;

    /* to be used on wearable
    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }*/
    /*
    private List<String> getNodes() {
        ArrayList<String> results = new ArrayList<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(gClient);
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());

        }
        return results;
    }*/
    private List<String> getNodes() {
        final ArrayList<String> mResults = new ArrayList<String>();
        /*NodeApi.GetConnectedNodesResult mNodes =*/
        Wearable.NodeApi.getConnectedNodes(gClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                for (Node mNode : getConnectedNodesResult.getNodes()) {
                    mResults.add(mNode.getId());
                }
            }
        });
        /*for(Node mNode : mNodes.getNodes()){
            mResults.add(mNode.getId());
        }*/

        return mResults;
    }
}
