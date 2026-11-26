package com.springbootacademy.api.dto;

/**
 * Standard envelope returned by every endpoint (success and failure).
 */
public class StandardResponse<T> {

    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL = "FAIL";

    private String status;   // "SUCCESS" / "FAIL"
    private String message;
    private T data;

    public StandardResponse() {
    }

    public StandardResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> StandardResponse<T> success(String message, T data) {
        return new StandardResponse<>(SUCCESS, message, data);
    }

    public static <T> StandardResponse<T> fail(String message, T data) {
        return new StandardResponse<>(FAIL, message, data);
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
