package br.psi.giganet.stockapi.templates.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class InsertTemplateItemRequest {

    @NotEmpty(message = "Produto não pode ser nulo")
    private String product;

    @NotNull(message = "Quantidade não pode ser nula")
    private Double quantity;

}
