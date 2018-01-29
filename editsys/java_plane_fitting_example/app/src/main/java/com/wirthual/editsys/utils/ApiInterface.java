package com.wirthual.editsys.utils;

import okhttp3.MultipartBody;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by raphael on 29.11.17.
 */

public interface ApiInterface {
        @Multipart
        @POST("/api/file/upload/scene")
        Call<ResponseBody> uploadScene(
                @Header("Authorization") String authorization,
                @Part MultipartBody.Part filePart,
                @Part("type") String type,
                @Part("desc") String desc,
                @Part("name") String name,
                @Part("lat") Double lat,
                @Part("lng") Double lng,
                @Part("lvl") int lvl);


        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


        public static final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://129.13.169.197:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
}
