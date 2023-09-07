package br.psi.giganet.stockapi.common.address.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Address {

    @NotEmpty
    @Column(length = 8)
    @Pattern(regexp = "^[0-9]{8}$")
    private String postalCode;
    private String complement;
    @NotEmpty
    private String street;
    @NotEmpty
    private String number;
    @NotEmpty
    private String district;
    @NotEmpty
    private String city;
    @NotEmpty
    private String state;

}
