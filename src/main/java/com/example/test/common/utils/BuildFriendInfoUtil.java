package com.example.test.common.utils;

import com.example.test.pojo.entity.User;
import com.example.test.pojo.vo.FriendInfoVO;


public class BuildFriendInfoUtil {
    public static FriendInfoVO buildFriendInfo(User user) {
        FriendInfoVO friendInfoVO = new FriendInfoVO();
        friendInfoVO.setUuNumber(user.getUuNumber());
        friendInfoVO.setUsername(user.getUsername() == null ? "用户" + user.getUuNumber() : user.getUsername());
        friendInfoVO.setAvatarUrl(user.getAvatarUrl() == null ? "" : UrlUtil.fillUrl(user.getAvatarUrl()));
        friendInfoVO.setUpdateAt(TimeUtil.dateTimeToSecond(user.getUpdateAt()));
        return friendInfoVO;
    }

}
