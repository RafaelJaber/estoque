package br.psi.giganet.stockapi.patrimonies_locations.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "patrimony_locations")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PatrimonyLocation extends AbstractModel {

    @NotEmpty
    private String name;
    @NotEmpty
    @Column(unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @NotNull
    private PatrimonyLocationType type;

    private String note;

}
