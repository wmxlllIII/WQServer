package com.example.test.common.utils;




import com.example.test.server.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Component
public class VerifyUtil {
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;



    // 生成6位数字验证码
    public String generateCode() {
        return String.valueOf(new Random().nextInt(899999) + 100000);
    }

    // 存储验证码（5分钟有效）
    public void storeCode(String email, String code) {
        redisTemplate.opsForValue().set(
                "verification:" + email,
                code,
                5,
                TimeUnit.MINUTES
        );
    }

    public boolean getOnlineState(String uuid) {
        return WebSocketServer.isUserOnline(uuid);
    }



    public boolean isRightCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get("verification:" + email);
        return code.equals(storedCode);
    }

    public void deleteCode(String email) {
        redisTemplate.delete("verification:" + email);
    }

    public boolean isEmail(String email) {
        if (Pattern.matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$", email)) {
            return true;
        }
        return false;
    }
}
