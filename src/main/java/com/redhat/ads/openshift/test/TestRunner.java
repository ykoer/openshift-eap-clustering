package com.redhat.ads.openshift.test;

import com.redhat.ads.openshift.model.Pod;
import com.redhat.ads.openshift.service.OpenshiftServiceImpl;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TestRunner {


    private final String openshiftUrl;
    private final String openshiftToken;
    private final String openshiftProject;
    private final String openshiftService;
    private final OpenshiftServiceImpl oc;

    private static final String OPENSHIFT_POD_STATUS_RUNNING = "Running";
    private static final String OPENSHIFT_API_URI = "api/v1/namespaces";


    public TestRunner(String openshiftUrl, String openshiftToken, String openshiftProject, String openshiftService) {
        this.openshiftUrl = openshiftUrl;
        this.openshiftToken = openshiftToken;
        this.openshiftProject = openshiftProject;
        this.openshiftService = openshiftService;
        oc = new OpenshiftServiceImpl();
        oc.initClient(openshiftUrl);
        oc.setToken(openshiftToken);
    }

    public void killPods() {
        List<String> podNames = getPodNames();
        System.out.println("Deleting Pods...");
        for (String podName : podNames) {
            oc.deletePod(openshiftProject, podName);
        }
        System.exit(1);
    }

    public void initializeJolokiaMetrics(String resultsPath, String testRunName, int runEverySeconds ) {
        JolokiaClient jolokiaClient = new JolokiaClient(openshiftUrl, OPENSHIFT_API_URI, openshiftProject, openshiftToken, resultsPath, testRunName);


        Runnable jvmStatsRunner = () -> {
            try {
                while(true) {
                   List<String> podNames = getPodNames();
                   Collections.sort(podNames);

                    for (String podName : podNames) {
                        String pod = "pod"+(podNames.indexOf(podName)+1);
                        System.out.println(pod+":"+podName);
                        jolokiaClient.getJVMStats(podName, pod);
                    }
                    TimeUnit.SECONDS.sleep(runEverySeconds);
                }
            } catch (Exception e) {

            }
        };
        Thread thread = new Thread(jvmStatsRunner);
        thread.start();
    }

    public void initializeTimer(int testDurationInMinutes) {
        Timer timer = new Timer();
        TimerTask exitApp = new TimerTask() {
            public void run() {
                System.out.println("Test Done");
                System.exit(1);
            }
        };
        timer.schedule(exitApp, new Date(System.currentTimeMillis()+(testDurationInMinutes*60*1000)));
    }

    public void runTests(int concurrentUsers, int writeReadsPerUser, int writeReadDelay, int randomByteSize, int loopCount) {
        ExecutorService sessionTestExecutor = Executors.newFixedThreadPool(concurrentUsers);
        Runnable sessionTestRunner = () -> {
            try {
                SessionTest test = new SessionTest();
                int errors = test.runTest(writeReadsPerUser, randomByteSize, writeReadDelay);
                if(errors!=0) {
                    System.out.println(errors);
                }
                TimeUnit.SECONDS.sleep(1);

            } catch (Exception e) {
                e.printStackTrace();
            }

        };

        for (int i = 0; i < loopCount; i++) {
            sessionTestExecutor.execute(sessionTestRunner);
        }
    }

    private List<String> getPodNames() {
        List<Pod> pods = oc.getPods(openshiftProject, openshiftService, OPENSHIFT_POD_STATUS_RUNNING);
        if (pods!=null && pods.size()>0) {
            return pods.stream().map(pod-> pod.getName()).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public void killPodsRandomly() {

        Runnable randomPodKill = () -> {
            try {
                while (true) {
                    TimeUnit.SECONDS.sleep(120);
                    List<String> podNames = getPodNames();
                    Random rand = new Random();
                    String randomPod = podNames.get(rand.nextInt(podNames.size()));
                    oc.deletePod(openshiftProject, randomPod);

                }
            } catch (Exception e) {

            }
        };
        Thread thread = new Thread(randomPodKill);
        thread.start();
    }

}
