package com.example.android.sunshine.app.Facewatch;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WatchInitService extends WearableListenerService {

    String TAG= WatchInitService.class.getName();
    public WatchInitService() {
        Log.d(TAG, " Constructor()");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        Log.d(TAG, " onDataChanged()");
        WatchUtility.updateWatch(getApplicationContext());
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, " : onMessageReceived");
        super.onMessageReceived(messageEvent);
    }



}
