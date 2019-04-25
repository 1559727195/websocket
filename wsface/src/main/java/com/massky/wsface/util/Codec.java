package com.massky.wsface.util;

import com.alibaba.fastjson.JSON;
import com.massky.wsface.response.Response;

public class Codec {
    public static Response decoder(String text) {
        return JSON.parseObject(text, Response.class);
    }
}
