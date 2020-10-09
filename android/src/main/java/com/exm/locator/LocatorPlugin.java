package com.exm.locator;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * LocatorPlugin
 */
public class LocatorPlugin implements FlutterPlugin, MethodCallHandler {
    private MethodChannel channel;
    private LocationReceiver locationReceiver;
    private LocationService locationService;
    private Boolean bound = false;
    private FlutterPluginBinding flutterPluginBinding;
    private Context context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "exm_locator");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
        locationReceiver = new LocationReceiver(channel);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

        switch (call.method) {
            case "start_location_service":
                try {

                    LocationService.NOTIFICATION_TITLE = call.argument("notificationTitle");
                    LocationService.NOTIFICATION_TEXT = call.argument("notificationText");
                    LocationService.UPDATE_INTERVAL_IN_SECONDS = call.argument("updateIntervalInSecond");
                    startLocationService();
                    result.success(true);
                } catch (SecurityException ex) {
                    result.error(ex.getClass().getSimpleName(), ex.getMessage(), Log.getStackTraceString(ex));
                }
                break;
            case "stop_location_service":
                try {
                    stopLocationService();
                    result.success(true);
                } catch (SecurityException ex) {
                    result.error(ex.getClass().getSimpleName(), ex.getMessage(), Log.getStackTraceString(ex));
                }
                break;
            case "last_location":
                    lastLocation(result);
                break;
            default:
                result.notImplemented();
        }
    }

    private void startLocationService() {
        stopLocationService();
        LocalBroadcastManager.getInstance(context).registerReceiver(locationReceiver,
                new IntentFilter(LocationService.ACTION_BROADCAST));
        if (!bound) {
            context.bindService(new Intent(context, LocationService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    private void stopLocationService() {
        if (locationService != null) {
            locationService.removeLocationUpdates();
            LocalBroadcastManager.getInstance(context).unregisterReceiver(locationReceiver);
            if (bound) {
                context.unbindService(serviceConnection);
                bound = false;
            }
        }
    }

    private void lastLocation(final Result result) {
        if (locationService != null) {
            locationService.getLastLocation(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location mLocation = task.getResult();
                        result.success(locationToMap(mLocation));
                    } else {
                        Exception ex = task.getException();
                        result.error(ex.getClass().getSimpleName(), ex.getMessage(), Log.getStackTraceString(ex));
                    }
                }
            });
        }
    }

    public static HashMap<String, Object> locationToMap(Location location) {
        HashMap<String, Object> locationMap = new HashMap<>();
        locationMap.put("latitude", location.getLatitude());
        locationMap.put("longitude", location.getLongitude());
        locationMap.put("altitude", location.getAltitude());
        locationMap.put("accuracy", (double) location.getAccuracy());
        locationMap.put("bearing", (double) location.getBearing());
        locationMap.put("speed", (double) location.getSpeed());
        locationMap.put("time", (double) location.getTime());

        return locationMap;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        stopLocationService();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
            locationService.requestLocationUpdates();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    private static final class LocationReceiver extends BroadcastReceiver {

        LocationReceiver(MethodChannel channel) {
            this.channel = channel;
        }

        MethodChannel channel;

        public void onReceive(Context context, Intent intent) {
            Location location = (Location) intent.getParcelableExtra(LocationService.EXTRA_LOCATION);
            if (location != null) {
                channel.invokeMethod("location", locationToMap(location), (Result) null);
            }
        }
    }
}
