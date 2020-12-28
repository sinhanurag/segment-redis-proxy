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

    private static final String REDIS_HOST = System.getProperty("REDIS_HOST");
    private static final String REDIS_PORT = System.getProperty("REDIS_PORT");
    private static final String PROXY_HOST = System.getProperty("PROXY_HOST");
    private static final String PROXY_PORT = System.getProperty("PROXY_PORT");
    private static final String CACHE_SIZE = System.getProperty("CACHE_SIZE");
    private static final String GLOBAL_EXPIRY = System.getProperty("GLOBAL_EXPIRY");

    private static final Jedis redis = new Jedis(REDIS_HOST, Integer.parseInt(REDIS_PORT), 1800);
    private static final HttpClient client = HttpClientBuilder.create().build();
    private static final Logger logger = LoggerFactory.getLogger(TestProxyServerE2E.class);
    private static final List<String> testData = new ArrayList<>();
    private static final String NOT_FOUND_MESSAGE = "{\"errorCode\":\"RESOURCE_NOT_FOUND\",\"details\":\"Requestedresourcenotfoundontheserver\"}";

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
    public void test1() {
        logger.info("\n\n[TEST 1] - This test sets a key value pair in Redis and then queries the proxy service API with that key to assert if we get back the same value");
        String expected = getRandomValueFromTestData();
        String key = UUID.randomUUID().toString();
        setKeyValueToRedisWithRetry(key, expected);

        String actual = getValueForKeyFromProxy(key, 200);

        Assertions.assertEquals(expected,actual);
        logger.info("Value obtained from Proxy successfully");
        logger.info("Test Passed");
    }

    @Test
    @DisplayName("This test sets a key value pair in Redis and then queries the proxy service API with that key after that it deletes that key from redis " +
            "and queries Proxy Service to ensure it is returned from local cache")

    public void test2() {
        logger.info("\n\n[TEST 2] - This test sets a key value pair in Redis and then queries the proxy service API with that key. After that it deletes that key from redis " +
                "and queries Proxy Service to ensure it is returned from local cache");

        String expected = getRandomValueFromTestData();
        String key = UUID.randomUUID().toString();
        setKeyValueToRedisWithRetry(key, expected);

        String actual = getValueForKeyFromProxy(key,200);
        Assertions.assertEquals(expected,actual);
        logger.info("Value obtained from Proxy successfully for the first time");
        logger.info("Unsetting key in Redis");
        redis.del(key);

        Assertions.assertNull(redis.get(key));

        String retrievedAgain = getValueForKeyFromProxy(key,200);
        Assertions.assertEquals(expected,retrievedAgain);

        logger.info("Value obtained from Proxy successfully for the second time");
        logger.info("Test Passed");
    }

    @Test
    @DisplayName("This test sets a key value pair in Redis and then queries the proxy service API with that key. After that it sleeps for the duration of expiry of the key " +
            "and queries Proxy Service again to ensure it returns Not Found")

    public void test3() {
        logger.info("\n\n[TEST 3] - This test sets a key value pair in Redis and then queries the proxy service API with that key. After that it sleeps for the duration of expiry of the key "+
                "and queries Proxy Service again to ensure it returns Not Found");

        String expected = getRandomValueFromTestData();
        String key = UUID.randomUUID().toString();
        setKeyValueToRedisWithRetry(key, expected);

        String actual = getValueForKeyFromProxy(key,200);
        Assertions.assertEquals(expected,actual);
        logger.info("Value obtained from Proxy successfully for the first time");
        logger.info("Sleeping for duration of Expiry");
        try {
            Thread.sleep((Long.parseLong(GLOBAL_EXPIRY)*1000));
        } catch (Exception ex) {
            logger.error("Error during thread.sleep", ExceptionUtils.getStackTrace(ex));
            Assertions.fail();
        }

        Assertions.assertNotNull(redis.get(key));

        String retrievedAgain = getValueForKeyFromProxy(key,404);
        Assertions.assertEquals(NOT_FOUND_MESSAGE,retrievedAgain);

        logger.info("Value NOT FOUND from Proxy for the second time");
        logger.info("Test Passed");
    }

    @Test
    @DisplayName("This test queries the proxy service for a key that doesn't exist either in the proxy cache or redis and checks if 404 RESOURCE_NOT_FOUND is returned")
    public void test4() {
        logger.info("\n\n[TEST 4] - This test queries the proxy service for a key that doesn't exist either in the proxy cache or redis and checks if 404 RESOURCE_NOT_FOUND is returned");
        String key = UUID.randomUUID().toString();
        String actual = getValueForKeyFromProxy(key,404);
        Assertions.assertEquals(NOT_FOUND_MESSAGE,actual);
        logger.info("Test Passed");
    }


    private String getValueForKeyFromProxy(String key, int expectedStatusCode) {
    HttpGet request = new HttpGet("http://"+PROXY_HOST+":"+PROXY_PORT+"/cache/"+key);
        StringBuilder responseString = new StringBuilder();
        try {
            HttpResponse response = client.execute(request);
            Assertions.assertEquals(expectedStatusCode, response.getStatusLine().getStatusCode());
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                responseString.append(line);
            }
        }catch(Exception ex) {
                logger.error("Error connecting to ProxyService", ExceptionUtils.getStackTrace(ex));
                Assertions.fail();
            }
        return responseString.toString().replace(" ","");
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
