package br.psi.giganet.stockapi.purchase_order.controller.request;

import br.psi.giganet.stockapi.common.address.model.Address;
import br.psi.giganet.stockapi.purchase_order.model.enums.FreightType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderFreightWebhookRequest {

    private Long id;
    private FreightType type;
    private BigDecimal price;
    private ZonedDateTime deliveryDate;
    private Address deliveryAddress;

}
