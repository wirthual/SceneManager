/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wirthual.editsys;

import com.google.atap.tangoservice.TangoPoseData;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.wirthual.editsys.persistance.DatabaseHelper;
import com.wirthual.editsys.persistance.data.PoiObject;
import com.wirthual.editsys.events.RedrawObjectsEvent;
import com.wirthual.editsys.ui.stepper.StepperActivity;

import android.content.Context;

import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.RectangularPrism;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

import java.util.List;
import java.util.Stack;

import javax.microedition.khronos.opengles.GL10;

/**
 * Very simple example augmented reality renderer which displays a cube fixed in place.
 * The position of the cube in the OpenGL world is updated using the {@code updateObjectPose}
 * method.
 */
public class PlaneFittingRenderer extends RajawaliRenderer implements OnObjectPickedListener {

    public static final int MOVE_RIGHT=10;
    public static final int MOVE_LEFT=20;
    public static final int MOVE_UP=30;
    public static final int MOVE_DOWN=40;
    private double MOVE_UNIT = 0.1;


    private static final String TAG = PlaneFittingRenderer.class.getSimpleName();

    // Augmented Reality related fields
    private ATexture mTangoCameraTexture;
    private boolean mSceneCameraConfigured;

    // Field to show if new Pois are available, if true --> Render scene again
    private boolean redrawObjects = false;

    //Database Stuff
    private DatabaseHelper helper;
    private RuntimeExceptionDao<PoiObject, Integer> dao;

    //Picker class to handle selection of objects
    private ObjectColorPicker mPicker;

    //Currently selected Object (-1 = no selected)
    private int selectedObjectId =-1;
    private Object3D selecedObject;

