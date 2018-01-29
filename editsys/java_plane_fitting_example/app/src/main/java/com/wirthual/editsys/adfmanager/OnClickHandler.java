package com.wirthual.editsys.adfmanager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.wirthual.editsys.R;

/**
 * Created by raphael on 10.08.16.
 */
public class OnClickHandler implements View.OnClickListener {
    private Context context = null;
    static String URL = "http://192.168.3.121:8080";

    EditText ediLat;
    EditText ediLng;
    EditText ediLvl;
    SeekBar sebRadius;

    LocationManager lm = null;
    LocationListener listener=null;

    public OnClickHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        ediLat = (EditText) view.getRootView().findViewById(R.id.editText2);
        ediLng = (EditText) view.getRootView().findViewById(R.id.editText);
        ediLvl = (EditText) view.getRootView().findViewById(R.id.editText3);
        sebRadius = (SeekBar) view.getRootView().findViewById(R.id.seekBar);


        switch (view.getId()) {
            case R.id.button:
                String request = makeRequest();
                (new AsyncAdfDescriptionLoader(context)).execute(request);
                break;
            case R.id.imageButton:
                getCurrentPostition(view);
                break;
            case R.id.imageButton2:
                Toast.makeText(this.context,"Not implemented",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }


    public void getCurrentPostition(View view) {
        lm = (LocationManager) view.getContext().getSystemService(Context.LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                populateTextFields(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Toast.makeText(context,"changed",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onProviderEnabled(String s) {
                Toast.makeText(context,"Enabled",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        //If Permission is not given, ask user for it
        permissionCheck(view);

        //Check again if permission is givan, and execute task depending on it
        int permissionCheck = ContextCompat.checkSelfPermission(view.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1,listener);
            //Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //if(location!=null){
            //    populateTextFields(location);
            //}

        }
    }

    public void permissionCheck(View view) {
        if (ContextCompat.checkSelfPermission(view.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale((DownloadAcitvity) context,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                createPermissionDialog();
            } else {
                ActivityCompat.requestPermissions((DownloadAcitvity) context,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        DownloadAcitvity.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
    }

    public String makeRequest() {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context.getApplicationContext());

        String ipaddress = prefs.getString("ipadress", "192.168.2.107");
        String port = prefs.getString("port", "8080");

        URL = "http://" + ipaddress+ ":" + port;

        String lat = ediLat.getText().toString();
        String lng = ediLng.getText().toString();
        String lvl = ediLvl.getText().toString();
        String radius = String.valueOf(sebRadius.getProgress());

        Uri.Builder b = Uri.parse(URL).buildUpon();
        b.appendPath("api");
        b.appendPath("nearby");
        b.appendQueryParameter("lng", lng);
        b.appendQueryParameter("lat", lat);
        b.appendQueryParameter("lvl", lvl);
        b.appendQueryParameter("radius", radius);


        Toast.makeText(context,b.toString(),Toast.LENGTH_SHORT).show();

        return b.toString();
    }

    public void createPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Permission for Position is needed to figure out current Location." +
                " If activated you can find recorded ADF-Files around you. Do you want to enable permission for location?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                ActivityCompat.requestPermissions((DownloadAcitvity) context,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        DownloadAcitvity.MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do things
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    public void populateTextFields(Location location) {
        try {
            lm.removeUpdates(listener);
        }catch(SecurityException ex){
            Toast.makeText(context,"Security exception occured on removing update listener for location",Toast.LENGTH_SHORT).show();
        }
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

