package com.segment.common.exception.handlers;

import com.segment.common.exception.objects.AppException;
import com.segment.common.exception.objects.ErrorResponse;
import com.segment.common.util.JsonUtil;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

import static com.segment.common.exception.objects.ErrorCode.*;

public class AppExceptionHandler implements ExceptionHandler {

    public void handle(Exception e, Request request, Response response) {

        AppException appException = null;

        if (e instanceof AppException) {

            appException = (AppException) e;

            switch (appException.getErrorCode()) {
                case VALIDATION_ERROR:
                    response.status(400);
                    break;
                case NOT_FOUND:
                    response.status(404);
                    break;
                default:
                    response.status(500);
                    break;

            }
        }
         else {
             appException = new AppException(INTERNAL_SERVICE_ERROR,null);
             response.status(500);
         }

        ErrorResponse errorResponse = new ErrorResponse(appException.getErrorCode(), appException.getDetails());
         response.type("application/json");
         response.body(JsonUtil.objectToJson(errorResponse));
    }
}
