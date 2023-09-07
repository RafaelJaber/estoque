package br.psi.giganet.stockapi.stock.controller.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateStockItemParametersRequest {

    @NotNull(message = "Código do item do estoque não pode ser nulo")
    private Long id;

    @NotNull(message = "Código do estoque não pode ser nulo")
    private Long stock;

    @PositiveOrZero(message = "Quantidade não pode ser menor do que 0")
    private Double quantity;

    private Double blockedQuantity;

    @NotNull(message = "Quantidade mínima não pode ser nula")
    @PositiveOrZero(message = "Quantidade mínima não pode ser menor do que 0")
    private Double minQuantity;

    @NotNull(message = "Quantidade mínima não pode ser nula")
    @PositiveOrZero(message = "Quantidade mínima não pode ser menor do que 0")
    private Double maxQuantity;

    @NotNull(message = "Preço unitário não pode ser nul0")
    @PositiveOrZero(message = "Preço unitário não pode ser menor do que 0")
    private BigDecimal pricePerUnit;

    @Valid
    private List<UpdateStockItemQuantityLevelRequest> levels;

}
