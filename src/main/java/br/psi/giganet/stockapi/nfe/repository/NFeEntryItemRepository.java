package br.psi.giganet.stockapi.nfe.repository;

import br.psi.giganet.stockapi.nfe.model.NFeEntryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NFeEntryItemRepository extends JpaRepository<NFeEntryItem, Long> {

    @Query("SELECT e FROM NFeEntryItem e WHERE " +
            "e.product.id = :product AND " +
            "e.supplier.cnpj = :supplier AND " +
            "e.documentProductCode = :documentProductCode")
    Optional<NFeEntryItem> findByProductAndSupplierAndDocumentProductCode(Long product, String supplier, String documentProductCode);

}
