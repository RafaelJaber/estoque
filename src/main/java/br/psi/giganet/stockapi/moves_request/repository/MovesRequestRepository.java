package br.psi.giganet.stockapi.moves_request.repository;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.moves_request.model.RequestedMove;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovesRequestRepository extends JpaRepository<RequestedMove, Long> {

    @Query("SELECT r FROM RequestedMove r WHERE r.to.stock = :technicianStock")
    Page<RequestedMove> findAllByStockTo(TechnicianStock technicianStock, Pageable pageable);

    @Query("SELECT r FROM RequestedMove r WHERE r.to.stock = :technicianStock AND r.status = :status")
    Page<RequestedMove> findAllByStockToAndStatus(TechnicianStock technicianStock, MoveStatus status, Pageable pageable);

    @Query("SELECT r FROM RequestedMove r WHERE r.from.stock = :technicianStock")
    Page<RequestedMove> findAllByStockFrom(TechnicianStock technicianStock, Pageable pageable);

    @Query("SELECT r FROM RequestedMove r WHERE r.from.stock = :technicianStock AND r.status = :status")
    Page<RequestedMove> findAllByStockFromAndStatus(TechnicianStock technicianStock, MoveStatus status, Pageable pageable);

    @Query("SELECT r FROM RequestedMove r WHERE r.status = 'REQUESTED' AND r.from.stock.city = :city")
    Page<RequestedMove> findAllPendingByCityStockFrom(CityOptions city, Pageable pageable);

    @Query("SELECT r FROM RequestedMove r WHERE r.status = 'REQUESTED' AND r.to.stock.city = :city")
    Page<RequestedMove> findAllPendingByCityStockTo(CityOptions city, Pageable pageable);

    @Query("SELECT r FROM RequestedMove r WHERE r.status = 'REQUESTED' AND " +
            "r.branchOffice = :branchOffice  AND " +
            "r.from.stock.type IN :types")
    Page<RequestedMove> findAllPendingByStockTypeFromAndBranchOffice(List<StockType> types, BranchOffice branchOffice, Pageable pageable);

    @Query("SELECT r FROM RequestedMove r WHERE r.status = 'REQUESTED' AND " +
            "r.branchOffice = :branchOffice  AND " +
            "r.to.stock.type IN :types")
    Page<RequestedMove> findAllPendingByStockTypeToAndBranchOffice(List<StockType> types, BranchOffice branchOffice, Pageable pageable);

    @Query("SELECT r FROM RequestedMove r WHERE r.branchOffice = :branchOffice")
    Page<RequestedMove> findAllByBranchOffice(BranchOffice branchOffice, Pageable pageable);

}
