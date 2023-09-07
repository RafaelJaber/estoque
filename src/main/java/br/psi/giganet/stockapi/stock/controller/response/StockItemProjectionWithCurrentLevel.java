package br.psi.giganet.stockapi.stock.controller.response;

import br.psi.giganet.stockapi.stock.model.enums.QuantityLevel;
import lombok.Data;

@Data
public class StockItemProjectionWithCurrentLevel extends StockItemProjection {

    private QuantityLevel currentLevel;

}
