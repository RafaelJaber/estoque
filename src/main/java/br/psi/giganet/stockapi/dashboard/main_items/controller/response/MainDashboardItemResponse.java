package br.psi.giganet.stockapi.dashboard.main_items.controller.response;

import br.psi.giganet.stockapi.products.controller.response.ProductProjectionWithoutUnit;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MainDashboardItemResponse {

    private Long id;
    private ProductProjectionWithoutUnit product;

}
