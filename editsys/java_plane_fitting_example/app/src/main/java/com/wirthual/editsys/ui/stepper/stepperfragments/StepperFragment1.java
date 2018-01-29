package com.wirthual.editsys.ui.stepper.stepperfragments;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.wirthual.editsys.R;
import com.wirthual.editsys.persistance.data.PoiObject;
import com.wirthual.editsys.ui.stepper.StepperActivity;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;



/**
 * Created by raphael on 23.10.16.
 */

public class StepperFragment1 extends Fragment implements Step {

    EditText name;
    EditText desc;
    EditText lngDesc;

    PoiObject poi;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_step1, container, false);

        //initialize your UI
        TextView text = (TextView) v.findViewById(R.id.textFragment1);
        name = (EditText) v.findViewById(R.id.editName);
        desc = (EditText) v.findViewById(R.id.editShortDesc);
        lngDesc = (EditText) v.findViewById(R.id.editLongDesc);

        this.poi = ((StepperActivity)getContext()).getPoi();

        return v;
    }

    @Override
    @StringRes
    public int getName() {
        //return string resource ID for the tab title used when StepperLayout is in tabs mode
        return R.string.tab1;
    }

    @Override
    public VerificationError verifyStep() {
        //return null if the user can go to the next step, create a new VerificationError instance otherwise
        poi.setName(name.getText().toString());
        poi.setDescription(desc.getText().toString());
        poi.setLongDescription(lngDesc.getText().toString());
        //TODO: Check for wrong input here!
        return null;
    }

    @Override
    public void onSelected() {
        //update UI when selected
        name.setText(this.poi.getName());
        desc.setText(this.poi.getDescription());
        desc.setText(this.poi.getLongDescription());
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        //handle error inside of the fragment, e.g. show error on EditText
    }

}