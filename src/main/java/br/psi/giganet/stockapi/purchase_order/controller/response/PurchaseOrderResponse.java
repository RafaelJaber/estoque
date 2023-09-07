package br.psi.giganet.stockapi.purchase_order.controller.response;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderResponse {

    private String id;
    private ProcessStatus status;
    private String responsible;
    private String costCenter;
    private String description;
    private ZonedDateTime date;
    private LocalDate dateOfNeed;
    private List<PurchaseOrderItemResponse> items;
    private BigDecimal total;
    private String note;
    private OrderFreightResponse freight;
    private PurchaseOrderSupplierProjection supplier;

}
