package br.psi.giganet.stockapi.stock_moves.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedStockMoveProjectionWithCustomerName extends AdvancedStockMoveProjection {

    private String customerName;

}
