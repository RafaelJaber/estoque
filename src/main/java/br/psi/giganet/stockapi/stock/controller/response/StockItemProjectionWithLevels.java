package br.psi.giganet.stockapi.stock.controller.response;

import lombok.Data;

import java.util.List;

@Data
public class StockItemProjectionWithLevels extends StockItemProjection {

    private List<QuantityLevelProjection> levels;
    private Long stock;

}
