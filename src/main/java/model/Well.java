package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Well {
    public String uid;
    public String name;

    public Well(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }
}
