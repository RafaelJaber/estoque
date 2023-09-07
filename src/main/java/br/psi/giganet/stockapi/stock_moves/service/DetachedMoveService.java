package br.psi.giganet.stockapi.stock_moves.service;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.config.exception.exception.UnauthorizedException;
import br.psi.giganet.stockapi.stock.model.StockItem;
import br.psi.giganet.stockapi.stock_moves.model.DetachedStockMove;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Service
public class DetachedMoveService extends AbstractMovesHandler<DetachedStockMove> {

    @Transactional
    @Override
    public Optional<DetachedStockMove> create(DetachedStockMove move) {
        validate(move);

        if ((move.getType().equals(MoveType.BETWEEN_STOCKS) || move.getType().equals(MoveType.OUT_ITEM)) &&
                !move.getFrom().getStock().isCustomer()) {
            move.getFrom().setBlockedQuantity(
                    move.getFrom().getBlockedQuantity() == null ?
                            move.getQuantity() :
                            move.getFrom().getBlockedQuantity() + move.getQuantity());
        }

        switch (move.getType()) {
            case ENTRY_ITEM:
                move.setBranchOffice(move.getTo().getStock().getBranchOffice());
                break;

            case BETWEEN_STOCKS:
                move.setBranchOffice(move.getFrom().getStock().isShed() &&
                        move.getTo().getStock().isShed() ? null :
                        move.getFrom().getStock().getBranchOffice());
                break;

            case OUT_ITEM:
                move.setBranchOffice(move.getFrom().getStock().getBranchOffice());
                break;

        }

        if (shouldExecuteOnInsert(move)) {
            move.setStatus(MoveStatus.APPROVED);
            move.setResponsible(move.getRequester());
            execute(move);
        } else {
            move.setDescription(getMoveDescription(move));
        }

        return Optional.of(move);
    }

    @Override
    public void validate(DetachedStockMove move) throws IllegalArgumentException {
        if (!move.getStatus().equals(MoveStatus.REQUESTED)) {
            throw new IllegalArgumentException("Situação da movimentação é inválida");
        }

        if (move.getTo() == null && move.getFrom() == null) {
            throw new IllegalArgumentException("Nenhum estoque foi especificado");
        } else if (move.getTo() != null && move.getFrom() != null && move.getTo().equals(move.getFrom())) {
            throw new IllegalArgumentException("Estoque de origem é o mesmo do estoque de destino");
        } else if (move.getType().equals(MoveType.ENTRY_ITEM)) {
            if (move.getTo() == null) {
                throw new IllegalArgumentException("Estoque de destino é inválido");
            } else if (move.getTo().getStock().getBranchOffice() == null) {
                throw new IllegalArgumentException("Estoque de destino não está vinculado a nenhuma filial");
            }

        } else if (move.getType().equals(MoveType.BETWEEN_STOCKS) || move.getType().equals(MoveType.OUT_ITEM)) {
            if (move.getFrom() == null) {
                throw new IllegalArgumentException("Estoque de origem é inválido");
            } else if (move.getFrom().getStock().getBranchOffice() == null) {
                throw new IllegalArgumentException("Estoque de origem não está vinculado a nenhuma filial");
            } else if (!move.getFrom().getStock().isCustomer()) {
                Double blockedQuantity = move.getFrom().getBlockedQuantity() == null ?
                        0d :
                        move.getFrom().getBlockedQuantity() + move.getQuantity();

                double availableQuantity = move.getFrom().getQuantity() - blockedQuantity;
                if (availableQuantity < 0) {
                    throw new IllegalArgumentException("Estoque de " + move.getFrom().getProduct().getName() + " não possui quantidade disponível para esta movimentação");
                }
            }

        }

        if (!requesterHasPermission(move)) {
            throw new UnauthorizedException("Funcionário não possui permissão para realizar esta operação");
        } else if (move.getType().equals(MoveType.BETWEEN_STOCKS) &&
                !move.isSameBranchOffice() &&
                (!move.getFrom().getStock().isShed() || !move.getTo().getStock().isShed())) {
            throw new IllegalArgumentException("Movimentações entre filiais pode ser realizada somente de galpão para galpão");
        }
    }

    @Override
    public boolean shouldExecuteOnInsert(DetachedStockMove move) {
        return super.shouldExecuteOnInsert(move) ||
                (move.getFrom() != null
                        && move.getFrom().getStock() != null
                        && move.getFrom().getStock().isCustomer());
    }

    @Override
    public Optional<DetachedStockMove> execute(DetachedStockMove move) {
        final StockItem from = move.getFrom();
        final StockItem to = move.getTo();
        final MoveType type = move.getType();
        final Double quantity = move.getQuantity();

        if (type.equals(MoveType.BETWEEN_STOCKS) && from.getStock().isCustomer()) {

            if (!Objects.requireNonNull(to).getProduct()
                    .equals(Objects.requireNonNull(from).getProduct())) {
                onMoveFailed(move, "Estoques incompatíves. Os produtos informados são diferentes");
            }
            from.setQuantity(Math.max(from.getQuantity() - quantity, 0d));
            from.setBlockedQuantity(0d);

            to.setQuantity(to.getQuantity() + quantity);

            if (to.getEntryMoves() == null) {
                to.setEntryMoves(new ArrayList<>());
            }
            to.getEntryMoves().add(move);

            if (from.getOutgoingMoves() == null) {
                from.setOutgoingMoves(new ArrayList<>());
            }
            from.getOutgoingMoves().add(move);

            move.setDescription(getMoveDescription(move));
            move.setStatus(MoveStatus.REALIZED);
            return Optional.of(move);

        } else {
            return super.execute(move);
        }
    }
}
