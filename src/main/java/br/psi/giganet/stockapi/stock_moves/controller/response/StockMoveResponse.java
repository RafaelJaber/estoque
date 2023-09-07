package br.psi.giganet.stockapi.stock_moves.controller.response;

import br.psi.giganet.stockapi.employees.controller.response.EmployeeProjection;
import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import br.psi.giganet.stockapi.stock.controller.response.StockProjection;
import lombok.Data;

@Data
public class StockMoveResponse extends StockMoveProjection {

    private String lastModifiedDate;
    private StockProjection from;
    private StockProjection to;
    private ProductProjection product;
    private EmployeeProjection requester;
    private EmployeeProjection responsible;
    private String note;

}
