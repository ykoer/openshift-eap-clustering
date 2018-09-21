package com.redhat.ads.openshift.test;

import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.IClient;
import com.redhat.ads.openshift.model.Pod;
import com.redhat.ads.openshift.service.OpenshiftServiceImpl;
import org.apache.commons.cli.*;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestRunnerOld {

    private static String OPENSHIFT_URL = "https://paas.dev.redhat.com";
    private static String OPENSHIFT_API_URI = "api/v1/namespaces";

    private static String  OPENSHIFT_TOKEN = "EAOm5DWLH0Vd_v9iGBLuOtSilaHh6ryqBNdE7e0I3MY";
    private static String OPENSHIFT_PROJECT = "ads--prototype";
    private static String OPENSHIFT_SERVICE = "eap-clustering-test";
    private static String OPENSHIFT_POD_STATUS_RUNNING = "Running";


    public static String[] getPodNames(OpenshiftServiceImpl oc) {
        List<Pod> pods = oc.getPods(OPENSHIFT_PROJECT, OPENSHIFT_SERVICE, OPENSHIFT_POD_STATUS_RUNNING);
        if (pods!=null && pods.size()>0) {
            return pods.stream().map(pod-> pod.getName()).toArray(String[]::new);
        } else {
            return new String[] {};
        }
    }

    public static void main2(String[] args) {
        IClient client = new ClientBuilder("https://paas.dev.redhat.com").build();
        System.out.println(client.getOpenShiftAPIVersion());

    }

    public static void main(String[] args) throws Exception {

        int CONCURRENT_USERS = 20;
        int LOOP_COUNT = 10000;
        int TEST_DURATION_MINUTES = 15;
        int RANDOM_BYTES = 10000;
        int WRITE_READ_OPERATIONS_PER_USER = 20;
        int WRITE_READ_DELAY_MILLIS = 1000;
        boolean killPods = false;
        String TEST_RESULTS_PATH = "/tmp/results";
        String TEST_RUN = "run4";


        Options options = new Options();
        options.addOption("h", "help", false, "show help.");
        options.addOption("o", "url", true, "Openshift URL, default: "+OPENSHIFT_URL);
        options.addRequiredOption("t", "token", true, "Openshift Bearer Token");
        options.addOption("p", "project", true, "Openshift Project, default: "+OPENSHIFT_PROJECT);
        options.addOption("s", "service", true, "Openshift Service, default: " +OPENSHIFT_SERVICE);

        options.addOption("c", "concurrent-users", true, "Number of concurrent users, default: "+CONCURRENT_USERS);
        options.addOption("l", "loop-count", true, "Number of iterations, default: "+LOOP_COUNT);
        options.addOption("d", "duration", true, "Duration, default:"+TEST_DURATION_MINUTES);
        options.addOption("b", "bytes", true, "Random byte length, default: "+RANDOM_BYTES);
        options.addOption("w", "write-read-operations", true, "Write/Read Operations, default: "+WRITE_READ_OPERATIONS_PER_USER);
        options.addOption("wd", "write-read-delay", true, "Write/Read Delay, default: "+WRITE_READ_DELAY_MILLIS);
        options.addOption("k", "killpods", false, "Kill existing pods");
        options.addOption("r", "results-output", false, "Path to results folder, default: " +TEST_RESULTS_PATH);

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);
        //

        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );

            if (line.hasOption('o')) {
                OPENSHIFT_URL = line.getOptionValue('o');
            }
            OPENSHIFT_TOKEN = line.getOptionValue('t');

            if (line.hasOption('p')) {
                OPENSHIFT_PROJECT = line.getOptionValue('p');
            }

            if (line.hasOption('s')) {
                OPENSHIFT_SERVICE = line.getOptionValue('s');
            }

            if (line.hasOption('c')) {
                CONCURRENT_USERS = Integer.parseInt(line.getOptionValue('c'));
            }

            if (line.hasOption('l')) {
                LOOP_COUNT = Integer.parseInt(line.getOptionValue('l'));
            }

            if (line.hasOption('d')) {
                TEST_DURATION_MINUTES = Integer.parseInt(line.getOptionValue('d'));
            }

            if (line.hasOption('b')) {
                RANDOM_BYTES = Integer.parseInt(line.getOptionValue('b'));
            }

            if (line.hasOption('w')) {
                WRITE_READ_OPERATIONS_PER_USER = Integer.parseInt(line.getOptionValue('w'));
            }

            if (line.hasOption("wd")) {
                WRITE_READ_DELAY_MILLIS = Integer.parseInt(line.getOptionValue("wd"));
            }

            if (line.hasOption('k')) {
                killPods = true;
            }

        }
        catch( ParseException exp ) {
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            formatter.printHelp( "test-runner", options );

            System.exit(0);
        }


        OpenshiftServiceImpl oc = new OpenshiftServiceImpl();
        oc.initClient(OPENSHIFT_URL);
        oc.setToken(OPENSHIFT_TOKEN);


        // ======================== RESTART PODS =====================================

        String[] podNames = getPodNames(oc);
        int podCount = podNames.length;

        if (killPods) {
            System.out.println("Restarting Pods...");
            for (String podName : podNames) {
                oc.deletePod(OPENSHIFT_PROJECT, podName);
            }

            System.exit(1);
        }

        final String[] newPods = podNames;


        // ======================== Initialize Test Runner =====================================


        ExecutorService sessionTestExecutor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        int fWRITE_READ_OPERATIONS_PER_USER = WRITE_READ_OPERATIONS_PER_USER;
        int fRANDOM_BYTES = RANDOM_BYTES;
        int fWRITE_READ_DELAY_MILLIS = WRITE_READ_DELAY_MILLIS;
        Runnable sessionTestRunner = () -> {
            try {
                SessionTest test = new SessionTest();
                int errors = test.runTest(fWRITE_READ_OPERATIONS_PER_USER, fRANDOM_BYTES, fWRITE_READ_DELAY_MILLIS);
                if(errors!=0) {
                    System.out.println(errors);
                }
                TimeUnit.SECONDS.sleep(1);

            } catch (Exception e) {
                e.printStackTrace();
            }

        };


        // ======================== Initialize Jolokia Client for fetching JVM stats =====================================

        JolokiaClient jolokiaClient = new JolokiaClient(OPENSHIFT_URL, OPENSHIFT_API_URI, OPENSHIFT_PROJECT, OPENSHIFT_TOKEN, TEST_RESULTS_PATH);
        Runnable jvmStatsRunner = () -> {
            try {
                while(true) {
                    for (String podName : newPods) {
                        jolokiaClient.getJVMStats(TEST_RUN, podName);
                    }
                    TimeUnit.SECONDS.sleep(10);
                }
            } catch (Exception e) {

            }
        };
        Thread thread = new Thread(jvmStatsRunner);
        thread.start();


        // ======================== Initialize Test Timer =====================================

        Timer timer = new Timer();
        TimerTask exitApp = new TimerTask() {
            public void run() {
                System.out.println("Test Done");
                System.exit(1);
            }
        };
        timer.schedule(exitApp, new Date(System.currentTimeMillis()+(TEST_DURATION_MINUTES*60*1000)));


        // ======================== Run Tests =====================================

        System.out.println("Test Started...");
        for (int i = 0; i < LOOP_COUNT; i++) {
            sessionTestExecutor.execute(sessionTestRunner);
        }


    }
}
