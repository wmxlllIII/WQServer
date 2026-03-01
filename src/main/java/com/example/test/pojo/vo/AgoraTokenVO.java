    package com.example.test.pojo.vo;

    import lombok.Builder;
    import lombok.Data;

    @Data
    @Builder
    public class AgoraTokenVO {
        private String token;
        private String appId;
        private long channelName;
        private long userId;
        private int role;
        private int expireTime;
    }
