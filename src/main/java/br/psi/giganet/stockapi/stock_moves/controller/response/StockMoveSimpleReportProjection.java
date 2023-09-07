package br.psi.giganet.stockapi.stock_moves.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockMoveSimpleReportProjection {

    private Long idFrom;
    private String from;
    private Long idTo;
    private String to;
    private String responsible;
    private String idProduct;
    private String product;
    private Long total;
    private String description;
}
