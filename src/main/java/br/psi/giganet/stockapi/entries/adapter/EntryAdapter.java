package br.psi.giganet.stockapi.entries.adapter;

import br.psi.giganet.stockapi.employees.adapter.EmployeeAdapter;
import br.psi.giganet.stockapi.entries.controller.request.InsertEntryRequest;
import br.psi.giganet.stockapi.entries.controller.response.*;
import br.psi.giganet.stockapi.entries.model.Entry;
import br.psi.giganet.stockapi.entries.model.EntryItem;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import br.psi.giganet.stockapi.purchase_order.adapter.PurchaseOrderAdapter;
import br.psi.giganet.stockapi.purchase_order.adapter.SupplierAdapter;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.units.adapter.UnitAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

@Component
public class EntryAdapter {

    @Autowired
    private PurchaseOrderAdapter purchaseOrderAdapter;

    @Autowired
    private EmployeeAdapter employeeAdapter;

    @Autowired
    private ProductAdapter productAdapter;

    @Autowired
    private SupplierAdapter supplierAdapter;

    @Autowired
    private UnitAdapter unitAdapter;

    @Autowired
    private StockFactory stockFactory;

    public Entry transform(InsertEntryRequest request) {
        final Entry entry = new Entry();
        entry.setIsManual(request.getIsManual());
        entry.setPurchaseOrder(purchaseOrderAdapter.createOrder(request.getOrder()));
        entry.setFiscalDocumentNumber(request.getFiscalDocument());
        entry.setDocumentAccessCode(request.getDocumentAccessCode());
        entry.setNote(request.getNote());
        entry.setItems(
                request.getItems().stream()
                        .map(item -> {

                            final EntryItem entryItem = new EntryItem();
                            entryItem.setEntry(entry);
                            entryItem.setPurchaseOrderItem(purchaseOrderAdapter.createOrderItem(item.getOrderItem()));
                            entryItem.setDocumentProductCode(item.getDocumentProductCode());
                            entryItem.setQuantity(item.getReceivedQuantity());

                            return entryItem;
                        })
                        .collect(Collectors.toList()));

        return entry;
    }

    @Transactional
    public EntryResponse transformToFullResponse(Entry entry) {
        final EntryResponse response = new EntryResponse();
        response.setId(entry.getId());
        response.setPurchaseOrder(purchaseOrderAdapter.transform(entry.getPurchaseOrder()));
        response.setResponsible(employeeAdapter.transform(entry.getResponsible()));
        response.setFiscalDocumentNumber(entry.getFiscalDocumentNumber());
        response.setNote(entry.getNote());
        response.setDate(entry.getCreatedDate());
        response.setStatus(entry.getStatus());
        response.setIsManual(entry.getIsManual());
        response.setItems(
                entry.getItems().stream()
                        .map(item -> {
                            final EntryItemResponse itemResponse = new EntryItemResponse();
                            itemResponse.setId(item.getId());
                            itemResponse.setProduct(productAdapter.transform(item.getProduct()));
                            itemResponse.setSupplier(supplierAdapter.transform(item.getSupplier()));
                            itemResponse.setQuantity(item.getQuantity());
                            itemResponse.setUnit(unitAdapter.transform(item.getUnit()));
                            itemResponse.setIcms(item.getIcms());
                            itemResponse.setIpi(item.getIpi());
                            itemResponse.setPrice(item.getPrice());
                            itemResponse.setTotal(item.getTotal());
                            itemResponse.setStatus(item.getStatus());
                            itemResponse.setDocumentProductCode(item.getDocumentProductCode());

                            return itemResponse;
                        })
                        .collect(Collectors.toList())
        );

        return response;
    }

    @Transactional
    public EntryItemWithMetaDataProjection transformToEntryItemWithMetaDataProjection(EntryItem item) {
        EntryItemWithMetaDataProjection response = new EntryItemWithMetaDataProjection();
        response.setEntryItem(new EntryItemProjection());
        response.getEntryItem().setId(item.getId());
        response.getEntryItem().setProduct(productAdapter.transform(item.getProduct()));
        response.getEntryItem().setQuantity(item.getQuantity());
        response.setRegisteredPatrimonies(item.getPatrimonies() != null ? item.getPatrimonies().size() : 0d);

        return response;
    }

    @Transactional
    public ItemWithEntryProjection transformToItemWithEntryProjection(EntryItem item) {
        ItemWithEntryProjection response = new ItemWithEntryProjection();
        response.setEntry(item.getEntry().getId());
        response.setFiscalDocumentNumber(item.getEntry().getFiscalDocumentNumber());
        response.setId(item.getId());
        return response;
    }

    public EntryProjection transform(Entry entry) {
        final EntryProjection response = new EntryProjection();
        response.setId(entry.getId());
        response.setPurchaseOrder(purchaseOrderAdapter.transform(entry.getPurchaseOrder()));
        response.setResponsible(employeeAdapter.transform(entry.getResponsible()));
        response.setFiscalDocumentNumber(entry.getFiscalDocumentNumber());
        response.setNote(entry.getNote());
        response.setDate(entry.getCreatedDate());
        response.setStatus(entry.getStatus());

        return response;
    }

}
