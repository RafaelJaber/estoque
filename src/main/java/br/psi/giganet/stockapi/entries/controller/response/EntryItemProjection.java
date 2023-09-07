package br.psi.giganet.stockapi.entries.controller.response;

import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import lombok.Data;

@Data
public class EntryItemProjection {

    private Long id;
    private ProductProjection product;
    private Double quantity;

}
