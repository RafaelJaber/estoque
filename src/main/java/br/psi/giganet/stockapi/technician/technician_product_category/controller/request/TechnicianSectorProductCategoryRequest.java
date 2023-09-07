package br.psi.giganet.stockapi.technician.technician_product_category.controller.request;

import br.psi.giganet.stockapi.technician.model.TechnicianSector;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class TechnicianSectorProductCategoryRequest {

    @NotNull(message = "Setor não pode ser nulo")
    private TechnicianSector sector;

    @NotNull(message = "Categorias não podem ser nulas")
    private List<String> categories;

}
