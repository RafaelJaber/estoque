package br.psi.giganet.stockapi.entries.repository;

import br.psi.giganet.stockapi.entries.model.EntryItem;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface EntryItemRepository extends JpaRepository<EntryItem, Long> {

    Set<EntryItem> findByPurchaseOrderItem(PurchaseOrderItem purchaseOrderItem);

    @Query("SELECT i FROM EntryItem i WHERE i.product = :product ORDER BY i.lastModifiedDate DESC")
    Page<EntryItem> findLastEntryItemByProduct(Product product, Pageable pageable);

}
