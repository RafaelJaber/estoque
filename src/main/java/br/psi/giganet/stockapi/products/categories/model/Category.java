package br.psi.giganet.stockapi.products.categories.model;

import br.psi.giganet.stockapi.config.security.model.AbstractExternalModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "product_categories")
public class Category extends AbstractExternalModel {

    @NotEmpty
    private String name;
    private String pattern;
    private String description;

}
