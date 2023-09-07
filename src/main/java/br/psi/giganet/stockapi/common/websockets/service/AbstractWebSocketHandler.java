package br.psi.giganet.stockapi.common.websockets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public abstract class AbstractWebSocketHandler {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    protected void send(String path, Object data) {
        this.simpMessagingTemplate.convertAndSend(path, data);
    }

}
