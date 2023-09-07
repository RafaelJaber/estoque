package br.psi.giganet.stockapi.entries.model;

import br.psi.giganet.stockapi.branch_offices.model.BranchOffice;
import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.entries.model.enums.EntryStatus;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrder;
import br.psi.giganet.stockapi.stock.model.Stock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "entries")
public class Entry extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_entries_purchase_order"),
            name = "purchaseOrder",
            referencedColumnName = "id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_entries_responsible"),
            name = "responsible",
            referencedColumnName = "id"
    )
    private Employee responsible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_entries_stock"),
            name = "stock",
            nullable = false,
            referencedColumnName = "id"
    )
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_entries_branch_office"),
            name = "branchOffice",
            referencedColumnName = "id"
    )
    private BranchOffice branchOffice;

    private String fiscalDocumentNumber;

    private String documentAccessCode;

    @OneToMany(
            mappedBy = "entry",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            orphanRemoval = true
    )
    @NotEmpty
    private List<EntryItem> items;

    @Enumerated(EnumType.STRING)
    @NotNull
    private EntryStatus status;

    private Boolean isManual;

    private String note;

}
