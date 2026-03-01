package com.example.test.common.utils;

public class UrlUtil {
    private static final String TAG = "WQ_Server_UrlUtil";
    private static final String REGIN = "cn-beijing";
    private static final String bucketName = "wwwmemory";

//    https://wwwmemory.oss-cn-beijing.aliyuncs.com/WQ_1a8d0fa2-43ff-42fa-91ea-93bb952c7f2c.png

//    public static String fillUrl(String url) {
//        return "https://" + bucketName + ".oss-" + REGIN + ".aliyuncs.com/" + url;
//    }


    public static final String AvatarPart1 = "http://139.199.70.159:8080";

    public static String fillUrl(String url) {
        return AvatarPart1 + url;
    }
}
