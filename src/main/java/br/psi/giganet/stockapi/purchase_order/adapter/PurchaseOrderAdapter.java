package br.psi.giganet.stockapi.purchase_order.adapter;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.stockapi.employees.adapter.EmployeeAdapter;
import br.psi.giganet.stockapi.entries.model.EntryItem;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import br.psi.giganet.stockapi.purchase_order.controller.request.PurchaseOrderItemWebhookRequest;
import br.psi.giganet.stockapi.purchase_order.controller.request.PurchaseOrderWebhookRequest;
import br.psi.giganet.stockapi.purchase_order.controller.response.*;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrder;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrderFreight;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrderItem;
import br.psi.giganet.stockapi.purchase_order.repository.PurchaseOrderRepositoryProjection;
import br.psi.giganet.stockapi.purchase_order.repository.dto.OrderItemDTO;
import br.psi.giganet.stockapi.units.adapter.UnitAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class PurchaseOrderAdapter {

    @Autowired
    private EmployeeAdapter employeeAdapter;
    @Autowired
    private ProductAdapter productAdapter;
    @Autowired
    private SupplierAdapter supplierAdapter;
    @Autowired
    private UnitAdapter unitAdapter;

    public PurchaseOrder createOrder(String id) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(id);
        return order;
    }

    public PurchaseOrderItem createOrderItem(String id) {
        PurchaseOrderItem order = new PurchaseOrderItem();
        order.setId(id);
        return order;
    }


    @Transactional
    public PurchaseOrderResponse transformToFullResponse(final PurchaseOrder order) {
        return transformToFullResponse(order, false);
    }

    @Transactional
    public PurchaseOrderProjection transform(final PurchaseOrder order) {
        final PurchaseOrderProjection projection = new PurchaseOrderProjection();
        projection.setId(order.getId());
        projection.setStatus(order.getStatus());
        projection.setResponsible(order.getResponsible());
        projection.setDescription(order.getDescription());
        projection.setSupplier(supplierAdapter.transform(order.getSupplier()));
        projection.setDate(order.getExternalCreatedDate());
        projection.setTotal(order.getTotal());

        return projection;
    }

    public PurchaseOrderProjection transform(final PurchaseOrderRepositoryProjection order) {
        final PurchaseOrderProjection projection = new PurchaseOrderProjection();
        projection.setId(order.getId());
        projection.setStatus(order.getStatus());
        projection.setResponsible(order.getResponsible());
        projection.setDescription(order.getDescription());
        projection.setSupplier(supplierAdapter.transform(order.getSupplierId(), order.getSupplierName()));
        projection.setDate(order.getDate());
        projection.setTotal(order.getTotal());

        return projection;
    }

    @Transactional
    public PurchaseOrderItemResponse transform(PurchaseOrderItem item) {
        final PurchaseOrderItemResponse i = new PurchaseOrderItemResponse();
        i.setId(item.getId());
        i.setPrice(item.getPrice());
        i.setProduct(productAdapter.transform(item.getProduct()));
        i.setQuantity(item.getQuantity());
        i.setUnit(unitAdapter.transform(item.getUnit()));
        i.setIcms(item.getIcms());
        i.setIpi(item.getIpi());
        i.setStatus(item.getStatus());
        i.setDiscount(item.getDiscount());
        i.setTotal(item.getTotal());
        return i;
    }

    public OrderItemProjection transformToOrderItemProjection(OrderItemDTO item) {
        final OrderItemProjection i = new OrderItemProjection();
        i.setCreatedDate(item.getCreatedDate());
        i.setProduct(productAdapter.transformWithoutUnit(item.getProductCode(), item.getProductName()));
        i.setSupplier(supplierAdapter.transform(item.getSupplierId(), item.getSupplierName()));
        i.setStatus(item.getStatus());
        i.setPurchaseOrder(item.getPurchaseOrder());
        i.setQuantity(item.getQuantity());
        i.setPrice(item.getPrice());
        return i;
    }

    @Transactional
    public PurchaseOrderItem transform(PurchaseOrderItemWebhookRequest item) {
        final PurchaseOrderItem i = new PurchaseOrderItem();
        i.setId(item.getId());
        i.setPrice(item.getPrice());
        i.setProduct(productAdapter.transform(item.getProduct()));
        i.setQuantity(item.getQuantity());
        i.setUnit(unitAdapter.transform(item.getUnit()));
        i.setIcms(item.getIcms());
        i.setIpi(item.getIpi());
        i.setStatus(item.getStatus());
        i.setDiscount(item.getDiscount());
        i.setTotal(item.getTotal());
        return i;
    }

    @Transactional
    public PurchaseOrderResponse transformToFullResponse(final PurchaseOrder order, boolean filterPendingItems) {
        final PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.setId(order.getId());
        response.setSupplier(supplierAdapter.transform(order.getSupplier()));
        response.setStatus(order.getStatus());
        response.setResponsible(order.getResponsible());
        response.setCostCenter(order.getCostCenter());
        response.setDescription(order.getDescription());
        response.setDateOfNeed(order.getDateOfNeed());
        response.setDate(order.getExternalCreatedDate());
        response.setTotal(order.getTotal());
        response.setNote(order.getNote());
        response.setItems(
                order.getItems()
                        .stream()
                        .filter(item -> !filterPendingItems || item.getStatus().equals(ProcessStatus.PENDING) || item.getStatus().equals(ProcessStatus.PARTIALLY_RECEIVED))
                        .map(item -> {
                            final PurchaseOrderItemResponse i = new PurchaseOrderItemResponse();

                            i.setId(item.getId());
                            i.setPrice(item.getPrice());
                            i.setProduct(productAdapter.transform(item.getProduct()));
                            i.setQuantity(!filterPendingItems || item.getEntries() == null ? item.getQuantity() :
                                    item.getQuantity() - item.getEntries().stream()
                                            .map(EntryItem::getQuantity)
                                            .reduce(Double::sum)
                                            .orElse(0d));
                            i.setUnit(unitAdapter.transform(item.getUnit()));
                            i.setIcms(item.getIcms());
                            i.setIpi(item.getIpi());
                            i.setStatus(item.getStatus());
                            i.setDiscount(item.getDiscount());
                            i.setTotal(item.getTotal());

                            return i;
                        })
                        .collect(Collectors.toList()));

        response.setFreight(new OrderFreightResponse());
        response.getFreight().setId(order.getFreight().getId());
        response.getFreight().setDeliveryAddress(order.getFreight().getDeliveryAddress());
        response.getFreight().setDeliveryDate(order.getFreight().getDeliveryDate() != null ?
                order.getFreight().getDeliveryDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
        response.getFreight().setPrice(order.getFreight().getPrice());
        response.getFreight().setType(order.getFreight().getType());

        return response;
    }

    @SuppressWarnings("unchecked")
    public PurchaseOrder transform(PurchaseOrderWebhookRequest request) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(request.getId());
        order.setStatus(request.getStatus());
        order.setResponsible(request.getResponsible().getName());
        order.setCostCenter(request.getCostCenter().getName());
        order.setExternalCreatedDate(request.getDate() != null ? ZonedDateTime.parse(request.getDate()) : null);
        order.setDateOfNeed(request.getDateOfNeed() != null ? LocalDate.parse(request.getDateOfNeed()) : null);
        order.setTotal(request.getTotal());
        order.setNote(request.getNote());
        order.setDescription(request.getDescription());

        order.setFreight(new PurchaseOrderFreight());
        order.getFreight().setDeliveryAddress(request.getFreight().getDeliveryAddress());
        order.getFreight().setDeliveryDate(request.getFreight().getDeliveryDate() != null ?
                ZonedDateTime.parse(request.getFreight().getDeliveryDate()) : null);
        order.getFreight().setOrder(order);
        order.getFreight().setPrice(request.getFreight().getPrice());
        order.getFreight().setType(request.getFreight().getType());
        order.getFreight().setId(request.getFreight().getId());

        order.setSupplier(supplierAdapter.transform(request.getSupplier()));

        order.setItems(request.getItems().stream()
                .map(this::transform)
                .peek(i -> i.setOrder(order))
                .collect(Collectors.toList()));


        return order;
    }

}
