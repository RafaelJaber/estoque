package br.psi.giganet.stockapi.stock_moves.model;

import br.psi.giganet.stockapi.schedules.model.ScheduledMoveItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "scheduled_stock_moves")
public class ScheduledStockMove extends StockMove {

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "move")
    private ScheduledMoveItem scheduledMoveItem;

}
