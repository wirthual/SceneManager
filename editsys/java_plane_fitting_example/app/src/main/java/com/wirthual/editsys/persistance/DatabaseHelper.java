package com.wirthual.editsys.persistance;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.wirthual.editsys.R;
import com.wirthual.editsys.persistance.data.CubeForm;
import com.wirthual.editsys.persistance.data.PanelForm;
import com.wirthual.editsys.persistance.data.PoiObject;

import java.io.File;
import java.sql.SQLException;



/**
 * Created by raphael on 22.09.16.
 * adapted from https://github.com/j256/ormlite-examples/blob/master/android/HelloAndroid
 */

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    // the DAO object for PoiObject (2nd parameter = id)
    private Dao<PoiObject, Integer> simpleDao = null;
    private RuntimeExceptionDao<PoiObject, Integer> simpleRuntimeDao = null;
    private  RuntimeExceptionDao<PanelForm,Integer> panelDao = null;
    private  RuntimeExceptionDao<CubeForm,Integer> cubeDao = null;


    public DatabaseHelper(Context context,String name) {
        super(context, context.getExternalFilesDir(null).getAbsolutePath() + "/Database/" +name+".db", null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTableIfNotExists(connectionSource, PoiObject.class);
            TableUtils.createTableIfNotExists(connectionSource, PanelForm.class);
            TableUtils.createTableIfNotExists(connectionSource, CubeForm.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }


    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, PoiObject.class, true);
            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached
     * value.
     */
    public Dao<PoiObject, Integer> getDao() throws SQLException {
        if (simpleDao == null) {
            simpleDao = getDao(PoiObject.class);
        }
        return simpleDao;
    }

    public RuntimeExceptionDao<PanelForm, Integer> getPanelDao() {
        if (panelDao == null) {
            panelDao = getRuntimeExceptionDao(PanelForm.class);
        }
        return panelDao;
    }

    public RuntimeExceptionDao<CubeForm, Integer> getCubeDao() {
        if (cubeDao == null) {
            cubeDao = getRuntimeExceptionDao(CubeForm.class);
        }
        return cubeDao;
    }

    /**
     * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our SimpleData class. It will
     * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
     */
    public RuntimeExceptionDao<PoiObject, Integer> getPoiObjectDao() {
        if (simpleRuntimeDao == null) {
            simpleRuntimeDao = getRuntimeExceptionDao(PoiObject.class);
        }
        return simpleRuntimeDao;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        simpleDao = null;
        panelDao = null;
        cubeDao = null;
        simpleRuntimeDao = null;
    }
}
