package br.psi.giganet.stockapi.stock.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.stock.model.enums.QuantityLevel;
import br.psi.giganet.stockapi.stock_moves.model.StockMove;
import br.psi.giganet.stockapi.products.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_items")
public class StockItem extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_stock_items_stock"),
            name = "stock",
            nullable = false,
            referencedColumnName = "id")
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_stock_items_product"),
            name = "product",
            nullable = false,
            referencedColumnName = "id")
    private Product product;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "from")
    private List<StockMove> outgoingMoves;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "to")
    private List<StockMove> entryMoves;

    private Double quantity;

    private Double blockedQuantity;

    private Double minQuantity;

    private Double maxQuantity;

    private BigDecimal lastPricePerUnit;

    @Enumerated(EnumType.STRING)
    private QuantityLevel currentLevel;

    @OneToMany(
            mappedBy = "stockItem",
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<StockItemQuantityLevel> levels;

    public Double getAvailableQuantity(){
        return this.quantity - this.blockedQuantity;
    }
}
