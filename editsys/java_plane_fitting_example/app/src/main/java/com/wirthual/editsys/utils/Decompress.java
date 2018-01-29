package com.wirthual.editsys.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author jon
 * Class to unzip files. Used for AdfFile and Database.
 * Source:
 * http://jondev.net/articles/Unzipping_Files_with_Android_(Programmatically)
 * Adapted from here because far to slow:
 * http://stackoverflow.com/questions/14218178/how-to-unzip-large-zip-file-write-to-sdcard-in-less-time
 */
public class Decompress {
    private String _zipFile;
    private String _location;

    public Decompress(String zipFile, String location) {
        _zipFile = zipFile;
        _location = location;

        _dirChecker("");
    }

    public void unzip() {
        try  {
            FileInputStream fis = new FileInputStream(_zipFile);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                System.out.println("Unzipping: " + entry.getName());

                int size;
                byte[] buffer = new byte[2048];

                FileOutputStream fos = new FileOutputStream(_location+entry.getName());
                BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);

                while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, size);
                }
                bos.flush();
                bos.close();
            }
            zis.close();
            fis.close();
        } catch(Exception e) {
            Log.e("Decompress", "unzip", e);
        }
        Log.d(getClass().getName(),"unzip done");

    }

    private void _dirChecker(String dir) {
        File f = new File(_location + dir);

        if(!f.isDirectory()) {
            f.mkdirs();
        }
    }
}
