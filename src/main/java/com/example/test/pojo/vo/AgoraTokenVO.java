    package com.example.test.pojo.vo;

    import lombok.Builder;
    import lombok.Data;

    @Data
    @Builder
    public class AgoraTokenVO {
        private String token;
        private String appId;
        private String channelName;
        private String userId;
        private int role;
        private int expireTime;
    }
