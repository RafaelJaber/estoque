package br.psi.giganet.stockapi.entries.controller.response;

import br.psi.giganet.stockapi.employees.controller.response.EmployeeProjection;
import br.psi.giganet.stockapi.entries.model.enums.EntryStatus;
import br.psi.giganet.stockapi.purchase_order.controller.response.PurchaseOrderProjection;
import br.psi.giganet.stockapi.purchase_order.controller.response.PurchaseOrderResponse;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class EntryResponse {

    private Long id;
    private PurchaseOrderProjection purchaseOrder;
    private EmployeeProjection responsible;
    private String fiscalDocumentNumber;
    private List<EntryItemResponse> items;
    private String note;
    private ZonedDateTime date;
    private EntryStatus status;
    private Boolean isManual;

}
