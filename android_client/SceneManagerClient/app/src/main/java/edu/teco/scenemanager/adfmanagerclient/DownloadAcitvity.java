package edu.teco.scenemanager.adfmanagerclient;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadAcitvity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    public static final String TAG = "DownloadActivity";


    AdfListAdapter adapter;

    public Button btnFind;
    public ImageButton btnLocate;

    public EditText ediLat;
    public EditText ediLng;
    public EditText ediLvl;
    public ListView lvResults;

    protected static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS=10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adfdownloader);

        //Create Folder Download and Database in Internal Storage if not already exists(/Android/data/com.wirhual.editsys/files/...)
        createFolderIfNotExits("Download");
        createFolderIfNotExits("Database");

        btnFind = (Button)findViewById(R.id.button);
        btnLocate = (ImageButton)findViewById(R.id.btn_location);

        ediLat = (EditText)findViewById(R.id.editLat);
        ediLng = (EditText)findViewById(R.id.editLng);
        ediLvl = (EditText)findViewById(R.id.editLvl);

        lvResults = (ListView)findViewById(R.id.listView);

        OnClickHandler handler = new OnClickHandler(this);

        btnLocate.setOnClickListener(handler);
        btnFind.setOnClickListener(handler);

        lvResults = (ListView)findViewById(R.id.listView);

        TextView emptyText = (TextView)findViewById(android.R.id.empty);
        lvResults.setEmptyView(emptyText);

        adapter = new AdfListAdapter(new ArrayList(),this);
        lvResults.setAdapter(adapter);
        lvResults.setOnItemClickListener(this);

        checkPermission();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case OnClickHandler.GPS_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Granted",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this,"Not Granted",Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                updateButtonState();
            }
        };

        ediLat.addTextChangedListener(tw);
        ediLng.addTextChangedListener(tw);

        updateButtonState();

    }

    private void updateButtonState(){
        boolean isEmpty  =ediLat.getText().length() > 0 && ediLng.getText().length() > 0;
        btnFind.setEnabled(isEmpty);
    }

    public void refreshAdfList(List<AdfDescription> results){
        adapter.notifyDataSetInvalidated();
        adapter.setItemList(results);
        adapter.notifyDataSetChanged();
    }

    protected void createFolderIfNotExits(String folderName){
        File f = new File(this.getExternalFilesDir(null), folderName);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        AdfDescription desc = (AdfDescription) view.getTag(R.id.myId);
        final String uuid = desc.getUuid();

        final ProgressDialog dialog = ProgressDialog.show(this, "", "downloading...");
        ApiInterface apiInterface = RestClient.get(this);
        Call<ResponseBody> call = apiInterface.downloadFile(desc.getId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "server contacted and has file");
                    dialog.dismiss();

                    boolean writtenToDisk = writeResponseBodyToDisk(response.body(),uuid);

                    Log.d(TAG, "file download was a success? " + writtenToDisk);
                } else {
                    Log.d(TAG, "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dialog.dismiss();
                Log.e(TAG, "error");
            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body,String name) {
        try {
            // todo change the file location/name according to your needs

            File futureStudioIconFile = new File(getFilename()+ "/"+ name);
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();
                Log.d(TAG,"File written to:"+futureStudioIconFile.getAbsolutePath());
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }


    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, OnClickHandler.GPS_PERMISSION);
        }
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath + "/TangoADFs" );
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

}
