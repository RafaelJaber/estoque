package br.psi.giganet.stockapi.purchase_order.service;

import br.psi.giganet.stockapi.common.address.service.AddressService;
import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.stockapi.common.webhooks.service.WebHooksHandlerService;
import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.entries.model.Entry;
import br.psi.giganet.stockapi.entries.model.EntryItem;
import br.psi.giganet.stockapi.entries.service.EntryService;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrder;
import br.psi.giganet.stockapi.purchase_order.repository.PurchaseOrderItemRepository;
import br.psi.giganet.stockapi.purchase_order.repository.PurchaseOrderRepository;
import br.psi.giganet.stockapi.purchase_order.repository.PurchaseOrderRepositoryProjection;
import br.psi.giganet.stockapi.purchase_order.repository.dto.OrderItemDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository orders;

    @Autowired
    private PurchaseOrderItemRepository orderItems;

    @Autowired
    private AddressService addressService;

    @Autowired
    private WebHooksHandlerService webHooksHandlerService;

    @Autowired
    private EntryService entryService;

    public List<PurchaseOrder> findAll() {
        return this.orders.findAll(Sort.by(Sort.Direction.DESC, "externalCreatedDate"));
    }

    public List<PurchaseOrderRepositoryProjection> findAllFetchAsProjection() {
        return this.orders.findAllFetchAsProjection(Sort.by(Sort.Direction.DESC, "externalCreatedDate"));
    }

    public Page<OrderItemDTO> findAllItemFetchAsDTO(Integer page, Integer pageSize) {
        return this.orderItems.findAllFetch(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "order.externalCreatedDate")));
    }

    public List<PurchaseOrder> findAllPendingToReceive() {
        return this.orders.findAllPendingToReceive(
                Arrays.asList(ProcessStatus.PENDING, ProcessStatus.REALIZED, ProcessStatus.IN_TRANSIT, ProcessStatus.PARTIALLY_RECEIVED),
                Sort.by(Sort.Direction.DESC, "externalCreatedDate"));
    }

    public Optional<PurchaseOrder> findById(final String id) {
        return this.orders.findById(id);
    }

    public void save(PurchaseOrder order) {
        this.findById(order.getId()).ifPresentOrElse(
                saved -> {

                    BeanUtils.copyProperties(order.getSupplier(), saved.getSupplier(), "id", "createdDate", "lastModifiedDate");
                    BeanUtils.copyProperties(order.getFreight(), saved.getFreight(), "id", "order");
                    BeanUtils.copyProperties(order, saved, "id", "createdDate", "lastModifiedDate", "items", "supplier", "freight");

                    order.getItems().forEach(item -> {
                        int index = saved.getItems().indexOf(item);
                        if (index < 0) {
                            throw new IllegalArgumentException("Item " + item.getId() + " não foi encontrado");
                        }
                        BeanUtils.copyProperties(item, saved.getItems().get(index),
                                "id", "createdDate", "lastModifiedDate", "product", "order");
                    });

                    orders.save(saved);
                },
                () -> orders.save(order));
    }

    public void onInsertEntry(Entry entry) {
        entry.getItems().forEach(item -> {
            if (entryService.findItemsByPurchaseOrder(item.getPurchaseOrderItem())
                    .stream()
                    .map(EntryItem::getQuantity)
                    .reduce(Double::sum)
                    .orElse(0d)
                    .equals(item.getPurchaseOrderItem().getQuantity())) {
                item.getPurchaseOrderItem().setStatus(ProcessStatus.RECEIVED);

            } else {
                item.getPurchaseOrderItem().setStatus(ProcessStatus.PARTIALLY_RECEIVED);

            }
        });

        entry.getPurchaseOrder().setStatus(entry.getPurchaseOrder().getItems().stream()
                .allMatch(i -> i.getStatus().equals(ProcessStatus.RECEIVED)) ?
                ProcessStatus.RECEIVED : ProcessStatus.PARTIALLY_RECEIVED);

        webHooksHandlerService.onSendPurchaseOrder(orders.save(entry.getPurchaseOrder()));
    }
}
