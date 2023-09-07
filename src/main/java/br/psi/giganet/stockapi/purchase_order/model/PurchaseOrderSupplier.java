package br.psi.giganet.stockapi.purchase_order.model;

import br.psi.giganet.stockapi.common.address.model.Address;
import br.psi.giganet.stockapi.config.security.model.AbstractExternalModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CNPJ;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "purchase_order_suppliers")
public class PurchaseOrderSupplier extends AbstractExternalModel {

    @NotEmpty
    private String name;

    @Column(length = 14)
    private String cnpj;

    @Column(length = 11)
    private String cpf;

    @Column(length = 14)
    @Size(min = 2, max = 14)
    private String stateRegistration;

    @Column(length = 15)
    @Size(min = 1, max = 15)
    private String municipalRegistration;

    @NotEmpty
    private String email;

    @Column(length = 15)
    private String cellphone;

    @Column(length = 15)
    private String telephone;

    private String description;

    @NotNull
    @Embedded
    private Address address;

}
