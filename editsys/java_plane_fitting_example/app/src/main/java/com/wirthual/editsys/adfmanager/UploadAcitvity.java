package com.wirthual.editsys.adfmanager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.wirthual.editsys.R;
import com.wirthual.editsys.persistance.DatabaseHelper;
import com.wirthual.editsys.persistance.data.CubeForm;
import com.wirthual.editsys.persistance.data.PanelForm;
import com.wirthual.editsys.persistance.data.PoiObject;
import com.wirthual.editsys.utils.ApiInterface;
import com.wirthual.editsys.utils.Compress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import cz.msebera.android.httpclient.Header;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;



/*Quick and dirty copy and past from OnClickHander because upload and download
* very similar. Ugly. ToDO: Improve architecture here and avoid code duplication!*/
public class UploadAcitvity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener {

    public Button btnFind;
    public ImageButton btnLocate;

    private static AsyncHttpClient client = new AsyncHttpClient();

    private GoogleApiClient mGoogleApiClient;

    public static final String TAG = "UploadActivity";



    ProgressDialog dialog;

    public EditText ediName;
    public EditText ediLat;
    public EditText ediLng;
    public EditText editDesc;
    public EditText ediLvl;

    AdfDescription adf;
    File zip;

    LocationManager lm = null;
    LocationListener listener=null;

    Context context;

    String uuid;

    private static final int RC_GET_AUTH_CODE = 9003;


    protected static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS=10;

