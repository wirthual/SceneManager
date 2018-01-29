package com.wirthual.editsys.adfmanager;

/**
 * Created by raphael on 10.08.16.
 */
public class AdfDescription {

        public static final String TYPE_ADF = "ADF";
        public static final String TYPE_SCENE = "Scene";

        private int id;
        private double lat;
        private double lng;
        private int lvl;
        private String name;
        private String description;
        private String uuid;
        private String type;


        public AdfDescription() {}


        public AdfDescription(int id,double lat,double lng,int lvl,String name,String description) {
            this.id = id;
            this.lat = lat;
            this.lng = lng;
            this.name = name;
            this.lvl = lvl;
            this.description = description;
        }

        public AdfDescription(int id,double lat,double lng,int lvl,String name,String description,String uuid,String type) {
            this.id = id;
            this.lat = lat;
            this.lng = lng;
            this.name = name;
            this.lvl = lvl;
            this.description = description;
            this.uuid = uuid;
        }

        @Override
        public String toString() {
            return String.format(
                    "AdfDescription[ lat='.2f', lng='.2f', name=%s]", lng, lat, name);
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getLvl() {
            return lvl;
        }

        public void setLvl(int lvl) {
            this.lvl = lvl;
        }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }





}
