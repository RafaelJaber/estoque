package br.psi.giganet.stockapi.dashboard.main_items.model;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.employees.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "main_dashboard_item_groups",
        uniqueConstraints = @UniqueConstraint(name = "uk_group_label", columnNames = "label"))
@Entity
public class MainDashboardItemGroup extends AbstractModel {

    @NotNull
    private String label;

    @Enumerated(EnumType.STRING)
    private GroupCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_main_dashboard_item_groups_branch_office"),
            name = "branchOffice",
            referencedColumnName = "id"
    )
    private BranchOffice branchOffice;

    @ManyToMany(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinTable(name = "main_dashboard_items_group_has_users",
            joinColumns = @JoinColumn(name = "group_id", foreignKey = @ForeignKey(name = "fk_main_dashboard_items_group_has_users_group")),
            inverseJoinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_main_dashboard_items_group_has_users_user")))
    private Set<Employee> employees;

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "group",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<MainDashboardItem> items;

}
