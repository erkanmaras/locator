package com.exm.locator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Date;

/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 * <p>
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 * <p>
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification associated with that service is removed.
 */
public class LocationService extends Service {


    public static String NOTIFICATION_TITLE = "";
    public static String NOTIFICATION_TEXT = "";
    public static int UPDATE_INTERVAL_IN_SECONDS = 10;
    private static final String PACKAGE_NAME = "exm_locator";
    private static final String LOG_TAG = LocationService.class.getSimpleName();

    private static final String NOTIFICATION_CHANNEL_ID = PACKAGE_NAME + ".channel";

    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";

    private final IBinder mBinder = new LocalBinder();

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 1618033;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderClient}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    /**
     * The current location.
     */
    private Location mLocation;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(LOG_TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //**CharSequence name = getString(R.string.app_name);
            CharSequence name = LocationService.class.getSimpleName();
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.setSound(null, null);
            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        startForeground(NOTIFICATION_ID, getNotification());
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(LOG_TAG, "onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(LOG_TAG, "onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "onDestroy");
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates() throws SecurityException {
        Log.i(LOG_TAG, "Requesting location updates");
        LocationServiceState.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), LocationService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            LocationServiceState.setRequestingLocationUpdates(this, false);
            Log.e(LOG_TAG, "Lost location permission. Could not request updates. " + unlikely);
            throw unlikely;
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() throws SecurityException {
        Log.i(LOG_TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            LocationServiceState.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            LocationServiceState.setRequestingLocationUpdates(this, true);
            Log.e(LOG_TAG, "Lost location permission. Could not remove updates. " + unlikely);
            throw unlikely;
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {

        Intent intent = new Intent(this, LocationService.class);

        CharSequence text = getLocationText(mLocation);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentText(text)
                .setContentTitle(NOTIFICATION_TITLE)
                .setOngoing(true)
                .setSound(null)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
        }

        Class<?> cls = getMainActivityClass();
        if (cls != null) {
            PendingIntent launchIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, cls), PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(launchIntent);
        }


        return builder.build();
    }

    private Class<?> getMainActivityClass() {
        try {
            Intent intent = this.getApplicationContext().getPackageManager().getLaunchIntentForPackage(this.getPackageName());
            if (intent != null) {
                ComponentName componentName = intent.getComponent();
                if (componentName != null) {
                    String className = componentName.getClassName();
                    return Class.forName(className);
                }
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "main activity class cannot retrieve!." + e);
        }
        return null;
    }

    public void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.w(LOG_TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(LOG_TAG, "Lost location permission." + unlikely);
            throw unlikely;
        }
    }

    public void getLastLocation(OnCompleteListener<Location> listener) {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(listener);
        } catch (SecurityException unlikely) {
            Log.e(LOG_TAG, "Lost location permission." + unlikely);
            throw unlikely;
        }
    }

    static String getLocationText(Location location) {
        return NOTIFICATION_TEXT + " " + DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date().getTime()).toString();
    }

    private void onNewLocation(Location location) {
        Log.i(LOG_TAG, "New location: " + location);

        mLocation = location;

        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        mNotificationManager.notify(NOTIFICATION_ID, getNotification());
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_SECONDS * 1000);
        /**
         * The fastest rate for active location updates. Updates will never be more frequent
         * than this value.
         */
        mLocationRequest.setFastestInterval(UPDATE_INTERVAL_IN_SECONDS * 500);
        mLocationRequest.setSmallestDisplacement(20);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }


}