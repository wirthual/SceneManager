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

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoException;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import android.content.Intent;
import android.opengl.Matrix;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.ASceneFrameCallback;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.table.TableUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.wirthual.editsys.adfmanager.DownloadAcitvity;
import com.wirthual.editsys.adfmanager.UploadAcitvity;
import com.wirthual.editsys.events.SceneDownloadDone;
import com.wirthual.editsys.persistance.data.CubeForm;
import com.wirthual.editsys.persistance.data.PanelForm;
import com.wirthual.editsys.persistance.data.PoiObject;
import com.wirthual.editsys.events.RedrawObjectsEvent;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.projecttango.tangosupport.TangoSupport;
import com.projecttango.tangosupport.TangoSupport.IntersectionPointPlaneModelPair;

import com.wirthual.editsys.persistance.*;
import com.wirthual.editsys.ui.stepper.NoStepperActivity;
import com.wirthual.editsys.ui.stepper.StepperActivity;

/**
 * An example showing how to use the Tango APIs to create an augmented reality application
 * that uses depth perception to detect flat surfaces on the real world.
 * This example displays a cube in space. whenever the user clicks on the screen, the cube is placed
 * flush with the surface detected with the depth camera in the position clicked.
 * <p/>
 * This example uses Rajawali for the OpenGL rendering. This includes the color camera image in the
 * background and the cube with instructions positioned in space or in the last surface detected.
 * This part is implemented in the {@code AugmentedRealityRenderer} class, like a regular Rajawali
 * application.
 * <p/>
 * This example focuses on using the depth sensor data to detect a plane and position it on the
 * corresponding position in the 3D OpenGL space.
 * <p/>
 * For more details on the augmented reality effects, including color camera texture rendering,
 * see java_augmented_reality_example or java_hello_video_example.
 * <p/>
 * Note that it is important to include the KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION
 * configuration parameter in order to achieve best results synchronizing the
 * Rajawali virtual world with the RGB camera.
 */
public class PlaneFittingActivity extends AppCompatActivity implements View.OnTouchListener {
    private static final String TAG = PlaneFittingActivity.class.getSimpleName();
    private static final int INVALID_TEXTURE_ID = 0;

    //Gui Components
    RelativeLayout arWrapper;
    LinearLayout infoView;
    LinearLayout editView;
    TextView uuidTxt;
    TextView emptyText;
    ProgressBar waitSpiner;
    TextView databaseCount;
    Toolbar myToolbar;
    Spinner sItems;
    Spinner sAxis;


    //Misc
    boolean surfaceAdded = false;
    String uuid;
    long timeTouchDown =0;
    long timeTouchUp =0;
    private RajawaliSurfaceView mSurfaceView;
    private PlaneFittingRenderer mRenderer;
    private TangoCameraIntrinsics mIntrinsics;
    private TangoPointCloudManager mPointCloudManager;
    private Tango mTango;
    private TangoConfig mConfig;
    private boolean mIsConnected = false;
    private double mCameraPoseTimestamp = 0;
    //Database Stuff
    private DatabaseHelper helper;
    private RuntimeExceptionDao<PoiObject, Integer> dao;
    private RuntimeExceptionDao<PanelForm, Integer> panelDao;
    private RuntimeExceptionDao<CubeForm, Integer> cubeDao;

    // Texture rendering related fields
    // NOTE: Naming indicates which thread is in charge of updating this variable
    private int mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
    private AtomicBoolean mIsFrameAvailableTangoThread = new AtomicBoolean(false);
    private double mRgbTimestampGlThread;


    //Intent Stuff for Importing and Exporting ADFs
    private static final String INTENT_CLASSPACKAGE = "com.projecttango.tango";
    private static final String INTENT_IMPORTEXPORT_CLASSNAME = "com.google.atap.tango.RequestImportExportActivity";
    // startActivityForResult requires a code number.
    private static final String EXTRA_KEY_SOURCEUUID = "SOURCE_UUID";
    private static final String EXTRA_KEY_DESTINATIONFILE = "DESTINATION_FILE";
    private static final String EXTRA_KEY_SOURCEFILE = "SOURCE_FILE";
    //Identifier for OnActivityresult to distinquish between import and export intent
    private static final int INTENT_ACTIVITY_CODE_IMPORT = 0x3543;
    private static final int INTENT_ACTIVITY_CODE_EXPORT = 0x3544;

