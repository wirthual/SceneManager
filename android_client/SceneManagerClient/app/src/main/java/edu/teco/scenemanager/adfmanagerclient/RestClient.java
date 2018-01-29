package edu.teco.scenemanager.adfmanagerclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.api.Api;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static okhttp3.OkHttpClient.*;

/**
 * Created by raphael on 12.12.17.
 */

public class RestClient {
    public static final String TAG = "RestClient";

    private static ApiInterface REST_CLIENT;

    public static ApiInterface get(@NonNull Context context) {
        setupRestClient(context);
        return REST_CLIENT;
    }

    private static void setupRestClient(@NonNull Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String url = sharedPref.getString("url", "");
        Log.d(TAG,url);
        String port = sharedPref.getString("port", "");
        Log.d(TAG,port);
        String scenemanagerUrl = "http://"+url+":"+port;


        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).
                readTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();


         Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(scenemanagerUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        REST_CLIENT = retrofit.create(ApiInterface.class);
    }

}