package com.redhat.ads.openshift.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.ads.openshift.model.HttpSessionStateResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class SessionTest1 {

    private static final String API_URL2 = "https://eap-clustering-test-ads--prototype.int.paas.dev.redhat.com";
    private static final String API_URL = "http://localhost:9090";

    private ObjectMapper objectMapper = new ObjectMapper();

    private HttpClient httpClient = new DefaultHttpClient();
    private HttpContext httpContext;



    public SessionTest1() {
        httpClient = new DefaultHttpClient();;
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, new BasicCookieStore());
    }

    public void test(int count) throws Exception {

        Map<String, String> map = new HashMap();
        for (int i=0; i<count; i++) {
            String key = "key"+i;

            //UUID.randomUUID().toString();

            HttpSessionStateResponse res = setAttribute(key, "10000 bytes");
            System.out.println(res.getSessionId());
            String hash  = res.getAttributes().stream().findFirst().get().getHash();
            map.put(key, hash);
        }


        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String expectedHash = entry.getValue();
            HttpSessionStateResponse res = getAttribute(key);
            String gotHash  = res.getAttributes().stream().findFirst().get().getHash();

            if (!expectedHash.equals(gotHash)) {
                System.out.println(key + ":" + expectedHash + ":" + gotHash);
            }

        }



    }

    public HttpSessionStateResponse getAttribute(String key) throws Exception {
        HttpGet httpGet = new HttpGet(API_URL + "/rest/session/attributes/" + key );
        HttpResponse response = httpClient.execute(httpGet, httpContext);
        return objectMapper.readValue(response.getEntity().getContent(), HttpSessionStateResponse.class);
    }


    public HttpSessionStateResponse setAttribute(String key, String value) throws Exception {
        HttpPost httpPost = new HttpPost(API_URL + "/rest/session/attributes?attributes=MATCHING");
        JSONObject json = new JSONObject();
        json.put("key", key);
        json.put("value", value);


        StringEntity stringEntity = new StringEntity(json.toString());
        httpPost.setEntity(stringEntity);
        httpPost.addHeader("content-type", "application/json");

        HttpResponse response = httpClient.execute(httpPost, httpContext);
        //String result = EntityUtils.toString(response.getEntity());
        //System.out.println(result);


        return objectMapper.readValue(response.getEntity().getContent(), HttpSessionStateResponse.class);
    }

    public static void main(String[] args) throws Exception {
        SessionTest1 test = new SessionTest1();
        test.test(10);

        Thread.sleep(600000);
    }
}
