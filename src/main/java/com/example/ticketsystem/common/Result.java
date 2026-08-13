package com.example.ticketsystem.common;

import lombok.Data;
import org.slf4j.MDC;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private String traceId;

    private Result() {
        this.traceId = MDC.get("traceId");
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}