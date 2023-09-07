package br.psi.giganet.stockapi.purchase_order.model;

import br.psi.giganet.stockapi.common.utils.model.enums.ProcessStatus;
import br.psi.giganet.stockapi.config.security.model.AbstractExternalModel;
import br.psi.giganet.stockapi.entries.model.EntryItem;
import br.psi.giganet.stockapi.products.model.Product;
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
@Table(name = "purchase_order_items")
public class PurchaseOrderItem extends AbstractExternalModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_order_items_product"),
            name = "product",
            nullable = false,
            referencedColumnName = "id"
    )
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_order_items_purchase_order"),
            name = "purchase_order",
            nullable = false,
            referencedColumnName = "id")
    private PurchaseOrder order;

    @NotNull
    @Min(0)
    private Double quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_purchase_order_items_unit"),
            name = "unit",
            nullable = false,
            referencedColumnName = "id"
    )
    private Unit unit;

    @NotNull
    @Min(0)
    private BigDecimal price;

    @NotNull
    @Min(0)
    @Max(100)
    private Float ipi;

    @NotNull
    @Min(0)
    @Max(100)
    private Float icms;

    @Min(0)
    private BigDecimal discount;

    @NotNull
    @Min(0)
    private BigDecimal total;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private ProcessStatus status;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "purchaseOrderItem")
    private List<EntryItem> entries;
}
