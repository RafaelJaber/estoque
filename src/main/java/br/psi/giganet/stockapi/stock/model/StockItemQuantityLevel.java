package br.psi.giganet.stockapi.stock.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.stock.model.enums.QuantityLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_item_quantity_levels")
public class StockItemQuantityLevel  extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_stock_item_quantity_levels_stock_item"),
            name = "stockItem",
            nullable = false,
            referencedColumnName = "id")
    private StockItem stockItem;

    @NotNull
    @Enumerated(EnumType.STRING)
    private QuantityLevel level;

    @Column(name = "\"from\"")
    private Float from;

    @Column(name = "\"to\"")
    private Float to;

}
