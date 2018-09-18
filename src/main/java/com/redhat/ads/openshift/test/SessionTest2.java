package com.redhat.ads.openshift.test;

import com.redhat.ads.openshift.util.SessionSize;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SessionTest2 {

    ExecutorService executor = Executors.newFixedThreadPool(5);


    public void foo() {
        Runnable runnable = () -> {

            try {
                SessionTest1 test = new SessionTest1();
                test.test(10);
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        };

        for (int i = 0; i < 10; i++) {
            executor.execute(runnable);
        }
    }

    public static void main(String[] args) {
        SessionTest2 test = new SessionTest2();
        test.foo();
    }
}
