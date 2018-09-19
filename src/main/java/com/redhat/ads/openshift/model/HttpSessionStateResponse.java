package com.redhat.ads.openshift.model;

import java.io.Serializable;
import java.util.List;

public class HttpSessionStateResponse implements Serializable {

    private String node;
    private String sessionId;
    private List<Attribute> attributes;

    public HttpSessionStateResponse() {}

    public HttpSessionStateResponse(String sessionId, String hostName) {
        this.sessionId = sessionId;
        this.node = hostName;
    }

    public HttpSessionStateResponse(List<Attribute> attributes, String sessionId, String hostName) {
        this.attributes = attributes;
        this.sessionId = sessionId;
        this.node = hostName;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "HttpSessionStateResponse{" +
                "node='" + node + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
