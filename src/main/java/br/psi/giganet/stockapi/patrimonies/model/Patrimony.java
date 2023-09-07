package br.psi.giganet.stockapi.patrimonies.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.entries.model.EntryItem;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import br.psi.giganet.stockapi.stock_moves.model.StockMove;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.stock.model.Stock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "patrimonies")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Patrimony extends AbstractModel {

    @NotEmpty
    @Column(unique = true)
    private String code;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PatrimonyCodeType codeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_patrimonies_product"),
            name = "product",
            nullable = false,
            referencedColumnName = "id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_patrimonies_currentLocation"),
            name = "currentLocation",
            referencedColumnName = "id")
    private PatrimonyLocation currentLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_patrimonies_entry_item"),
            name = "entryItem",
            referencedColumnName = "id")
    private EntryItem entryItem;

    @NotNull
    private Boolean isVisible;

    @Column(length = 1024)
    private String note;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "patrimony")
    private List<PatrimonyMove> moves;

    public Boolean isVisible(){
        return this.isVisible;
    }
}
