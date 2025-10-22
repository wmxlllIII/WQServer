package com.example.test.common.utils;


import com.example.test.pojo.entity.FriendRelationship;
import com.example.test.pojo.entity.User;


import java.util.HashMap;
import java.util.Map;

public class BuildRelaUtil {

    public static Map<String, Object> buildRequest(User sender, User receiver, FriendRelationship relation) {
        Map<String, Object> requestInfo = new HashMap<>();

        requestInfo.put("sourceEmail",sender.getEmail());
        requestInfo.put("targetEmail",receiver.getEmail());
        requestInfo.put("sourceNickname",sender.getUsername());
        requestInfo.put("targetNickname",receiver.getUsername());
        requestInfo.put("sourceAvatarUrl", sender.getAvatarUrl());
        requestInfo.put("targetAvatarUrl", receiver.getAvatarUrl());

        requestInfo.put("serverId",relation.getId());
        requestInfo.put("validMsg",relation.getValidMsg());
        requestInfo.put("status",relation.getStatus());
        requestInfo.put("updateAt",relation.getUpdateAt());

        return requestInfo;
    }
}
