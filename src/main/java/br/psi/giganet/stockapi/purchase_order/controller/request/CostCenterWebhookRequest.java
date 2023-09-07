package br.psi.giganet.stockapi.purchase_order.controller.request;

import lombok.Data;

@Data
public class CostCenterWebhookRequest {

    private Long id;
    private String name;
    private String description;
}
