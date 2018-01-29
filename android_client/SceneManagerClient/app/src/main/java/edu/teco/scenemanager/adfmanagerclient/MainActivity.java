package edu.teco.scenemanager.adfmanagerclient;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public final String TAG = "MainActivity";
    Button download_adf;
    Button upload_adf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        download_adf = (Button)this.findViewById(R.id.button5);
        download_adf.setOnClickListener(this);
        upload_adf   = (Button)this.findViewById(R.id.button6);
        upload_adf.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button5:
                Intent upload_act = new Intent(this, UploadAcitvity.class);
                startActivity(upload_act);
                break;
            case R.id.button6:
                Intent download_act = new Intent(this, DownloadAcitvity.class);
                startActivity(download_act);
                break;
            default:
                Log.d(TAG,"Click not handled!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
                Intent settings_activity = new Intent(this, SettingsActivity.class);
                startActivity(settings_activity);
            }
        }
        return false;
    }
}
