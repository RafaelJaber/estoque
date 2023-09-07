package br.psi.giganet.stockapi.moves_request.controller.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class BasicInsertRequestedMoveRequest {

    @NotNull(message = "Estoque de origem não pode ser nulo")
    private Long from;

    private String note;
    @Valid
    @NotEmpty(message = "É necessário informar pelo menos 1 item")
    private List<InsertRequestedMoveItemRequest> items;

}
