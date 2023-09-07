package br.psi.giganet.stockapi.stock_moves.model;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.stock.model.StockItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_moves")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class StockMove extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_stock_moves_from"),
            name = "\"from\"",
            referencedColumnName = "id")
    private StockItem from;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_stock_moves_to"),
            name = "\"to\"",
            referencedColumnName = "id")
    private StockItem to;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_stock_moves_product"),
            name = "product",
            nullable = false,
            referencedColumnName = "id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_stock_moves_branch_office"),
            name = "branchOffice",
            referencedColumnName = "id"
    )
    private BranchOffice branchOffice;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MoveOrigin origin;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MoveReason reason;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MoveType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MoveStatus status;

    @NotNull
    @Positive
    private Double quantity;

    @Column(length = 1024)
    private String description;

    @Column(length = 512)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_stock_moves_requester"),
            name = "\"requester\"",
            referencedColumnName = "id")
    private Employee requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_stock_moves_responsible"),
            name = "\"responsible\"",
            referencedColumnName = "id")
    private Employee responsible;

    public Boolean isApproved() {
        return MoveStatus.APPROVED.equals(status);
    }

    public Boolean isSameBranchOffice() {
        return from != null && from.getStock().getBranchOffice() != null &&
                to != null && to.getStock().getBranchOffice() != null &&
                from.getStock().getBranchOffice().equals(to.getStock().getBranchOffice());
    }
}
