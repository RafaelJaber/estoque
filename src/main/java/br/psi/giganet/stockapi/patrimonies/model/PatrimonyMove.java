package br.psi.giganet.stockapi.patrimonies.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "patrimony_moves")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PatrimonyMove extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_patrimony_moves_from"),
            name = "\"from\"",
            referencedColumnName = "id")
    private PatrimonyLocation from;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_patrimony_moves_to"),
            name = "\"to\"",
            referencedColumnName = "id")
    private PatrimonyLocation to;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_patrimony_moves_responsible"),
            name = "\"responsible\"",
            referencedColumnName = "id")
    private Employee responsible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_patrimony_moves_patrimony"),
            name = "patrimony",
            nullable = false,
            referencedColumnName = "id")
    private Patrimony patrimony;

    private String note;

}
