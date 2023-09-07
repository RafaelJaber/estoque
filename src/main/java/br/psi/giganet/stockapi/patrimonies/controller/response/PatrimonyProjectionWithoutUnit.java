package br.psi.giganet.stockapi.patrimonies.controller.response;

import br.psi.giganet.stockapi.patrimonies.model.PatrimonyCodeType;
import br.psi.giganet.stockapi.patrimonies_locations.controller.response.PatrimonyLocationProjection;
import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import br.psi.giganet.stockapi.products.controller.response.ProductProjectionWithoutUnit;
import lombok.Data;

@Data
public class PatrimonyProjectionWithoutUnit {

    private Long id;
    private String code;
    private PatrimonyCodeType codeType;
    private ProductProjectionWithoutUnit product;
    private PatrimonyLocationProjection currentLocation;

}
