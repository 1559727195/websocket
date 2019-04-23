package com.crazysunj.websocket;

public  enum Action {
    BEAT("beat"),
    VALIDATE("validate");

    private String action;


    Action(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

}
