package com.wirthual.editsys.ui.stepper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.wirthual.editsys.persistance.data.PoiObject;
import com.wirthual.editsys.ui.stepper.stepperfragments.StepperFragment0;
import com.wirthual.editsys.ui.stepper.stepperfragments.StepperFragment1;
import com.wirthual.editsys.ui.stepper.stepperfragments.StepperFragment2;
import com.wirthual.editsys.ui.stepper.stepperfragments.StepperFragment3;
import com.stepstone.stepper.adapter.AbstractStepAdapter;


/**
 * Created by raphael on 23.10.16.
 */

public class StepperAdapter extends AbstractStepAdapter {

    private PoiObject poi = null;
    private StepperFragment0 step0 = null;
    private StepperFragment1 step1 = null;
    private StepperFragment2 step2 = null;
    private StepperFragment3 step3 = null;

    public StepperAdapter(FragmentManager fm) {
        super(fm);
        poi = new PoiObject("Test");
        Bundle b = new Bundle();
        b.putSerializable("KEY",poi);

        step0 = new StepperFragment0();
        step1 = new StepperFragment1();
        step2 = new StepperFragment2();
        step3 = new StepperFragment3();

        step0.setArguments(b);
        step1.setArguments(b);
        step2.setArguments(b);
        step3.setArguments(b);

    }

    @Override
    public Fragment createStep(int position) {
        final Fragment step;
        switch (position){
            case 0:
                  return step0;
            case 1:
                 return  step1;
            case 2:
                return step2;
            case 3:
                return step3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
