package com.gstracker.cordova.plugin.network;


import java.util.ArrayList;

import com.gstracker.cordova.plugin.model.Position;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;


/**
 * Created by thomaz on 05/06/18.
 */
public interface RESTfulClient {

    String API_URL = "http://vm-devtest1-ubu.gs.interno:6001/";

    @GET("location")
    Call<ArrayList<Position>> getPositions();

    @POST("location")
    Call<Void> setPositions(@Body ArrayList<Position> user);

}