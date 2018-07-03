
package com.gstracker.cordova.plugin;

// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.golsat.golfleetdriver.*;

public class GSTracker extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args,
                           final CallbackContext callbackContext) {
        // Verify that the user sent a 'show' action

        MainActivity mainActivity = ((MainActivity) cordova.getActivity());

        if ( action.equals("run") || action.equals("exit") ) {

            if( action.equals("run") ) {
                try {
                    JSONObject options = args.getJSONObject(0);
                    int userId = options.getInt("userId");

                    mainActivity.run(userId);
                } catch (JSONException e) {
                    callbackContext.error("Error encountered: " + e.getMessage());
                    return false;
                }

            } else {
                mainActivity.exit();
            }

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            callbackContext.sendPluginResult(pluginResult);

            return true;
        } else {
            callbackContext.error("\"" + action + "\" is not a recognized action.");

            PluginResult pluginResult = new PluginResult(PluginResult.Status.INVALID_ACTION);
            callbackContext.sendPluginResult(pluginResult);

            return false;
        }

    }

}