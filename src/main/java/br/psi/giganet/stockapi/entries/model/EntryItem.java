package br.psi.giganet.stockapi.entries.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.entries.model.enums.EntryStatus;
import br.psi.giganet.stockapi.stock_moves.model.EntryItemStockMove;
import br.psi.giganet.stockapi.stock_moves.model.StockMove;
import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrderItem;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrderSupplier;
import br.psi.giganet.stockapi.units.model.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "entry_items")
public class EntryItem extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_entry_items_entry"),
            name = "entry",
            nullable = false,
            referencedColumnName = "id")
    private Entry entry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_entry_items_purchase_order_item"),
            name = "purchaseOrderItem",
            referencedColumnName = "id")
    private PurchaseOrderItem purchaseOrderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_entry_items_product"),
            name = "product",
            nullable = false,
            referencedColumnName = "id"
    )
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_entry_items_supplier"),
            name = "supplier",
            nullable = false,
            referencedColumnName = "id"
    )
    private PurchaseOrderSupplier supplier;

    private String documentProductCode;

    @Enumerated(EnumType.STRING)
    @NotNull
    private EntryStatus status;

    @NotNull
    @Min(0)
    private Double quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_entry_items_unit"),
            name = "unit",
            nullable = false,
            referencedColumnName = "id"
    )
    private Unit unit;

    @NotNull
    @Min(0)
    @Max(100)
    private Float ipi;

    @NotNull
    @Min(0)
    @Max(100)
    private Float icms;

    @NotNull
    @Min(0)
    private BigDecimal price;

    @NotNull
    @Min(0)
    private BigDecimal total;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_entry_items_entry_move"),
            name = "entryMove",
            referencedColumnName = "id"
    )
    private EntryItemStockMove entryMove;


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "entryItem")
    private List<Patrimony> patrimonies;

}
