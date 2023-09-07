package br.psi.giganet.stockapi.moves_request.controller.response;

import br.psi.giganet.stockapi.products.controller.response.ProductProjectionWithoutUnit;
import br.psi.giganet.stockapi.stock.controller.response.StockProjection;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import lombok.Data;

@Data
public class RequestedMoveProjection {

    private Long id;
    private String date;
    private ProductProjectionWithoutUnit product;
    private Double quantity;
    private MoveStatus status;
    private StockProjection from;
    private StockProjection to;
    private String description;

}
