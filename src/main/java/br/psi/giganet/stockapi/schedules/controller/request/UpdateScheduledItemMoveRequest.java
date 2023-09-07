package br.psi.giganet.stockapi.schedules.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class UpdateScheduledItemMoveRequest {

    private Long id;
    @NotNull(message = "Produto não pode ser nulo")
    private String product;
    @NotNull(message = "Quantidade não pode ser nula")
    @Positive(message = "Quantidade deve ser maior do que zero")
    private Double quantity;

}
