package br.psi.giganet.stockapi.common.valid_mac_addresses.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "valid_mac_addresses")
public class ValidMacAddress {

    @Id
    private String address;
    @NotNull
    private Boolean isUsed;

    public Boolean isUsed() {
        return this.isUsed;
    }

}
