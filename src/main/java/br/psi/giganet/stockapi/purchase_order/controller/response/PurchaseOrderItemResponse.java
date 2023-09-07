package br.psi.giganet.stockapi.purchase_order.controller.response;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import br.psi.giganet.stockapi.units.controller.response.UnitProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class PurchaseOrderItemResponse {

    private String id;
    private ProductProjection product;
    private Double quantity;
    private UnitProjection unit;
    private BigDecimal price;
    private Float ipi;
    private Float icms;
    private BigDecimal discount;
    private BigDecimal total;
    private ProcessStatus status;

}
