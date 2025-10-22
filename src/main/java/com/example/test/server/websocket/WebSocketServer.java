package com.example.test.server.websocket;

import com.example.test.server.config.CustomSpringConfigurator;
import com.example.test.server.service.MessagePushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/ws/{sid}", configurator = CustomSpringConfigurator.class)
public class WebSocketServer {

    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationContext context;

    private MessagePushService messagePushService;

    @PostConstruct
    public void init() {
        System.out.println("初始化WebSocketServer，ApplicationContext: " + context);
        if (context != null) {
            this.messagePushService = context.getBean(MessagePushService.class);
            System.out.println("messagePushService注入成功: " + messagePushService);
        } else {
            System.err.println("ApplicationContext未初始化！");
        }
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        //TODO检查离线表,发通知

        System.out.println("客户端：" + sid + "建立连接");
        try {
            sessionMap.put(sid, session);
            if (messagePushService != null) {
                messagePushService.notifyPendingRequest(sid);
                messagePushService.notifyMSg(sid);
            } else {
                System.err.println("messagePushService is null!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端：" + sid + "的信息:" + message);
    }


    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开:" + sid);
        sessionMap.remove(sid);
    }

    public static void sendMessageToUser(String userId, String message) {
        Session session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isUserOnline(String sid) {
        Session session = sessionMap.get(sid);
        return session != null && session.isOpen();
    }


}
