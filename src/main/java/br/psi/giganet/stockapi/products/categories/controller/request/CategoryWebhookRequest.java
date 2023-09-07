package br.psi.giganet.stockapi.products.categories.controller.request;

import lombok.Data;

@Data
public class CategoryWebhookRequest {

    private String id;
    private String name;
    private String pattern;
    private String description;

}
