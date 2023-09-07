package br.psi.giganet.stockapi.templates.service.dto;

import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import br.psi.giganet.stockapi.products.model.Product;
import lombok.Data;

@Data
public class TemplateMountDTO {

    private Product product;
    private Patrimony patrimony;
    private Double quantity;
    private Double availableQuantityOnDestiny;

}
