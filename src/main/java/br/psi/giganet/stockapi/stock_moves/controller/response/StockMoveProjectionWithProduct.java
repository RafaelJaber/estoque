package br.psi.giganet.stockapi.stock_moves.controller.response;

import br.psi.giganet.stockapi.products.controller.response.ProductProjectionWithoutUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class StockMoveProjectionWithProduct extends StockMoveProjection {

    private ProductProjectionWithoutUnit product;

}
