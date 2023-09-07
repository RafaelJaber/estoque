package br.psi.giganet.stockapi.stock_moves.service;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.config.exception.exception.UnauthorizedException;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.stock.model.StockItem;
import br.psi.giganet.stockapi.stock_moves.model.MoveReason;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.stock_moves.model.SaleStockMove;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Service
public class SaleMoveService extends AbstractMovesHandler<SaleStockMove> {

    @Transactional
    @Override
    public Optional<SaleStockMove> create(SaleStockMove move) {
        this.validate(move);

        move.setResponsible(move.getRequester());
        move.setStatus(MoveStatus.APPROVED);
        return this.execute(move);
    }

    @Override
    public void validate(SaleStockMove move) throws IllegalArgumentException {
        if (move.getType().equals(MoveType.SALE)) {
            if (move.getFrom() == null) {
                throw new IllegalArgumentException("Estoque de origem é inválido");
            } else if (!(move.getFrom().getStock().isTechnician())) {
                throw new IllegalArgumentException("Estoque do técnico de origem informado é inválido");
            } else if (!move.getReason().equals(MoveReason.DETACHED)) {
                throw new IllegalArgumentException("Tipo da movimentação é inválido");
            } else if (move.getFrom().getStock().getBranchOffice() == null) {
                throw new IllegalArgumentException("Estoque de origem não possui nenhuma filial associada");
            }
            validateQuantity(move);
        }

        if (!requesterHasPermission(move)) {
            throw new UnauthorizedException("Funcionário não possui permissão para realizar esta operação");
        }
    }

    private void validateQuantity(SaleStockMove move) {
        Double blockedQuantity = move.getFrom().getBlockedQuantity() == null ?
                0d :
                move.getFrom().getBlockedQuantity() + move.getQuantity();

        double availableQuantity = move.getFrom().getQuantity() - blockedQuantity;
        if (availableQuantity < 0) {
            throw new IllegalArgumentException("Estoque de " + move.getFrom().getProduct().getName() + " não possui quantidade disponível para esta movimentação");
        }
    }

    @Override
    public Optional<SaleStockMove> execute(SaleStockMove move) {
        final StockItem from = move.getFrom();
        final StockItem to = move.getTo();
        final Double quantity = move.getQuantity();

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
    }

    @Override
    protected String getMoveDescription(SaleStockMove move) {
        final Employee employee = move.getRequester();
        final Double quantity = move.getQuantity();
        final StockItem from = move.getFrom();
        final StockItem to = move.getTo();

        final Product product = from != null ? from.getProduct() : to.getProduct();

        String templateMessage = ":move : :employee :description de :quantity :unit de :item ( :itemCode ) dia " +
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à's' HH:mm:ss"));

        return templateMessage
                .replaceAll(":move", "Venda")
                .replaceAll(":employee", employee.getName())
                .replaceAll(":description", "finalizou a venda")
                .replaceAll(":quantity", Double.toString(quantity))
                .replaceAll(":unit", product.getUnit().getAbbreviation())
                .replaceAll(":itemCode", product.getCode())
                .replaceAll(":item", product.getName());
    }
}
