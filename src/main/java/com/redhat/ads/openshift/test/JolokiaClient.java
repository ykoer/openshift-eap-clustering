package com.redhat.ads.openshift.test;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class JolokiaClient {

    private final String podProxyUrl;
    private final HttpClient httpClient;
    private final HttpContext httpContext;
    private final String openshiftToken;

    private final String resultsFolder;
    private final String testRunName;
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";

    private static SimpleDateFormat sm = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");


    public JolokiaClient(String openshiftUrl, String openshiftApiUri, String openshiftProject, String openshiftToken, String resultsFolder, String testRunName) {
        this.podProxyUrl = String.format("%s/%s/%s/pods/http:%s:8080/proxy/jolokia", openshiftUrl, openshiftApiUri, openshiftProject, "%s");
        this.openshiftToken = openshiftToken;



        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder = requestBuilder.setConnectTimeout(5);
        requestBuilder = requestBuilder.setConnectionRequestTimeout(5);

        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultRequestConfig(requestBuilder.build());
        this.httpClient = builder.build();

        this.httpContext = new BasicHttpContext();
        this.httpContext.setAttribute(HttpClientContext.COOKIE_STORE, new BasicCookieStore());
        this.resultsFolder = resultsFolder;
        this.testRunName = testRunName;
    }


    private String getCurrentDateTimeAsString() {
        return sm.format(new Date());
    }


    private String getPodProxyUrl(String podName) {
        return String.format(podProxyUrl, podName);
    }


    public void getJVMStats(String podName, String podIndex) throws Exception {
        HttpGet httpGet = new HttpGet(getPodProxyUrl(podName) + "/read/java.lang:type=Memory/HeapMemoryUsage");
        httpGet.addHeader("Authorization", "Bearer " + openshiftToken);

        HttpResponse response = httpClient.execute(httpGet, httpContext);
        String json = EntityUtils.toString(response.getEntity());

        FileWriter fileWriter = null;

        try {
            JSONObject jObj = new JSONObject(json);
            JSONObject value = jObj.getJSONObject("value");
            fileWriter = new FileWriter(resultsFolder + "/results-" + testRunName + "-" + podIndex + ".csv", true);
            fileWriter.append(getCurrentDateTimeAsString());
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(String.valueOf(value.getLong("init")/1000000));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(String.valueOf(value.getLong("committed")/1000000));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(String.valueOf(value.getLong("max")/1000000));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(String.valueOf(value.getLong("used")/1000000));
            fileWriter.append(NEW_LINE_SEPARATOR);

            //fileWriter.flush();
            fileWriter.close();
        } catch (JSONException e) {
            throw new Exception(e.getMessage());
        }

    }
}
