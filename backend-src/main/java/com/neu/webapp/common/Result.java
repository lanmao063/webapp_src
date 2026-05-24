package com.neu.webapp.common;
//这是一个通用的结果类，用于封装接口的返回结果，包含状态码、消息和数据
public class Result<T> {
    private int code;
    private String message;
    private T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }//私有的构造方法，实现统一创建方式

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }//成功的结果，默认状态码200，消息为"success"，数据由调用者提供，用于查

    public static <T> Result<T> ok() {
        return new Result<>(200, "success", null);
    }//成功的结果，没有数据，默认状态码200，消息为"success"，用于增删改

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }//失败的结果，状态码和消息由调用者提供，没有数据

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }

    public void setCode(int code) { this.code = code; }
    public void setMessage(String message) { this.message = message; }
    public void setData(T data) { this.data = data; }
}
