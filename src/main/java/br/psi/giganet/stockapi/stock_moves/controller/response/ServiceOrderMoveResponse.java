package br.psi.giganet.stockapi.stock_moves.controller.response;

import br.psi.giganet.stockapi.products.controller.response.ProductProjectionWithoutUnit;
import br.psi.giganet.stockapi.stock.controller.response.StockProjection;
import br.psi.giganet.stockapi.stock_moves.controller.response.enums.ServiceOrderMoveType;
import br.psi.giganet.stockapi.stock_moves.model.MoveOrigin;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrderMoveResponse {

    private Long id;
    private String date;
    private String lastModifiedDate;
    private MoveStatus status;
    private ProductProjectionWithoutUnit product;
    private Double quantity;
    private ServiceOrderMoveType type;
    private String from;
    private String to;
    private String description;

}
