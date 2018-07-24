function keepLine(acc, line) {
  return `${acc}
  ${line}`;
}
  
function writeImports(acc, line) {
  return `//gstrack modified
  ${acc}
  ${line}

  import android.Manifest;
  import android.content.Intent;
  import android.os.Bundle;
  import com.orhanobut.hawk.Hawk;
  
  import org.apache.cordova.*;

  import org.json.JSONException;
  import org.json.JSONObject;
  
  import com.gstracker.cordova.plugin.*;
  `;
}
  
function insertOnCreate(acc, line) {
  return `${acc}
  ${line}
          Hawk.init(this).build();

          intent = new Intent(this, SensorActivityService.class);
          startService(intent);
  
          SupportPermissions permissions = new SupportPermissions();
          permissions.requestForPermission(this,
                  Manifest.permission.ACCESS_COARSE_LOCATION,
                  Manifest.permission.ACCESS_FINE_LOCATION);
          `;
}
  
function gsTrackMethods(acc, line) {
  return `${acc}
  ${line}
  
      static public final String IS_CONNECTION_OK = "IS_CONNECTION_OK";
      static public final String USER_ID = "USER_ID";
      static public final String USER_EMAIL = "USER_EMAIL";
      static public final String USER_DEVICE_ID = "USER_DEVICE_ID";

      private Intent intent;
  
      public void run(JSONObject options) {

        try {

          int userId = options.getInt("userId");
          String userEmail = options.getString("userEmail");
          String userDeviceId = options.getString("userDeviceId");
  
          Hawk.put(USER_ID, userId);
          Hawk.put(USER_EMAIL, userEmail);
          Hawk.put(USER_DEVICE_ID, userDeviceId);

          Hawk.put(IS_CONNECTION_OK, true);

        } catch (JSONException e) {
            e.printStackTrace();
        }

      }
  
      public void exit() {
        stopService(intent);
      }
      `;
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