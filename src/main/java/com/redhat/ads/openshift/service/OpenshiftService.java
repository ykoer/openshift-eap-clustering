package com.redhat.ads.openshift.service;

import com.redhat.ads.openshift.model.Pod;

import java.util.List;

public interface OpenshiftService {

    List<Pod> getPods(String project, String service, String status);

    boolean deletePod(String project, String name);

}
