package br.psi.giganet.stockapi.stock_moves.controller.request;

import br.psi.giganet.stockapi.patrimonies.model.PatrimonyCodeType;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class BasicInsertItemMoveFromServiceOrderRequest {

    @NotEmpty(message = "Produto não pode ser nulo")
    private String product;

    @NotNull(message = "Quandidade não pode ser nula")
    private Double quantity;

    @Valid
    private Set<String> patrimonies;

}
