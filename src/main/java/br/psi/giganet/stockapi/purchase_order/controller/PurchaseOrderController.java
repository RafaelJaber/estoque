package br.psi.giganet.stockapi.purchase_order.controller;

import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.purchase_order.adapter.PurchaseOrderAdapter;
import br.psi.giganet.stockapi.purchase_order.controller.response.OrderItemProjection;
import br.psi.giganet.stockapi.purchase_order.controller.response.PurchaseOrderProjection;
import br.psi.giganet.stockapi.purchase_order.controller.response.PurchaseOrderResponse;
import br.psi.giganet.stockapi.purchase_order.controller.security.RolePurchaseOrdersRead;
import br.psi.giganet.stockapi.purchase_order.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService orders;

    @Autowired
    private PurchaseOrderAdapter adapter;

    @GetMapping
    @RolePurchaseOrdersRead
    public List<PurchaseOrderProjection> findAll() {
        return orders.findAllFetchAsProjection()
                .stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/items")
    @RolePurchaseOrdersRead
    public Page<OrderItemProjection> findAllItems(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return orders.findAllItemFetchAsDTO(page, pageSize)
                .map(adapter::transformToOrderItemProjection);
    }

    @GetMapping("/pending")
    @RolePurchaseOrdersRead
    public List<PurchaseOrderProjection> findAllPendingToReceive() {
        return orders.findAllPendingToReceive()
                .stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RolePurchaseOrdersRead
    public PurchaseOrderResponse findById(@PathVariable String id) {
        return this.orders.findById(id)
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de compra não encontrada"));
    }

    @GetMapping("/{id}/pending")
    @RolePurchaseOrdersRead
    public PurchaseOrderResponse findByIdFilteringPendingItems(@PathVariable String id) {
        return this.orders.findById(id)
                .map(order -> adapter.transformToFullResponse(order, true))
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de compra não encontrada"));
    }

}
