package br.psi.giganet.stockapi.purchase_order.controller.request;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.stockapi.products.controller.request.ProductProjectionWebhookRequest;
import br.psi.giganet.stockapi.units.controller.request.UnitProjectionWebhookRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class PurchaseOrderItemRequest {

    private Long id;
    private ProductProjectionWebhookRequest product;
    private Double quantity;
    private UnitProjectionWebhookRequest unit;
    private BigDecimal price;
    private Float ipi;
    private Float icms;
    private BigDecimal total;
    private ProcessStatus status;

}
