package com.wirthual.editsys.ui.stepper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.colorpicker.ColorPickerPalette;
import com.android.colorpicker.ColorPickerSwatch;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.stepstone.stepper.StepperLayout;
import com.wirthual.editsys.R;
import com.wirthual.editsys.SettingsActivity;
import com.wirthual.editsys.persistance.DatabaseHelper;
import com.wirthual.editsys.persistance.data.PoiObject;

import java.util.ArrayList;

/**
 * Created by raphael on 27.01.17.
 */

public class NoStepperActivity extends AppCompatActivity {


    PoiObject poi = null;
    private DatabaseHelper helper;
    private RuntimeExceptionDao<PoiObject, Integer> dao;



    RadioGroup type;
    EditText webAPIurl;

    EditText name;
    EditText desc;
    EditText lngDesc;

    ColorPickerPalette colorPickerPalette=null;

    int currentColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get POI from Intent
        this.poi = (PoiObject) getIntent().getSerializableExtra("poi");
        if (this.poi == null) {
            this.poi = new PoiObject("Bla");
            Log.d(StepperActivity.class.getName(), "Got no poi from intent. Created new one:" + this.poi.toString());
        } else {
            Log.d(StepperActivity.class.getName(), "Got poi from intent:" + this.poi.toString());
        }

        setContentView(R.layout.poicreate);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String nameADF = prefs.getString("uuid", "none");

        helper = new DatabaseHelper(this, nameADF);
        dao = helper.getPoiObjectDao();

        type = (RadioGroup) findViewById(R.id.radiogroup);

        webAPIurl = (EditText) findViewById(R.id.url_edittext);
        name = (EditText) findViewById(R.id.editName);
        desc = (EditText) findViewById(R.id.editShortDesc);
        lngDesc = (EditText) findViewById(R.id.editLongDesc);

        String[] colorStringArray = getResources().getStringArray(R.array.picker_colors);
        final ArrayList<Integer> colors = new ArrayList<Integer>();
        for(String colorCode:colorStringArray){
            Color c = new Color();
            int co = Color.parseColor(colorCode);
            colors.add(co);
        }

        Integer[] colorInts = colors.toArray(new Integer[colors.size()]);
        final int[] clr = toPrimitive(colorInts);

        colorPickerPalette = (ColorPickerPalette) findViewById(R.id.palette);
        colorPickerPalette.init(colors.size(), 6, new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                currentColor = color;
                colorPickerPalette.drawPalette(clr,color);
            }
        });

        colorPickerPalette.drawPalette(clr,this.poi.getColor());

        currentColor = poi.getColor();

    }

    public static int[] toPrimitive(Integer[] IntegerArray) {

        int[] result = new int[IntegerArray.length];
        for (int i = 0; i < IntegerArray.length; i++) {
            result[i] = IntegerArray[i].intValue();
        }
        return result;
    }

    //Needed to show buttons in toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    //Method to handle clicks on menu in toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ok:
                poi.setName(name.getText().toString());
                poi.setDescription(desc.getText().toString());
                poi.setLongDescription(lngDesc.getText().toString());
                poi.setColor(currentColor);
                poi.setApiCallURL(webAPIurl.getText().toString());
                Dao.CreateOrUpdateStatus status = dao.createOrUpdate(poi);
                if(status.isCreated()){
                    Toast.makeText(this,"poi created",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"poi updatet",Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.cancel:
                finish();
                return true;
            default:return false;
        }
    }

}
