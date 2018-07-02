function keepLine(acc, line) {
  return `${acc}
  ${line}`;
}
  
function writeImports(acc, line) {
  return `//gstrack modified
  ${acc}
  ${line}
  import android.Manifest;
  import android.content.ComponentName;
  import android.content.Context;
  import android.content.Intent;
  import android.content.IntentFilter;
  import android.content.ServiceConnection;
  import android.os.IBinder;
  import android.support.v4.content.LocalBroadcastManager;
  import com.gstracker.cordova.plugin.SensorActivityService;
  import com.gstracker.cordova.plugin.SupportPermissions;
  import com.orhanobut.hawk.Hawk;`;
}
  
function insertOnCreate(acc, line) {
  return `${acc}
  ${line}
          Hawk.init(this).build();
          myReceiver = new SensorActivityService.MyReceiver();
          intent = new Intent(this, SensorActivityService.class);
          SupportPermissions permissions = new SupportPermissions();
          permissions.requestForPermission(getActivity(),
                  Manifest.permission.ACCESS_COARSE_LOCATION,
                  Manifest.permission.ACCESS_FINE_LOCATION);
          `;
}
  
function gsTrackMethods(acc, line) {
  return `${acc}
  ${line}
  
      private SensorActivityService.MyReceiver myReceiver;
      public SensorActivityService mSensor = null;
      private boolean mBound = false;
      private Intent intent;
  
      private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          SensorActivityService.LocalBinder binder = (SensorActivityService.LocalBinder) service;
          mSensor = binder.getService();
          mBound = true;
        }
  
        @Override
        public void onServiceDisconnected(ComponentName name) {
          mSensor = null;
          mBound = false;
        }
      };
  
      @Override
      protected void onStart() {
        super.onStart();
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
      }
  
      @Override
      protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, new IntentFilter(SensorActivityService.ACTION_BROADCAST));
      }
  
      @Override
      protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
      }
  
      @Override
      protected void onStop() {
        if (mBound) {
          unbindService(mServiceConnection);
          mBound = false;
        }
        super.onStop();
      }
  
      public void run() {
        mSensor.requestActivityUpdates(intent);
      }
  
      public void exit() {
        mSensor.removeActivityUpdates(intent);
        stopService(intent);
      }`;
}

module.exports = ctx => {
  if (ctx.opts.platforms.indexOf('android') < 0)
    return;
	const fs = ctx.requireCordovaModule('fs');
	const path = ctx.requireCordovaModule('path');
	const deferral = ctx.requireCordovaModule('q').defer();
  const platformRoot = path.join(ctx.opts.projectRoot, 'platforms/android');
  
  const mainActivityPath = path.join(platformRoot, 'app/src/main/java/br/com/golsat/golfleetdriver/MainActivity.java');
  fs.readFile(mainActivityPath, 'utf-8', (err, data) => {
    if (err)
      deferral.resolve();
    else if (data.includes('//gstrack modified'))
      deferral.resolve();
    else {
      const fileLines = data.split('\n');
      const editedFile = fileLines.reduce((acc, line, index, arr) => {
        if (line.includes('package'))
          return writeImports(acc, line);
        else if (line.includes('loadUrl'))
          return insertOnCreate(acc, line);
        else if (index == arr.length - 2)
          return gsTrackMethods(acc, line);
        else
          return keepLine(acc, line);
      }, '');
      console.log(editedFile);
      fs.writeFile(mainActivityPath, editedFile, err => {
        if(err)
          deferral.resolve();
        deferral.resolve();
      });
    }
  });

  return deferral.promise;
};

// const fs = require('fs');

// fs.readFile('MainActivity.java', 'utf-8', (err, data) => {
//   if (err)
//     throw err;
//   else if (data.includes('//gstrack modified'))
//     return console.log('Done!');
//   else {
//     const fileLines = data.split('\n');
//     const editedFile = fileLines.reduce((acc, line, index, arr) => {
//       if (line.includes('package'))
//         return writeImports(acc, line);
//       else if (line.includes('loadUrl'))
//         return insertOnCreate(acc, line);
//       else if (index == arr.length - 2)
//         return gsTrackMethods(acc, line);
//       else
//         return keepLine(acc, line);
//     }, '');
    
//     fs.writeFile('MainActivity.java', editedFile, err => {
//       if(err)
//         throw err;
//       console.log('Done!');
//     });
//   }
// });