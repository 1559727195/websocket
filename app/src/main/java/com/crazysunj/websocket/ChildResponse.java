package com.crazysunj.websocket;

public class ChildResponse {
    private int code;
    private String msg;
    private String data;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getData() {
        return data;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isOK(){
        return code == 0;
    }
}
