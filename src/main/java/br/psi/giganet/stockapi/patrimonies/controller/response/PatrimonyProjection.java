package br.psi.giganet.stockapi.patrimonies.controller.response;

import br.psi.giganet.stockapi.patrimonies.model.PatrimonyCodeType;
import br.psi.giganet.stockapi.patrimonies_locations.controller.response.PatrimonyLocationProjection;
import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import br.psi.giganet.stockapi.stock.controller.response.StockProjection;
import lombok.Data;

@Data
public class PatrimonyProjection {

    private Long id;
    private String code;
    private PatrimonyCodeType codeType;
    private ProductProjection product;
    private PatrimonyLocationProjection currentLocation;

}
