package com.redhat.ads.openshift.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.SerializationUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/session")
public class HttpSessionController {

    private MessageDigest md;

    private Pattern pattern = Pattern.compile("(^\\d+) bytes$");

    @Value("#{environment.HOSTNAME}")
    private String hostName;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        md = MessageDigest.getInstance("MD5");
    }


    @RequestMapping(
            method = RequestMethod.GET,
            value = "/attributes")
    public HttpSessionStateResponse getAttributes(HttpSession httpSession) {

        List<Attribute> attributes = Collections.list(httpSession.getAttributeNames()).stream()
                .map(key -> new Attribute(key, httpSession.getAttribute(key))).collect(Collectors.toList());
        return new HttpSessionStateResponse(attributes, httpSession);

    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/attributes",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HttpSessionStateResponse setAttribute(@RequestBody Attribute attribute, HttpSession httpSession) {
        if (StringUtils.isNotBlank(attribute.key) && StringUtils.isNotBlank(attribute.value) ) {
            Matcher m = pattern.matcher(attribute.value);
            if (m.find()) {
                int bytes = Integer.parseInt(m.group(1));
                setAttributeRandomBytes(attribute.key, bytes, httpSession);
            } else {
                httpSession.setAttribute(attribute.key, attribute.value);
            }
        }

        return getAttributes(httpSession);
    }

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/attributes",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public  HttpSessionStateResponse deleteAttribute(@RequestBody Attribute attribute, HttpSession httpSession) {
        httpSession.removeAttribute(attribute.key);
        return getAttributes(httpSession);
    }

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/attributes/{key}")
    public HttpSessionStateResponse deleteAttribute(@PathVariable String key, HttpSession httpSession) {
        httpSession.removeAttribute(key);
        return getAttributes(httpSession);
    }



    @RequestMapping(
            method = RequestMethod.GET,
            value = "/attributes/{key}/hash")
    public String getAttributeMd5Hash(@PathVariable String key, HttpSession httpSession) {

        Object object = httpSession.getAttribute(key);
        if(object != null) {
            return getMD5Hash(object);
        }
        return null;
    }


    @RequestMapping(
            method = RequestMethod.POST,
            value = "/attributes/{key}/bytes/{length}")
    public void setAttributeRandomBytes(@PathVariable String key, int length, HttpSession httpSession) {
        byte[] bytes = new byte[length];
        new Random().nextBytes(bytes);

        httpSession.setAttribute(key, bytes);
    }

    public String getAttribute_(String key, HttpSession httpSession) {
        Object value = httpSession.getAttribute(key);

        if (value==null) {
            return null;
        }
        if (value instanceof String) {
            return(String) value;
        } else {
            return "MD5:" + getMD5Hash(value);
        }
    }


    @RequestMapping(
            method = RequestMethod.GET,
            value = "/attributes/hash")
    public String getAttributesHash(HttpSession httpSession) {

        return  getMD5Hash(Collections.list(httpSession.getAttributeNames()).stream()
                .map(key -> getMD5Hash(httpSession.getAttribute(key)))
                .collect( Collectors.joining()));
    }

    private static String getMD5Hash(byte[] bytes) {
        return DigestUtils.md5Hex(bytes);
    }

    private static String getMD5Hash(Object object) {
        if (object instanceof String) {
            return DigestUtils.md5Hex((String) object);
        } else {
            return getMD5Hash(SerializationUtils.serialize(object));
        }
    }

    public static class Attribute implements Serializable {

        private String key;
        private String value;
        private String hash;


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



    public class HttpSessionStateResponse implements Serializable {

        private String node;
        private String sessionId;
        private List<Attribute> attributes;

        public HttpSessionStateResponse(List<Attribute> attributes, HttpSession httpSession) {
            this.attributes = attributes;
            this.sessionId = httpSession.getId();
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
    }


}
