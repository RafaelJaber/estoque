package br.psi.giganet.stockapi.units.controller.request;

import lombok.Data;

@Data
public class UnitConversionWebhookRequest {

    private String id;
    private UnitProjectionWebhookRequest to;
    private Double conversion;

}
