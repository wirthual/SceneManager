package com.wirthual.editsys.ui.stepper.stepperfragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.colorpicker.ColorPickerPalette;
import com.android.colorpicker.ColorPickerSwatch;
import com.wirthual.editsys.R;
import com.wirthual.editsys.persistance.data.PoiObject;
import com.wirthual.editsys.ui.stepper.StepperActivity;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;


/**
 * Created by raphael on 23.10.16.
 */

public class StepperFragment2 extends Fragment implements Step {




    PoiObject poi;
    int currentColor = 0;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_step2, container, false);

        this.poi = ((StepperActivity)getContext()).getPoi();

        String[] colorStringArray = getResources().getStringArray(R.array.picker_colors);
        final ArrayList<Integer> colors = new ArrayList<Integer>();
        for(String colorCode:colorStringArray){
            Color c = new Color();
            int co = Color.parseColor(colorCode);
            colors.add(co);
        }

        Integer[] colorInts = colors.toArray(new Integer[colors.size()]);
        final int[] clr = toPrimitive(colorInts);

       final ColorPickerPalette colorPickerPalette = (ColorPickerPalette) v.findViewById(R.id.palette);
        colorPickerPalette.init(colors.size(), 6, new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                currentColor = color;
                colorPickerPalette.drawPalette(clr,color);
            }
        });

        colorPickerPalette.drawPalette(clr,this.poi.getColor());

        currentColor = poi.getColor();

        //initialize your UI

        return v;
    }

    @Override
    @StringRes
    public int getName() {
        //return string resource ID for the tab title used when StepperLayout is in tabs mode
        return R.string.tab2;
    }

    @Override
    public VerificationError verifyStep() {
        //return null if the user can go to the next step, create a new VerificationError instance otherwise
        this.poi.setColor(currentColor);
        return null;
    }

    @Override
    public void onSelected() {
        //update UI when selected
        currentColor = this.poi.getColor();
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        //handle error inside of the fragment, e.g. show error on EditText
    }

    public static int[] toPrimitive(Integer[] IntegerArray) {

        int[] result = new int[IntegerArray.length];
        for (int i = 0; i < IntegerArray.length; i++) {
            result[i] = IntegerArray[i].intValue();
        }
        return result;
    }

}