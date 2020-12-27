package com.segment;

import com.segment.cache.controller.ProxyService;
import com.segment.cache.dao.LocalCache;
import com.segment.common.exception.handlers.AppExceptionHandler;
import com.segment.common.exception.handlers.NotFoundExceptionHandler;
import com.segment.common.util.Path;

import static spark.Spark.*;

public class Application {

    public static LocalCache cache;
    public static AppExceptionHandler appExceptionHandler;

    public static void main(String[] args) {
        String REDIS_HOST = "127.0.0.1";
        int REDIS_PORT = 6379;
        int GLOBAL_EXPIRY = 60;
        int CACHE_SIZE = 15;
        int PROXY_PORT = 8080;

        for (String arg:args) {
            if(arg.startsWith("--REDIS_HOST")) {
                REDIS_HOST = arg.substring(13);
            }
            if(arg.startsWith("--REDIS_PORT")) {
                REDIS_PORT = Integer.parseInt(arg.substring(13));
            }
            if(arg.startsWith("--GLOBAL_EXPIRY")) {
                GLOBAL_EXPIRY = Integer.parseInt(arg.substring(16));
            }
            if(arg.startsWith("--CACHE_SIZE")) {
                CACHE_SIZE = Integer.parseInt(arg.substring(13));
            }
            if(arg.startsWith("--PROXY_PORT")) {
                PROXY_PORT = Integer.parseInt(arg.substring(13));
            }
        }

        cache = new LocalCache(CACHE_SIZE, GLOBAL_EXPIRY, REDIS_HOST, REDIS_PORT);
        appExceptionHandler = new AppExceptionHandler();

        notFound(NotFoundExceptionHandler.handle);

        port(PROXY_PORT);

        get(Path.CACHE_GET, (request, response) -> {
            String value = ProxyService.getValueForKey(cache, request.params(":key"));
            return value;
        });

        exception(Exception.class, appExceptionHandler);

        after((req, res) -> {
            res.type("application/json");
        });

    }
}
