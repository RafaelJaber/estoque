package br.psi.giganet.stockapi.branch_offices.model;

import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.stock.model.*;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "branch_offices")
public class BranchOffice extends AbstractModel {

    @NotEmpty
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CityOptions city;

    @ManyToMany(mappedBy = "branchOffices", fetch = FetchType.LAZY)
    private Set<Employee> employees;

    public Boolean hasAccess(Employee employee) {
        return employees != null && employees.contains(employee);
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "branchOffice", orphanRemoval = true)
    private Set<Stock> stocks;

    public Optional<ShedStock> shed() {
        return this.find(StockType.SHED)
                .stream()
                .filter(s -> s instanceof ShedStock)
                .map(s -> (ShedStock) s)
                .findFirst();
    }

    public Optional<MaintenanceStock> maintenance() {
        return this.find(StockType.MAINTENANCE)
                .stream()
                .filter(s -> s instanceof MaintenanceStock)
                .map(s -> (MaintenanceStock) s)
                .findFirst();
    }

    public Optional<ObsoleteStock> obsolete() {
        return this.find(StockType.OBSOLETE)
                .stream()
                .filter(s -> s instanceof ObsoleteStock)
                .map(s -> (ObsoleteStock) s)
                .findFirst();
    }

    public Optional<DefectiveStock> defective() {
        return this.find(StockType.DEFECTIVE)
                .stream()
                .filter(s -> s instanceof DefectiveStock)
                .map(s -> (DefectiveStock) s)
                .findFirst();
    }

    public Optional<CustomerStock> customer() {
        return this.find(StockType.CUSTOMER)
                .stream()
                .filter(s -> s instanceof CustomerStock)
                .map(s -> (CustomerStock) s)
                .findFirst();
    }

    public Set<? extends Stock> find(StockType type) {
        return this.stocks.stream()
                .filter(s -> s.getType().equals(type))
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "BranchOffice{" +
                "name='" + name + '\'' +
                ", city=" + city +
                ", stocks=" + stocks +
                '}';
    }
}
