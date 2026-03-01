package com.example.test.common.enums;

public enum FriendStatuType {
    PENDING(0),
    ACCEPTED(1),
    REJECTED(2);

    private final int value;

    FriendStatuType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static FriendStatuType fromStatus(int value) {
        for (FriendStatuType type : FriendStatuType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unexpected status: " + value);
    }
}
