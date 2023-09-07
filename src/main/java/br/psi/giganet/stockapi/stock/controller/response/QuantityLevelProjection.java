package br.psi.giganet.stockapi.stock.controller.response;

import br.psi.giganet.stockapi.stock.model.enums.QuantityLevel;
import lombok.Data;

@Data
public class QuantityLevelProjection {

    private Long id;
    private QuantityLevel level;
    private Float from;
    private Float to;

}
