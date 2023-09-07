package br.psi.giganet.stockapi.purchase_order.controller.response;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.stockapi.products.controller.response.ProductProjectionWithoutUnit;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Setter
@Getter
public class OrderItemProjection {

    private ProductProjectionWithoutUnit product;
    private String purchaseOrder;
    private PurchaseOrderSupplierProjection supplier;
    private ProcessStatus status;
    private ZonedDateTime createdDate;
    private Double quantity;
    private BigDecimal price;

}
