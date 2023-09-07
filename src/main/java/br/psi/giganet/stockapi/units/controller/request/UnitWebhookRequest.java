package br.psi.giganet.stockapi.units.controller.request;

import lombok.Data;

import java.util.List;

@Data
public class UnitWebhookRequest {

    private String id;
    private String name;
    private String description;
    private String abbreviation;
    private List<UnitConversionWebhookRequest> conversions;
}
