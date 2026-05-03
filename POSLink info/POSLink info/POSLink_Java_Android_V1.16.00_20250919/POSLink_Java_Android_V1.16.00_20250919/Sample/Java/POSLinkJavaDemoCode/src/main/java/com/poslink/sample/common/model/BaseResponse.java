package com.poslink.sample.common.model;

/**
 * Created by Chilling on 2018/2/12.
 */

public class BaseResponse {
    public static final String CODE_OK = "200";
    public static final String MESSAGE_OK = "Success";
    public static final String CODE_ERROR = "-1";
    public static final String MESSAGE_REQUEST_ERROR = "Error No Request";
    private String code;
    private String message;

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
