package br.psi.giganet.stockapi.branch_offices.controller.response;

import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import lombok.Data;

@Data
public class BranchOfficeProjection {

    private Long id;
    private String name;
    private CityOptions city;

}
