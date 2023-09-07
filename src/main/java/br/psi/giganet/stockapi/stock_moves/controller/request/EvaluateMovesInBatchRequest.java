package br.psi.giganet.stockapi.stock_moves.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EvaluateMovesInBatchRequest {

    @NotEmpty(message = "É necessário informar pelo menos uma movimentação")
    private List<Long> moves;

}
