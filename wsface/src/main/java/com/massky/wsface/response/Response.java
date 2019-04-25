package com.massky.wsface.response;


import java.io.Serializable;

public class Response implements Serializable {

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
    private String result;


}
