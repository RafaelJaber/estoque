package br.psi.giganet.stockapi.common.webhooks.controller.request;

import br.psi.giganet.stockapi.common.webhooks.model.WebHookType;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class EmitWebhookRequest {

    @NotNull(message = "Tipo do webhook não pode ser nulo")
    private WebHookType type;
    private String id;

}
