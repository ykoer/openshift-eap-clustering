package com.redhat.ads.openshift.controller;


import com.redhat.ads.openshift.model.Attribute;
import com.redhat.ads.openshift.model.HttpSessionStateResponse;
import com.redhat.ads.openshift.util.SessionSize;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
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
            method = RequestMethod.POST,
            value = "/attributes",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HttpSessionStateResponse setAttribute(@RequestBody Attribute attribute, HttpSession httpSession) {
        if (StringUtils.isNotBlank(attribute.key) && StringUtils.isNotBlank(attribute.value) ) {

            System.out.println("sessionid=" + httpSession.getId() + ":key=" + attribute.key);


            Matcher m = pattern.matcher(attribute.value);
            if (m.find()) {
                int bytes = Integer.parseInt(m.group(1));
                setAttributeRandomBytes(attribute.key, bytes, httpSession);
            } else {
                httpSession.setAttribute(attribute.key, attribute.value);
            }
        }
        return getAttribute(attribute.key, httpSession);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/attributes/{key}")
    public HttpSessionStateResponse getAttribute(@PathVariable String key, HttpSession httpSession) {

        System.out.println("sessionid=" + httpSession.getId() + ":key=" +key);
        Attribute attribute = new Attribute(key, httpSession.getAttribute(key));
        HttpSessionStateResponse response = new HttpSessionStateResponse(attribute, httpSession.getId(), hostName);

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
            method = RequestMethod.POST,
            value = "/attributes/{key}/bytes/{length}")
    public void setAttributeRandomBytes(@PathVariable String key, int length, HttpSession httpSession) {
        byte[] bytes = new byte[length];
        new Random().nextBytes(bytes);

        System.out.println(bytes.length);


        httpSession.setAttribute(key, bytes);
    }


}
