package br.psi.giganet.stockapi.stock.model;


import br.psi.giganet.stockapi.sellers.model.Seller;
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
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class SellerStock extends Stock {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_stocks_seller"),
            name = "seller",
            referencedColumnName = "id")
    private Seller seller;

}
