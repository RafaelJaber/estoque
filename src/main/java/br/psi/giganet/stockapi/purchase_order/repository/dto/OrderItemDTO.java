package br.psi.giganet.stockapi.purchase_order.repository.dto;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface OrderItemDTO {

    String getProductCode();

    String getProductName();

    String getPurchaseOrder();

    String getSupplierId();

    String getSupplierName();

    ProcessStatus getStatus();

    ZonedDateTime getCreatedDate();

    Double getQuantity();

    BigDecimal getPrice();

}
