package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Wellbore {
    public String uid;
    public String well_uid;
    public String name;

    public Wellbore(String uid, String well_uid, String name) {
        this.uid = uid;
        this.well_uid = well_uid;
        this.name = name;
    }
}
