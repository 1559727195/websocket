package com.massky.wsface.response;

import java.io.Serializable;

public class Request<T> implements Serializable {

    private String action;

    private int reqEvent;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setReqEvent(int reqEvent) {
        this.reqEvent = reqEvent;
    }

    public void setSeqId(long seqId) {
        this.seqId = seqId;
    }

    public void setReq(T req) {
        this.req = req;
    }

    public void setReqCount(int reqCount) {
        this.reqCount = reqCount;
    }

    public int getReqEvent() {
        return reqEvent;
    }

    public long getSeqId() {
        return seqId;
    }

    public T getReq() {
        return req;
    }

    public int getReqCount() {
        return reqCount;
    }

    private long seqId;

    private T req;

    private transient int reqCount;

    public Request(String action, int reqEvent, long seqId, T req, int reqCount) {
        this.action = action;
        this.reqEvent = reqEvent;
        this.seqId = seqId;
        this.req = req;
        this.reqCount = reqCount;
    }


    //这里还有各个参数对应get、set方法,为节省篇幅省略了

    public static class Builder<T> {
        private String action;
        private int reqEvent;
        private long seqId;
        private T req;
        private int reqCount;

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder reqEvent(int reqEvent) {
            this.reqEvent = reqEvent;
            return this;
        }

        public Builder seqId(long seqId) {
            this.seqId = seqId;
            return this;
        }

        public Builder req(T req) {
            this.req = req;
            return this;
        }

        public Builder reqCount(int reqCount) {
            this.reqCount = reqCount;
            return this;
        }

        public Request build() {
            return new Request<T>(action, reqEvent, seqId, req, reqCount);
        }

    }
}
