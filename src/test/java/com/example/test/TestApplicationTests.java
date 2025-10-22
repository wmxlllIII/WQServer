package com.example.test;

import com.example.test.server.websocket.WebSocketServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestApplicationTests {

    @Test

    void contextLoads() {
    }
    @Autowired
    private WebSocketServer webSocketServer;

    @Test
    void testDependencyInjection() {
        assertNotNull(webSocketServer);
    }

}
