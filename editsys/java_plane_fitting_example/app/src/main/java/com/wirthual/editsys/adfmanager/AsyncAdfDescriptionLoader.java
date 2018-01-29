package com.wirthual.editsys.adfmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by raphael on 10.08.16.
 */
class AsyncAdfDescriptionLoader extends AsyncTask<String, Void, List<AdfDescription>> {
    private Context context = null;
    private final ProgressDialog dialog;

    public AsyncAdfDescriptionLoader(Context context) {
        this.context = context;
        dialog = new ProgressDialog(context);
    }


    @Override
    protected void onPostExecute(List<AdfDescription> result) {
        super.onPostExecute(result);
        dialog.dismiss();
        if(null==result){
            Toast.makeText(context,"Error connecting to host.",Toast.LENGTH_SHORT).show();
        }
        ((DownloadAcitvity)context).refreshAdfList(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Downloading AdfDescriptions...");
        dialog.show();
    }

    @Override
    protected List<AdfDescription> doInBackground(String... params) {
        List<AdfDescription> result = new ArrayList<AdfDescription>();

        try {
            URL u = new URL(params[0]);

            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");

            conn.connect();
            InputStream is = conn.getInputStream();

            // Read the stream
            byte[] b = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            while ( is.read(b) != -1)
                baos.write(b);

            String JSONResp = new String(baos.toByteArray());

            JSONArray arr = new JSONArray(JSONResp);
            for (int i=0; i < arr.length(); i++) {
                result.add(convertAdfDescription(arr.getJSONObject(i)));
            }

            return result;
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    private AdfDescription convertAdfDescription(JSONObject obj) throws JSONException {
        int id = obj.getInt("id");
        String description = obj.getString("description");
        String name = obj.getString("name");
        double lat = obj.getDouble("lat");
        double lng = obj.getDouble("lng");
        int lvl = obj.getInt("lvl");
        String uuid = obj.getString("uuid");
        String type = obj.getString("type");

        return new AdfDescription(id,lat,lng,lvl,name,description,uuid,type);
    }

}