package com.redhat.ads.openshift.controller;

import com.redhat.ads.openshift.model.Pod;
import com.redhat.ads.openshift.service.OpenshiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/openshift")
public class OpenshiftController {

    @Autowired
    private OpenshiftService openshiftService;


    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/authenticate/{token}")
    public void setToken(@PathVariable String token, HttpSession httpSession) {
        httpSession.setAttribute("OPENSHIFT_TOKEN", token);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{project}/{service}/pods",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Pod> getProjectPods(
            @PathVariable String project,
            @PathVariable String service,
            @RequestParam(required = false) String status) {
        return openshiftService.getPods(project, service, status);
    }

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/{project}/pods/{name}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public boolean deletePod(
            @PathVariable String project,
            @PathVariable String name) {

        return openshiftService.deletePod(project, name);
    }
}
