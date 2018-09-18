package com.redhat.ads.openshift.service;

import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.IClient;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IPod;
import com.openshift.restclient.model.IService;
import com.redhat.ads.openshift.model.Pod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class OpenshiftServiceImpl implements OpenshiftService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz");




    IClient client;

    @Value("${openshift.console.url}")
    String openshiftConsoleUrl;


    @Autowired
    public void initClient(@Value("${openshift.console.url}") String openshiftConsoleUrl) {
        client = new ClientBuilder(openshiftConsoleUrl).build();
    }

    @Override
    public void setToken(String token) {
        client.getAuthorizationContext().setToken(token);
    }

    @Override
    public List<Pod> getPods(String project, String service, String status) {

        List<IService> services = client.list(ResourceKind.SERVICE, project);
        Optional<IService> iServiceOptional = services.stream().filter(s->s.getName().startsWith(service)).findFirst();

        if(iServiceOptional.isPresent()) {
            IService iService = iServiceOptional.get();

            List<IPod> pods = iService.getPods();

            if (status!=null) {
                pods = pods.stream().filter(p->p.getStatus().equalsIgnoreCase(status)).collect(Collectors.toList());
            }

            return Optional.ofNullable(pods).orElse(Collections.emptyList()).stream()
                    .map(ipodToPod)
                    .collect(Collectors.toList());

        }

        return Collections.emptyList();
    }

    @Override
    public boolean deletePod(String project, String name) {

        List<IPod> pods = client.list(ResourceKind.POD, project);
        Optional<IPod> podOptional = pods.stream().filter(p->p.getName().equals(name)).findFirst();

        if(podOptional.isPresent()) {
            client.delete(podOptional.get());
            return true;
        } else {
            return false;
        }
    }



    // ----------------------------------------- CONVERTERS ------------------------------------------

    private Function<IPod, Pod> ipodToPod = ipod -> new Pod()
            .name(ipod.getName())
            //.creationTimeStamp(Date.from(LocalDateTime.parse(ipod.getCreationTimeStamp(), formatter).toInstant(ZoneOffset.UTC)))
            .creationTimeStamp(ipod.getCreationTimeStamp())
            .status(ipod.getStatus())
            .host(ipod.getHost())
            .IP(ipod.getIP());

}
