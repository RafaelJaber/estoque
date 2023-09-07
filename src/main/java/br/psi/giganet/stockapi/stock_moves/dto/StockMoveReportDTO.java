package br.psi.giganet.stockapi.stock_moves.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockMoveReportDTO {

    private String from;
    private String to;
    private String responsible;
    private String idProduct;
    private String product;
    private Long total;
    private String description;
}
