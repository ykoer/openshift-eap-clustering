package com.redhat.ads.openshift.model;

public class Pod {

    private String name;
    private String status;
    private String host;
    private String IP;
    private String creationTimeStamp;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getCreationTimeStamp() {
        return creationTimeStamp;
    }

    public void setCreationTimeStamp(String creationTimeStamp) {
        this.creationTimeStamp = creationTimeStamp;
    }

    public Pod name(final String name) {
        this.name = name;
        return this;
    }

    public Pod status(final String status) {
        this.status = status;
        return this;
    }

    public Pod host(final String host) {
        this.host = host;
        return this;
    }

    public Pod IP(final String IP) {
        this.IP = IP;
        return this;
    }

    public Pod creationTimeStamp(final String creationTimeStamp) {
        this.creationTimeStamp = creationTimeStamp;
        return this;
    }


}
