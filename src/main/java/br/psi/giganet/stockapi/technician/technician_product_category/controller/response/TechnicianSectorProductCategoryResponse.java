package br.psi.giganet.stockapi.technician.technician_product_category.controller.response;

import br.psi.giganet.stockapi.products.categories.controller.response.CategoryResponse;
import br.psi.giganet.stockapi.technician.model.TechnicianSector;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TechnicianSectorProductCategoryResponse {

    private CategoryResponse category;
    private TechnicianSector sector;

}
