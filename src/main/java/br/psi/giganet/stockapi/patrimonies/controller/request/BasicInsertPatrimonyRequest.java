package br.psi.giganet.stockapi.patrimonies.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class BasicInsertPatrimonyRequest {

    @NotEmpty(message = "Código do patrimonio não pode ser nulo")
    private String code;

    @NotNull(message = "Produto não pode ser nulo")
    private String product;

    private String note;

}
