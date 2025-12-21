package com.example.test.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将对象转换为JSON字符串
     * @param obj 要转换的对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
           log.info("对象转JSON失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将JSON字符串转换为指定类型的对象
     * @param json JSON字符串
     * @param clazz 目标类类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("JSON转对象失败: {}", e.getMessage());
            return null;
        }
    }
}
