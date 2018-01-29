package com.wirthual.editsys.ui.stepper.stepperfragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;
import com.wirthual.editsys.R;
import com.wirthual.editsys.persistance.data.CubeForm;
import com.wirthual.editsys.persistance.data.PanelForm;
import com.wirthual.editsys.persistance.data.PoiObject;
import com.wirthual.editsys.ui.stepper.StepperActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by raphael on 23.10.16.
 */

public class StepperFragment0 extends Fragment implements Step {

    RadioGroup type;
    EditText act;
    EditText webAPIurl;

    float POI_SIDE_LENGTH = 0.02f;
    float PLANE_SIDE_LENGTH = 0.3f;

    PoiObject poi;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_step0, container, false);

        //initialize your UI
        type = (RadioGroup) v.findViewById(R.id.radiogroup);
        act = (EditText) v.findViewById(R.id.edit_activities);

        webAPIurl = (EditText) v.findViewById(R.id.url_edittext);

        this.poi = ((StepperActivity)getContext()).getPoi();

        return v;
    }

    @Override
    @StringRes
    public int getName() {
        //return string resource ID for the tab title used when StepperLayout is in tabs mode
        return R.string.tab0;
    }

    @Override
    public VerificationError verifyStep() {
        //return null if the user can go to the next step, create a new VerificationError instance otherwise
        switch (type.getCheckedRadioButtonId()){
            case R.id.radioPoint:
                poi.setPanelForm(new PanelForm(POI_SIDE_LENGTH,POI_SIDE_LENGTH));
                poi.setCubeForm(null);
                break;
            case R.id.radioPlane:
                poi.setPanelForm(new PanelForm(PLANE_SIDE_LENGTH,PLANE_SIDE_LENGTH));
                poi.setCubeForm(null);
                break;
            case R.id.radioCube:
                poi.setPanelForm(null);
                poi.setCubeForm(new CubeForm(0.3f,0.4f,0.3f));
                break;
            default:
                Log.d(this.getClass().getName(),"NO RADIO BUTTON CHECKED!");
                //TODO: Check for wrong input here!
        }
        String actText = act.getText().toString();
        if(!actText.equals("")){poi.setActivities(actText.split(","));}

        if(webAPIurl.getText().toString()!=""){
            poi.setApiCallURL(webAPIurl.getText().toString());
        }

        return null;
    }

    @Override
    public void onSelected() {
        if(null!=poi) {
            if(poi.getPanelForm()!=null){
                if(poi.getPanelForm().getHeigth()==POI_SIDE_LENGTH){
                    type.check(R.id.radioPoint);
                }else if(poi.getPanelForm().getHeigth()==PLANE_SIDE_LENGTH){
                    type.check(R.id.radioPlane);
                }

            }else if (poi.getCubeForm()!=null){
                type.check(R.id.radioCube);
            }
            act.setText(TextUtils.join(",",poi.getActivities()));
        }
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        //handle error inside of the fragment, e.g. show error on EditText
    }

}