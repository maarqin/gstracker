
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

        PluginResult pluginResult;
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

            case "statusConnection" :
                Hawk.init(mainActivity).build();

                Boolean status = Hawk.get(MainActivity.IS_CONNECTION_OK);

                if( status == null || status ) {
                    pluginResult = new PluginResult(PluginResult.Status.OK);
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);
                    Hawk.deleteAll();
                }
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