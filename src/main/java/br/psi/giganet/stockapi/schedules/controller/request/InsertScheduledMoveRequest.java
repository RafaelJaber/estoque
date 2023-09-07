package br.psi.giganet.stockapi.schedules.controller.request;

import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.schedules.model.ScheduledExecution;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class InsertScheduledMoveRequest {

    @NotNull(message = "Data do agendamento não pode ser nulo")
    private String date;
    @NotNull(message = "O tipo da execução não pode ser nula")
    private ScheduledExecution execution;

    private Long from;
    private Long to;
    @NotNull(message = "Tipo de movimentação não pode ser nulo")
    private MoveType type;
    private String note;
    @Valid
    @NotEmpty(message = "É necessário informar pelo menos 1 item")
    private List<InsertScheduledItemMoveRequest> items;

}
