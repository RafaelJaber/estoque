package br.psi.giganet.stockapi.dashboard.main_items.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class MainDashboardItemRequest {

    private Long id;

    @NotEmpty(message = "Código ID do produto não informado")
    private String product;

    @NotNull(message = "Índice do produto não pode ser nulo")
    private Integer index;

}
