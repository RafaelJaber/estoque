package br.psi.giganet.stockapi.purchase_order.controller.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PurchaseOrderSupplierProjection {

    @EqualsAndHashCode.Include
    private String id;
    private String name;

}
