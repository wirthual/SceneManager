/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wirthual.editsys;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;
import com.google.atap.tangoservice.TangoErrorException;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Start Activity for Area Description example. Gives the ability to choose a particular
 * configuration and also Manage Area Description Files (ADF).
 */
public class StartActivity extends Activity implements ListView.OnItemClickListener{

    // Permission request action.
    public static final int REQUEST_CODE_TANGO_PERMISSION = 0;

    private static final String INTENT_CLASSPACKAGE = "com.projecttango.tango";
    private static final String INTENT_IMPORTEXPORT_CLASSNAME = "com.google.atap.tango.RequestImportExportActivity";

    // startActivityForResult requires a code number.
    private static final String EXTRA_KEY_SOURCEUUID = "SOURCE_UUID";
    private static final String EXTRA_KEY_DESTINATIONFILE = "DESTINATION_FILE";

    private ListView mTangoSpaceAdfListView;
    private AdfUuidArrayAdapter mTangoSpaceAdfListAdapter;
    private ArrayList<AdfData> mTangoSpaceAdfDataList;

    private Tango mTango;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_start);
        setTitle(R.string.app_name);

        startActivityForResult(
                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE), 0);


        mTangoSpaceAdfListView = (ListView) findViewById(R.id.uuid_list_view_tango_space);
        mTangoSpaceAdfDataList = new ArrayList<AdfData>();
        mTangoSpaceAdfListAdapter = new AdfUuidArrayAdapter(this, mTangoSpaceAdfDataList);
        mTangoSpaceAdfListView.setAdapter(mTangoSpaceAdfListAdapter);
        mTangoSpaceAdfListView.setOnItemClickListener(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The result of the permission activity.
        //
        // Note that when the permission activity is dismissed, the HelloAreaDescriptionActivity's
        // onResume() callback is called. As the TangoService is connected in the onResume()
        // function, we do not call connect here.
        //
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_TANGO_PERMISSION) {
            // Make sure the request was successful
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cannot start Application without permission", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                mTango = new Tango(this, new Runnable() {
                    // Pass in a Runnable to be called from UI thread when Tango is ready,
                    // this Runnable will be running on a new thread.
                    // When Tango is ready, we can call Tango functions safely here only
                    // when there is no UI thread changes involved.
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (this) {
                                    updateTangoSpaceAdfList();
                                    mTangoSpaceAdfListAdapter.setAdfData(mTangoSpaceAdfDataList);
                                    mTangoSpaceAdfListAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                });
            }
        }
    }


    private void updateTangoSpaceAdfList() {
        ArrayList<String> fullUuidList;
        TangoAreaDescriptionMetaData metadata = new TangoAreaDescriptionMetaData();

        try {
            // Get all ADF UUIDs.            android:background="#E6E6E6"

            fullUuidList = mTango.listAreaDescriptions();
            // Get the names from the UUIDs.
            mTangoSpaceAdfDataList.clear();
            for (String uuid : fullUuidList) {
               try {
                    metadata = mTango.loadAreaDescriptionMetaData(uuid);
                } catch (TangoErrorException e) {
                    Toast.makeText(this, R.string.tango_error, Toast.LENGTH_SHORT).show();
                }
                String name = new String(metadata.get(TangoAreaDescriptionMetaData.KEY_NAME));
                mTangoSpaceAdfDataList.add(new AdfData(uuid, name));
                mTango.disconnect();
            }
        } catch (TangoErrorException e) {
            Toast.makeText(this, R.string.tango_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String uuid = ((TextView)view.findViewById(R.id.adf_uuid)).getText().toString();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("uuid",uuid);
        editor.apply();
        finish();
    }
}
