package br.psi.giganet.stockapi.common.webhooks.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class Webhook {

    @NotNull(message = "Id não informado")
    private String id;
    @NotNull(message = "Origem não informada")
    private WebHookServer origin;
    @NotNull(message = "Tipo não informado")
    private WebHookType type;
    private Object data;

}
