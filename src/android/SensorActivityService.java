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
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import br.com.golsat.golfleetdriver.*;
import retrofit2.Response;
import rocks.alce.idlibrary.YelloAPI;
import rocks.alce.idlibrary.YelloMonitorService;
import rocks.alce.idlibrary.listeners.YelloApiDriverListener;
import rocks.alce.idlibrary.listeners.YelloApiVehicleListener;
import rocks.alce.idlibrary.models.YelloDriver;
import rocks.alce.idlibrary.models.YelloDriverData;
import rocks.alce.idlibrary.models.YelloVehicle;
import rocks.alce.idlibrary.models.YelloVehicleData;
import rocks.alce.idlibrary.receivers.YelloMonitorReceiver;

public class SensorActivityService extends Service implements ServiceConnection {

    // begin yello framework

    static final String API_KEY = "21d3d4a2-6b5e-4c71-85f3-71b1822f121d";
    static final String CLIENT_UID = "599c2cef-a546-4d93-9e92-14d14a29985b";

    private YelloAPI yelloAPI;

    private String mDriverUUID;
    private String mVehicleUUID;

    // end

    public int counter = 0;

    private static final String PACKAGE_NAME = "com.google.android.gms.location.sample.sensoractivityforegroundservice";
    private final String TAG = SensorActivityService.class.getSimpleName();
    private static final String CHANNEL_ID = "channel_01";
    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    public static final String EXTRA_ACTIVITY = PACKAGE_NAME + ".location";

    private static final int NOTIFICATION_ID = 87654321;

    private NotificationManager mNotificationManager;
    private Handler mServiceHandler;

    private LocationUpdatesService mService = null;

    public PendingIntent pendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // myReceiver = new LocationUpdatesService.MyReceiver();

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        // begin yello framework

        Hawk.init(this).build();

        String email = Hawk.get(MainActivity.USER_EMAIL);
        int userId = Hawk.get(MainActivity.USER_ID);

        yelloAPI = new YelloAPI(this, API_KEY, CLIENT_UID);

        YelloDriver driver = new YelloDriver(email, userId + "", new Date(), YelloDriver.MALE,
                YelloDriver.DIVORCED);

        yelloAPI.setDriver(driver, new YelloApiDriverListener() {

            @Override
            public void setDriverFinished(boolean success, YelloDriver driver, String errorMessage) {
                Log.d("YELLO", "setDriverFinished: " + driver.uuid);

                mDriverUUID = driver.uuid;
            }

            @Override
            public void getDriverFinished(boolean success, YelloDriverData data, String errorMessage) {}
        });

        YelloVehicle vehicle = new YelloVehicle("AAA-0000", "Carro próprio", YelloVehicle.CAR, "", "", "2017", "");
        yelloAPI.setVehicle(vehicle, new YelloApiVehicleListener() {

            @Override
            public void setVehicleFinished(boolean success, YelloVehicle vehicle, String
                    errorMessage) {
                Log.d("YELLO", "setVehicleFinished: " + vehicle.uuid);

                mVehicleUUID = vehicle.uuid;
            }

            @Override
            public void getVehiclesFinished(boolean success, ArrayList<YelloVehicle> list,
                                            String errorMessage) {}
            @Override
            public void getVehicleDataFinished(boolean success, YelloVehicleData data,
                                               String errorMessage) {}
        });

        // end

    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        startTimer();

        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
        } else {
            final int SPLASH_DISPLAY_LENGTH = 5000;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestActivityUpdates(intent);

                    startForeground(NOTIFICATION_ID, getNotification());
                }
            }, SPLASH_DISPLAY_LENGTH);
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ActivityRecognition.getClient(this).removeActivityUpdates(pendingIntent);

        Log.i("Exit", "OnDestroy!");
        mServiceHandler.removeCallbacksAndMessages(null);
        stopTaskTimer();

        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        return null;
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

    public void stopTaskTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * @param intent {@link Intent}
     */
    public void requestActivityUpdates(Intent intent) {
        pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        ActivityRecognition.getClient(this).removeActivityUpdates(pendingIntent);
        ActivityRecognition.getClient(this).requestActivityUpdates(100, pendingIntent);
    }

    /**
     * @param probableActivities {@link List<DetectedActivity>}
     */
    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        for( DetectedActivity activity : probableActivities ) {
            System.out.println("activity = " + activity);

            if( activity.getType() == DetectedActivity.IN_VEHICLE && activity.getConfidence() >= 75 ) {
                onNewActivity(activity);
                
                // begin yello framework
                if ( !YelloMonitorService.isServiceRunning ) {
                    YelloMonitorService.startServiceMonitoring(getApplicationContext(), 
                    new MonitoringReceiver(getApplicationContext()), API_KEY, CLIENT_UID);
                }
                // end
            } else if( (activity.getType() == DetectedActivity.STILL && activity.getConfidence() >= 75) ||
                    (activity.getType() == DetectedActivity.WALKING && activity.getConfidence() >= 75)) {

                if( mService != null && mService.mBound ) {
                    mService.removeLocationUpdates();

                    unbindService(this);

                    stopService(new Intent(getApplicationContext(), LocationUpdatesService.class));

                    // begin yello framework
                    YelloMonitorService.stopServiceMonitoring(getApplicationContext(), 
                    new MonitoringReceiver(getApplicationContext()), API_KEY, CLIENT_UID, mDriverUUID,
                        mVehicleUUID);
                    // end
                }

                Hawk.init(this).build();

                ArrayList<Position> positions = Hawk.get(LocationUpdatesService.KEY_POSITIONS);

                String email = Hawk.get(MainActivity.USER_EMAIL);
                String deviceId = Hawk.get(MainActivity.USER_DEVICE_ID);

                // Save registered data from user
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

    /**
     * @return Notification
     */
    private Notification getNotification() {
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
     * @param activity {@link DetectedActivity}
     */
    private void onNewActivity(DetectedActivity activity) {
        Log.i(TAG, "New activity: " + activity);

        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_ACTIVITY, activity);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        if( mService == null || !mService.mBound ) {
            bindService(new Intent(this, LocationUpdatesService.class), this, Context.BIND_AUTO_CREATE);
        }

        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    /**
     * @param context {@link Context}
     * @return boolean
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

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
        mService = binder.getService();
        mService.requestLocationUpdates();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
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

    // begin yello framework
    private static class MonitoringReceiver implements YelloMonitorReceiver.ResultReceiverCallback {

        private Context context;

        private MonitoringReceiver(Context context) {
            this.context = context;
        }

        @Override
        public void monitoringStatusChanged(boolean isMonitoring, String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void fileStorageFinished(boolean success, String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void syncFinished(boolean success, String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
    // end

}
