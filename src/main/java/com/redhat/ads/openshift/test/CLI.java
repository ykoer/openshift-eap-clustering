package com.redhat.ads.openshift.test;

import org.apache.commons.cli.*;

public class CLI {

    public static void main(String[] args) {

        String OPENSHIFT_URL = "https://paas.dev.redhat.com";
        String OPENSHIFT_PROJECT = "ads--prototype";
        String OPENSHIFT_SERVICE = "eap-clustering-test";
        String OPENSHIFT_TOKEN = null;

        int CONCURRENT_USERS = 20;
        int LOOP_COUNT = 10000;
        int TEST_DURATION_MINUTES = 15;
        int RANDOM_BYTES = 10000;
        int WRITE_READ_OPERATIONS_PER_USER = 20;
        int WRITE_READ_DELAY_MILLIS = 1000;
        String TEST_RESULTS_PATH = "/tmp/results";
        String TEST_RUN_NAME = "runX";
        int JOLOKIA_FREQUENCY_SECONDS= 10;

        boolean killPods = false;


        Options options = new Options();
        options.addOption("h", "help", false, "show help.");
        options.addOption("o", "url", true, "Openshift URL, default: " + OPENSHIFT_URL);
        options.addRequiredOption("t", "token", true, "Openshift Bearer Token");
        options.addOption("p", "project", true, "Openshift Project, default: " + OPENSHIFT_PROJECT);
        options.addOption("s", "service", true, "Openshift Service, default: " + OPENSHIFT_SERVICE);

        options.addOption("c", "concurrent-users", true, "Number of concurrent users, default: " + CONCURRENT_USERS);
        options.addOption("l", "loop-count", true, "Number of iterations, default: "+LOOP_COUNT);
        options.addOption("d", "duration", true, "Duration, default:" + TEST_DURATION_MINUTES);
        options.addOption("b", "bytes", true, "Random byte length, default: " + RANDOM_BYTES);
        options.addOption("w", "write-read-operations", true, "Write/Read Operations, default: " + WRITE_READ_OPERATIONS_PER_USER);
        options.addOption("wd", "write-read-delay", true, "Write/Read Delay, default: " + WRITE_READ_DELAY_MILLIS);
        options.addOption("k", "killpods", false, "Kill existing pods");
        options.addOption("tp", "results-path", false, "Path to results folder, default: " + TEST_RESULTS_PATH);
        options.addRequiredOption("n", "test-name", true, "Test run name used as prefix of the results file: " + TEST_RUN_NAME);
        options.addOption("f", "jolokia-frequency", true, "Frequency of the metrics calls: " + TEST_RUN_NAME);

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);


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

            if (line.hasOption("tp")) {
                TEST_RESULTS_PATH =line.getOptionValue("tp");
            }

            if (line.hasOption("n")) {
                TEST_RUN_NAME =line.getOptionValue("n");
            }

            if (line.hasOption("f")) {
                JOLOKIA_FREQUENCY_SECONDS=Integer.parseInt(line.getOptionValue("f"));
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



        TestRunner runner = new TestRunner(OPENSHIFT_URL, OPENSHIFT_TOKEN, OPENSHIFT_PROJECT, OPENSHIFT_SERVICE);
        if (killPods) {
            runner.killPods();
        } else {
            runner.initializeJolokiaMetrics(TEST_RESULTS_PATH, TEST_RUN_NAME, JOLOKIA_FREQUENCY_SECONDS);
            runner.initializeTimer(TEST_DURATION_MINUTES);
            runner.runTests(CONCURRENT_USERS, WRITE_READ_OPERATIONS_PER_USER, WRITE_READ_DELAY_MILLIS, RANDOM_BYTES, LOOP_COUNT);
            runner.killPodsRandomly();

        }
    }
}
