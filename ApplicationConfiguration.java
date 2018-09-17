package com.redhat.ads.openshift;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan("com.redhat.ads.openshift")
public class ApplicationConfiguration {
}
