package com.massky.wsface.response;

public  enum Action {
    beat("beat"),
    validate("validate");

    private String action;


    Action(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

}
