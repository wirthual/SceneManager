package edu.teco.scenemanager.adfmanagerclient;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;



/*Quick and dirty copy and past from OnClickHander because upload and download
* very similar. Ugly. ToDO: Improve architecture here and avoid code duplication!*/
public class UploadAcitvity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener {


    private FusedLocationProviderClient mFusedLocationClient;
    OnClickHandler handler;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    protected static final int GPS_PERMISSION = 0x2;

    public Button btnFind;
    public ImageButton btnLocate;
    public Button btnSelectFile;

    public TextView tvFile;


    private GoogleApiClient mGoogleApiClient;

    public static final String TAG = "UploadActivity";

    public EditText ediName;
    public EditText ediLat;
    public EditText ediLng;
    public EditText editDesc;
    public EditText ediLvl;

    AdfDescription adf;

    Context context;

    String uuid;

    Uri selectedFile;

    private static final int RC_GET_AUTH_CODE = 9003;


    protected static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS=10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adfuploader);


        btnFind = (Button)findViewById(R.id.button);
        btnLocate = (ImageButton)findViewById(R.id.btn_location);
        btnSelectFile = (Button) findViewById(R.id.btn_selectFile);


        handler = new OnClickHandler(this);
        btnLocate.setOnClickListener(handler);
        btnFind.setOnClickListener(this);
        btnSelectFile.setOnClickListener(this);

        ediName = (EditText)findViewById(R.id.editName);
        ediLat = (EditText)findViewById(R.id.editLat);
        ediLng = (EditText)findViewById(R.id.editLng);
        editDesc = (EditText)findViewById(R.id.editDesc);
        ediLvl = (EditText)findViewById(R.id.editLvl);

        tvFile = (TextView)findViewById(R.id.tv_File);
        context = this;



        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            uuid = extras.getString("uuid");
        }


        permissionCheck();

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
        boolean isNotEmpty  =ediLat.getText().length() > 0 && ediLng.getText().length() > 0;
        boolean fileIsSelected = selectedFile!=null;
        btnFind.setEnabled(isNotEmpty && fileIsSelected);
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.button:
                onUploadClicked();
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
                    ActivityCompat.requestPermissions((DownloadAcitvity)view.getContext(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, OnClickHandler.GPS_PERMISSION);
                }                break;
            case R.id.btn_selectFile:
                performFileSearch();
                break;
            default:
                break;
        }

    }



    public void permissionCheck() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        DownloadAcitvity.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
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
        handleSignIn();
    }

    private void handleSignIn() {
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
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                String fileName = queryName(uri);
                uuid = fileName;
                tvFile.setText(fileName, TextView.BufferType.EDITABLE);
                Log.i(TAG, "Uri: " + uri.toString());
                selectedFile = uri;
            }
        }
    }

    private class GetAccessTokenFromCode extends AsyncTask<String, Void, String> {

        String accessToken = "nope";

        final ProgressDialog dialog = ProgressDialog.show(context,"","uploading...");



        @Override
        protected String doInBackground(String... params) {

            try {
                GoogleTokenResponse tokenResponse =
                        new GoogleAuthorizationCodeTokenRequest(
                                new NetHttpTransport(),
                        JacksonFactory.getDefaultInstance(),
                                getString(R.string.token_url),
                                getString(R.string.server_client_id),
                                        getString(R.string.client_password),
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

            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());

            RequestBody requestBody = null;
            try {
                InputStream is = getContentResolver().openInputStream(selectedFile);
                byte[] buf;
                buf = new byte[is.available()];
                while (is.read(buf) != -1);
                requestBody = RequestBody
                        .create(MediaType.parse("application/octet-stream"), buf);
                is.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // MultipartBody.Part is used to send also the actual file name
            ApiInterface apiInterface = RestClient.get(context);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file",uuid, requestBody);
            retrofit2.Call call = apiInterface.uploadFile("Bearer "+result,body,adf.getType(),adf.getName(),adf.getDescription(),adf.getLat(),adf.getLng(),adf.getLvl());
            call.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, retrofit2.Response response) {
                    Log.v(TAG,"Success"+response.toString());
                    if(response.code()== 200) {
                        Toast.makeText(context, "Upload sucessful.",Toast.LENGTH_LONG).show();
                    }else if(response.code()== 401){
                        Toast.makeText(context, "Upload failed. Unauthorized.",Toast.LENGTH_LONG).show();
                    }else{
                        try {
                            Toast.makeText(context, "Upload failed."+response.errorBody().string(),Toast.LENGTH_LONG).show();
                        }catch (IOException e){
                            Toast.makeText(context,"Unexpected internal error.",Toast.LENGTH_LONG).show();
                        }
                    }
                    ((UploadAcitvity)context).finish();
                }

                @Override
                public void onFailure(Call call, Throwable t) {
                    Log.v(TAG,"Error"+t.getMessage());
                    dialog.dismiss();
                }
            });
        }
            @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


    private static final int READ_REQUEST_CODE = 42;

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private String queryName(Uri uri) {
        String scheme = uri.getScheme();
        String fileName = "";
        if (scheme.equals("file")) {
           fileName = uri.getLastPathSegment();
        }
        else if (scheme.equals("content")) {
            String[] proj = { MediaStore.Images.Media.TITLE };
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                cursor.moveToFirst();
                fileName = cursor.getString(columnIndex);
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileName;
    }
}
