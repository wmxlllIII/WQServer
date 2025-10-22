package com.example.test.server.service;

import com.example.test.pojo.dto.AgoraTokenDTO;

public interface AgoraService {
    String generateToken(AgoraTokenDTO agoraTokenDTO);
}
