package br.psi.giganet.stockapi.products.controller.request;

import br.psi.giganet.stockapi.products.categories.controller.request.CategoryWebhookRequest;
import br.psi.giganet.stockapi.units.controller.request.UnitWebhookRequest;
import lombok.Data;

@Data
public class ProductWebHookRequest {

    private String id;
    private String code;
    private String name;
    private CategoryWebhookRequest category;
    private String manufacturer;
    private UnitWebhookRequest unit;
    private String description;

}
