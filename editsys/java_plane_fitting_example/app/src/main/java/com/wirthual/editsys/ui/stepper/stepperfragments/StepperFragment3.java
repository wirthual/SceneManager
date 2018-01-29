package com.wirthual.editsys.ui.stepper.stepperfragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wirthual.editsys.R;
import com.wirthual.editsys.ui.stepper.StepperActivity;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import com.wirthual.editsys.persistance.data.PoiObject;

/**
 * Created by raphael on 23.10.16.
 */

public class StepperFragment3 extends Fragment implements Step {

    TextView txtName;
    TextView txtShortDesc;
    TextView txtLongDesc;
    ImageView imgColor;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_step3, container, false);

        //initialize your UI
        txtName = (TextView) v.findViewById(R.id.txtName);
        txtShortDesc = (TextView) v.findViewById(R.id.txtShortDesc);
        txtLongDesc = (TextView) v.findViewById(R.id.txtLongDesc);
        imgColor = (ImageView)v.findViewById(R.id.imgColor);


        return v;
    }

    @Override
    @StringRes
    public int getName() {
        //return string resource ID for the tab title used when StepperLayout is in tabs mode
        return R.string.tab3;
    }

    @Override
    public VerificationError verifyStep() {
        //return null if the user can go to the next step, create a new VerificationError instance otherwise
        return null;
    }

    @Override
    public void onSelected() {
        //update UI when selected
        PoiObject poi = ((StepperActivity)getContext()).getPoi();
        txtName.setText(poi.getName());
        txtShortDesc.setText(poi.getDescription());
        txtLongDesc.setText(poi.getLongDescription());
        imgColor.setBackgroundColor(poi.getColor());
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        //handle error inside of the fragment, e.g. show error on EditText
    }

}