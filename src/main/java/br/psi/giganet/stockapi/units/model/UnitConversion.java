package br.psi.giganet.stockapi.units.model;

import br.psi.giganet.stockapi.config.security.model.AbstractExternalModel;
import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "unit_conversions")
public class UnitConversion extends AbstractExternalModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_unit_conversions_from"),
            name = "\"from\"",
            nullable = false,
            referencedColumnName = "id"
    )
    private Unit from;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_unit_conversions_to"),
            name = "\"to\"",
            nullable = false,
            referencedColumnName = "id"
    )
    private Unit to;

    @NotNull
    @Min(0)
    private Double conversion;

    public Boolean hasConversion(Unit unit) {
        return to != null && to.equals(unit) ||
                from != null && from.equals(unit);
    }
}
