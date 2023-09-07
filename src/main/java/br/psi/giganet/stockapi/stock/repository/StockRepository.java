package br.psi.giganet.stockapi.stock.repository;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.stock.model.SellerStock;
import br.psi.giganet.stockapi.stock.model.ShedStock;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Query("SELECT s FROM Stock s WHERE s.isVisible = TRUE AND s.branchOffice = :branchOffice")
    Page<Stock> findByBranchOfficeAndIsVisible(BranchOffice branchOffice, Pageable pageable);

    @Query("SELECT s FROM Stock s WHERE s.type = :type")
    Page<Stock> findByStockType(StockType type, Pageable pageable);

    @Query("SELECT s FROM Stock s WHERE s.type = :type AND s.isVisible = TRUE AND s.branchOffice = :branchOffice")
    Page<Stock> findByStockTypeAndBranchOfficeAndIsVisible(StockType type, BranchOffice branchOffice, Pageable pageable);

    @Query("SELECT s FROM ShedStock s WHERE s.city = :option")
    Optional<ShedStock> findByCity(CityOptions option);

    @Query("SELECT s FROM TechnicianStock s WHERE s.technician.userId = :userId")
    Optional<TechnicianStock> findByUserId(String userId);

    @Query("SELECT s FROM SellerStock s WHERE s.userId = :userId")
    Optional<SellerStock> findSellerStockByUserId(String userId);

    @Query("SELECT s FROM Stock s WHERE s.isVisible = TRUE AND s.userId = :userId AND s.branchOffice = :branchOffice")
    Optional<Stock> findByUserIdBranchOffice(String userId, BranchOffice branchOffice);

    @Query("SELECT s FROM Stock s WHERE s.userId = :userId")
    Optional<Stock> findByUser(String userId);

    @Query("SELECT s FROM Stock s WHERE " +
            "s != :stock AND " +
            "s.isVisible = TRUE AND " +
            "s.type IN ('SHED', 'TECHNICIAN', 'MAINTENANCE')")
    Page<Stock> findAllAvailableToMoveByTechnician(TechnicianStock stock, Pageable pageable);

    @Query("SELECT s FROM Stock s WHERE " +
            "s != :stock AND " +
            "s.isVisible = TRUE AND " +
            "s.type IN ('SHED', 'TECHNICIAN', 'MAINTENANCE') AND " +
            "s.branchOffice = :branchOffice")
    Page<Stock> findAllAvailableToMoveByTechnician(TechnicianStock stock, BranchOffice branchOffice, Pageable pageable);

    @Query("SELECT s FROM Stock s WHERE " +
            "s.isVisible = TRUE AND " +
            "s.type IN ('SHED', 'TECHNICIAN', 'MAINTENANCE', 'DEFECTIVE', 'OBSOLETE')")
    Page<Stock> findAllAvailableToMove(Pageable pageable);

    @Query("SELECT s FROM Stock s WHERE " +
            "s.isVisible = TRUE AND " +
            "s.type IN ('SHED', 'TECHNICIAN', 'MAINTENANCE', 'DEFECTIVE', 'OBSOLETE') AND s.branchOffice = :branchOffice")
    Page<Stock> findAllAvailableToMoveByBranchOffice(BranchOffice branchOffice, Pageable pageable);
}
