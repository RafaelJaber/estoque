package br.psi.giganet.stockapi.schedules.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.stock_moves.model.MoveOrigin;
import br.psi.giganet.stockapi.stock_moves.model.MoveReason;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.stock_moves.model.StockMove;
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
@Table(name = "schedule_stock_moves_items")
@Entity
public class ScheduledMoveItem extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_schedule_stock_moves_items_scheduled"),
            name = "scheduledMove",
            referencedColumnName = "id")
    private ScheduledMove scheduled;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_schedule_stock_moves_items_move"),
            name = "move",
            referencedColumnName = "id")
    private StockMove move;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_schedule_stock_moves_items_from"),
            name = "\"from\"",
            referencedColumnName = "id")
    private StockItem from;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_schedule_stock_moves_items_to"),
            name = "\"to\"",
            referencedColumnName = "id")
    private StockItem to;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_schedule_stock_moves_items_product"),
            name = "product",
            nullable = false,
            referencedColumnName = "id")
    private Product product;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MoveOrigin origin;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MoveType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MoveReason reason;

    @NotNull
    @Positive
    private Double quantity;

    @Column(length = 1024)
    private String description;

    @Column(length = 512)
    private String note;

}
