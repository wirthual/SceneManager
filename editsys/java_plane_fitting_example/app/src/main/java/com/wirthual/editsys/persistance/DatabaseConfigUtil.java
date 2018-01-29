package com.wirthual.editsys.persistance;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.IOException;
import java.sql.SQLException;
/**
 * Created by raphael on 22.09.16.
 * adapted from https://github.com/j256/ormlite-examples/blob/master/android/HelloAndroid
 */


/**
 * DatabaseConfigUtl writes a configuration file to avoid using annotation processing in runtime which is very slow
 * under Android. This gains a noticeable performance improvement.
 *
 * The configuration file is written to /res/raw/ by default. More info at: http://ormlite.com/docs/table-config
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    public static void main(String[] args) throws SQLException, IOException {
        writeConfigFile("ormlite_config.txt");
    }
}

