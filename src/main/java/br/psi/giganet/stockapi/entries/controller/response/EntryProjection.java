package br.psi.giganet.stockapi.entries.controller.response;

import br.psi.giganet.stockapi.employees.controller.response.EmployeeProjection;
import br.psi.giganet.stockapi.entries.model.enums.EntryStatus;
import br.psi.giganet.stockapi.purchase_order.controller.response.PurchaseOrderProjection;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class EntryProjection {

    private Long id;
    private PurchaseOrderProjection purchaseOrder;
    private EmployeeProjection responsible;
    private String fiscalDocumentNumber;
    private String note;
    private ZonedDateTime date;
    private EntryStatus status;

}
