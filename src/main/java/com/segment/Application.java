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
        String REDIS_HOST = (args[0] != null ? args[0]:"127.0.0.1");
        int REDIS_PORT = (args[1] != null ? Integer.parseInt(args[1]):6379);
        int GLOBAL_EXPIRY = (args[2] != null ? Integer.parseInt(args[2]):60);
        int CACHE_SIZE = (args[3] != null ? Integer.parseInt(args[3]):15);
        int PROXY_PORT = (args[4] != null ? Integer.parseInt(args[4]):8080);

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
