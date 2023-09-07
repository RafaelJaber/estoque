package br.psi.giganet.stockapi.patrimonies.controller.response;

import br.psi.giganet.stockapi.employees.controller.response.EmployeeProjection;
import br.psi.giganet.stockapi.patrimonies_locations.controller.response.PatrimonyLocationProjection;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import br.psi.giganet.stockapi.stock.controller.response.StockProjection;
import br.psi.giganet.stockapi.stock_moves.model.MoveOrigin;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class PatrimonyMoveProjection {

    private Long id;
    private ZonedDateTime date;
    private PatrimonyLocationProjection from;
    private PatrimonyLocationProjection to;
    private EmployeeProjection responsible;
    private String note;

}
