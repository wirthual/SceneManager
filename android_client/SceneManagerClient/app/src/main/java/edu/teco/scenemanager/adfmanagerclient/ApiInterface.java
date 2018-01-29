package edu.teco.scenemanager.adfmanagerclient;


import java.util.List;

import okhttp3.MultipartBody;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by raphael on 29.11.17.
 */

public interface ApiInterface {
        @Multipart
        @POST("api/file/upload/adf")
        Call<ResponseBody> uploadFile(
                @Header("Authorization") String authorization,
                @Part MultipartBody.Part filePart,
                @Part("type") String type,
                @Part("name") String name,
                @Part("desc") String desc,
                @Part("lat") Double lat,
                @Part("lng") Double lng,
                @Part("lvl") int lvl);

        @GET("api/file/id/{id}")
        Call<ResponseBody> downloadFile(@Path("id") int id);

        @GET("api/nearby")
        Call<List<AdfDescription>> fetchSceneDescriptions(@Query("lng") Double lng,
                                                         @Query("lat") Double lat,
                                                         @Query("lvl") Integer lvl,
                                                         @Query("radius") Double radius);

}
