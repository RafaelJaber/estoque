package br.psi.giganet.stockapi.purchase_order.controller.response;

import br.psi.giganet.stockapi.common.address.model.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderSupplierResponse {

    private String id;
    private String name;
    private String cnpj;
    private String stateRegistration;
    private String municipalRegistration;
    private String email;
    private String cellphone;
    private String telephone;
    private String description;
    private Address address;
}
