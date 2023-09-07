package br.psi.giganet.stockapi.purchase_order.repository;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {

    @Query("SELECT o FROM PurchaseOrder o WHERE o.status IN :statuses")
    List<PurchaseOrder> findAllPendingToReceive(List<ProcessStatus> statuses, Sort sort);

    @Query("SELECT " +
            "   o.id AS id, " +
            "   o.status AS status, " +
            "   o.responsible AS responsible, " +
            "   o.description AS description, " +
            "   o.externalCreatedDate AS date, " +
            "   o.total AS total, " +
            "   sup.id AS supplierId, " +
            "   sup.name AS supplierName " +
            "FROM PurchaseOrder o " +
            "JOIN FETCH PurchaseOrderSupplier sup ON o.supplier = sup")
    List<PurchaseOrderRepositoryProjection> findAllFetchAsProjection(Sort sort);

}
