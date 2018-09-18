package com.redhat.ads.openshift;

import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.IClient;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IPod;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IService;

import java.util.List;

public class OpenshiftClient {

    private static final String OPENSHIFT_PROJECT = "ads--prototype";


    public static void main(String[] args) {


        IClient client1 = new ClientBuilder("https://paas.dev.redhat.com")
                .withUserName("ykoer")
                .withPassword("E:m61yusuf")
                .build();

        IClient client = new ClientBuilder("https://paas.dev.redhat.com").build();
        client.getAuthorizationContext().setToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJhZHMtLWFjdGl2YXRlIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImRlZmF1bHQtdG9rZW4tZ201a2YiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZGVmYXVsdCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6ImY3M2FjM2U1LTQ5NGMtMTFlOC1hYjU5LTAwMWE0YTFiNWQ2NyIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDphZHMtLWFjdGl2YXRlOmRlZmF1bHQifQ.2ZOGFCTEJVeS1q3-Xa3VmzxNCX6P1zLdK7Fu0n6iUd0_2B2IvBtQn8qCkgsKFVLU-yTniY2_NuW9kB4MgtE5BED927Ss2iKN6Z-WKhpRbHRk5y-4PNhNUabNhmGtoSpwMblFKcUk8TOQOLW9z3eUI0pT2ek6bBbimu__F2DdODIK9zZ9oARlwyW7Uyekh_IjDm0Qb8LMqGqdcyOzinR5ypFndN8xeQiZuDVAWN2lN1LLOGY3MOPMxpe46o9GaofR84Qntyag6ghGyLr-X44UKrhmVTjxAu7U886zoQttWsV_dxZUzB4AmJGTzWCW3KGSLX6cYeM_vUUZGjcuKOvr0A");



        System.out.println("=======================================================================");
        System.out.println("Openshift API Version: " + client.getOpenShiftAPIVersion() + ", Openshift Status: " + client.getServerReadyStatus());


        IProject project = (IProject)client.getResourceFactory().stub(ResourceKind.PROJECT, OPENSHIFT_PROJECT);
        System.out.println(project.getName());

        System.out.println("\n========================Openshift Pods==============================");
        List<IPod> pods = client.list(ResourceKind.POD, OPENSHIFT_PROJECT);
        IPod pod = (IPod) pods.stream().filter(p->p.getName().startsWith("eap-clustering-test-3-szcr2")).findFirst().orElse(null);



        //client.delete(pod);


        /*
        for(IPod pod : pods) {
            System.out.println(pod.getName() + ":" + pod.getHost() + ":" + pod.getStatus() + ":" + pod.getCreationTimeStamp());
            //client.delete(pod);
        }*/


        System.out.println("\n========================Openshift Services==============================");
        List<IService> services = client.list(ResourceKind.SERVICE, OPENSHIFT_PROJECT);
        IService service = (IService) services.stream().filter(s->s.getName().startsWith("eap-clustering-test")).findFirst().orElse(null);
        for(IPod p : service.getPods()) {
            System.out.println(p.getName() + ":" + p.getStatus());
        }

        IPod foo = client.get(ResourceKind.POD, "eap-clustering-test-3-szcr2", OPENSHIFT_PROJECT);
        System.out.println(foo);






/*
        IDeploymentConfig deploymentConfig = client.getResourceFactory().stub(ResourceKind.DEPLOYMENT_CONFIG, "postgresql", project.getName());
        IDeploymentTriggerable capability = deploymentConfig.getCapability(IDeploymentTriggerable.class);
        capability.setForce(true);
        capability.setLatest(true);
        capability.trigger();
        */


        System.exit(1);


    }
}
