package br.psi.giganet.stockapi.entries.controller.response;

import br.psi.giganet.stockapi.entries.model.enums.EntryStatus;
import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import br.psi.giganet.stockapi.purchase_order.controller.response.PurchaseOrderItemResponse;
import br.psi.giganet.stockapi.purchase_order.controller.response.PurchaseOrderSupplierProjection;
import br.psi.giganet.stockapi.units.controller.response.UnitProjection;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EntryItemResponse {

    private Long id;
    private String documentProductCode;
    private ProductProjection product;
    private PurchaseOrderSupplierProjection supplier;
    private Double quantity;
    private UnitProjection unit;
    private Float icms;
    private Float ipi;
    private BigDecimal price;
    private BigDecimal total;
    private EntryStatus status;

}
