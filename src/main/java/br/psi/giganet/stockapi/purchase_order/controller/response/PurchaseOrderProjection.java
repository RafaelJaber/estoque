package br.psi.giganet.stockapi.purchase_order.controller.response;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderProjection {

    private String id;
    private ProcessStatus status;
    private String responsible;
    private String description;
    private ZonedDateTime date;
    private BigDecimal total;
    private PurchaseOrderSupplierProjection supplier;

}
