package br.psi.giganet.stockapi.purchase_order.controller.request;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.stockapi.products.controller.request.ProductWebHookRequest;
import br.psi.giganet.stockapi.units.controller.request.UnitWebhookRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class PurchaseOrderItemWebhookRequest {

    private String id;
    private ProductWebHookRequest product;
    private Double quantity;
    private UnitWebhookRequest unit;
    private BigDecimal price;
    private Float ipi;
    private Float icms;
    private BigDecimal discount;
    private BigDecimal total;
    private ProcessStatus status;

}
