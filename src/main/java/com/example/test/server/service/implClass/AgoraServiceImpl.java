package com.example.test.server.service.implClass;


import com.example.test.pojo.dto.AgoraTokenDTO;
import com.example.test.server.service.AgoraService;


import io.agora.rtc.RtcTokenBuilder2;
import io.agora.rtc.RtcTokenBuilder2.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AgoraServiceImpl implements AgoraService {

    @Value("${agora.appId:}")
    private String appId;

    @Value("${agora.appCertificate:}")
    private String appCertificate;


    @Override
    public String generateToken(AgoraTokenDTO tokenDTO) {
        // 创建 Token 生成器
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();

        // 设置角色
        Role role = (tokenDTO.getRole() == 1) ? Role.ROLE_PUBLISHER : Role.ROLE_SUBSCRIBER;

        // 生成 Token - 所有权限使用相同过期时间
//        return tokenBuilder.buildTokenWithUserAccount(
//                appId,
//                appCertificate,
//                tokenDTO.getChannelName(),
//                tokenDTO.getUserId(),
//                role,
//                tokenDTO.getExpire(), // Token 总有效期（秒）
//                tokenDTO.getExpire()   // 权限过期时间（秒）
//        );
        return tokenBuilder.buildTokenWithRtm(
                appId,
                appCertificate,
                tokenDTO.getChannelName(),
                tokenDTO.getUserId(),
                role,
                tokenDTO.getExpire(),
                tokenDTO.getExpire()
        );
    }

}


