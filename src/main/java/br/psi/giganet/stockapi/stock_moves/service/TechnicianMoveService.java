package br.psi.giganet.stockapi.stock_moves.service;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.config.exception.exception.UnauthorizedException;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.StockItem;
import br.psi.giganet.stockapi.stock_moves.model.MoveReason;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.stock_moves.model.TechnicianStockMove;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Service
public class TechnicianMoveService extends AbstractMovesHandler<TechnicianStockMove> {

    @Transactional
    @Override
    public Optional<TechnicianStockMove> create(TechnicianStockMove move) {
        validate(move);

        if ((move.getType().equals(MoveType.BETWEEN_STOCKS) || move.getType().equals(MoveType.OUT_ITEM)) &&
                move.getFrom().getStock().isTechnician()) {
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
            case OUT_ITEM:
                move.setBranchOffice(move.getFrom().getStock().getBranchOffice());
                break;

        }

        if (shouldExecuteOnInsert(move)) {
            move.setResponsible(move.getRequester());
            move.setStatus(MoveStatus.APPROVED);
            execute(move);
        } else {
            move.setDescription(getMoveDescription(move));
        }

        return Optional.of(move);
    }

    @Override
    public void validate(TechnicianStockMove move) throws IllegalArgumentException {
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

            } else if (!(move.getTo().getStock().isTechnician())) {
                throw new IllegalArgumentException("Estoque do técnico de destino informado é inválido");
            } else if (move.getTo().getStock().getBranchOffice() == null) {
                throw new IllegalArgumentException("Estoque do técnico não possui nenhuma filial associada");
            }


        } else if (move.getType().equals(MoveType.BETWEEN_STOCKS)) {

            if (move.getFrom() == null) {
                throw new IllegalArgumentException("Estoque de origem é inválido");

            } else if (move.getTo() == null) {
                throw new IllegalArgumentException("Estoque de destino é inválido");

            } else if (move.getReason().equals(MoveReason.SERVICE_ORDER)) {

                Stock stockFrom = move.getFrom().getStock();
                Stock stockTo = move.getTo().getStock();

                if (stockFrom == null || stockTo == null) {
                    throw new IllegalArgumentException("Estoques informados são inválidos");

                } else if (!(stockFrom.isTechnician() && stockTo.isCustomer()) &&
                        !(stockFrom.isCustomer() && stockTo.isTechnician())) {
                    throw new IllegalArgumentException("Tipo da movimentação é inválido. Estoques informados são inválidos");

                } else if (stockFrom.isTechnician()) {

                    if (stockFrom.getBranchOffice() == null) {
                        throw new IllegalArgumentException("Estoque de origem não possui nenhuma filial associada");

                    }

                    validateQuantity(move);

                } else if (stockTo.isTechnician()) {

                    if (stockTo.getBranchOffice() == null) {
                        throw new IllegalArgumentException("Estoque de origem não possui nenhuma filial associada");

                    }

                }

            } else if (move.getFrom().getStock().getBranchOffice() == null) {
                throw new IllegalArgumentException("Estoque de origem não possui nenhuma filial associada");

            } else if (move.getTo().getStock().getBranchOffice() == null) {
                throw new IllegalArgumentException("Estoque de destino não possui nenhuma filial associada");

            } else if (!move.isSameBranchOffice()) {
                throw new IllegalArgumentException("A filial do estoque do técnico de origem é diferente do técnico de destino.");

            } else {
                validateQuantity(move);
            }

        } else if (move.getType().equals(MoveType.OUT_ITEM)) {

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

    private void validateQuantity(TechnicianStockMove move) {
        Double blockedQuantity = move.getFrom().getBlockedQuantity() == null ?
                0d :
                move.getFrom().getBlockedQuantity() + move.getQuantity();

        double availableQuantity = move.getFrom().getQuantity() - blockedQuantity;
        if (availableQuantity < 0) {
            throw new IllegalArgumentException("Estoque de " + move.getFrom().getProduct().getName() + " não possui quantidade disponível para esta movimentação");
        }
    }

    @Override
    public Optional<TechnicianStockMove> execute(TechnicianStockMove move) {
        final StockItem from = move.getFrom();
        final StockItem to = move.getTo();
        final MoveType type = move.getType();
        final MoveReason reason = move.getReason();
        final Double quantity = move.getQuantity();

        if (type.equals(MoveType.BETWEEN_STOCKS) &&
                reason.equals(MoveReason.SERVICE_ORDER) &&
                from.getStock().isCustomer()) {

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

    @Override
    protected String getMoveDescription(TechnicianStockMove move) {
        final StockItem from = move.getFrom();
        final StockItem to = move.getTo();

        final boolean isServiceOrderMove = move.getType().equals(MoveType.BETWEEN_STOCKS) &&
                move.getReason().equals(MoveReason.SERVICE_ORDER);

        final boolean returnMoveToMaintenanceByServiceOrder = move.getType().equals(MoveType.BETWEEN_STOCKS) &&
                move.getFrom().getStock().isTechnician() &&
                move.getTo().getStock().isMaintenance() &&
                move.getCustomerName() != null;

        if (isServiceOrderMove) {
            final double quantity = move.getQuantity();
            final Employee employee = move.getRequester();
            final Product product = from != null ? from.getProduct() : to.getProduct();
            String templateMessage = ":move : :employee :type :quantity :unit de :item ( :itemCode ) dia " +
                    ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à's' HH:mm:ss"));

            if (Objects.requireNonNull(from).getStock().isCustomer()) {
                final String fromName = move.getCustomerName() != null ?
                        move.getCustomerName() : Objects.requireNonNull(from).getStock().getName();
                templateMessage = templateMessage
                        .replaceAll(":type", "moveu do cliente " +
                                fromName +
                                " para " + to.getStock().getName());

            } else {
                final String toName = move.getCustomerName() != null ?
                        move.getCustomerName() : to.getStock().getName();
                templateMessage = templateMessage
                        .replaceAll(":type", "moveu do estoque " +
                                Objects.requireNonNull(from).getStock().getName() +
                                " para o cliente " + toName);
            }

            return templateMessage
                    .replaceAll(":move", "Entre estoques")
                    .replaceAll(":employee", employee.getName())
                    .replaceAll(":quantity", Double.toString(quantity))
                    .replaceAll(":unit", product.getUnit().getAbbreviation())
                    .replaceAll(":itemCode", product.getCode())
                    .replaceAll(":item", product.getName());

        } else if (returnMoveToMaintenanceByServiceOrder) {

            final double quantity = move.getQuantity();
            final Employee employee = move.getRequester();
            final Product product = from != null ? from.getProduct() : to.getProduct();
            final String customerName = move.getCustomerName();
            String templateMessage = ":move : :employee :type :quantity :unit de :item ( :itemCode ) :customer dia " +
                    ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à's' HH:mm:ss"));

            return templateMessage
                    .replaceAll(":move", "Entre estoques")
                    .replaceAll(":employee", employee.getName())
                    .replaceAll(":quantity", Double.toString(quantity))
                    .replaceAll(":unit", product.getUnit().getAbbreviation())
                    .replaceAll(":itemCode", product.getCode())
                    .replaceAll(":item", product.getName())
                    .replaceAll(":customer", "recolhido do cliente " + customerName)
                    .replaceAll(":type", "moveu do estoque " +
                            Objects.requireNonNull(from).getStock().getName() +
                            " para " + to.getStock().getName());

        } else {
            return super.getMoveDescription(move);
        }

    }
}
