package br.psi.giganet.stockapi.moves_request.controller.response;

import br.psi.giganet.stockapi.employees.controller.response.EmployeeProjection;
import br.psi.giganet.stockapi.stock_moves.controller.response.StockMoveProjection;
import lombok.Data;

@Data
public class RequestedMoveResponse extends RequestedMoveProjection {

    private EmployeeProjection requester;
    private StockMoveProjection move;
    private String note;

}
