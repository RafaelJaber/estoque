package br.psi.giganet.stockapi.stock_moves.service;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.config.exception.exception.UnauthorizedException;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.stock_moves.model.ScheduledStockMove;
import org.springframework.stereotype.Service;

@Service
public class ScheduledStockMoveService extends AbstractMovesHandler<ScheduledStockMove> {

    @Override
    public void validate(ScheduledStockMove move) throws IllegalArgumentException {
        if (!move.getStatus().equals(MoveStatus.REQUESTED)) {
            throw new IllegalArgumentException("Situação da movimentação é inválida");
        }

        if (move.getTo() == null || move.getFrom() == null) {
            throw new IllegalArgumentException("Devem ser informados os estoques de origem e destino");
        } else if (move.getTo() != null && move.getFrom() != null && move.getTo().equals(move.getFrom())) {
            throw new IllegalArgumentException("Estoque de origem é o mesmo do estoque de destino");
        } else if (!move.getType().equals(MoveType.BETWEEN_STOCKS)) {
            throw new IllegalArgumentException("Tipo da movimentação é inválido");
        } else if (move.getFrom().getStock().isCustomer()) {
            throw new IllegalArgumentException("O estoque de origem informado é inválido");
        } else if (move.getTo().getStock().isCustomer()) {
            throw new IllegalArgumentException("O estoque de destino informado é inválido");
        }

        if (!requesterHasPermission(move)) {
            throw new UnauthorizedException("Funcionário não possui permissão para realizar esta operação");
        }

        Double blockedQuantity = move.getFrom().getBlockedQuantity() == null ?
                0d :
                move.getFrom().getBlockedQuantity() + move.getQuantity();

        double availableQuantity = move.getFrom().getQuantity() - blockedQuantity;
        if (availableQuantity < 0) {
            throw new IllegalArgumentException("Estoque de " + move.getFrom().getProduct().getName() + " não possui quantidade disponível para esta movimentação");
        }

    }

    @Override
    protected boolean shouldExecuteOnInsert(ScheduledStockMove move) {
        return move.getRequester().hasRole("ROLE_MOVES_WRITE_ROOT");
    }
}
