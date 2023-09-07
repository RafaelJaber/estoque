package br.psi.giganet.stockapi.stock.controller.response;

import br.psi.giganet.stockapi.stock.model.enums.StockType;
import lombok.Data;

import java.util.List;

@Data
public class StockProjection {

    private Long id;
    private StockType type;
    private String name;
    private List<StockItemProjection> stockItems;

}
