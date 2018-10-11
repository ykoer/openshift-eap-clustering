package com.redhat.ads.openshift.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class Pod {

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private String name;
    private String status;
    private String host;
    private String IP;
    private String creationTimeStampString;
    private Date creationTimeStamp;


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

    public String getCreationTimeStampString() {
        return creationTimeStampString;
    }

    public void setCreationTimeStampString(String creationTimeStampString) {
        this.creationTimeStampString = creationTimeStampString;
        this.creationTimeStamp = parseDate(creationTimeStampString);
    }

    public Date getCreationTimeStamp() {
        return creationTimeStamp;
    }

    public void setCreationTimeStamp(Date creationTimeStamp) {
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

    public Pod creationTimeStampString(final String creationTimeStampString) {
        setCreationTimeStampString(creationTimeStampString);
        return this;
    }

    public Pod creationTimeStamp(final Date creationTimeStamp) {
        this.creationTimeStamp = creationTimeStamp;
        return this;
    }

    private static Date parseDate(String dateString) {
        try {
            return formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


}
