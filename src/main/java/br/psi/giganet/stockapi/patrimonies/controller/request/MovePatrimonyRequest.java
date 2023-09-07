package br.psi.giganet.stockapi.patrimonies.controller.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class MovePatrimonyRequest {

    @NotNull(message = "Código ID do patrimonio não pode ser nulo")
    private Long patrimony;

    @NotNull(message = "Nova localização não pode ser nula")
    private Long newLocation;

    private String note;

}
