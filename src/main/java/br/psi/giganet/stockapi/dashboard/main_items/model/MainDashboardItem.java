package br.psi.giganet.stockapi.dashboard.main_items.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.products.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "main_dashboard_items")
@Entity
public class MainDashboardItem extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_main_dashboard_items_product"),
            name = "product",
            nullable = false,
            referencedColumnName = "id"
    )
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_main_dashboard_items_group"),
            name = "\"group\"",
            referencedColumnName = "id"
    )
    private MainDashboardItemGroup group;

    private Integer index;

}
