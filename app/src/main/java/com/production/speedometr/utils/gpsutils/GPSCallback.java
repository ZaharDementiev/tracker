package com.production.speedometr.utils.gpsutils;

import android.location.Location;

/**
 * Created by mobiweb on 1/8/16.
 */

public interface GPSCallback {
    public abstract void onGPSUpdate(Location location);
}

