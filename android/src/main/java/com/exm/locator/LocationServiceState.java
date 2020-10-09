package com.exm.locator;

import android.content.Context;
import android.content.SharedPreferences;

class LocationServiceState {

    static String SharedPreferencesName;
    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        SharedPreferences  sharedPreferences =
                context.getSharedPreferences(SharedPreferencesName, Context.MODE_PRIVATE);
    return    sharedPreferences
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        SharedPreferences  sharedPreferences =
                context.getSharedPreferences(SharedPreferencesName, Context.MODE_PRIVATE);
        sharedPreferences
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

//    /**
//     * Returns the {@code location} object as a human readable string.
//     * @param location  The {@link Location}.
//     */
//    static String getLocationText(Location location) {
//        return location == null ? "Unknown location" :
//                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
//    }


}