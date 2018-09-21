package com.redhat.ads.openshift.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.ads.openshift.model.Attribute;
import com.redhat.ads.openshift.model.HttpSessionStateResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.TimeUnit;

public class SessionTest {

    private static final String API_URL = "https://eap-clustering-test-ads--prototype.int.paas.dev.redhat.com";
    private static final String API_URL1 = "http://localhost:9090";

    private ObjectMapper objectMapper = new ObjectMapper();

    private HttpClient httpClient = new DefaultHttpClient();
    private HttpContext httpContext;



    public SessionTest() {
        httpClient = new DefaultHttpClient();;
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, new BasicCookieStore());
    }

    public int runTest(int count, int randomByteSize, int writeReadDelayMillis) throws Exception {

        int errorCount = 0;
        for (int i=0; i<count; i++) {

            String key = "key" + i;

            HttpSessionStateResponse writeResponse = setAttribute(key, randomByteSize);
            String hashWrite = writeResponse.getAttributes().stream().findFirst().get().getHash();

            TimeUnit.MILLISECONDS.sleep(writeReadDelayMillis);

            HttpSessionStateResponse readResponse = getAttribute(key);
            String hashRead = readResponse.getAttributes().stream().findFirst().get().getHash();

            boolean matches = true;
            if (!hashWrite.equals(hashRead)) {
                System.out.println(writeResponse.getSessionId() + ":" + key + ":" + hashWrite + ":" + hashRead + ":" + matches);
                matches = false;
                errorCount++;
            }

        }

        //logout();
        return errorCount;
    }

    public HttpSessionStateResponse getAttribute(String key) throws Exception {
        HttpGet httpGet = new HttpGet(API_URL + "/rest/session/attributes/" + key );
        HttpResponse response = httpClient.execute(httpGet, httpContext);
        return objectMapper.readValue(response.getEntity().getContent(), HttpSessionStateResponse.class);
    }

    public HttpSessionStateResponse setAttribute(String key, int randomBytesLength) throws Exception {
        Attribute attribute = new Attribute();
        attribute.setKey(key);
        attribute.setRandomByteLength(randomBytesLength);
        return setAttribute(attribute);
    }

    public HttpSessionStateResponse setAttribute(String key, String value) throws Exception {
        Attribute attribute = new Attribute();
        attribute.setKey(key);
        attribute.setValue(value);
        return setAttribute(attribute);
    }

    public HttpSessionStateResponse setAttribute(Attribute attribute) throws Exception {
        HttpPost httpPost = new HttpPost(API_URL + "/rest/session/attributes");
        StringEntity stringEntity = new StringEntity(objectMapper.writeValueAsString(attribute));
        httpPost.setEntity(stringEntity);
        httpPost.addHeader("content-type", "application/json");

        HttpResponse response = httpClient.execute(httpPost, httpContext);
        return objectMapper.readValue(response.getEntity().getContent(), HttpSessionStateResponse.class);
    }

    public void logout() throws Exception {
        HttpGet httpGet = new HttpGet(API_URL + "/rest/session/logout" );
        HttpResponse response = httpClient.execute(httpGet, httpContext);
        String result = EntityUtils.toString(response.getEntity());

    }

    public void gc() throws Exception {
        HttpGet httpGet = new HttpGet(API_URL + "/rest/session/gc" );
        HttpResponse response = httpClient.execute(httpGet, httpContext);
        String result = EntityUtils.toString(response.getEntity());

    }

}
