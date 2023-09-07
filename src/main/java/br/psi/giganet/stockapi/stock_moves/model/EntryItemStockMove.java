package br.psi.giganet.stockapi.stock_moves.model;

import br.psi.giganet.stockapi.entries.model.EntryItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "entry_items_from_order_stock_moves")
public class EntryItemStockMove extends StockMove {

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "entryMove")
    private EntryItem entryItem;

}
