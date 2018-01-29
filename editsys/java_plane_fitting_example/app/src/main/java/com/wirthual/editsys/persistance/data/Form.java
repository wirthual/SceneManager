package com.wirthual.editsys.persistance.data;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by raphael on 10.11.16.
 */

public class Form {

    @DatabaseField(generatedId = true)
    int id;

    public Form(){}

}
