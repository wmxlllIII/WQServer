package com.example.test.pojo.dto;

public class WebSocketDTO<T> {
    private T data;
    private String event_type;

    public WebSocketDTO(String event_type, T data) {
        this.event_type = event_type;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }
}
