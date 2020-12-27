package com.segment.cache.controller;

import com.segment.cache.dao.LocalCache;
import com.segment.common.exception.objects.AppException;
import com.segment.common.exception.objects.ErrorCode;
import com.segment.common.util.JsonUtil;

public class ProxyService {

    public static String getValueForKey(LocalCache cache, String key) throws AppException {
        String value = cache.get(key);
        if (value == null) {
            throw new AppException(ErrorCode.NOT_FOUND,"Requested resource not found on the server");
        }
        return JsonUtil.stringToJson(value);
    }
}
