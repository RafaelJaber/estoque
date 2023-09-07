package br.psi.giganet.stockapi.stock_moves.controller.response;

import br.psi.giganet.stockapi.stock_moves.model.MoveOrigin;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import lombok.Data;

@Data
public class StockMoveProjection {

    private Long id;
    private String date;
    private MoveOrigin origin;
    private MoveStatus status;
    private MoveType type;
    private Double quantity;
    private String description;

}
