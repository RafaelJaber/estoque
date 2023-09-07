package br.psi.giganet.stockapi.schedules.controller.response;

import br.psi.giganet.stockapi.stock_moves.controller.response.StockMoveProjection;
import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import lombok.Data;

@Data
public class ScheduledMoveItemResponse {

    private Long id;
    private Double quantity;
    private ProductProjection product;
    private StockMoveProjection move;

}