    public PlaneFittingRenderer(Context context) {
        super(context);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initScene() {
        // Create a quad covering the whole background and assign a texture to it where the
        // Tango color camera contents will be rendered.
        ScreenQuad backgroundQuad = new ScreenQuad();
        Material tangoCameraMaterial = new Material();
        tangoCameraMaterial.setColorInfluence(0);
        // We need to use Rajawali's {@code StreamingTexture} since it sets up the texture
        // for GL_TEXTURE_EXTERNAL_OES rendering
        mTangoCameraTexture =
                new StreamingTexture("camera", (StreamingTexture.ISurfaceListener) null);
        try {
            tangoCameraMaterial.addTexture(mTangoCameraTexture);
            backgroundQuad.setMaterial(tangoCameraMaterial);
        } catch (ATexture.TextureException e) {
            Log.e(TAG, "Exception creating texture for RGB camera contents", e);
        }
        getCurrentScene().addChildAt(backgroundQuad, 0);

        // Add a directional light in an arbitrary direction.
        DirectionalLight light = new DirectionalLight(1, 0.2, -1);
        light.setColor(1, 1, 1);
        light.setPower(0.8f);
        light.setPosition(3, 2, 4);
        getCurrentScene().addLight(light);
        

        mPicker = new ObjectColorPicker(this);
        mPicker.setOnObjectPickedListener(this);

    }

    @Override
    protected void onRender(long elapsedRealTime, double deltaTime) {
        // Update the AR object if necessary
        // Synchronize against concurrent access with the setter below.
        synchronized (this) {
            if (redrawObjects) {
                Log.i(StepperActivity.class.getName(), "Redraw Objects");
                //Remove all Old Objects
                getCurrentScene().clearChildren();
                //Add light and Background (camera image)
                initScene();
                //Get all Pois and render them with 3DObject
                List<PoiObject> list= dao.queryForAll();
                for (PoiObject obj:list){
                    Object3D mObject = null;
                    Log.i(StepperActivity.class.getName(), "Draw POI"+obj.toString());

                    //First choose if Panel or Cube
                    if(null!=obj.getPanelForm()) { //If panelForm != null, POI is Panel
                        mObject = new Plane(obj.getPanelForm().getWidth(), obj.getPanelForm().getHeigth(), 1, 1);
                    }else if(null!=obj.getCubeForm()){ //If cubeForm != null, POI is Cube (eighter panel or cube should be null)
                        mObject = new RectangularPrism(obj.getCubeForm().getWidth(),obj.getCubeForm().getHeight(),obj.getCubeForm().getDepth());
                    }

                    //Then set Position and Orientation
                    mObject.setPosition(obj.getPosition());
                    Log.i(PlaneFittingRenderer.class.getName(), "Position:"+ String.valueOf(obj.getPosition()));
                    mObject.setRotation(obj.getRotation());
                    Log.i(PlaneFittingRenderer.class.getName(), "Color:"+ String.valueOf(obj.getColor()));

                   //Then set Color
                    Material mat = new Material();
                    mat.setColor(obj.getColor());
                    if(obj.getId()==selectedObjectId){
                        //Make selected Object red
                        mat.setColor(Color.RED);
                    }
                    mObject.setMaterial(mat);
                    mObject.setName(String.valueOf(obj.getId()));

                    //Register for Object Picking
                    mPicker.registerObject(mObject);

                    //Add to scene so Object gets rendererd
                    getCurrentScene().addChild(mObject);
                }
                redrawObjects = false;
            }
        }
        super.onRender(elapsedRealTime, deltaTime);
    }


    /**
     * Update the scene camera based on the provided pose in Tango start of service frame.
     * The camera pose should match the pose of the camera color at the time the last rendered RGB
     * frame, which can be retrieved with this.getTimestamp();
     * <p/>
     * NOTE: This must be called from the OpenGL render thread - it is not thread safe.
     */
    public void updateRenderCameraPose(TangoPoseData cameraPose) {
        float[] rotation = cameraPose.getRotationAsFloats();
        float[] translation = cameraPose.getTranslationAsFloats();
        Quaternion quaternion = new Quaternion(rotation[3], rotation[0], rotation[1], rotation[2]);
        // Conjugating the Quaternion is need because Rajawali uses left handed convention for
        // quaternions.
        getCurrentCamera().setRotation(quaternion.conjugate());
        getCurrentCamera().setPosition(translation[0], translation[1], translation[2]);
    }

    /**
     * It returns the ID currently assigned to the texture where the Tango color camera contents
     * should be rendered.
     * NOTE: This must be called from the OpenGL render thread - it is not thread safe.
     */
    public int getTextureId() {
        return mTangoCameraTexture == null ? -1 : mTangoCameraTexture.getTextureId();
    }

    /**
     * We need to override this method to mark the camera for re-configuration (set proper
     * projection matrix) since it will be reset by Rajawali on surface changes.
     */
    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);
        mSceneCameraConfigured = false;
    }

    public boolean isSceneCameraConfigured() {
        return mSceneCameraConfigured;
    }

    /**
     * Sets the projection matrix for the scene camera to match the parameters of the color camera,
     * provided by the {@code TangoCameraIntrinsics}.
     */
    public void setProjectionMatrix(float[] matrix) {
        getCurrentCamera().setProjectionMatrix(new Matrix4(matrix));
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset,
                                 float xOffsetStep, float yOffsetStep,
                                 int xPixelOffset, int yPixelOffset) {
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        //Handled by activity
    }




    //Recieve Message that new Pois were created or deleted
    @Subscribe
    public synchronized void onMessageEvent(RedrawObjectsEvent event) {
        redrawObjects = true;
        Log.i(PlaneFittingRenderer.class.getName(),"message for redraw Objects received");
    }

    //Method called from Activity --> if mPicker finds object onObjectPicked-method is called
    public void getObjectAt(float x, float y) {
        mPicker.getObjectAt(x, y);
    }

    @Override
    public void onObjectPicked(Object3D object) {
        selectedObjectId = Integer.valueOf(object.getName());
        selecedObject = object;
        redrawObjects = true;
        ((PlaneFittingActivity)mContext).objectIsSelected(true);
    }

    //If object is deselected, redraw scene so normal color is used instead of color that indicates selection
    public void onObjectUnpicked(){
        selectedObjectId = -1;
        redrawObjects = true;
        ((PlaneFittingActivity)mContext).objectIsSelected(false);
    }

    public void moveSelectedObject(int move){
        int id = Integer.valueOf(selectedObjectId);
        switch (move){
            case MOVE_DOWN:selecedObject.moveUp(-MOVE_UNIT);break;
            case MOVE_UP:selecedObject.moveUp(MOVE_UNIT);break;
            case MOVE_LEFT:selecedObject.moveRight(-MOVE_UNIT);break;
            case MOVE_RIGHT:selecedObject.moveRight(MOVE_UNIT);break;
            default:break;
        }
        PoiObject obj = dao.queryForId(id);
        obj.setPosition(selecedObject.getPosition());
        dao.update(obj);
    }

    public void rotateSelectedObject(Vector3.Axis axis,int value){
        int id = Integer.valueOf(selectedObjectId);
        switch (axis){
            case X:selecedObject.rotate(Vector3.Axis.X,selecedObject.getRotX()+value);break;
            case Y:selecedObject.rotate(Vector3.Axis.Y,selecedObject.getRotY()+value);break;
            case Z:selecedObject.rotate(Vector3.Axis.Z,selecedObject.getRotZ()+value);break;
        }
        PoiObject obj = dao.queryForId(id);
        obj.setRotation(selecedObject.getOrientation());
        dao.update(obj);
    }

    public void deleteSelectedObject(){
        if(selectedObjectId !=-1){
            int id = Integer.valueOf(selectedObjectId);
            dao.deleteById(id);
            Log.i(PlaneFittingRenderer.class.getName(),"deleted selected Object with id: "+ String.valueOf(selectedObjectId));
            selectedObjectId =-1;
        }else{
            Log.i(PlaneFittingRenderer.class.getName(),"Received deleteSelectedObject but no Object selected. Do nothing.");
        }
    }

    //Returns id of PoiObject of current selected, -1 if no POI is selected
    public int getSelectedObject(){
            return selectedObjectId;
    }

    public void setHelper(DatabaseHelper helper){
        this.helper =helper;
        dao = this.helper.getPoiObjectDao();
    }
}
