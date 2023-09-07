package br.psi.giganet.stockapi.products.controller.request;

import br.psi.giganet.stockapi.units.controller.response.UnitProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductProjectionWebhookRequest {

    private Long id;
    private String name;
    private String code;
    private UnitProjection unit;
    private String manufacturer;

}
