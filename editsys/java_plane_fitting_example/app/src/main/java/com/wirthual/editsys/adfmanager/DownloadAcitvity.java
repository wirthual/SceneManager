package com.wirthual.editsys.adfmanager;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.wirthual.editsys.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadAcitvity extends AppCompatActivity {

    AdfListAdapter adapter;

    public Button btnFind;
    public ImageButton btnLocate;
    public ImageButton btnLevel;

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
        btnLocate = (ImageButton)findViewById(R.id.imageButton);
        btnLevel = (ImageButton)findViewById(R.id.imageButton2);

        ediLat = (EditText)findViewById(R.id.editText);
        ediLng = (EditText)findViewById(R.id.editText2);
        ediLvl = (EditText)findViewById(R.id.editText3);

        lvResults = (ListView)findViewById(R.id.listView);

        OnClickHandler handler = new OnClickHandler(this);

        btnLocate.setOnClickListener(handler);
        btnFind.setOnClickListener(handler);
        btnLevel.setOnClickListener(handler);

        lvResults = (ListView)findViewById(R.id.listView);

        TextView emptyText = (TextView)findViewById(android.R.id.empty);
        lvResults.setEmptyView(emptyText);

        adapter = new AdfListAdapter(new ArrayList(),this);
        lvResults.setAdapter(adapter);

        lvResults.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AdfDescription desc = (AdfDescription) view.getTag(R.id.myId);
                String uuid = ((TextView)view.findViewById(R.id.uuid)).getText().toString();
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("uuid",uuid);
                editor.apply();
                (new AsyncFileLoader(DownloadAcitvity.this)).execute(desc);
            }
        });
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
        btnLevel.setEnabled(isEmpty);
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

}
