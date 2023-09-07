package br.psi.giganet.stockapi.entries.repository;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.entries.model.Entry;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {

    Set<Entry> findByPurchaseOrder(PurchaseOrder purchaseOrder);

    List<Entry> findAllByBranchOffice(BranchOffice branchOffice, Sort sort);
}
