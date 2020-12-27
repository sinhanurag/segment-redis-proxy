package com.segment.common.exception.handlers;

import com.segment.common.exception.objects.ErrorResponse;
import com.segment.common.util.JsonUtil;
import spark.Request;
import spark.Response;
import spark.Route;

import static com.segment.common.exception.objects.ErrorCode.NOT_FOUND;

public class NotFoundExceptionHandler {

    public static Route handle = (Request request, Response response) -> {
        ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND, "Requested resource not found on the server");
        return JsonUtil.objectToJson(errorResponse);
    };
}
