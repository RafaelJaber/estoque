package br.psi.giganet.stockapi.purchase_order.repository;

import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrderItem;
import br.psi.giganet.stockapi.purchase_order.repository.dto.OrderItemDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, String> {

    @Query("SELECT i FROM PurchaseOrderItem i WHERE " +
            "i.product.code = :product")
    List<PurchaseOrderItem> findLastByProduct(String product, Pageable pageable);

    @Query("SELECT i FROM PurchaseOrderItem i WHERE " +
            "i.product = :product")
    List<PurchaseOrderItem> findLastByProduct(Product product, Pageable pageable);

    @Query("SELECT " +
            "   i.product.code AS productCode, " +
            "   i.product.name AS productName, " +
            "   i.order.supplier.id AS supplierId, " +
            "   i.order.supplier.name AS supplierName, " +
            "   i.order.id AS purchaseOrder, " +
            "   i.status AS status, " +
            "   i.order.externalCreatedDate AS createdDate, " +
            "   i.quantity AS quantity, " +
            "   i.price AS price " +
            "FROM PurchaseOrderItem i")
    Page<OrderItemDTO> findAllFetch(Pageable pageable);

}
