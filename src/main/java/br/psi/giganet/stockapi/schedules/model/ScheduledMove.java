package br.psi.giganet.stockapi.schedules.model;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock_moves.model.MoveOrigin;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "scheduled_moves")
@Entity
public class ScheduledMove extends AbstractModel {

    @NotNull
    @Enumerated(EnumType.STRING)
    private ScheduledStatus status;

    @NotNull
    private ZonedDateTime date;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ScheduledExecution execution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_schedule_stock_moves_items_responsible"),
            name = "\"responsible\"",
            referencedColumnName = "id")
    private Employee responsible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_schedule_stock_moves_from"),
            name = "\"from\"",
            referencedColumnName = "id")
    private Stock from;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_schedule_stock_moves_to"),
            name = "\"to\"",
            referencedColumnName = "id")
    private Stock to;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_schedule_stock_moves_branch_office"),
            name = "branchOffice",
            referencedColumnName = "id"
    )
    private BranchOffice branchOffice;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MoveOrigin origin;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MoveType type;

    @Column(length = 512)
    private String description;

    @OneToMany(
            mappedBy = "scheduled",
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<ScheduledMoveItem> items;

    @Column(length = 512)
    private String note;

    public Boolean isSameBranchOffice() {
        return from != null && from.getBranchOffice() != null &&
                to != null && to.getBranchOffice() != null &&
                from.getBranchOffice().equals(to.getBranchOffice());
    }

}
