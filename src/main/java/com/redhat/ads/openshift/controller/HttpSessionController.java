package com.redhat.ads.openshift.controller;


import com.redhat.ads.openshift.model.Attribute;
import com.redhat.ads.openshift.model.HttpSessionStateResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

import static com.redhat.ads.openshift.util.MD5Util.getMD5Hash;

@RestController
@RequestMapping("/session")
public class HttpSessionController {

    private Runtime runtime = Runtime.getRuntime();

    @Value("#{environment.HOSTNAME}")
    private String hostName;


    @RequestMapping(
            method = RequestMethod.POST,
            value = "/attributes",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HttpSessionStateResponse setAttribute(@RequestBody Attribute attribute, HttpSession httpSession) {

        if (StringUtils.isNotBlank(attribute.getKey()) && StringUtils.isNotBlank(attribute.getValue()) ) {

            System.out.printf("WRITE--> sessionid=%s:key=%s\n",
                    httpSession.getId(),
                    attribute.getKey()
                    //attribute.getRandomByteLength()!=null?attribute.getRandomByteLength()+" bytes":attribute.getValue()
            );

            Object sessionValue;
            if (attribute.getRandomByteLength()!=null) {
                sessionValue = generateRandomBytes(attribute.getRandomByteLength());
            } else {
                sessionValue = attribute.getValue();
            }

            String hash = getMD5Hash(sessionValue);
            attribute.setHash(hash);
            httpSession.setAttribute(attribute.getKey(), sessionValue);
            return new HttpSessionStateResponse(Arrays.asList(attribute), httpSession.getId(), hostName);
        }

        return null;
    }

    public void setAttribute(String key, int randomBytes, HttpSession httpSession) {
        Attribute attribute = new Attribute();
        attribute.setKey(key);
        attribute.setRandomByteLength(randomBytes);
        setAttribute(attribute, httpSession);
    }

    public void setAttribute(int randomBytes, HttpSession httpSession) {
        String key = UUID.randomUUID().toString();
        Attribute attribute = new Attribute();
        attribute.setKey(key);
        attribute.setRandomByteLength(randomBytes);
        setAttribute(attribute, httpSession);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/attributes/{key}")
    public HttpSessionStateResponse getAttribute(@PathVariable String key, HttpSession httpSession) {

        System.out.println("READ---> sessionid=" + httpSession.getId() + ":key=" +key);


        Object sessionValue = httpSession.getAttribute(key);
        Attribute attribute = new Attribute(key, sessionValue);

        if (sessionValue!=null) {
            attribute.setHash(getMD5Hash(sessionValue));
        }
        HttpSessionStateResponse response = new HttpSessionStateResponse(Arrays.asList(attribute), httpSession.getId(), hostName);

        return response;
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/attributes")
    public HttpSessionStateResponse getAllAttributes(HttpSession httpSession) {

        System.out.println("sessionid=" + httpSession.getId() + ":fetch all attributes");
        List<Attribute> attributes = Collections.list(httpSession.getAttributeNames()).stream()
                .map(key -> new Attribute(key, httpSession.getAttribute(key))).collect(Collectors.toList());
        return new HttpSessionStateResponse(attributes, httpSession.getId(), hostName);

    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/logout")
    public void logout(HttpSession httpSession) {
       httpSession.invalidate();
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/pod")
    public String getPodName(HttpSession httpSession) {
        return hostName;
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/gc")
    public void runGC() {
        System.out.println("Running GC...");
        runtime.gc();
    }

    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new Random().nextBytes(bytes);
        return bytes;
    }





}
