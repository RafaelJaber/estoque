package br.psi.giganet.stockapi.nfe.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.purchase_order.model.PurchaseOrderSupplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Table(name = "util_nfe_item_entry")
@Entity
public class NFeEntryItem extends AbstractModel {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_util_nfe_item_entry_product"),
            name = "product",
            nullable = false,
            referencedColumnName = "id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_util_nfe_item_entry_supplier"),
            name = "supplier",
            nullable = false,
            referencedColumnName = "id")
    private PurchaseOrderSupplier supplier;

    @NotEmpty
    private String documentProductCode;
}
