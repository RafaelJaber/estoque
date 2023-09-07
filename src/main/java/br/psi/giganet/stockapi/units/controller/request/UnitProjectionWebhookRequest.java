package br.psi.giganet.stockapi.units.controller.request;

import lombok.Data;

@Data
public class UnitProjectionWebhookRequest {

    private String id;
    private String name;
    private String abbreviation;

}
