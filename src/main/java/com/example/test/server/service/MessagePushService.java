package com.example.test.server.service;

import java.util.Map;

public interface MessagePushService {

    void pushToUser(String userId, Map<String, Object> message);

    void notifyPendingRequest(String sid);

    void notifyMSg(String sid);
}
