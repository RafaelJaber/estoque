package br.psi.giganet.stockapi.stock.repository;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.StockItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {


    @Query("SELECT i FROM StockItem i  WHERE " +
            "i.stock = :stock AND " +
            "(UPPER(i.product.name) LIKE CONCAT('%', UPPER(:name) ,'%') AND " +
            "UPPER(i.product.code) LIKE CONCAT('%', UPPER(:code) ,'%'))")
    Page<StockItem> findByStockAndNameContainingAndCode(Stock stock, String name, String code, Pageable pageable);

    @Query("SELECT i FROM StockItem i  WHERE " +
            "i.stock = :stock AND " +
            "i.quantity > 0 AND " +
            "(UPPER(i.product.name) LIKE CONCAT('%', UPPER(:name) ,'%') AND " +
            "UPPER(i.product.code) LIKE CONCAT('%', UPPER(:code) ,'%'))")
    Page<StockItem> findByStockAndNameContainingAndCodeFilteringEmpties(Stock stock, String name, String code, Pageable pageable);

    @Query("SELECT i FROM StockItem i  WHERE " +
            "i.stock = :stock AND " +
            "i.quantity > 0 AND i.quantity > i.blockedQuantity AND " +
            "(UPPER(i.product.name) LIKE CONCAT('%', UPPER(:name) ,'%') AND " +
            "UPPER(i.product.code) LIKE CONCAT('%', UPPER(:code) ,'%'))")
    Page<StockItem> findByStockAndNameContainingAndCodeFilteringAvailable(Stock stock, String name, String code, Pageable pageable);

    @Query("SELECT i FROM StockItem i  WHERE " +
            "i.stock = :stock AND " +
            "i.product.code LIKE :code")
    Optional<StockItem> findByStockAndCode(Stock stock, String code);

    @Query("SELECT i FROM StockItem i  WHERE " +
            "i.stock = :stock AND " +
            "i.product.id LIKE :id")
    Optional<StockItem> findByStockAndProductId(Stock stock, String id);

    @Query("SELECT p AS product, SUM(i.quantity) AS quantity, SUM(i.quantity * i.lastPricePerUnit) AS price FROM StockItem i " +
            "INNER JOIN i.product p " +
            "WHERE UPPER(p.name) LIKE CONCAT('%', UPPER(:name), '%') AND " +
            "i.stock.type != 'CUSTOMER' AND i.stock.branchOffice = :branchOffice " +
            "GROUP BY p")
    Page<GeneralStockItem> findByProductAndBranchOfficeGroupByProduct(String name, BranchOffice branchOffice, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE StockItem i SET i.lastPricePerUnit = :newPrice WHERE i.product = :product")
    void updatePricePerUnitByProduct(Product product, BigDecimal newPrice);
}
