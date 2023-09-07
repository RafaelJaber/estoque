package br.psi.giganet.stockapi.employees.model;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.config.security.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Employee extends User {

    @NotEmpty
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "branch_office_has_employees",
            joinColumns = @JoinColumn(name = "employee", foreignKey = @ForeignKey(name = "fk_branch_office_has_employees_employee")),
            inverseJoinColumns = @JoinColumn(name = "branchOffice", foreignKey = @ForeignKey(name = "fk_branch_office_has_employees_branch_office")))
    private Set<BranchOffice> branchOffices;

    public Boolean hasAccess(BranchOffice branchOffice) {
        return branchOffices != null && branchOffices.contains(branchOffice);
    }

}
