package br.psi.giganet.stockapi.purchase_order.controller.request;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.stockapi.employees.controller.response.EmployeeProjection;
import br.psi.giganet.stockapi.purchase_order.controller.response.OrderFreightResponse;
import br.psi.giganet.stockapi.purchase_order.controller.response.PurchaseOrderItemResponse;
import br.psi.giganet.stockapi.purchase_order.controller.response.PurchaseOrderSupplierProjection;
import br.psi.giganet.stockapi.purchase_order.controller.response.PurchaseOrderSupplierResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrderWebhookRequest {

    private String id;
    private ProcessStatus status;
    private String description;
    private EmployeeProjection responsible;
    private CostCenterWebhookRequest costCenter;
    private String date;
    private String dateOfNeed;
    private List<PurchaseOrderItemWebhookRequest> items;
    private BigDecimal total;
    private String note;
    private OrderFreightResponse freight;
    private PurchaseOrderSupplierRequest supplier;

}
