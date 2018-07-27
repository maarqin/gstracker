package com.gstracker.cordova.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.orhanobut.hawk.Hawk;

import br.com.golsat.golfleetdriver.*;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Hawk.init(context).build();

        Boolean status = Hawk.get(MainActivity.IS_CONNECTION_OK);

        if( status != null && status ) {
            context.startService(new Intent(context, SensorActivityService.class));
        }
    }

}