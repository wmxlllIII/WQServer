package com.example.test.common.utils;

import com.example.test.pojo.entity.User;
import com.example.test.pojo.vo.FriendInfoVO;


public class BuildFriendInfoUtil {
    public static FriendInfoVO buildFriendInfo(User user) {
        FriendInfoVO friendInfoVO = new FriendInfoVO();
        friendInfoVO.setUserName(user.getUsername() == null ? "(无名氏)" : user.getUsername());
        friendInfoVO.setEmail(user.getEmail());
        friendInfoVO.setAvatarUrl(user.getAvatarUrl() == null ? "" : user.getAvatarUrl());
        friendInfoVO.setUpdateAt(user.getUpdateAt());
        return friendInfoVO;
    }

}
