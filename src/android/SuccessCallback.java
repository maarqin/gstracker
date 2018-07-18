package com.gstracker.cordova.plugin;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.GsonBuilder;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Intent;


/**
 * Created by thomaz on 05/06/18.
 */
abstract class SuccessCallback<T> extends BaseCallBack<T> implements Callback<T> {


    private Context context;
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(RESTfulClient.API_URL)
            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create()))
            .build();

    RESTfulClient rest = retrofit.create(RESTfulClient.class);

    public SuccessCallback(Context context) {
        this.context = context;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {

        System.out.println("response.code() = " + response.code());

        switch ( response.code() ) {
            case HttpURLConnection.HTTP_OK :
            case HttpURLConnection.HTTP_ACCEPTED :
            case HttpURLConnection.HTTP_NOT_AUTHORITATIVE :
            case HttpURLConnection.HTTP_NO_CONTENT :
            case HttpURLConnection.HTTP_RESET :
            case HttpURLConnection.HTTP_PARTIAL :
                onSuccess(response);
                break;
            case 422 :
                onFailureValidation(response);
                break;
            case HttpURLConnection.HTTP_NOT_FOUND :
            case HttpURLConnection.HTTP_BAD_METHOD :
            case HttpURLConnection.HTTP_INTERNAL_ERROR :
            case HttpURLConnection.HTTP_NOT_IMPLEMENTED :
            case HttpURLConnection.HTTP_BAD_GATEWAY :
            case HttpURLConnection.HTTP_UNAVAILABLE :
            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT :
            case HttpURLConnection.HTTP_VERSION :
                onFailure(response);
                break;
            case HttpURLConnection.HTTP_UNAUTHORIZED :
                
                quitApplication();
                break;
        }

    }

    private void quitApplication(){
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(intent);
    }

    @Override
    public void onFailure(Call<T> call, Throwable t){
        if( t instanceof UnknownHostException) {
            Toast.makeText(context, "Sem conexão com a internet", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(Response<T> response) {
        Toast.makeText(context, response.message() + ": " + response.errorBody(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailureValidation(Response<T> response) {

    }

}