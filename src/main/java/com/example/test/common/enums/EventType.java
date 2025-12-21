package com.example.test.common.enums;

import java.util.Arrays;

public enum EventType {
    EVENT_TYPE_REQUEST_FRIEND("EVENT_TYPE_REQUEST_FRIEND"),
    EVENT_TYPE_MSG("EVENT_TYPE_MSG"),
    EVENT_TYPE_SHAREMSG("EVENT_TYPE_SHAREMSG"),
    UNKNOWN("UNKNOWN");
    private final String type;

    EventType(String type) {
        this.type = type;
    }

    // 客户端通过 event_type 字符串解析，需提供 getter
    public String getType() {
        return type;
    }

    // 用于服务端接收客户端消息时，通过字符串解析枚举
    public static EventType fromType(String type) {
        return Arrays.stream(EventType.values())
                .filter(eventType -> eventType.type.equals(type))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static int getIntEventType(EventType type) {
        switch (type) {
            case EVENT_TYPE_REQUEST_FRIEND:
                return 1;
            case EVENT_TYPE_MSG:
                return 2;
            case EVENT_TYPE_SHAREMSG:
                return 3;
            default:
                return -1;
        }
    }

    public static EventType getEventTypeInt(int type) {
        switch (type) {
            case 1:
                return EVENT_TYPE_REQUEST_FRIEND;
            case 2:
                return EVENT_TYPE_MSG;
            case 3:
                return EVENT_TYPE_SHAREMSG;
            default:
                return UNKNOWN;
        }
    }
}
