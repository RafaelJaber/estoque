package br.psi.giganet.stockapi.purchase_order.controller.response;

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
public class OrderFreightResponse {

    private String id;
    private FreightType type;
    private BigDecimal price;
    private String deliveryDate;
    private Address deliveryAddress;

}
