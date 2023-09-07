package br.psi.giganet.stockapi.entries.service;

import br.psi.giganet.stockapi.branch_offices.service.BranchOfficeService;
import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.entries.model.Entry;
import br.psi.giganet.stockapi.entries.model.EntryItem;
import br.psi.giganet.stockapi.entries.model.enums.EntryStatus;
import br.psi.giganet.stockapi.entries.repository.EntryItemRepository;
import br.psi.giganet.stockapi.entries.repository.EntryRepository;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrderItem;
import br.psi.giganet.stockapi.purchase_order.service.PurchaseOrderService;
import br.psi.giganet.stockapi.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class EntryService {

    @Autowired
    private EntryRepository entries;
    @Autowired
    private EntryItemRepository entryItemRepository;

    @Autowired
    private EmployeeService employees;

    @Autowired
    private PurchaseOrderService purchaseOrders;

    @Autowired
    private StockService stockService;

    @Autowired
    private BranchOfficeService branchOfficeService;

    public List<Entry> findAll() {
        return entries.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public List<Entry> findAllByCurrentBranchOffice() {
        return entries.findAllByBranchOffice(
                branchOfficeService.getCurrentBranchOffice()
                        .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")),
                Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public Optional<Entry> findById(Long id) {
        return entries.findById(id);
    }

    public Optional<EntryItem> findByEntryItemId(Long id) {
        return entryItemRepository.findById(id);
    }

    @Transactional
    public Optional<Entry> insert(Entry entry, Boolean updateStock) {
        entry.setPurchaseOrder(purchaseOrders.findById(entry.getPurchaseOrder().getId())
                .orElseThrow(() -> new IllegalArgumentException("Pedido de compra não encontrado")));

        if (entry.getPurchaseOrder().getStatus().equals(ProcessStatus.CANCELED) ||
                entry.getPurchaseOrder().getStatus().equals(ProcessStatus.FINALIZED) ||
                entry.getPurchaseOrder().getStatus().equals(ProcessStatus.RECEIVED)) {
            throw new IllegalArgumentException("Não é possivel realizar este lançamento. Ordem de compra já encerrada");
        }

        entry.setResponsible(
                employees.getCurrentLoggedEmployee()
                        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado")));

        entry.setBranchOffice(branchOfficeService.getCurrentBranchOffice()
                .orElseThrow(() -> new IllegalArgumentException("Filial não encontrada")));

        entry.setStock(entry.getBranchOffice().shed()
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado")));

        entry.getItems().forEach(item -> {
            item.setPurchaseOrderItem(
                    entry.getPurchaseOrder().getItems()
                            .stream()
                            .filter(i -> i.equals(item.getPurchaseOrderItem()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Item do pedido de compra " + item.getPurchaseOrderItem().getId() + " não encontrado")));
            item.setProduct(item.getPurchaseOrderItem().getProduct());

            Double received = entryItemRepository.findByPurchaseOrderItem(item.getPurchaseOrderItem())
                    .stream()
                    .map(EntryItem::getQuantity)
                    .reduce(Double::sum)
                    .orElse(0d);

            if (received.equals(item.getPurchaseOrderItem().getQuantity())) {
                throw new IllegalArgumentException("Não é possivel realizar este lançamento." +
                        " A quantidade presente na ordem de compra já foi completamente lançada para o item " + item.getProduct().getName());
            } else if (received + item.getQuantity() > item.getPurchaseOrderItem().getQuantity()) {
                throw new IllegalArgumentException("Não é possivel realizar este lançamento." +
                        " A quantidade informada para o item " + item.getProduct().getName() + " é superior a quantidade comprada");
            }

            item.setStatus(EntryStatus.RECEIVED);

            item.setSupplier(item.getPurchaseOrderItem().getOrder().getSupplier());
            item.setUnit(item.getPurchaseOrderItem().getUnit());
            item.setIcms(item.getPurchaseOrderItem().getIcms());
            item.setIpi(item.getPurchaseOrderItem().getIpi());
            item.setPrice(item.getPurchaseOrderItem().getPrice());
            item.setTotal(item.getPurchaseOrderItem().getTotal());
        });

        entry.setStatus(EntryStatus.RECEIVED);

        Entry saved = this.entries.save(entry);

        if (updateStock) {
            stockService.addItemsToStockByEntry(saved);
            saved = this.entries.save(saved);
        }

        purchaseOrders.onInsertEntry(saved);

        return Optional.of(saved);
    }

    public Set<EntryItem> findItemsByPurchaseOrder(PurchaseOrderItem item) {
        return this.entryItemRepository.findByPurchaseOrderItem(item);
    }

    public Optional<EntryItem> findLastEntryItemByProduct(Product product) {
        return this.entryItemRepository.findLastEntryItemByProduct(product, PageRequest.of(0, 1))
                .stream()
                .findFirst();
    }

}
