package br.psi.giganet.stockapi.stock.model;


import br.psi.giganet.stockapi.technician.model.Technician;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class TechnicianStock extends Stock {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_stocks_technician"),
            name = "technician",
            referencedColumnName = "id")
    private Technician technician;

}
