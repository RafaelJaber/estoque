package br.psi.giganet.stockapi.stock_moves.repository;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock_moves.model.MoveReason;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.stock_moves.model.StockMove;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovesRepository extends JpaRepository<StockMove, Long> {

    @Query("SELECT m FROM StockMove m " +
            "INNER JOIN m.from " +
            "INNER JOIN m.to " +
            "INNER JOIN m.product " +
            "WHERE UPPER(m.description) LIKE CONCAT('%', UPPER(:description), '%') AND " +
            "m.status = 'REQUESTED' AND m.reason = 'SERVICE_ORDER' ")
    Page<StockMove> findAllPendingFromServiceOrder(String description, Pageable pageable);

    @Query("SELECT m FROM StockMove m " +
            "INNER JOIN m.from " +
            "INNER JOIN m.to " +
            "INNER JOIN m.product " +
            "WHERE UPPER(m.description) LIKE CONCAT('%', UPPER(:description), '%') AND " +
            "m.status = 'REQUESTED' AND m.reason = 'SERVICE_ORDER' AND " +
            "m.branchOffice = :branchOffice ")
    Page<StockMove> findAllPendingFromServiceOrderAndBranchOffice(String description, BranchOffice branchOffice, Pageable pageable);

    @Query("SELECT m FROM StockMove m WHERE UPPER(m.description) LIKE CONCAT('%', UPPER(:description), '%') AND " +
            "m.status = 'REQUESTED'")
    Page<StockMove> findAllPending(String description, Pageable pageable);

    @Query("SELECT m FROM StockMove m WHERE " +
            "UPPER(m.description) LIKE CONCAT('%', UPPER(:description), '%') AND " +
            "m.status = 'REQUESTED' AND " +
            "m.type = :type")
    Page<StockMove> findAllPendingAndType(String description, MoveType type, Pageable pageable);

    @Query("SELECT m FROM StockMove m WHERE UPPER(m.description) LIKE CONCAT('%', UPPER(:description), '%')")
    Page<StockMove> findAllByDescription(String description, Pageable pageable);

    @Query("SELECT m FROM StockMove m WHERE " +
            "UPPER(m.description) LIKE CONCAT('%', UPPER(:description), '%') AND " +
            "(m.from.stock = :stock OR m.to.stock = :stock)")
    Page<StockMove> findAllByDescriptionAndStock(Stock stock, String description, Pageable pageable);

    @Query("SELECT m FROM StockMove m WHERE m.status = 'REALIZED' AND UPPER(m.description) LIKE CONCAT('%', UPPER(:description), '%')")
    Page<StockMove> findAllRealizedByDescription(String description, Pageable pageable);

    @Query("SELECT m FROM StockMove m WHERE " +
            "m.status = 'REALIZED' AND " +
            "UPPER(m.description) LIKE CONCAT('%', UPPER(:description), '%') AND " +
            "(m.from.stock = :stock OR m.to.stock = :stock)")
    Page<StockMove> findAllRealizedByDescriptionAndStock(Stock stock, String description, Pageable pageable);

    @Query("SELECT m FROM StockMove m WHERE m.status = 'REQUESTED' AND m.from.stock = :stock")
    Page<StockMove> findAllPendingByStockFrom(Stock stock, Pageable pageable);

    @Query("SELECT m FROM StockMove m WHERE m.status = 'REQUESTED' AND m.from.stock.city = :city")
    Page<StockMove> findAllPendingByCityStockFrom(CityOptions city, Pageable pageable);

    @Query("SELECT m FROM StockMove m WHERE m.status = 'REQUESTED' AND " +
            "(" +
            "   (m.branchOffice IS NULL AND m.from.stock.branchOffice = :branchOffice ) OR " +
            "   m.branchOffice = :branchOffice" +
            ") AND " +
            "m.from.stock.type IN :types")
    Page<StockMove> findAllPendingByStockTypeFromAndBranchOffice(List<StockType> types, BranchOffice branchOffice, Pageable pageable);

    @Query("SELECT m FROM StockMove m WHERE m.status = 'REQUESTED' AND m.to.stock = :stock")
    Page<StockMove> findAllPendingByStockTo(Stock stock, Pageable pageable);

    @Query("SELECT m FROM StockMove m WHERE m.status = 'REQUESTED' AND m.to.stock = :stock AND m.reason IN :reasons")
    Page<StockMove> findAllPendingByStockToAndMoveReasonIn(Stock stock, List<MoveReason> reasons, Pageable pageable);

    @Query("SELECT m FROM StockMove m WHERE m.status = 'REQUESTED' AND m.to.stock.city = :city")
    Page<StockMove> findAllPendingByCityStockTo(CityOptions city, Pageable pageable);

    @Query("SELECT m FROM StockMove m WHERE m.status = 'REQUESTED' AND " +
            "(" +
            "   (m.branchOffice IS NULL AND m.to.stock.branchOffice = :branchOffice ) OR " +
            "   m.branchOffice = :branchOffice" +
            ") AND " +
            "m.to.stock.type IN :types")
    Page<StockMove> findAllPendingByStockTypeToAndBranchOffice(List<StockType> types, BranchOffice branchOffice, Pageable pageable);

    @Query("SELECT m FROM TechnicianStockMove m WHERE " +
            "m.status = 'REQUESTED' AND " +
            "m.orderId = :orderId AND " +
            "m.reason = 'SERVICE_ORDER'")
    List<? extends StockMove> findAllPendingTechnicianStockMoveByOrderId(String orderId, Sort sort);

    @Query("SELECT m FROM TechnicianStockMove m WHERE " +
            "m.orderId = :orderId AND " +
            "m.reason = 'SERVICE_ORDER'")
    List<? extends StockMove> findAllTechnicianStockMoveByOrderId(String orderId, Sort sort);

    @Query("SELECT m FROM TechnicianStockMove m WHERE " +
            "m.orderId = :orderId AND " +
            "m.activationId = :activationId AND " +
            "m.reason = 'SERVICE_ORDER'")
    List<? extends StockMove> findAllTechnicianStockMoveByOrderIdAndActivationId(String orderId, String activationId, Sort sort);

    @Query("SELECT COUNT(m) > 0 FROM StockMove m WHERE m.status = 'REQUESTED' AND ( m.from.stock = :stock OR m.to.stock = :stock )")
    Boolean existsAnyPendingMoveByStock(Stock stock);

}
