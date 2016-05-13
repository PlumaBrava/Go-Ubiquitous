package com.example.android.sunshine.app.Facewatch;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.sunshine.app.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.util.Random;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */

public class WatchActualizationService extends IntentService
        implements  DataApi.DataListener,
                    GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "WatchActualization";

    GoogleApiClient mGoogleApiClient;
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "ACTION_FOO";
    private static final String ACTION_BAZ = "com.example.android.sunshine.app.facewatch.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.android.sunshine.app.facewatch.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.android.sunshine.app.facewatch.extra.PARAM2";

    private String mMaxTemp;
    private String mMinTemp;
    private int mIcono;

    public WatchActualizationService() {

        super("WatchActualizationService");
        Log.d(TAG, "constructor : WatchActualizationService()");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Log.d(TAG, "startActionFoo");
        Intent intent = new Intent(context, WatchActualizationService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Log.d(TAG, "startActionBaz");
        Intent intent = new Intent(context, WatchActualizationService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

     mGoogleApiClient = new GoogleApiClient.Builder(WatchActualizationService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                mMaxTemp=intent.getStringExtra("EXTRA_MAXTEMP");
                mMinTemp=intent.getStringExtra("EXTRA_MINTEMP");
                mIcono=intent.getIntExtra("EXTRA_ICONO",R.drawable.ic_light_rain);
//                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
//                final String param2 = intent.getStringExtra(EXTRA_PARAM2);

//                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        Log.d(TAG, "handleActionFoo");
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        Log.d(TAG, "handleActionBaz");
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override  // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected: " + connectionHint);
        }
        sendDataToWatch(mMaxTemp,mMinTemp,mIcono);
        Log.d(TAG, "onConnected: " + connectionHint);
        //mMaxTemp=147;
        Wearable.DataApi.addListener(mGoogleApiClient, this);
//          updateConfigDataItemAndUiOnStartup(); levanta la configuracion default
    }

    @Override  // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {


        Log.d(TAG, "onConnectionSuspended: " + cause);
    }

    @Override  // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionFailed: " + result);
        }
        Log.d(TAG, "onConnectionFailed: " + result);
    }

    @Override // DataApi.DataListener
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {


                continue;
            }

            DataItem dataItem = dataEvent.getDataItem();

            //sacar el dato
            DataItem item = dataEvent.getDataItem();
//                    Log.i(LOG, "getItemUri :" + item.getUri().getPath().toString());
//                    Log.i(LOG, "getItemUri.compareTO :" + item.getUri().getPath().compareTo("/envionumero"));
            if (item.getUri().getPath().compareTo("/weatherdata") == 0) {
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
//
//                mMaxTemp=dataMap.getInt("maxtemp");
//                mMinTemp=dataMap.getInt("mintemp");
//                Asset weatherImageAsset = dataMap.getAsset("weatherImage");
//                mbitmap= BitmapFactory.decodeResource(getResources(), R.drawable.ic_clear);



                // Loads image on background thread.
//                new LoadBitmapAsyncTask().execute(weatherImageAsset);






//                    mbitmap = loadBitmapFromAsset(profileAsset);
//                        mTextView.setText("int: " + dataMap.getInt("numero"));
//                        Log.i(LOG, "setText_numero :" + dataMap.getInt("numero"));
//                    mTextView.append("   ---   long:" + dataMap.getLong(KEYB));
            }
//                    Log.i(LOG, "NO Compara");


//              Lee la configuracion del reloj que se envia desde el telefono
//                if (!dataItem.getUri().getPath().equals(
//                        DigitalWatchFaceUtil.PATH_WITH_FEATURE)) {
//                    continue;
//                }

            DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
            DataMap config = dataMapItem.getDataMap();
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Config DataItem updated:" + config);
            }
//                updateUiForConfigDataMap(config); estudiar.....
        }
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }



    public void sendDataToWatch(String tmax, String tmin, int icono){

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/weatherdata");

        putDataMapRequest.getDataMap().putString("maxtemp",tmax);
        putDataMapRequest.getDataMap().putString("mintemp",tmin);
//        putDataMapRequest.getDataMap().putLong("tiemStamp",timeStamp);



        Bitmap bitmap= BitmapFactory.decodeResource(getResources(), icono);
        Asset asset = createAssetFromBitmap(bitmap);
        putDataMapRequest.getDataMap().putAsset("weatherImage", asset);



        PutDataRequest recuest=putDataMapRequest.asPutDataRequest();
        recuest.setUrgent();

        Wearable.DataApi.putDataItem(mGoogleApiClient,recuest)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                        if(dataItemResult.getStatus().isSuccess()){
                            Log.i(TAG,"Envio Exitoso");


                        }else{
                            //Fallo al enviar los datos
                        }
                    }
                });
    }

}
