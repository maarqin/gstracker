package com.gstracker.cordova.plugin;


import android.content.Context;
import android.location.Location;

import java.text.DateFormat;
import java.util.Date;

public class Utils {

    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" : "Sua última localização foi: [" + location.getLatitude() + ", " + location.getLongitude() + "]";
    }

    public static String getLocationTitle(Context context) {
        return "Your last location: " +  DateFormat.getDateTimeInstance().format(new Date());
    }
}
