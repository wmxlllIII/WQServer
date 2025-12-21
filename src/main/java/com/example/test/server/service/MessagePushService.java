package com.example.test.server.service;

import com.example.test.common.enums.EventType;

import java.util.Map;

public interface MessagePushService {

    <T> void pushToUser(long receiver_id, EventType eventType, T data);

    <T> void pushToAll(EventType eventType, T data);

    void checkPendingRequest(long sid);
}
