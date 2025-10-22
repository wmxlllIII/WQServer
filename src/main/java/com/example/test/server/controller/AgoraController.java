package com.example.test.server.controller;

import com.example.test.common.result.Result;
import com.example.test.pojo.dto.AgoraTokenDTO;
import com.example.test.pojo.vo.AgoraTokenVO;
import com.example.test.server.service.AgoraService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
@Slf4j
public class AgoraController {

    @Autowired
    private AgoraService agoraService;

    @Value("${agora.appId}")
    private String appId;

    @PostMapping("/token")
    @ApiOperation(value = "获取声网token")
    public Result<AgoraTokenVO> generateToken(@RequestBody AgoraTokenDTO tokenDTO) {
        // 参数校验
        if (tokenDTO.getChannelName() == null || tokenDTO.getChannelName().isEmpty()) {
            return Result.error("频道名称不能为空");
        }

        if (tokenDTO.getExpire() < 30 || tokenDTO.getExpire() > 86400) {
            log.warn("请求的Token有效期 {} 秒超出范围，使用默认值3600秒", tokenDTO.getExpire());
            tokenDTO.setExpire(3600);
        }

        try {

            String token = agoraService.generateToken(tokenDTO);

            AgoraTokenVO tokenVO = AgoraTokenVO.builder()
                    .token(token)
                    .appId(appId)
                    .channelName(tokenDTO.getChannelName())
                    .userId(tokenDTO.getUserId())
                    .role(tokenDTO.getRole())
                    .expireTime(tokenDTO.getExpire())
                    .build();

            log.info("成功生成Token: channel={}, user={}", tokenDTO.getChannelName(), tokenDTO.getUserId());
            return Result.success(tokenVO);

        } catch (Exception e) {
            log.error("生成Token失败: {}", e.getMessage(), e);
            return Result.error("生成Token失败: " + e.getMessage());
        }
    }
}
