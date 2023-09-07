package br.psi.giganet.stockapi.common.notifications.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MarkAsReadRequest {

    @NotEmpty(message = "Lista de notificações não pode ser nula")
    private List<Long> notifications;

}
