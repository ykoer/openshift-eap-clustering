package com.redhat.ads.openshift.model;

import java.io.Serializable;


public class Attribute implements Serializable {

    private String key;
    private String value;
    private String hash;
    private Integer randomByteLength;


    public Attribute() {
    }

    public Attribute(String key, Object object) {
        this.key = key;
        if (object!=null) {
            if (object instanceof String) {
                this.value = (String) object;
            } else {
                this.value = "[BYTE ARRAY]";
            }
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        //this.hash = getMD5Hash(value);
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getRandomByteLength() {
        return randomByteLength;
    }

    public void setRandomByteLength(Integer randomByteLength) {
        this.randomByteLength = randomByteLength;
        this.setValue("[RANDOM BYTE ARRAY]");
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", hash='" + hash + '\'' +
                ", randomByteLength=" + randomByteLength +
                '}';
    }
}
