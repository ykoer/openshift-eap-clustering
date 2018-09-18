package com.redhat.ads.openshift.model;

import java.io.Serializable;

import static com.redhat.ads.openshift.util.MD5Util.getMD5Hash;

public class Attribute implements Serializable {

    public String key;
    public String value;
    public String hash;


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
            this.hash = getMD5Hash(object);
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
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
