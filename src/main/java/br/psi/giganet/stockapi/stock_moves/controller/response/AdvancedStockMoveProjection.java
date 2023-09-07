package br.psi.giganet.stockapi.stock_moves.controller.response;

import br.psi.giganet.stockapi.products.controller.response.ProductProjectionWithoutUnit;
import br.psi.giganet.stockapi.stock_moves.controller.response.enums.ServiceOrderMoveType;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedStockMoveProjection {

    private Long id;
    private String date;
    private MoveStatus status;
    private ProductProjectionWithoutUnit product;
    private Double quantity;
    private String from;
    private String to;
    private String description;

}
