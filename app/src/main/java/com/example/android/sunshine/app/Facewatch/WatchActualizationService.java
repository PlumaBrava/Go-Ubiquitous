package com.example.android.sunshine.app.Facewatch;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;
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
                    GoogleApiClient.OnConnectionFailedListener,
                    Loader.OnLoadCompleteListener<Cursor>
                    {

    private static final String TAG = "WatchActualization";

    GoogleApiClient mGoogleApiClient;

    private static final String ACTION_FOO = "ACTION_FOO";



    private String mMaxTemp;
    private String mMinTemp;
    private int mIcono;
    private Boolean isLoaderReady=false;


    private CursorLoader mCursorLoader;

    private static final int FORECAST_LOADER = 1;
    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public WatchActualizationService() {



        super("WatchActualizationService");
        Log.d(TAG, "constructor : WatchActualizationService()");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");


        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {

            }
        }
    }




    @Override  // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected: " + connectionHint);
        sendDataToWatch();


        Wearable.DataApi.addListener(mGoogleApiClient, this);

    }

    @Override  // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {


        Log.d(TAG, "onConnectionSuspended: " + cause);
    }

    @Override  // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {

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

            if (item.getUri().getPath().compareTo("/weatherdata") == 0) {
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();


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



    public void sendDataToWatch(){

        if(isLoaderReady && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "sendDataToWatch(): true ");

            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/weatherdata");

            putDataMapRequest.getDataMap().putString("maxtemp", mMaxTemp);
            putDataMapRequest.getDataMap().putString("mintemp", mMinTemp);
        putDataMapRequest.getDataMap().putLong("tiemStamp",new Random().nextLong());

            Log.d(TAG, "sendDataToWatch(): micono" +mIcono);
            Log.d(TAG, "Utlity.getArtResourceForWeatherCondition(mIcono)"+ Utility.getArtResourceForWeatherCondition(mIcono));
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), Utility.getArtResourceForWeatherCondition(mIcono) );
            Asset asset = createAssetFromBitmap(bitmap);
            putDataMapRequest.getDataMap().putAsset("weatherImage", asset);


            PutDataRequest recuest = putDataMapRequest.asPutDataRequest();
            recuest.setUrgent();

            Wearable.DataApi.putDataItem(mGoogleApiClient, recuest)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                            if (dataItemResult.getStatus().isSuccess()) {
                                Log.i(TAG, "Envio Exitoso");


                            } else {
                                //Fallo al enviar los datos
                            }
                        }
                    });

        }else {
            Log.d(TAG, "sendDataToWatch(): false ");
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();


        mGoogleApiClient = new GoogleApiClient.Builder(WatchActualizationService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();


        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        String locationSetting = Utility.getPreferredLocation(this);
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

//        mCursorLoader = new CursorLoader(context, contentUri, projection, selection, selectionArgs, orderBy);
        mCursorLoader = new CursorLoader(this,
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);

        mCursorLoader.registerListener(FORECAST_LOADER, this);
        mCursorLoader.startLoading();

        Log.d(TAG, "On create, carga el loader:");
    }
                        @Override
                        public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
                            Log.d(TAG, "onLoadComplete:");


                            if(data != null && data.moveToFirst()){
                                // Read weather condition ID from cursor
                                mIcono = data.getInt(COL_WEATHER_CONDITION_ID);

                                // Read high temperature from cursor and update view
                                boolean isMetric = Utility.isMetric(this);

                                double high = data.getDouble(COL_WEATHER_MAX_TEMP);
                                mMaxTemp = Utility.formatTemperature(this, high);


                                // Read low temperature from cursor and update view
                                double low = data.getDouble(COL_WEATHER_MIN_TEMP);
                                mMinTemp = Utility.formatTemperature(this, low);


                                Log.d(TAG, "onLoadComplete: Max tem"+mMaxTemp+" min temp"+mMinTemp);
                                isLoaderReady=true;
                            }

                            sendDataToWatch();

                        }


//                        @Override
//                        public void onDestroy() {
//                            super.onDestroy();
//
//                            // Stop the cursor loader
//                            if (mCursorLoader != null) {
//                                mCursorLoader.unregisterListener(this);
//                                mCursorLoader.cancelLoad();
//                                mCursorLoader.stopLoading();
//                            }
//                        }

}
