package edu.teco.scenemanager.adfmanagerclient;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by raphael on 10.08.16.
 */
public class OnClickHandler implements View.OnClickListener {
    public static String TAG = "OnClickHandler";

    private Context context = null;
    static String URL = "http://192.168.3.121:8080";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    protected static final int GPS_PERMISSION = 0x2;


    EditText ediLat;
    EditText ediLng;
    EditText ediLvl;
    SeekBar sebRadius;

    LocationListener listener=null;

    private FusedLocationProviderClient mFusedLocationClient;


    public OnClickHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        ediLat = (EditText) view.getRootView().findViewById(R.id.editLat);
        ediLng = (EditText) view.getRootView().findViewById(R.id.editLng);
        ediLvl = (EditText) view.getRootView().findViewById(R.id.editLvl);
        sebRadius = (SeekBar) view.getRootView().findViewById(R.id.seekBar);


        switch (view.getId()) {
            case R.id.button:
                double lat = Double.valueOf(ediLat.getText().toString());
                double lng = Double.valueOf(ediLng.getText().toString());
                int lvl = Integer.valueOf(ediLvl.getText().toString());
                double radius = (double)sebRadius.getProgress();

                ApiInterface apiInterface = RestClient.get(this.context);
                Call<List<AdfDescription>> call = apiInterface.fetchSceneDescriptions(lng, lat, lvl, radius);
                 call.enqueue(new Callback<List<AdfDescription>>() {

                     @Override
                    public void onResponse(Call<List<AdfDescription>> call,Response<List<AdfDescription>> response) {
                        List<AdfDescription> result = response.body();
                        Log.d(TAG,"Success"+response.toString());
                        ((DownloadAcitvity)context).refreshAdfList(result);
                    }

                    @Override
                    public void onFailure(Call call, Throwable t) {
                        Log.d(TAG,"Error"+t.getMessage());
                    }
                });

                break;
            case R.id.btn_location:
                if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(view.getContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        ){
                    try {
                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        // Got last known location. In some rare situatiolatns this can be null.
                                        if (location != null) {
                                            populateTextFields(location);
                                        }
                                    }
                                }).addOnFailureListener((Activity) context, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (e instanceof ResolvableApiException) {
                                    // Location settings are not satisfied, but this can be fixed
                                    // by showing the user a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        ResolvableApiException resolvable = (ResolvableApiException) e;
                                        resolvable.startResolutionForResult((DownloadAcitvity) context,
                                                REQUEST_CHECK_SETTINGS);
                                    } catch (IntentSender.SendIntentException sendEx) {
                                        // Ignore the error.ailureListener()
                                    }
                                }
                            }
                        });
                    } catch (SecurityException e) {
                        Toast.makeText(context, "There was a problem getting your GPS location.", Toast.LENGTH_SHORT).show();
                    }}else{
                    ActivityCompat.requestPermissions((Activity)view.getContext(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, OnClickHandler.GPS_PERMISSION);
                    }
                break;
            default:
                break;
        }
    }


    public void populateTextFields(Location location) {
        double longitude = 0.0;
        double latitude = 0.0;
        Integer lvl = null;

        longitude = location.getLongitude();
        latitude = location.getLatitude();
        ediLat.setText(String.valueOf(latitude));
        ediLng.setText(String.valueOf(longitude));

        if (!TextUtils.isEmpty(ediLvl.getText().toString())) {
            lvl = Integer.valueOf(ediLvl.getText().toString());
        } else {
            lvl = 0;
            ediLvl.setText(String.valueOf(lvl));
        }
    }

}

