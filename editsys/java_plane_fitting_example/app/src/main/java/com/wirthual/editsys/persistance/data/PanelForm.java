package com.wirthual.editsys.persistance.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.wirthual.editsys.persistance.data.Form;

import java.io.Serializable;

/**
 * Created by raphael on 10.11.16.
 */

@DatabaseTable(tableName = "panel")
public class PanelForm extends Form implements Serializable {

    @DatabaseField
    private float width,heigth;

    public PanelForm(){}

    public PanelForm(float width,float height){
        this.heigth = height;
        this.width = width;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeigth() {
        return heigth;
    }

    public void setHeigth(float heigth) {
        this.heigth = heigth;
    }
}
