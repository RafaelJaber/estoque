package br.psi.giganet.stockapi.schedules.controller.response;

import br.psi.giganet.stockapi.stock.controller.response.StockProjection;
import lombok.Data;

import java.util.List;

@Data
public class ScheduledMoveResponse extends ScheduledMoveProjection {

    private StockProjection from;
    private StockProjection to;
    private List<ScheduledMoveItemResponse> items;

}