    private static final Matrix4 DEPTH_T_OPENGL = new Matrix4(new float[] {
            1.0f,  0.0f, 0.0f, 0.0f,
            0.0f,  0.0f, 1.0f, 0.0f,
            0.0f, -1.0f, 0.0f, 0.0f,
            0.0f,  0.0f, 0.0f, 1.0f
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "- onCreate()");
        setContentView(R.layout.activity_main);

        //Read default values from settings file: Happens exactly one time --> because readAgain is false
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.settings,false);
        //Get Permission for Tango service --> Calls on ActivityResult
        startActivityForResult(
                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE),
                Tango.TANGO_INTENT_ACTIVITYCODE);

        //Setup surfaceView with rajawali renderer
        mSurfaceView = new RajawaliSurfaceView(this);
        mRenderer = new PlaneFittingRenderer(this);
        mSurfaceView.setSurfaceRenderer(mRenderer);
        mSurfaceView.setOnTouchListener(this);

        mPointCloudManager = new TangoPointCloudManager();

        //Setup Gui elements
        arWrapper = (RelativeLayout) findViewById(R.id.ar_wrapper);
        uuidTxt = (TextView) findViewById(R.id.current_uuid);
        databaseCount = (TextView) findViewById(R.id.databaseCount);

        editView = (LinearLayout)findViewById(R.id.edit_view);
        editView.setVisibility(View.INVISIBLE);

        infoView = (LinearLayout)findViewById(R.id.info_view);
        infoView.setVisibility(View.GONE);

        emptyText = (TextView)findViewById(R.id.emptyText);
        waitSpiner = (ProgressBar)findViewById(R.id.progressBarWait);
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        sItems = (Spinner)findViewById(R.id.dimension);
        sAxis = (Spinner)findViewById(R.id.axis);

        List<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add(getResources().getString(R.string.xaxis));
        spinnerArray.add(getResources().getString(R.string.yaxis));
        spinnerArray.add(getResources().getString(R.string.zaxis));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sAxis = (Spinner) findViewById(R.id.axis);
        sAxis.setAdapter(adapter);


        //Register this class for event bus so it recieves Events (Eventbus = library)
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "- onResume()");
        //Get current uuid from preferences
        uuid = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("uuid","notLoaded");
        uuidTxt.setText(uuid);

        if(Objects.equals(uuid, "notLoaded")) {
            //If no uuid is available, do nothing.
            emptyText.setText(R.string.noAdfLoaded);
            waitSpiner.setVisibility(View.GONE);
            Toast.makeText(this,"No ADF File is loaded. Please select one",Toast.LENGTH_LONG).show();
        }else {
            //If no uuid is selected, show progress bar and info to move around.
            emptyText.setText(R.string.waitForLoc);
            waitSpiner.setVisibility(View.VISIBLE);
            helper = new DatabaseHelper(this, uuid);
            dao = helper.getPoiObjectDao();
            panelDao = helper.getPanelDao();
            cubeDao = helper.getCubeDao();
            databaseCount.setText(String.valueOf(dao.countOf()));
            mRenderer.setHelper(helper);
            if (!mIsConnected) {
                mTango = new Tango(PlaneFittingActivity.this, new Runnable() {
                    // Pass in a Runnable to be called from UI thread when Tango is ready, this Runnable
                    // will be running on a new thread.
                    // When Tango is ready, we can call Tango functions safely here only when there is no UI
                    // thread changes involved.
                    @Override
                    public void run() {
                        // Synchronize against disconnecting while the service is being used in the OpenGL
                        // thread or in the UI thread.
                        synchronized (PlaneFittingActivity.this) {
                            try {
                                TangoSupport.initialize();
                                mConfig = setupTangoConfig(mTango);
                                mTango.connect(mConfig);
                                mIsConnected = true;
                                startupTango();
                                connectRenderer();
                            } catch (TangoOutOfDateException e) {
                                Log.e(TAG, getString(R.string.exception_out_of_date), e);
                            } catch (TangoErrorException e) {
                                Log.e(TAG, getString(R.string.exception_tango_error), e);
                            } catch (TangoInvalidException e) {
                                Log.e(TAG, getString(R.string.exception_tango_invalid), e);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "- onPause()");
        if(mIsConnected) {
            arWrapper.removeView(mSurfaceView);
            surfaceAdded = false;
            // Synchronize against disconnecting while the service is being used in the OpenGL thread or
            // in the UI thread.
            synchronized (this) {
                try {
                    mRenderer.getCurrentScene().clearFrameCallbacks();
                    mTango.disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
                    // We need to invalidate the connected texture ID so that we cause a re-connection
                    // in the OpenGL thread after resume
                    mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
                    mTango.disconnect();
                    mIsConnected = false;
                } catch (TangoErrorException e) {
                    Log.e(TAG, getString(R.string.exception_tango_error), e);
                }
            }
        }
    }

    /**
     * Sets up the tango configuration object. Make sure mTango object is initialized before
     * making this call.
     */
    private TangoConfig setupTangoConfig(Tango tango) {
        // Use default configuration for Tango Service (motion tracking), plus low latency
        // IMU integration, color camera, depth and drift correction.
        TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION, uuid);
        // NOTE: Low latency integration is necessary to achieve a precise alignment of
        // virtual objects with the RBG image and produce a good AR effect.
        config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        config.putInt(TangoConfig.KEY_INT_DEPTH_MODE, TangoConfig.TANGO_DEPTH_MODE_POINT_CLOUD);

        return config;
    }

    /**
     * Set up the callback listeners for the Tango service and obtain other parameters required
     * after Tango connection.
     * Listen to updates from the RGB camera and Point Cloud.
     */
    private void startupTango() {
        // No need to add any coordinate frame pairs since we are not
        // using pose data. So just initialize.
        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                TangoPoseData.COORDINATE_FRAME_DEVICE));
        mTango.connectListener(framePairs, new OnTangoUpdateListener() {

            @Override
            public void onPoseAvailable(TangoPoseData pose) {
                if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION
                        && pose.targetFrame == TangoPoseData
                        .COORDINATE_FRAME_DEVICE) {
                    if (pose.statusCode == TangoPoseData.POSE_VALID) {
                        if(!surfaceAdded){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addSurface();
                            }
                        });}
                    } else if (pose.statusCode == TangoPoseData.POSE_INVALID) {
                        Log.d(PlaneFittingActivity.class.getName(), "Localisation not ok");
                    }
                }
            }

            @Override
            public void onFrameAvailable(int cameraId) {
                // Check if the frame available is for the camera we want and update its frame
                // on the view.
                if (cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR) {
                    // Mark a camera frame is available for rendering in the OpenGL thread
                    mIsFrameAvailableTangoThread.set(true);
                    mSurfaceView.requestRender();
                }
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                // We are not using onXyzIjAvailable for this app.
            }

            @Override
            public void onPointCloudAvailable(TangoPointCloudData pointCloud) {
                // Save the cloud and point data for later use.
                mPointCloudManager.updatePointCloud(pointCloud);
            }

            @Override
            public void onTangoEvent(TangoEvent event) {
                // We are not using OnPoseAvailable for this app.
            }
        });

        // Obtain the intrinsic parameters of the color camera.
        mIntrinsics = mTango.getCameraIntrinsics(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
    }

    /**
     * Use Tango camera intrinsics to calculate the projection Matrix for the Rajawali scene.
     */
    private static float[] projectionMatrixFromCameraIntrinsics(TangoCameraIntrinsics intrinsics) {
        // Uses frustumM to create a projection matrix taking into account calibrated camera
        // intrinsic parameter.
        // Reference: http://ksimek.github.io/2013/06/03/calibrated_cameras_in_opengl/
        float near = 0.1f;
        float far = 100;

        float xScale = near / (float) intrinsics.fx;
        float yScale = near / (float) intrinsics.fy;
        float xOffset = (float) (intrinsics.cx - (intrinsics.width / 2.0)) * xScale;
        // Color camera's coordinates has y pointing downwards so we negate this term.
        float yOffset = (float) -(intrinsics.cy - (intrinsics.height / 2.0)) * yScale;

        float m[] = new float[16];
        Matrix.frustumM(m, 0,
                xScale * (float) -intrinsics.width / 2.0f - xOffset,
                xScale * (float) intrinsics.width / 2.0f - xOffset,
                yScale * (float) -intrinsics.height / 2.0f - yOffset,
                yScale * (float) intrinsics.height / 2.0f - yOffset,
                near, far);
        return m;
    }


    /**
     * Connects the view and renderer to the color camara and callbacks.
     */
    private void connectRenderer() {
        // Register a Rajawali Scene Frame Callback to update the scene camera pose whenever a new
        // RGB frame is rendered.
        // (@see https://github.com/Rajawali/Rajawali/wiki/Scene-Frame-Callbacks)
        mRenderer.getCurrentScene().registerFrameCallback(new ASceneFrameCallback() {
            @Override
            public void onPreFrame(long sceneTime, double deltaTime) {
                // NOTE: This is called from the OpenGL render thread, after all the renderer
                // onRender callbacks had a chance to run and before scene objects are rendered
                // into the scene.

                try {
                    synchronized (PlaneFittingActivity.this) {
                        // Don't execute any tango API actions if we're not connected to the service
                        if (!mIsConnected) {
                            return;
                        }

                        // Set-up scene camera projection to match RGB camera intrinsics
                        if (!mRenderer.isSceneCameraConfigured()) {
                            mRenderer.setProjectionMatrix(
                                    projectionMatrixFromCameraIntrinsics(mIntrinsics));
                        }

                        // Connect the camera texture to the OpenGL Texture if necessary
                        // NOTE: When the OpenGL context is recycled, Rajawali may re-generate the
                        // texture with a different ID.
                        if (mConnectedTextureIdGlThread != mRenderer.getTextureId()) {
                            mTango.connectTextureId(TangoCameraIntrinsics.TANGO_CAMERA_COLOR,
                                    mRenderer.getTextureId());
                            mConnectedTextureIdGlThread = mRenderer.getTextureId();
                            Log.d(TAG, "connected to texture id: " + mRenderer.getTextureId());
                        }

                        // If there is a new RGB camera frame available, update the texture with it
                        if (mIsFrameAvailableTangoThread.compareAndSet(true, false)) {
                            mRgbTimestampGlThread =
                                    mTango.updateTexture(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
                        }

                        if (mRgbTimestampGlThread > mCameraPoseTimestamp) {
                            // Calculate the camera color pose at the camera frame update time in
                            // OpenGL engine.
                            //
                            // When drift correction mode is enabled in config file, we need
                            // to query the device with respect to Area Description pose in
                            // order to use the drift corrected pose.
                            //
                            // Note that if you don't want to use the drift corrected pose, the
                            // normal device with respect to start of service pose is still
                            // available.
                            TangoPoseData lastFramePose = TangoSupport.getPoseAtTime(
                                    mRgbTimestampGlThread,
                                    TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                                    TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR,
                                    TangoSupport.TANGO_SUPPORT_ENGINE_OPENGL, 0);
                            if (lastFramePose.statusCode == TangoPoseData.POSE_VALID) {
                                // Update the camera pose from the renderer
                                mRenderer.updateRenderCameraPose(lastFramePose);
                                mCameraPoseTimestamp = lastFramePose.timestamp;
                            } else {
                                // When the pose status is not valid, it indicates the tracking has
                                // been lost. In this case, we simply stop rendering.
                                // This is also the place to display UI to suggest the user walk
                                // to recover tracking.
                                Log.w(TAG, "Can't get device pose at time: " +
                                        mRgbTimestampGlThread);
                            }
                        }
                    }
                    // Avoid crashing the application due to unhandled exceptions
                } catch (TangoErrorException e) {
                    Log.e(TAG, "Tango API call error within the OpenGL render thread", e);
                } catch (Throwable t) {
                    Log.e(TAG, "Exception on the OpenGL thread", t);
                }
            }

            @Override
            public void onPreDraw(long sceneTime, double deltaTime) {

            }

            @Override
            public void onPostFrame(long sceneTime, double deltaTime) {

            }

            @Override
            public boolean callPreFrame() {
                return true;
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) { //Todo: Move to renderer?
        Log.d(TAG, "+ onTouchEvent(event:" + motionEvent + ")");
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
            timeTouchDown = motionEvent.getEventTime();
        }
        if(motionEvent.getAction() == MotionEvent.ACTION_UP){
            mRenderer.onObjectUnpicked(); //On Every up deselect all
            timeTouchUp = motionEvent.getEventTime();
            long deltaTime = timeTouchUp - timeTouchDown;
            if(deltaTime>=1000){
                Log.d(TAG,"Long press detected!");
                mRenderer.getObjectAt(motionEvent.getX(), motionEvent.getY());
            }else{
                Log.d(TAG,"Short press detected!");
                float u = motionEvent.getX() / view.getWidth();
                float v = motionEvent.getY() / view.getHeight();

                try {
                    // Fit a plane on the clicked point using the latest poiont cloud data
                    // Synchronize against concurrent access to the RGB timestamp in the OpenGL thread
                    synchronized (this) {
                       doFitPlane(u, v, mRgbTimestampGlThread);
                    }
                } catch (TangoException t) {
                    Toast.makeText(getApplicationContext(),
                            R.string.failed_measurement,
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, getString(R.string.failed_measurement), t);
                } catch (SecurityException t) {
                    Toast.makeText(getApplicationContext(),
                            R.string.failed_permissions,
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, getString(R.string.failed_permissions), t);
                }
            }
        }
        Log.d(TAG, "- onTouchEvent()");
        return true;
    }

    /**
     * Use the TangoSupport library with point cloud data to calculate the plane
     * of the world feature pointed at the location the camera is looking.
     * It returns the transform of the fitted plane in a double array.
     */
    private void doFitPlane(float u, float v, double rgbTimestamp) {
        TangoPointCloudData pointCloud = mPointCloudManager.getLatestPointCloud();

        if (pointCloud != null) {

            TangoPoseData colorTdepthPose = TangoSupport.calculateRelativePose(
                    rgbTimestamp, TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR,
                    pointCloud.timestamp, TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH);

            // Perform plane fitting with the latest available point cloud data.
            IntersectionPointPlaneModelPair intersectionPointPlaneModelPair =
                    TangoSupport.fitPlaneModelNearPoint(pointCloud,
                            colorTdepthPose, u, v);

            // Get the transform from depth camera to OpenGL world at the timestamp of the cloud.
            TangoSupport.TangoMatrixTransformData transform =
                    TangoSupport.getMatrixTransformAtTime(pointCloud.timestamp,
                            TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                            TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH,
                            TangoSupport.TANGO_SUPPORT_ENGINE_OPENGL,
                            TangoSupport.TANGO_SUPPORT_ENGINE_TANGO,0);
            if (transform.statusCode == TangoPoseData.POSE_VALID) {
                float[] openGlTPlane = calculatePlaneTransform(
                        intersectionPointPlaneModelPair.intersectionPoint,
                        intersectionPointPlaneModelPair.planeModel, transform.matrix);
                Log.d(TAG, String.valueOf(intersectionPointPlaneModelPair.intersectionPoint[0]) +
                                " " + String.valueOf(intersectionPointPlaneModelPair.intersectionPoint[1] +
                                String.valueOf(intersectionPointPlaneModelPair.intersectionPoint[2]))
                        );

                Matrix4 m = new Matrix4(openGlTPlane);

                //Create Poi Object and save position and orientation
                PoiObject a = new PoiObject("");
                a.setPosition(m.getTranslation());
                a.setRotation(new Quaternion().fromMatrix(new Matrix4(openGlTPlane)).conjugate());

                //Pass Object to StepperActivity to set Name,Description,Color etc. if Object was created,
                //StepperActivity sends RedrawObjectsEvent
                Intent i = new Intent(this, StepperActivity.class);
                i.putExtra("poi", a);
                startActivity(i);

            } else {
                Log.w(TAG, "Can't get depth camera transform at time " + pointCloud.timestamp);
            }
        }
    }

    /**
     * Calculate the pose of the plane based on the position and normal orientation of the plane
     * and align it with gravity.
     */
    private float[] calculatePlaneTransform(double[] point, double normal[],
                                            float[] openGlTdepth) {
        // Vector aligned to gravity.
        float[] openGlUp = new float[]{0, 1, 0, 0};
        float[] depthTOpenGl = new float[16];
        Matrix.invertM(depthTOpenGl, 0, openGlTdepth, 0);
        float[] depthUp = new float[4];
        Matrix.multiplyMV(depthUp, 0, depthTOpenGl, 0, openGlUp, 0);
        // Create the plane matrix transform in depth frame from a point, the plane normal and the
        // up vector.
        float[] depthTplane = matrixFromPointNormalUp(point, normal, depthUp);
        float[] openGlTplane = new float[16];
        Matrix.multiplyMM(openGlTplane, 0, openGlTdepth, 0, depthTplane, 0);
        return openGlTplane;
    }

    /**
     * Calculates a transformation matrix based on a point, a normal and the up gravity vector.
     * The coordinate frame of the target transformation will a right handed system with Z+ in
     * the direction of the normal and Y+ up.
     */
    private float[] matrixFromPointNormalUp(double[] point, double[] normal, float[] up) {
        float[] zAxis = new float[]{(float) normal[0], (float) normal[1], (float) normal[2]};
        normalize(zAxis);
        float[] xAxis = crossProduct(up, zAxis);
        normalize(xAxis);
        float[] yAxis = crossProduct(zAxis, xAxis);
        normalize(yAxis);
        float[] m = new float[16];
        Matrix.setIdentityM(m, 0);
        m[0] = xAxis[0];
        m[1] = xAxis[1];
        m[2] = xAxis[2];
        m[4] = yAxis[0];
        m[5] = yAxis[1];
        m[6] = yAxis[2];
        m[8] = zAxis[0];
        m[9] = zAxis[1];
        m[10] = zAxis[2];
        m[12] = (float) point[0];
        m[13] = (float) point[1];
        m[14] = (float) point[2];
        return m;
    }

    /**
     * Normalize a vector.
     */
    private void normalize(float[] v) {
        double norm = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] /= norm;
        v[1] /= norm;
        v[2] /= norm;
    }

    /**
     * Cross product between two vectors following the right hand rule.
     */
    private float[] crossProduct(float[] v1, float[] v2) {
        float[] result = new float[3];
        result[0] = v1[1] * v2[2] - v2[1] * v1[2];
        result[1] = v1[2] * v2[0] - v2[2] * v1[0];
        result[2] = v1[0] * v2[1] - v2[0] * v1[1];
        return result;
    }


    //Needed to show buttons in toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    //Show menu items depending on object selected or not (eg. hide delete icon)
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean objectSelected = mRenderer.getSelectedObject()!=-1;
        menu.findItem(R.id.action_download).setVisible(!objectSelected);
        menu.findItem(R.id.action_upload).setVisible(!objectSelected);
        menu.findItem(R.id.action_loadNewAdf).setVisible(!objectSelected);
        menu.findItem(R.id.action_cleanDb).setVisible(!objectSelected);;
        menu.findItem(R.id.action_settings).setVisible(!objectSelected);;
        menu.findItem(R.id.action_showHideInfo).setVisible(!objectSelected);
        menu.findItem(R.id.action_exportJson).setVisible(!objectSelected);;
        menu.findItem(R.id.action_deleteSelectedObject).setVisible(objectSelected);
        menu.findItem(R.id.action_editSelectedObject).setVisible(objectSelected);
        menu.findItem(R.id.action_cancelSelection).setVisible(objectSelected);
        menu.findItem(R.id.action_triggerFunction).setVisible(objectSelected);
        return super.onPrepareOptionsMenu(menu);
    }

    //Method to handle clicks on menu in toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.action_upload:
                createUploadFolderIfNotExits();
                Intent exportIntent = new Intent();
                exportIntent.setClassName(INTENT_CLASSPACKAGE, INTENT_IMPORTEXPORT_CLASSNAME);
                exportIntent.putExtra(EXTRA_KEY_SOURCEUUID, uuid);
                exportIntent.putExtra(EXTRA_KEY_DESTINATIONFILE,getExternalFilesDir(null).getAbsolutePath()+"/Upload/");
                Toast.makeText(this,"try to export adf to:"+getExternalFilesDir(null).getAbsolutePath()+"/Upload/",Toast.LENGTH_LONG).show();
                this.startActivityForResult(exportIntent, INTENT_ACTIVITY_CODE_EXPORT);
                return true;
            case R.id.action_download:
                Intent i3 = new Intent(this, DownloadAcitvity.class);
                startActivity(i3);
                return true;
            case R.id.action_cleanDb:
                onDeleteClicked();
                return true;
            case R.id.action_showHideInfo:
                infoView.setVisibility(infoView.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
                return true;
            case R.id.action_exportJson:
                this.exportPoisToJson();
                return true;
            case R.id.action_deleteSelectedObject:
                mRenderer.deleteSelectedObject();
                this.objectIsSelected(false);
                EventBus.getDefault().post(new RedrawObjectsEvent());
                return true;
            case R.id.action_editSelectedObject:
                Intent intent = new Intent(this, StepperActivity.class);
                int selectedObjectId = mRenderer.getSelectedObject();
                PoiObject poi = dao.queryForId(selectedObjectId);
                intent.putExtra("poi",poi);
                startActivity(intent);
                return true;
            case R.id.action_cancelSelection:
                this.objectIsSelected(false);
                mRenderer.onObjectUnpicked();
                return true;
            case R.id.action_loadNewAdf:
                Intent i4 = new Intent(this, StartActivity.class);
                startActivity(i4);
                return true;
            case R.id.action_triggerFunction:
                AsyncHttpClient client = new AsyncHttpClient();
                int selectedObjectId2 = mRenderer.getSelectedObject();
                final PoiObject poi2 = dao.queryForId(selectedObjectId2);
                String webApiCallUrl = poi2.getApiCallURL();
                if(!URLUtil.isValidUrl(webApiCallUrl)){
                    Toast.makeText(getApplicationContext(),"Invalid URL!",Toast.LENGTH_SHORT).show();
                    return true;
                }
                client.get(poi2.getApiCallURL(), new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                        Toast.makeText(getApplicationContext(),"WebAPICall to"+poi2.getApiCallURL()+"successfull!",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getApplicationContext(),"WebAPICall to"+poi2.getApiCallURL()+"failed!",Toast.LENGTH_SHORT).show();
                    }
                });

                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void exportPoisToJson() {
        JSONArray arr = new JSONArray();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
        for (PoiObject obj:dao.queryForAll()){
            JSONObject j = new JSONObject(gson.toJson(obj));
            arr.put(j);
        }
        File file = new File(this.getExternalFilesDir(null), uuid+".json");
        Log.d(TAG,"Write JSON file: "+file.getAbsolutePath());

            FileOutputStream fileOutput = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fileOutput);
            outputStreamWriter.write(arr.toString());
            outputStreamWriter.flush();
            fileOutput.getFD().sync();
            outputStreamWriter.close();
        }catch (IOException e){
            Log.e(TAG,"Error writing Json");
            Toast.makeText(this,"Error writing Json",Toast.LENGTH_SHORT).show();
        }catch (JSONException ex){
            Log.e(TAG,"Error converting to Json");
            Toast.makeText(this,"Error converting to Json",Toast.LENGTH_SHORT).show();
        }
    }


    public void onDeleteClicked() {
        try {
            TableUtils.clearTable(helper.getConnectionSource(), PoiObject.class);
            Toast.makeText(this, "Database Deleted", Toast.LENGTH_SHORT).show();
            EventBus.getDefault().post(new RedrawObjectsEvent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addSurface() {
        Log.d(getClass().getName(),"AddSurfaceIsCalled: "+String.valueOf(surfaceAdded));
        if (!surfaceAdded) {
            arWrapper.addView(mSurfaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            surfaceAdded = true;
            EventBus.getDefault().post(new RedrawObjectsEvent());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Toast.makeText(this,String.valueOf(requestCode)+" "+String.valueOf(resultCode),Toast.LENGTH_SHORT).show();
        if (requestCode == INTENT_ACTIVITY_CODE_EXPORT) {
            // Make sure the request was successful
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cannot export without permission", Toast.LENGTH_SHORT).show();
            }else{
                Intent i2 = new Intent(this, UploadAcitvity.class);
                i2.putExtra("uuid",uuid);
                startActivity(i2);
            }
        }
        if (requestCode == Tango.TANGO_INTENT_ACTIVITYCODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cannot run application without permission", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
        if (requestCode == INTENT_ACTIVITY_CODE_IMPORT) {
            // Make sure the request was successful
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cannot import ADF File without permission", Toast.LENGTH_SHORT).show();
            }else{
                helper = new DatabaseHelper(this,uuid);
                dao = helper.getPoiObjectDao();
                databaseCount.setText(String.valueOf(dao.countOf()));
            }
        }
    }

    //If Files from ADF-Manager are Downloaded, Acitivy receives this event and imports downloaded ADF-File
    @Subscribe
    public void onMessageEvent(SceneDownloadDone event) {
        uuid = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("uuid","notLoaded");

        Log.d(this.getClass().getName(),"SceneDownloadDone-Event received: Start importing "+uuid);

        Intent importIntent = new Intent();
        importIntent.setClassName(INTENT_CLASSPACKAGE, INTENT_IMPORTEXPORT_CLASSNAME);
        importIntent.putExtra(EXTRA_KEY_SOURCEFILE, getExternalFilesDir(null)+"/Download/"+uuid);
        this.startActivityForResult(importIntent, INTENT_ACTIVITY_CODE_IMPORT);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "- onStop()");
    }

    public void objectIsSelected(final boolean selected){
        this.invalidateOptionsMenu();
        findViewById(R.id.edit_view).post(new Runnable()
        {
            @Override
            public void run()
            {
                int tmp = selected ? View.VISIBLE : View.INVISIBLE;
                editView.bringToFront();
                editView.setVisibility(tmp);
            }
        });
        findViewById(R.id.dimension).post(new Runnable() {
            @Override
            public void run() {
                if(selected) {
                    List<String> spinnerArray = new ArrayList<String>();
                    spinnerArray.add(getResources().getString(R.string.height));
                    spinnerArray.add(getResources().getString(R.string.width));
                    if (dao.queryForId(mRenderer.getSelectedObject()).getCubeForm() != null) {
                        spinnerArray.add(getResources().getString(R.string.depth));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray);

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    Spinner sItems = (Spinner) findViewById(R.id.dimension);
                    sItems.setAdapter(adapter);
                }
            }
        });

    }

    protected void createUploadFolderIfNotExits(){
        String uploadFolder = "Upload";

        File f = new File(this.getExternalFilesDir(null), uploadFolder);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    //Method vor Edit-Buttons on right side if object is seleced(declared in xml activty_main.xml)
    public void sizeControlPressed(View v){
        PoiObject selected = dao.queryForId(mRenderer.getSelectedObject());

        String dimension = sItems.getSelectedItem().toString();
        Log.d(TAG,"dimension:"+ dimension);

        float h = 0.0f;
        float w = 0.0f;
        float d = 0.0f;
        if(selected.getCubeForm()!=null){
            CubeForm form = selected.getCubeForm();
            h = form.getHeight();
            w = form.getWidth();
            d = form.getDepth();
            if (dimension.equals(getResources().getString(R.string.height))) {
                Log.d(TAG,"Height");
                if(v.getId()==R.id.btn_minus){h = h - 0.1f;}else{h = h + 0.1f;}
                form.setHeight(h);
            }
            if (dimension.equals(getResources().getString(R.string.width))) {
                Log.d(TAG,"Width");
                if(v.getId()==R.id.btn_minus){w = w - 0.1f;}else{w = w + 0.1f;}
                form.setWidth(w);
            }
            if (dimension.equals(getResources().getString(R.string.depth))) {
                Log.d(TAG,"Depth");
                if(v.getId()==R.id.btn_minus){d = d - 0.1f;}else{d = d + 0.1f;}
                form.setDepth(d);
            }
            cubeDao.update(form);
            dao.update(selected);
            Log.d(TAG,"Updated Cube:"+selected.toString());
            Log.d(TAG,"Cube height:"+String.valueOf(selected.getCubeForm().getHeight()));
            Log.d(TAG,"Cube width:"+String.valueOf(selected.getCubeForm().getWidth()));
            Log.d(TAG,"Cube depth:"+String.valueOf(selected.getCubeForm().getDepth()));

        }else{
            PanelForm form = selected.getPanelForm();
            h = selected.getPanelForm().getHeigth();
            w = selected.getPanelForm().getWidth();
            if (dimension.equals(getResources().getString(R.string.height))) {
                Log.d(TAG,"Height");
                if(v.getId()==R.id.btn_minus){h = h - 0.1f;}else{h = h + 0.1f;}
                form.setHeigth(h);
            }
            if (dimension.equals(getResources().getString(R.string.width))) {
                Log.d(TAG,"Width");
                if(v.getId()==R.id.btn_minus){w = w - 0.1f;}else{w = w + 0.1f;}
                form.setWidth(w);
            }
            panelDao.update(form);
            dao.update(selected);
            Log.d(TAG,"Updated Panel:"+selected.toString());
            Log.d(TAG,"Panel height:"+String.valueOf(selected.getPanelForm().getHeigth()));
            Log.d(TAG,"Panel width:"+String.valueOf(selected.getPanelForm().getWidth()));
        }
        EventBus.getDefault().post(new RedrawObjectsEvent());
    }
    //Method vor Edit-Buttons on right side if object is seleced(declared in xml activty_main.xml)
    public void positionControlPressed(View v){

            switch (v.getId()) {
                case R.id.btn_right:
                    mRenderer.moveSelectedObject(PlaneFittingRenderer.MOVE_RIGHT);
                    break;
                case R.id.btn_left:
                    mRenderer.moveSelectedObject(PlaneFittingRenderer.MOVE_LEFT);
                    break;
                case R.id.btn_down:
                    mRenderer.moveSelectedObject(PlaneFittingRenderer.MOVE_DOWN);
                    break;
                case R.id.btn_up:
                    mRenderer.moveSelectedObject(PlaneFittingRenderer.MOVE_UP);
                    break;
                default:
                    break;
            }
            EventBus.getDefault().post(new RedrawObjectsEvent());
        }

    //Method vor Edit-Buttons on right side if object is seleced(declared in xml activty_main.xml)
    public void rotationControlPressed(View v){
        String axis = sAxis.getSelectedItem().toString();
        Vector3.Axis ax = null;
        if(axis == getString(R.string.xaxis)){ax = Vector3.Axis.X;}
        if(axis == getString(R.string.yaxis)){ax = Vector3.Axis.Y;}
        if(axis == getString(R.string.zaxis)){ax = Vector3.Axis.Z;}

        switch (v.getId()) {
            case R.id.btn_rotate1:
                mRenderer.rotateSelectedObject(ax,5);
                break;
            case R.id.btn_rotate2:
                mRenderer.rotateSelectedObject(ax,-5);
                break;
            default:
                break;
        }
        EventBus.getDefault().post(new RedrawObjectsEvent());
    }


}

