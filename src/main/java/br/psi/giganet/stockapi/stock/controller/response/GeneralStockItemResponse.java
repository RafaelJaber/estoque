package br.psi.giganet.stockapi.stock.controller.response;

import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GeneralStockItemResponse {

    private ProductProjection product;
    private Double quantity;
    private BigDecimal price;

}
