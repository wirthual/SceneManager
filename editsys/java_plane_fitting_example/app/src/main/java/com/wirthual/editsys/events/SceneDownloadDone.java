package com.wirthual.editsys.events;

/**
 * Created by raphael on 03.11.16.
 *
 * Description:
 * Evnet is fired when scene download is finished.
 * Includes uuid of new downloaded file.
 */

public class SceneDownloadDone {
    public final String uuid;

    public SceneDownloadDone(String uuid) {
        this.uuid = uuid;
    }}
