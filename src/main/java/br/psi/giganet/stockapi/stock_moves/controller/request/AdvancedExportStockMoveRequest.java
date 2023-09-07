package br.psi.giganet.stockapi.stock_moves.controller.request;

import br.psi.giganet.stockapi.stock_moves.controller.response.AdvancedStockMoveProjection;
import br.psi.giganet.stockapi.stock_moves.dto.ColumnDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedExportStockMoveRequest {

    private List<ColumnDTO> columns;
    private List<AdvancedStockMoveProjection> data;

}
