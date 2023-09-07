package br.psi.giganet.stockapi.stock_moves.repository;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.stock_moves.controller.response.StockMoveSimpleReportProjection;
import br.psi.giganet.stockapi.stock_moves.model.StockMove;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public interface AdvancedStockMovesRepository {

    Page<? extends StockMove> findAll(List<String> queries, Pageable pageable);

    Page<? extends StockMove> findAll(List<String> queries, BranchOffice branchOffice, Pageable pageable);

    Page<? extends StockMove> findAll(List<String> queries, HashMap<String, Object> criteries, BranchOffice branchOffice, Pageable pageable);

    Page<? extends StockMoveSimpleReportProjection> findAllStockMovesSimpleReport(List<String> groupProperties, List<String> queries,
                                                                                  BranchOffice branchOffice, Pageable pageable);
}
