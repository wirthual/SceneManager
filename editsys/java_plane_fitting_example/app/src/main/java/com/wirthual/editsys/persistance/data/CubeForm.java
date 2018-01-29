package com.wirthual.editsys.persistance.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.wirthual.editsys.persistance.data.Form;

import java.io.Serializable;

/**
 * Created by raphael on 10.11.16.
 */

@DatabaseTable(tableName = "cube")
public class CubeForm extends Form implements Serializable {

    @DatabaseField
    private float width,height,depth;


    public CubeForm(){}

    public CubeForm(float width, float height, float depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }
}

