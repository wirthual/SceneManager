package com.wirthual.editsys.persistance;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.wirthual.editsys.adfmanager.AdfDescription;

import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by raphael on 14.11.16.
 */

public class FileUploader {

    private static AsyncHttpClient client = new AsyncHttpClient();
    String port,ip;

    public FileUploader(String ip,String port){
        this.ip = ip;
        this.port = port;
    }

    public void uploadFileExecute(AdfDescription adf,File file) {

        RequestParams params = new RequestParams();
        params.put("desc",adf.getDescription());
        params.put("lat",adf.getLat());
        params.put("lng",adf.getLng());
        params.put("lvl",adf.getLvl());


        try { params.put("file", file); } catch (FileNotFoundException e) {
            Log.d(this.getClass().getName(),"File not found!");
        }

        client.post("http://"+this.ip+":"+this.port+"/", params,

                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d(this.getClass().getName(),"uploadFile response: "+String.valueOf(statusCode)+"ResponseBody:"+responseBody.toString());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.d(this.getClass().getName(),"uploadFile ERROR!"+String.valueOf(statusCode)+"ResponseBody:"+responseBody.toString());
                    }

                }

        );

    }
}
