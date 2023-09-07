package br.psi.giganet.stockapi.stock.controller.response;

import br.psi.giganet.stockapi.stock_moves.controller.response.StockMoveProjection;
import lombok.Data;

import java.util.List;

@Data
public class StockItemResponse extends StockItemProjection {

    private List<StockMoveProjection> lastOutgoingMoves;
    private List<StockMoveProjection> lastEntryMoves;
    private Long stock;

}
