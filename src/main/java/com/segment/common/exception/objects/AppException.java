package com.segment.common.exception.objects;

public class AppException extends Exception {

    private String errorCode;
    private String details;

    public AppException(String errorCode, String details) {
        super(errorCode);
        this.errorCode = errorCode;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
