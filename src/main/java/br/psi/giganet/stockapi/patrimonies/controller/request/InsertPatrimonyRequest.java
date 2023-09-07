package br.psi.giganet.stockapi.patrimonies.controller.request;

import br.psi.giganet.stockapi.patrimonies.model.PatrimonyCodeType;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class InsertPatrimonyRequest {

    @NotEmpty(message = "Código do patrimonio não pode ser nulo")
    private String code;

    @NotNull(message = "Tipo do código não pode ser nulo")
    private PatrimonyCodeType codeType;

    @NotNull(message = "Produto não pode ser nulo")
    private String product;

    @NotNull(message = "Localização atual não pode ser nula")
    private Long currentLocation;

    private String note;

}
