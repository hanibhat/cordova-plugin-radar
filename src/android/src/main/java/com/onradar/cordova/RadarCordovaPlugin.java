package com.onradar.cordova;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.radar.sdk.Radar;
import io.radar.sdk.RadarReceiver;
import io.radar.sdk.Radar.RadarCallback;
import io.radar.sdk.model.RadarEvent;
import io.radar.sdk.model.RadarUser;

public class RadarCordovaPlugin extends CordovaPlugin {

    private static CallbackContext eventsCallbackContext;
    private static CallbackContext errorCallbackContext;

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("initialize"))
            init(args, callbackContext);
        else if (action.equals("setUserId"))
            setUserId(args, callbackContext);
        else if (action.equals("setDescription"))
            setDescription(args, callbackContext);
        else if (action.equals("setPlacesProvider"))
            setPlacesProvider(args, callbackContext);
        else if (action.equals("startTracking"))
            startTracking(args, callbackContext);
        else if (action.equals("stopTracking"))
            stopTracking(args, callbackContext);
        else if (action.equals("acceptEvent"))
            startTracking(args, callbackContext);
        else if (action.equals("rejectEvent"))
            stopTracking(args, callbackContext);
        else if (action.equals("trackOnce"))
            trackOnce(args, callbackContext);
        else if (action.equals("updateLocation"))
            updateLocation(args, callbackContext);
        else if (action.equals("onEvents"))
            onEvents(args, callbackContext);
        else if (action.equals("onError"))
            onError(args, callbackContext);
        else if (action.equals("offEvents"))
            offEvents(args, callbackContext);
        else if (action.equals("offError"))
            offError(args, callbackContext);
        else
            return false;

        return true;
    }

    public void init(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final String key = args.getString(0);

        Radar.initialize(key);

        final Activity activity = this.cordova.getActivity();

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

//         TODO: Call the corresponding methods on ActivityCompat and ContextCompat instead
//          https://developer.android.com/training/permissions/requesting
      /*  if (!Radar.checkSelfPermissions()) {
            Radar.requestPermissions(activity);
        }*/

        notify("Title", "text");
    }

    private void notify(String title, String text) {
        Context context = this.cordova.getActivity().getApplicationContext();
        Intent intent = new Intent(context, RadarCordovaPlugin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager =
                (NotificationManager)context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        String channelName = "Kalimny";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel
                    channel = new NotificationChannel(channelName, channelName,  NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(context, channelName)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pending)
                .setSmallIcon(io.radar.sdk.R.drawable.notification)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .build();

        notificationManager.notify("Tag", 1111, notification);
    }

    public void setUserId(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final String userId = args.getString(0);

        Radar.setUserId(userId);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void setDescription(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final String description = args.getString(0);
        Radar.setDescription(description);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void setPlacesProvider(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final String provider = args.getString(0);

        Radar.RadarPlacesProvider p;
        if ("facebook".equals(provider)) {
            p = Radar.RadarPlacesProvider.FACEBOOK;
        }
        else {
            p = Radar.RadarPlacesProvider.NONE;
        }
        Radar.setPlacesProvider(p);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void startTracking(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Radar.startTracking();

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void stopTracking(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Radar.stopTracking();

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void acceptEvent(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final String eventId = args.getString(0);
        final String placeId = args.getString(1);

        Radar.acceptEvent(eventId, placeId);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    public void rejectEvent(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final String eventId = args.getString(0);

        Radar.rejectEvent(eventId);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }


    public void trackOnce(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Radar.trackOnce(new RadarCallback() {
            @Override
            public void onComplete(Radar.RadarStatus status, Location location, RadarEvent[] events, RadarUser user) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("status", RadarCordovaUtils.stringForStatus(status));
                    if (location != null)
                        obj.put("location", RadarCordovaUtils.jsonObjectForLocation(location));
                    if (events != null)
                        obj.put("events", RadarCordovaUtils.jsonArrayForEvents(events));
                    if (user != null)
                        obj.put("user", RadarCordovaUtils.jsonObjectForUser(user));

                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
                } catch (JSONException e) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
                }
            }
        });
    }

    public void updateLocation(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final JSONObject locationObj = args.getJSONObject(0);

        double latitude = locationObj.getDouble("latitude");
        double longitude = locationObj.getDouble("longitude");
        float accuracy = (float)locationObj.getDouble("accuracy");

        Location location = new Location("RadarCordovaPlugin");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(accuracy);

        Radar.updateLocation(location, new RadarCallback() {
            @Override
            public void onComplete(Radar.RadarStatus status, Location location, RadarEvent[] events, RadarUser user) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("status", RadarCordovaUtils.stringForStatus(status));
                    if (location != null)
                        obj.put("location", RadarCordovaUtils.jsonObjectForLocation(location));
                    if (events != null)
                        obj.put("events", RadarCordovaUtils.jsonArrayForEvents(events));
                    if (user != null)
                        obj.put("user", RadarCordovaUtils.jsonObjectForUser(user));

                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
                } catch (JSONException e) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
                }
            }
        });
    }

    public void onEvents(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        RadarCordovaPlugin.eventsCallbackContext = callbackContext;
    }

    public void onError(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        RadarCordovaPlugin.errorCallbackContext = callbackContext;
    }

    public void offEvents(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        RadarCordovaPlugin.eventsCallbackContext = null;
    }

    public void offError(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        RadarCordovaPlugin.errorCallbackContext = null;
    }

    public static class RadarCordovaReceiver extends RadarReceiver {

        @Override
        public void onEventsReceived(Context context, RadarEvent[] events, RadarUser user) {
            if (RadarCordovaPlugin.eventsCallbackContext == null)
                return;

            try {
                JSONObject obj = new JSONObject();
                obj.put("events", RadarCordovaUtils.jsonArrayForEvents(events));
                obj.put("user", RadarCordovaUtils.jsonObjectForUser(user));

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
                pluginResult.setKeepCallback(true);
                RadarCordovaPlugin.eventsCallbackContext.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                RadarCordovaPlugin.eventsCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        }

        @Override
        public void onError(Context context, Radar.RadarStatus status) {
            if (RadarCordovaPlugin.errorCallbackContext == null)
                return;

            try {
                JSONObject obj = new JSONObject();
                obj.put("status", RadarCordovaUtils.stringForStatus(status));

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
                pluginResult.setKeepCallback(true);
                RadarCordovaPlugin.errorCallbackContext.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                RadarCordovaPlugin.errorCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            }
        }

    }
}
