package br.psi.giganet.stockapi.products.controller.response;

import br.psi.giganet.stockapi.products.categories.controller.response.CategoryResponse;
import br.psi.giganet.stockapi.units.controller.response.UnitResponse;
import lombok.Data;

@Data
public class ProductResponse {

    private String id;
    private String code;
    private String name;
    private CategoryResponse category;
    private String manufacturer;
    private UnitResponse unit;
    private String description;

}
