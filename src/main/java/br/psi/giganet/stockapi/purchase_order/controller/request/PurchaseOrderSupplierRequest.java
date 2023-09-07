package br.psi.giganet.stockapi.purchase_order.controller.request;

import br.psi.giganet.stockapi.common.address.model.Address;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrderSupplierRequest {

    private String id;
    private String name;
    private String cnpj;
    private String cpf;
    private String stateRegistration;
    private String municipalRegistration;
    private String email;
    private String cellphone;
    private String telephone;
    private String description;
    private Address address;
}
