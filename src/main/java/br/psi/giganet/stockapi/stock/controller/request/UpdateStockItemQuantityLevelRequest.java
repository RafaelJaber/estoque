package br.psi.giganet.stockapi.stock.controller.request;

import br.psi.giganet.stockapi.stock.model.enums.QuantityLevel;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
public class UpdateStockItemQuantityLevelRequest {

    private Long id;

    @NotNull(message = "Nível não pode ser nulo")
    private QuantityLevel level;

    @PositiveOrZero(message = "Porcentagem mínima não pode ser menor do que 0")
    @Max(value = 100, message = "Porcentagem mínima deve ser no máximo igual a 100")
    private Float from;

    @PositiveOrZero(message = "Porcentagem máxima não pode ser menor do que 0")
    @Max(value = 100, message = "Porcentagem máxima deve ser no máximo igual a 100")
    private Float to;

}