    //Database Stuff
    private DatabaseHelper helper;
    private RuntimeExceptionDao<PoiObject, Integer> dao;
    private RuntimeExceptionDao<PanelForm, Integer> panelDao;
    private RuntimeExceptionDao<CubeForm, Integer> cubeDao;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adfuploader);


        btnFind = (Button)findViewById(R.id.button);
        btnLocate = (ImageButton)findViewById(R.id.imageButton);

        ediName = (EditText)findViewById(R.id.editName);
        ediLat = (EditText)findViewById(R.id.editLat);
        ediLng = (EditText)findViewById(R.id.editLng);
        editDesc = (EditText)findViewById(R.id.editDesc);
        ediLvl = (EditText)findViewById(R.id.editLvl);

        context = this;

        btnLocate.setOnClickListener(this);
        btnFind.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            uuid = extras.getString("uuid");
        }
        helper = new DatabaseHelper(this, uuid);
        dao = helper.getPoiObjectDao();
        panelDao = helper.getPanelDao();
        cubeDao = helper.getCubeDao();

        this.dialog = new ProgressDialog(context);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this,"Granted",Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this,"Not Granted",Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
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

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.button:
                onUploadClicked();
                break;
            case R.id.imageButton:
                getCurrentPostition();
                break;
            default:
                break;
        }

    }


    public void getCurrentPostition() {
        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

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
        permissionCheck();

        //Check again if permission is givan, and execute task depending on it
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1,listener);
            //Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //if(location!=null){
            //    populateTextFields(location);
            //}

        }
    }

    public void permissionCheck() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                createPermissionDialog();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        DownloadAcitvity.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
    }


    public void createPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Permission for Position is needed to figure out current Location." +
                " If activated you can find recorded ADF-Files around you. Do you want to enable permission for location?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                ActivityCompat.requestPermissions((UploadAcitvity) context,
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
            Toast.makeText(this,"Security exception occured on removing update listener for location",Toast.LENGTH_SHORT).show();
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

    public void onUploadClicked() {
        adf = new AdfDescription();
        adf.setName(ediName.getText().toString());
        adf.setLat(Double.valueOf(ediLat.getText().toString()));
        adf.setLng(Double.valueOf(ediLng.getText().toString()));
        adf.setLvl(Integer.valueOf(ediLvl.getText().toString()));
        adf.setDescription(editDesc.getText().toString());
        adf.setType(AdfDescription.TYPE_SCENE);

        createUploadFolderIfNotExits();



        File adfFile = new File(getExternalFilesDir(null).getAbsolutePath()+"/Upload/"+uuid);
        Log.d("ADFFilePath:",adfFile.toString());
        String path = getExternalFilesDir(null).getAbsolutePath()+"/Database/"+uuid+".db";
        File jsonFile = exportPoisToJson();
        Log.d("DatabasePath:",jsonFile.toString());
        boolean a = adfFile.exists();
        boolean b = jsonFile.exists();
        if(a&&b){
            Toast.makeText(this,"AdfFile and Database found. Starting to process.",Toast.LENGTH_SHORT).show();
            String[] files = {adfFile.getPath(),jsonFile.getPath()};
            Compress compressor = new Compress(files,this.getExternalFilesDir(null).getAbsolutePath()+"/Upload/"+uuid+".zip");
            compressor.zip(); //tODO: Important! Make async so gui does not freeze while zipping --> Async Task


            zip = new File(this.getExternalFilesDir(null).getAbsolutePath()+"/Upload/"+uuid+".zip");

            dialog.setTitle("Uploading in Progress");
            dialog.setMessage("Uploading file to Scene Manger.");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();


            handleSignIn();
        }else {
            Toast.makeText(this,"AdfFile:"+String.valueOf(a)+"\nDatabase:"+String.valueOf(b),Toast.LENGTH_SHORT).show();
        }

    }

    private void handleSignIn() {
        String serverClientId = getString(R.string.server_client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.EMAIL))
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();
        // [END configure_signin]

        // Build GoogleAPIClient with the Google Sign-In API and the above options.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GET_AUTH_CODE);
    }


    protected void createUploadFolderIfNotExits(){
        String uploadFolder = "Upload";

        File f = new File(this.getExternalFilesDir(null), uploadFolder);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(this.getLocalClassName(), "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GET_AUTH_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "onActivityResult:GET_AUTH_CODE:success:" + result.getStatus().isSuccess());

            if (result.isSuccess()) {
                // [START get_auth_code]
                GoogleSignInAccount acct = result.getSignInAccount();
                String authCode = acct.getServerAuthCode();

                // Show signed-in UI.
                new GetAccessTokenFromCode().execute(authCode);
                // [END get_auth_code]
            }
            Log.d(TAG,"Result: "+result.getStatus().getStatusMessage());
        }
    }

    private class GetAccessTokenFromCode extends AsyncTask<String, Void, String> {
        String accessToken = "nope";
        @Override
        protected String doInBackground(String... params) {
            try {
                GoogleTokenResponse tokenResponse =
                        new GoogleAuthorizationCodeTokenRequest(
                                new NetHttpTransport(),
                        JacksonFactory.getDefaultInstance(),
                                "https://www.googleapis.com/oauth2/v4/token",
                                R.string.client-id,
                                R.string.client-secret,
                                params[0],
                                "")  // Specify the same redirect URI that you use with your web
                                // app. If you don't have a web version of your app, you can
                                // specify an empty string.
                                .execute();


                accessToken = tokenResponse.getAccessToken();
                Log.d(TAG,"Access Token: "+accessToken);


            }catch (Exception ex){
                Log.d(TAG,"Error changing auth code to access token"+ex.getLocalizedMessage());
            }


            return accessToken;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG,"Executed: "+result);
/*
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());

            String ip = prefs.getString("ipadress", "127.0.0.1");
            String port = prefs.getString("port","8080");

            RequestParams params = new RequestParams();
            params.put("desc",adf.getDescription());
            params.put("lat",adf.getLat());
            params.put("lng",adf.getLng());
            params.put("lvl",adf.getLvl());

            try { params.put("file", zip); } catch (FileNotFoundException e) {
                Log.d(this.getClass().getName(),"File not found!");
            }
            params.setForceMultipartEntityContentType(true);

            client.setMaxRetriesAndTimeout(2,10000);
            client.addHeader("Authentication","Bearer "+result);
            client.addHeader("Content-Type","multipart/form-data");
            client.post("http://"+ip+":"+port+"/api/file/upload/scene", params,

                    new AsyncHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.d(this.getClass().getName(),"uploadFile response: "+String.valueOf(statusCode)+"ResponseBody:"+responseBody.toString());
                            Toast.makeText(getApplicationContext(),"Upload successfull.",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            finish();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.d(this.getClass().getName(),"uploadFile ERROR!"+String.valueOf(statusCode)+"ResponseBody:"+responseBody);
                            if(statusCode!=302){ //302 is redirect. Send by ADFManager to trigger reload on browser
                                Toast.makeText(getApplicationContext(), "Upload failed.", Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                            finish();
                        }

                    }



            );*/

            ApiInterface apiInterface = ApiInterface.retrofit.create(ApiInterface.class);
            RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), zip);
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("file", zip.getName(), fileBody);
            retrofit2.Call call = apiInterface.uploadScene("Bearer "+result,body,adf.getType(),adf.getName(),adf.getDescription(),adf.getLat(),adf.getLng(),adf.getLvl());
            call.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, retrofit2.Response response) {
                    Log.v(TAG,"Success"+response.toString());
                    dialog.cancel();
                    ((UploadAcitvity)context).finish();
                }

                @Override
                public void onFailure(Call call, Throwable t) {
                    Log.v(TAG,"Error"+t.getMessage());
                    dialog.cancel();
                }
            });
        }
            @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


    private File exportPoisToJson() {
        JSONArray arr = new JSONArray();
        File jsonFile = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            for (PoiObject obj:dao.queryForAll()){
                JSONObject j = new JSONObject(gson.toJson(obj));
                //Reomve id field from json
                JSONObject panelForm = j.getJSONObject("panelForm");
                if(panelForm!=null){
                    panelForm.remove("id");
                }else {
                    JSONObject cubeForm = j.getJSONObject("cubeForm");
                    cubeForm.remove("id");
                }
                arr.put(j);
            }
            jsonFile = new File(this.getExternalFilesDir(null), uuid+".json");
            Log.d(TAG,"Write JSON file: "+jsonFile.getAbsolutePath());

            FileOutputStream fileOutput = new FileOutputStream(jsonFile);
            OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fileOutput);
            outputStreamWriter.write(arr.toString());
            outputStreamWriter.flush();
            fileOutput.getFD().sync();
            outputStreamWriter.close();
        }catch (IOException e){
            Log.e(TAG,"Error writing Json");
            Toast.makeText(this,"Error writing Json",Toast.LENGTH_SHORT).show();
        }catch (JSONException ex){
            Log.e(TAG,"Error converting to Json");
            Toast.makeText(this,"Error converting to Json",Toast.LENGTH_SHORT).show();
        }
        return jsonFile;
    }


}
