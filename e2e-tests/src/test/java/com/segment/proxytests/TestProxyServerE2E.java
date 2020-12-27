package com.segment.proxytests;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TestProxyServerE2E {

    private static final Jedis redis = new Jedis("127.0.0.1", 6379, 1800);
    private static final HttpClient client = HttpClientBuilder.create().build();
    private static final Logger logger = LoggerFactory.getLogger(TestProxyServerE2E.class);
    private static final List<String> testData = new ArrayList<>();

    @BeforeAll
    public static void initTestData(){
        testData.add("{\"name\":\"userName1\",\"company\":\"companyName1\"}");
        testData.add("{\"name\":\"userName2\",\"company\":\"companyName2\"}");
        testData.add("{\"name\":\"userName3\",\"company\":\"companyName3\"}");
        testData.add("{\"name\":\"userName4\",\"company\":\"companyName4\"}");
        testData.add("{\"name\":\"userName5\",\"company\":\"companyName5\"}");
        testData.add("{\"name\":\"userName6\",\"company\":\"companyName6\"}");
        testData.add("{\"name\":\"userName7\",\"company\":\"companyName7\"}");
        testData.add("{\"name\":\"userName8\",\"company\":\"companyName8\"}");
        testData.add("{\"name\":\"userName9\",\"company\":\"companyName9\"}");
        testData.add("{\"name\":\"userName10\",\"company\":\"companyName10\"}");
        testData.add("{\"name\":\"userName11\",\"company\":\"companyName11\"}");
        testData.add("{\"name\":\"userName12\",\"company\":\"companyName12\"}");
    }

    @Test
    @DisplayName("This test sets a key value pair in Redis and then queries the proxy service API with that key to assert if we get back the same value")
    public void testProxyGetsValueForKeyFromRedis() {
        String expected = getRandomValueFromTestData();
        String key = UUID.randomUUID().toString();
        setKeyValueToRedisWithRetry(key, expected);

        HttpGet request = new HttpGet("http://localhost:8080/cache/"+key);
        try {
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            StringBuilder actual = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                actual.append(line);
            }
            Assertions.assertEquals(expected,actual.toString().replace(" ",""));
        } catch(Exception ex) {
            logger.error("Error connecting to ProxyService", ExceptionUtils.getStackTrace(ex));
            Assertions.fail();
        }
    }

    private String getRandomValueFromTestData() {
        Random random = new Random();
        return testData.get(random.nextInt(12));
    }

    private void setKeyValueToRedisWithRetry(String key, String value) {
        String statusCode = redis.set(key, value);
        if (!"OK".equals(statusCode)) {
            logger.error("Error setting key, value in redis retrying");
            statusCode = redis.set(key, value);
            if (!"OK".equals(statusCode)) {
                logger.error("Error setting value to Redis - failing test");
                Assertions.fail();
            }
        }
        logger.info("Successfully set key value to Redis");
    }
}
