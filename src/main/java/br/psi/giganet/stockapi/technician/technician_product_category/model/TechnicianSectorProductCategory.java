package br.psi.giganet.stockapi.technician.technician_product_category.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.products.categories.model.Category;
import br.psi.giganet.stockapi.technician.model.TechnicianSector;
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
@Table(name = "technicians_sectors_has_product_categories")
public class TechnicianSectorProductCategory extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_technicians_sectors_has_product_categories_category"),
            name = "category",
            referencedColumnName = "id")
    private Category category;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TechnicianSector sector;

}
