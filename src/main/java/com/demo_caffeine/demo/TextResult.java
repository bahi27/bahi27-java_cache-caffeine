package com.demo_caffeine.demo;

public class TextResult {

    private String text;
    private long timeNs;

    public TextResult(String text, long timeNs) {
        this.text = text;
        this.timeNs = timeNs;
    }

    public String getText() {
        return text;
    }

    public long getTimeNs() {
        return timeNs;
    }
}
