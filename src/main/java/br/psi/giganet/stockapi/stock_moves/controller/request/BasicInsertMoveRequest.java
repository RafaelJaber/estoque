package br.psi.giganet.stockapi.stock_moves.controller.request;

import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class BasicInsertMoveRequest {

    private Long to;
    @NotNull(message = "Tipo de movimentação não pode ser nulo")
    private MoveType type;
    private String note;
    @Valid
    @NotEmpty(message = "É necessário informar pelo menos 1 item")
    private List<InsertItemMoveRequest> items;

}
