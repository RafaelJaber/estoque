package br.psi.giganet.stockapi.stock.controller.response;

import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockItemProjection {

    private Long id;
    private ProductProjection product;
    private Double quantity;
    private Double availableQuantity;
    private Double blockedQuantity;
    private Double minQuantity;
    private Double maxQuantity;
    private BigDecimal lastPricePerUnit;

}
