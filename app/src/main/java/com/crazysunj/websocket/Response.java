package com.crazysunj.websocket;

import com.google.gson.annotations.SerializedName;

public class Response {
    @SerializedName("cmd")
    private String cmd;

    public String getCmd() {
        return cmd;
    }

    public String getResult() {
        return result;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @SerializedName("result")
    private String result;


}
