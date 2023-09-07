package br.psi.giganet.stockapi.patrimonies_locations.controller.response;

import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PatrimonyLocationResponse extends PatrimonyLocationProjection {

    private PatrimonyLocationType type;
    private String note;
}
