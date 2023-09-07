package br.psi.giganet.stockapi.products.controller.response;

import br.psi.giganet.stockapi.units.controller.response.UnitProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductProjection {

    private String id;
    private String name;
    private String code;
    private UnitProjection unit;
    private String manufacturer;

}
