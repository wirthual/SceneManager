package com.wirthual.editsys.persistance.data;

import android.graphics.Color;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Created by raphael on 22.09.16.
 * adapted from https://github.com/j256/ormlite-examples/blob/master/android/HelloAndroid
 */
@DatabaseTable(tableName = "poi")
public class PoiObject implements Serializable {


    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField
    String name;
    @DatabaseField
    String description;
    @DatabaseField
    String longDescription;

    int priority=0; //No database field

    @DatabaseField
    int color = Color.parseColor("#F6402C"); //Default color reddish TODO: What default

    //Vector3 position; //Cannot be stored in db directly --> Use x,y,z
    @DatabaseField
    private double vectX;
    @DatabaseField
    private double vectY;
    @DatabaseField
    private double vectZ;

    //Quaternion rotation; //Orientation of Poi --> Alternative: yaw, pitch, roll (Euler Winkel) Cannot be stored in db directly --> Use w,x,y,z
    @DatabaseField
    private double quatW;
    @DatabaseField
    private double quatX;
    @DatabaseField
    private double quatY;
    @DatabaseField
    private double quatZ;

    @DatabaseField(foreign = true,foreignAutoRefresh = true,foreignAutoCreate = true)
    private PanelForm panelForm;

    @DatabaseField(foreign = true,foreignAutoRefresh = true,foreignAutoCreate = true)
    private CubeForm cubeForm;

    //Activities: Todo: if more items, put in collection (arraylist e.g)
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    String[] activities;

    @DatabaseField
    private String apiCallURL = "";



    //Empty contstructor needed for ORM
    PoiObject() {}

    public PoiObject(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Vector3 getPosition() {
        return new Vector3(this.vectX,this.vectY,this.vectZ);
    }

    public void setPosition(Vector3 position) {
        //this.position = position;
        this.vectX = position.x;
        this.vectY = position.y;
        this.vectZ = position.z;
    }

    public Quaternion getRotation() {
        return new Quaternion(this.quatW,this.quatX,this.quatY,this.quatZ);
    }

    public void setRotation(Quaternion rotation) {
        //this.rotation = rotation;
        this.quatX = rotation.x;
        this.quatY = rotation.y;
        this.quatZ = rotation.z;
        this.quatW = rotation.w;
    }

    public void setPriority(int priority){
        this.priority = priority;
    }

    public int getPriority(){
        return this.priority;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public CubeForm getCubeForm() {
        return cubeForm;
    }

    public void setCubeForm(CubeForm cubeForm) {
        this.cubeForm = cubeForm;
    }

    public PanelForm getPanelForm() {
        return panelForm;
    }

    public void setPanelForm(PanelForm panelForm) {
        this.panelForm = panelForm;
    }

    @Override
    public String toString() {
        Quaternion tmpQuat  = new Quaternion(this.quatW,this.quatX,this.quatY,this.quatZ);
        Vector3 tmpVec      = new Vector3(this.vectX,this.vectY,this.vectZ);



        StringBuilder sb = new StringBuilder();
        sb.append("id=").append(id);
        sb.append(", ").append("name=").append(name);
        sb.append(", ").append("rotation=").append(tmpQuat.toString());
        sb.append(", ").append("position=").append(tmpVec.toString());
        if(cubeForm!=null){
            sb.append(", ").append("cube=").append(cubeForm.toString());
        }
        if(panelForm!=null){
            sb.append(", ").append("panel=").append(panelForm.toString());
        }
        return sb.toString();
    }

    public void setActivities(String[] activities){
        this.activities = activities;
    }

   public String[] getActivities(){
        return this.activities;
   }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getApiCallURL() {
        return apiCallURL;
    }

    public void setApiCallURL(String apiCallURL) {
        this.apiCallURL = apiCallURL;
    }
}

