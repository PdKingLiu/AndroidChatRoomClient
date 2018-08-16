package com.example.client;

public class Msg {

    public static final int TYPE_SENT = 1;
    public static final int TYPE_RECEIVED = 0;
    public String content;
    private int type;

    public Msg(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }
}
