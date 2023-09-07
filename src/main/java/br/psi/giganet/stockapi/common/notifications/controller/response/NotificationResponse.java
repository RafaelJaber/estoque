package br.psi.giganet.stockapi.common.notifications.controller.response;

import br.psi.giganet.stockapi.common.notifications.model.NotificationType;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class NotificationResponse {

    private Long id;
    private String title;
    private String description;
    private ZonedDateTime date;
    private NotificationType type;
    private String data;
    private Boolean viewed;
    private ZonedDateTime viewedDate;

}
