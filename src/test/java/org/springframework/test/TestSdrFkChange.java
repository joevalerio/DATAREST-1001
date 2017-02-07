package org.springframework.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springframework.test.TestApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT, classes=TestApplication.class)
public class TestSdrFkChange {

    @Autowired
    ObjectMapper objectMapper;

    @LocalServerPort
    int port;

    TypeReference<HashMap<String,Object>> mapTypeRef = new TypeReference<HashMap<String,Object>>() {};

    @Test
    public void test() throws Exception{

        // Make Bar1
        HttpURLConnection con = makeRequest("POST", "http://localhost:" + port + "/test-app/bars", "{ \"name\": \"bar1\"}");
        assertEquals(201, con.getResponseCode());
        String response = getResponse(con);
        String bar1Href = getLink(response, "self");
        System.out.println("bar1: " + bar1Href);
        Map<String, Object> parsed = getMap(response);
        assertEquals("bar1", parsed.get("name"));

        // Make Bar2
        con = makeRequest("POST", "http://localhost:" + port + "/test-app/bars", "{ \"name\": \"bar2\"}");
        assertEquals(201, con.getResponseCode());
        response = getResponse(con);
        String bar2Href = getLink(response, "self");
        System.out.println("bar2: " + bar2Href);
        parsed = getMap(response);
        assertEquals("bar2", parsed.get("name"));

        // Make Foo with Bar1
        con = makeRequest("POST", "http://localhost:" + port + "/test-app/foos", "{ \"name\": \"foo\", \"bar\": \"" + bar1Href + "\"}");
        assertEquals(201, con.getResponseCode());
        response = getResponse(con);
        String fooHref = getLink(response, "self");
        parsed = getMap(response);
        assertEquals("foo", parsed.get("name"));
        assertEquals(bar1Href, getSelfLinkForBarFromFoo(response));

        // Update Foo with Bar2
        con = makeRequest("PUT", fooHref, "{ \"name\": \"fooUpdated\", \"bar\": \"" + bar2Href + "\"}");
        assertEquals(200, con.getResponseCode());
        response = getResponse(con);
        parsed = getMap(response);
        assertEquals("fooUpdated", parsed.get("name"));
        assertEquals(bar2Href, getSelfLinkForBarFromFoo(response)); // this fails, but should not


    }

    protected String getSelfLinkForBarFromFoo(String response) throws Exception {

        String fooBarHref = getLink(response, "bar");
        HttpURLConnection con = makeRequest("GET", fooBarHref);
        assertEquals(200, con.getResponseCode());
        response = getResponse(con);
        return getLink(response, "self");

    }

    protected HttpURLConnection makeRequest(String action, String url) throws Exception{
        return makeRequest(action, url, null);
    }

    protected HttpURLConnection makeRequest(String action, String url, String json) throws Exception{

        URL object=new URL(url);

        HttpURLConnection con = (HttpURLConnection) object.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod(action);

        if(json != null){
            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(json);
            wr.flush();
        }

        return con;
    }

    protected String getResponse(HttpURLConnection con) throws Exception {
        StringBuilder result = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
        String line = null;
        while ((line = br.readLine()) != null) {
            result.append(line + "\n");
        }
        br.close();
        return result.toString();
    }

    @SuppressWarnings("unchecked")
    protected String getLink(String response, String link) throws Exception {
        Map<String, Object> parsed = getMap(response);
        Map<String,Object> links = (Map<String, Object>) parsed.get("_links");
        Map<String,Object> target = (Map<String, Object>) links.get(link);
        return (String) target.get("href");
    }

    protected Map<String, Object> getMap(String response) throws Exception {
        return objectMapper.readValue(response, mapTypeRef);
    }

}
