package br.psi.giganet.stockapi.stock.model;

import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class ObsoleteStock extends Stock {

    @Enumerated(EnumType.STRING)
    @NotNull
    private CityOptions city;

}
