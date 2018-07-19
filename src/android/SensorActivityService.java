package com.gstracker.cordova.plugin;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import com.gstracker.cordova.plugin.Position;
import com.gstracker.cordova.plugin.BasePositioningApi;

import br.com.golsat.golfleetdriver.*;
import retrofit2.Response;


public class SensorActivityService extends Service {

    public int counter = 0;
    private final IBinder mBinder = new LocalBinder();

    private static final String PACKAGE_NAME = "com.google.android.gms.location.sample.sensoractivityforegroundservice";
    private final String TAG = SensorActivityService.class.getSimpleName();
    private static final String CHANNEL_ID = "channel_01";
    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    public static final String EXTRA_ACTIVITY = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME + ".started_from_notification";

    private static final int NOTIFICATION_ID = 87654321;

    private boolean mChangingConfiguration = false;
    private NotificationManager mNotificationManager;
    private Handler mServiceHandler;

    private LocationUpdatesService.MyReceiver myReceiver;

    private LocationUpdatesService mService = null;

    private boolean mBound = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();

            mService.requestLocationUpdates();

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        myReceiver = new LocationUpdatesService.MyReceiver();

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        mServiceHandler.removeCallbacksAndMessages(null);
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }


        Intent broadcastIntent = new Intent("br.com.golsat.pocservice.RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    @Override
    public IBinder onBind(Intent intent) {

        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {

        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("TAG", "Last client unbound from service");

        if (!mChangingConfiguration) {
            Log.i("TAG", "Starting foreground service");

            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true;
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        if( timerTask != null ) {
            timerTask.cancel();
        }

        timer = new Timer();
        initializeTimerTask();

        timer.schedule(timerTask, 1000, 1000);
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("In timer", "S ("+ (counter++) + ")");
            }
        };
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * @param intent Intent
     */
    public void requestActivityUpdates(Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        ActivityRecognition.getClient(this).removeActivityUpdates(pendingIntent);
        ActivityRecognition.getClient(this).requestActivityUpdates(1000, pendingIntent);
    }

    /**
     * @param intent Intent
     */
    public PendingIntent removeActivityUpdates(Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        ActivityRecognition.getClient(this).removeActivityUpdates(pendingIntent);

        return pendingIntent;
    }

    /**
     * @param probableActivities
     */
    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        for( DetectedActivity activity : probableActivities ) {
            System.out.println("activity = " + activity);

            if( activity.getType() == DetectedActivity.WALKING && activity.getConfidence() >= 75 ) {

                onNewActivity(activity);

            } else if( (activity.getType() == DetectedActivity.STILL && activity.getConfidence() >= 75) /*||
                    (activity.getType() == DetectedActivity.WALKING && activity.getConfidence() >= 75)*/) {


                if(  mService != null && mService.mBound ) {
                    mService.removeLocationUpdates();

                    unbindService(mServiceConnection);
                }

                Hawk.init(this).build();

                ArrayList<Position> positions = Hawk.get(LocationUpdatesService.KEY_POSITIONS);

                String email = Hawk.get(MainActivity.USER_EMAIL);
                String deviceId = Hawk.get(MainActivity.USER_DEVICE_ID);

                // Save registered data
                if( positions != null ) {
                    new BasePositioningApi.Create(getApplicationContext(), email, deviceId, positions) {
                        @Override
                        public void onSuccess(Response<Void> response) {
                            Toast.makeText(getApplication(), "Salvo com sucesso!", Toast.LENGTH_SHORT).show();

                            Hawk.delete(LocationUpdatesService.KEY_POSITIONS);
                        }
                    };
                }
            }

        }
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, SensorActivityService.class);

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Monitor de atividades")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.icon)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();
    }

    /**
     * @param activity
     */
    private void onNewActivity(DetectedActivity activity) {
        Log.i(TAG, "New activity: " + activity);

        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_ACTIVITY, activity);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        if( mService == null || !mService.mBound ) {
            bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }

        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    /**
     * @param context
     * @return
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    public class LocalBinder extends Binder {
        public SensorActivityService getService() {
            return SensorActivityService.this;
        }
    }

    public static class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            DetectedActivity activity = intent.getParcelableExtra(SensorActivityService.EXTRA_ACTIVITY);
            if (activity != null) {
                Toast.makeText(context, activity.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}