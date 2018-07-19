package com.gstracker.cordova.plugin;


import java.util.ArrayList;

import com.gstracker.cordova.plugin.Position;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Path;
import retrofit2.http.GET;
import retrofit2.http.POST;


/**
 * Created by thomaz on 05/06/18.
 */
public interface RESTfulClient {

    // String API_URL = "http://vm-devtest1-ubu.gs.interno:6001/";
    String API_URL = "http://driver-ws.golsat.com.br:6001/";

    @GET("driver-positioning")
    Call<ArrayList<Position>> getPositions();

    @POST("driver-positioning/{email}/{deviceId}")
    Call<Void> setPositions(@Path("email") String email, @Path("deviceId") String deviceId, @Body ArrayList<Position> user);

}