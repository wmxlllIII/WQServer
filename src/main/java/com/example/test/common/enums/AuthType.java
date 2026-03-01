package com.example.test.common.enums;

public enum AuthType {
    EMAIL(0),
    ACCOUNT(1),
    PHONE(2);

    private final int value;

    AuthType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AuthType fromInt(int value) {
        for (AuthType type : AuthType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + value);
    }
}
