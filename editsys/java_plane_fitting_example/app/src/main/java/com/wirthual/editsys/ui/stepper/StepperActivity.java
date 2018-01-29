package com.wirthual.editsys.ui.stepper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.wirthual.editsys.R;
import com.wirthual.editsys.events.RedrawObjectsEvent;
import com.wirthual.editsys.persistance.DatabaseHelper;
import com.wirthual.editsys.persistance.data.CubeForm;
import com.wirthual.editsys.persistance.data.PanelForm;
import com.wirthual.editsys.persistance.data.PoiObject;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import org.greenrobot.eventbus.EventBus;
import org.rajawali3d.primitives.Cube;


/**
 * Created by raphael on 23.10.16.
 */


public class StepperActivity extends FragmentActivity implements StepperLayout.StepperListener {

    private StepperLayout mStepperLayout;
    PoiObject poi = null;
    private DatabaseHelper helper;
    private RuntimeExceptionDao<CubeForm, Integer> daoCube;
    private RuntimeExceptionDao<PanelForm, Integer> daoPanel;
    private RuntimeExceptionDao<PoiObject, Integer> dao;



    public StepperActivity(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get POI from Intent
        this.poi = (PoiObject)getIntent().getSerializableExtra("poi");
        if(this.poi == null){
            this.poi = new PoiObject("Bla");
            Log.d(StepperActivity.class.getName(),"Got no poi from intent. Created new one:"+this.poi.toString());
        }else{
            Log.d(StepperActivity.class.getName(),"Got poi from intent:"+this.poi.toString());
        }

        setContentView(R.layout.activity_stepper);
        mStepperLayout = (StepperLayout) findViewById(R.id.stepperLayout);
        mStepperLayout.setListener(this);
        StepperAdapter adapter = new StepperAdapter(getSupportFragmentManager());
        mStepperLayout.setAdapter(adapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String name = prefs.getString("uuid","none");

        helper = new DatabaseHelper(this,name);
        dao = helper.getPoiObjectDao();
        daoPanel = helper.getPanelDao();
        daoCube = helper.getCubeDao();
    }


    @Override
    public void onCompleted(View completeButton) {

        long millis = System.currentTimeMillis();
        // create some entries in the onCreate

        Dao.CreateOrUpdateStatus status1 = daoPanel.createOrUpdate(this.poi.getPanelForm());
        Dao.CreateOrUpdateStatus status2 = daoCube.createOrUpdate(this.poi.getCubeForm());
        Dao.CreateOrUpdateStatus status = dao.createOrUpdate(this.poi);
        if(status.isCreated()){
            Log.i(DatabaseHelper.class.getName(), "created new entry for Poi:"+this.poi.toString());
        }else{
            Log.i(DatabaseHelper.class.getName(), "updated entry for Poi"+ this.poi.toString());
            //http://stackoverflow.com/questions/14456609/ormlite-not-loading-child-foreign-fields

        }
        //Close activity
        EventBus.getDefault().post(new RedrawObjectsEvent());
        finish();
    }

    @Override
    public void onError(VerificationError verificationError) {
        Toast.makeText(this, "onError! -> " + verificationError.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStepSelected(int newStepPosition) {
        //Toast.makeText(this, "onStepSelected! -> " + newStepPosition, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReturn() {

    }

    public PoiObject getPoi(){
        return this.poi;
    }
}
