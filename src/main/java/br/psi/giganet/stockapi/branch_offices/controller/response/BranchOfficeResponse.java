package br.psi.giganet.stockapi.branch_offices.controller.response;

import br.psi.giganet.stockapi.stock.controller.response.StockProjection;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import lombok.Data;

@Data
public class BranchOfficeResponse {

    private Long id;
    private String name;
    private CityOptions city;
    private StockProjection shed;
    private StockProjection maintenance;
    private StockProjection obsolete;
    private StockProjection defective;
    private StockProjection customer;

}
