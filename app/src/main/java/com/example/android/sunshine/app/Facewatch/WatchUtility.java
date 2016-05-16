package com.example.android.sunshine.app.Facewatch;

import android.content.Context;
import android.content.Intent;

/**
 * Created by perez.juan.jose on 13/05/2016.
 */
public class WatchUtility {

    public static void updateWatch(Context context) {

        Intent intent1 = new Intent(context, WatchActualizationService.class);
        intent1.setAction("ACTION_FOO");
//        intent1.putExtra("EXTRA_MAXTEMP", Utility.formatTemperature(getContext(), high));
//        intent1.putExtra("EXTRA_MINTEMP", Utility.formatTemperature(getContext(), low));
//        intent1.putExtra("EXTRA_ICONO",  Utility.getArtResourceForWeatherCondition(weatherId));

        context.startService(intent1);
    }

}
