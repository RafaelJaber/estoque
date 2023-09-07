package br.psi.giganet.stockapi.common.websockets.service;

import br.psi.giganet.stockapi.common.notifications.model.Notification;
import br.psi.giganet.stockapi.common.notifications.model.NotificationEmployee;
import br.psi.giganet.stockapi.config.security.model.User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WebSocketService extends AbstractWebSocketHandler {

    public void send(Notification notification) {
        List<? extends User> users = notification.getEmployees().stream()
                .map(NotificationEmployee::getEmployee)
                .collect(Collectors.toList());

        users.forEach(user -> send(
                "/topic/notifications/" + user.getId(),
                Collections.singletonMap("notification", notification.getId())));
    }

}
