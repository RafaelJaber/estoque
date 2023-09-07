package br.psi.giganet.stockapi.stock_moves.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateMoveRequest {

    @NotNull(message = "O produto é obrigatório!")
    private Long product;

    @NotNull(message = "A descrição é obrigatório!")
    private String description;
}
