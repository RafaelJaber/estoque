package br.psi.giganet.stockapi.templates.controller.response;

import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import lombok.Data;

@Data
public class TemplateMountResponse {

    private ProductProjection product;
    private Double quantity;
    private Double availableQuantityOnDestiny;

}
