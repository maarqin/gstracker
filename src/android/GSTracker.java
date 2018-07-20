
package com.gstracker.cordova.plugin;

// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.golsat.golfleetdriver.*;

import com.orhanobut.hawk.Hawk;

public class GSTracker extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {


        MainActivity mainActivity = ((MainActivity) cordova.getActivity());

        PluginResult pluginResult = null;
        boolean ret = true;
        switch( action ) {
            case "run" :
                try {
                    mainActivity.run(args.getJSONObject(0));
                } catch (JSONException e) {
                    callbackContext.error("Error encountered: " + e.getMessage());
                    return false;
                }
                pluginResult = new PluginResult(PluginResult.Status.OK);

                break;
            case "exit" :
                mainActivity.exit();

                pluginResult = new PluginResult(PluginResult.Status.OK);
                
                break;

            case "confirmConnectionUserStatus" :
                Hawk.init(this).build();
    
                boolean status = Hawk.get(MainActivity.IS_CONNECTION_OK);
                pluginResult = new PluginResult((status) ? PluginResult.Status.OK :  PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);

                break;
            default :
                callbackContext.error("\"" + action + "\" is not a recognized action.");

                pluginResult = new PluginResult(PluginResult.Status.INVALID_ACTION);

                ret = false;
                
        }

        callbackContext.sendPluginResult(pluginResult);

        return ret;

    }

}