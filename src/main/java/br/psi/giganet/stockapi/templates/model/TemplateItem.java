package br.psi.giganet.stockapi.templates.model;

import br.psi.giganet.stockapi.config.security.model.AbstractModel;
import br.psi.giganet.stockapi.products.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "templates_items")
public class TemplateItem extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_templates_template"),
            name = "template",
            nullable = false,
            referencedColumnName = "id")
    private Template template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            foreignKey = @ForeignKey(name = "fk_templates_product"),
            name = "product",
            nullable = false,
            referencedColumnName = "id")
    private Product product;

    @NotNull
    @Positive
    private Double quantity;

}
