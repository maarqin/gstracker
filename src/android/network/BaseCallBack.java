package com.gstracker.cordova.plugin.network;

import retrofit2.Response;

/**
 * Created by thomaz on 05/06/18.
 */

abstract class BaseCallBack<T> {

    public abstract void onSuccess(Response<T> response);
    public abstract void onFailure(Response<T> response);
    public abstract void onFailureValidation(Response<T> response);
}
