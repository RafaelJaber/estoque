package br.psi.giganet.stockapi.schedules.controller.response;

import br.psi.giganet.stockapi.stock_moves.model.MoveOrigin;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.schedules.model.ScheduledExecution;
import br.psi.giganet.stockapi.schedules.model.ScheduledStatus;
import lombok.Data;

@Data
public class ScheduledMoveProjection {

    private Long id;
    private String date;
    private MoveOrigin origin;
    private MoveType type;
    private String description;
    private ScheduledExecution execution;
    private ScheduledStatus status;

}
