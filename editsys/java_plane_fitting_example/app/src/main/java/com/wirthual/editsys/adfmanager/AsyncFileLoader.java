package com.wirthual.editsys.adfmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.wirthual.editsys.events.SceneDownloadDone;
import com.wirthual.editsys.utils.Decompress;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by raphael on 10.08.16.
 */
class AsyncFileLoader extends AsyncTask<AdfDescription, Void, File> {

    private final String TAG = "AsyncFileLoader";
    Context context = null;
    ProgressDialog dialog;
    static String URL = "";
    String uuid = "";

    public AsyncFileLoader(Context context) {
        this.context = context;
        this.dialog = new ProgressDialog(context);


        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context.getApplicationContext());

        String ipaddress = prefs.getString("ipadress", "192.168.2.107");
        String port = prefs.getString("port", "8080");
        uuid = prefs.getString("uuid","notAvailableError");

        URL = "http://" + ipaddress+ ":" + port;

    }


    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        dialog.dismiss();
        ((DownloadAcitvity)context).finish();
        EventBus.getDefault().post(new SceneDownloadDone(uuid));
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setTitle("Download in Progress");
        dialog.setMessage("Downloading AdfDescriptions...");
        dialog.show();
    }

    @Override
    protected File doInBackground(AdfDescription... params) {
        File file = null;
        try {
            AdfDescription adf = params[0];
            Uri.Builder b = Uri.parse(URL).buildUpon();
            b.appendPath("api");
            b.appendPath("file");
            b.appendPath("id");
            b.appendPath(String.valueOf(adf.getId()));

            URL url = new URL(b.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setRequestMethod("GET");

            conn.connect();
            InputStream is = conn.getInputStream();


            // Read the stream
            try {
                file = new File(context.getExternalFilesDir(null)+"/Download",uuid+".zip");
                OutputStream output = new FileOutputStream(file);
                try {
                    try {
                        byte[] buffer = new byte[4 * 1024]; // or other buffer size
                        int read;

                        while ((read = is.read(buffer)) != -1) {
                            output.write(buffer, 0, read);
                        }
                        output.flush();
                    } finally {
                        output.close();
                    }
                } catch (Exception e) {
                    Log.d(TAG,"Error on download");
                    e.printStackTrace(); // handle exception, define IOException and others
                }

                String pathDb = context.getExternalFilesDir(null)+"/Download/"+uuid+".db";
                String pathDestination = context.getExternalFilesDir(null) +"/Database/"+uuid+".db";
                File db = new File(pathDb);
                File destination = new File(pathDestination);

                Log.d(getClass().getName(),"pathDb:"+pathDb+" in Download folder exists:"+db.exists());
                Log.d(getClass().getName(),"destination"+pathDestination+" in Download folder exists"+destination.exists());

                new Decompress(file.getAbsolutePath(),context.getExternalFilesDir(null)+"/Download/").unzip();

                copyFile(db,destination);
            } finally {
                is.close();
            }
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        return file;
    }


    private boolean copyFile(File src,File dst)throws IOException {
        Log.d(getClass().getName(),"Copy File - Source:"+src.getAbsolutePath().toString()+" Destination:"+dst.getAbsolutePath().toString());
        if(src.getAbsolutePath().toString().equals(dst.getAbsolutePath().toString())){
            return true;
        }else{
            InputStream is=new FileInputStream(src);
            OutputStream os=new FileOutputStream(dst);
            byte[] buff=new byte[1024];
            int len;
            while((len=is.read(buff))>0){
                os.write(buff,0,len);
            }
            is.close();
            os.flush();
            os.close();
        }
        Log.d(this.getClass().getName(),"Copy File - Done");
        return true;
    }
}