package br.psi.giganet.stockapi.stock_moves.controller.request;

import br.psi.giganet.stockapi.stock_moves.dto.ColumnDTO;
import br.psi.giganet.stockapi.stock_moves.dto.StockMoveReportDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExportMovesReportRequest {

    private List<ColumnDTO> columns;
    private List<StockMoveReportDTO> data;

}
