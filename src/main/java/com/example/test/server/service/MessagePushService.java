package com.example.test.server.service;

import com.example.test.common.enums.EventType;

import java.util.Map;

public interface MessagePushService {

    <T> void pushToUser(String receiver_id, EventType eventType, T data);

    <T> void pushToAll(EventType eventType, T data);

}
