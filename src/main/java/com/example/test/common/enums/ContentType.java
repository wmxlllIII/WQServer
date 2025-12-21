package com.example.test.common.enums;

public enum ContentType {
    TYPE_TEXT(0),
    TYPE_IMAGE(1),
    TYPE_LINK(2),
    TYPE_VOICE(3);
    private final int value;

    ContentType(int type) {
        this.value = type;
    }

    public static ContentType fromInt(int ContentIntType) {
        for (ContentType type : ContentType.values()) {
            if (type.value == ContentIntType) {
                return type;
            }
        }

        return ContentType.TYPE_TEXT;
    }

    public int toInt() {
        return this.value;
    }
}
