package br.psi.giganet.stockapi.purchase_order.repository;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface PurchaseOrderRepositoryProjection {

    String getId();

    ProcessStatus getStatus();

    String getResponsible();

    String getDescription();

    ZonedDateTime getDate();

    BigDecimal getTotal();

    String getSupplierId();
    String getSupplierName();

}
