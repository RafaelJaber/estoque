package br.psi.giganet.stockapi.technician.model;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.config.security.model.AbstractExternalModel;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "technicians")
public class Technician extends AbstractExternalModel {

    @NotEmpty
    private String name;
    @NotEmpty
    @Email
    private String email;

    private String userId;
    private String technicianId;

    @NotNull
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    private TechnicianSector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_technicians_branch_office"),
            name = "branchOffice",
            referencedColumnName = "id"
    )
    private BranchOffice branchOffice;

    @OneToOne(mappedBy = "technician", fetch = FetchType.LAZY)
    private TechnicianStock stock;
}
