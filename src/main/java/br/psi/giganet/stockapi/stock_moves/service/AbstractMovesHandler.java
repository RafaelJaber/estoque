package br.psi.giganet.stockapi.stock_moves.service;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.config.security.Permissions;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.stock.model.StockItem;
import br.psi.giganet.stockapi.stock_moves.model.MoveReason;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.stock_moves.model.StockMove;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractMovesHandler<T extends StockMove> implements MoveHandler<T> {

    @Autowired
    protected EmployeeService employeeService;

    @Transactional
    public Optional<T> create(T move) {
        validate(move);

        if (move.getType().equals(MoveType.BETWEEN_STOCKS) || move.getType().equals(MoveType.OUT_ITEM)) {
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

    @Transactional
    public List<T> create(List<T> moves) {
        return moves.stream()
                .map(move -> create(move)
                        .orElseThrow(() -> new IllegalArgumentException("Não foi possível criar a movimentação")))
                .collect(Collectors.toList());
    }

    protected boolean shouldExecuteOnInsert(T move) {
        return move.getType().equals(MoveType.ENTRY_ITEM) ||
                move.getReason().equals(MoveReason.REQUEST) ||
                move.getRequester().hasRole("ROLE_MOVES_WRITE_ROOT");
    }

    @Override
    public void validate(T move) throws IllegalArgumentException {
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
            }

            Double blockedQuantity = move.getFrom().getBlockedQuantity() == null ?
                    0d :
                    move.getFrom().getBlockedQuantity() + move.getQuantity();

            double availableQuantity = move.getFrom().getQuantity() - blockedQuantity;
            if (availableQuantity < 0) {
                throw new IllegalArgumentException("Estoque de " + move.getFrom().getProduct().getName() + " não possui quantidade disponível para esta movimentação");
            }

        }

        if (!requesterHasPermission(move)) {
            throw new IllegalArgumentException("Funcionário não possui permissão para realizar esta operação");
        } else if (move.getType().equals(MoveType.BETWEEN_STOCKS) &&
                !move.isSameBranchOffice() &&
                (!move.getFrom().getStock().isShed() || !move.getTo().getStock().isShed())) {
            throw new IllegalArgumentException("Movimentações entre filiais pode ser realizada somente de galpão para galpão");
        }

    }

    @Transactional
    @Override
    public Optional<T> execute(T move) {
        final StockItem from = move.getFrom();
        final StockItem to = move.getTo();
        final MoveType type = move.getType();
        final Double quantity = move.getQuantity();

        if (!responsibleHasPermission(move)) {
            throw new IllegalArgumentException("Funcionário responsável não possui permissão para realizar esta operação");
        } else if (!move.getStatus().equals(MoveStatus.APPROVED)) {
            throw new IllegalArgumentException("Situação da movimentação é inválida");
        }

        if (type == MoveType.ENTRY_ITEM) {
            if (to == null) {
                onMoveFailed(move, "Estoque de destino é inválido");
            }

            Objects.requireNonNull(to)
                    .setQuantity(to.getQuantity() + quantity);

            if (to.getEntryMoves() == null) {
                to.setEntryMoves(new ArrayList<>());
            }
            to.getEntryMoves().add(move);

        } else if (type == MoveType.OUT_ITEM) {
            if (from == null) {
                onMoveFailed(move, "Estoque de origem é inválido");
            }

            if (quantity > Objects.requireNonNull(from).getQuantity()) {
                onMoveFailed(move, "Quantidade solicitada é superior a quantidade disponível para movimentação em estoque");
            }

            from.setQuantity(from.getQuantity() - quantity);
            from.setBlockedQuantity(from.getBlockedQuantity() - quantity);


            if (from.getOutgoingMoves() == null) {
                from.setOutgoingMoves(new ArrayList<>());
            }
            from.getOutgoingMoves().add(move);

        } else if (type == MoveType.BETWEEN_STOCKS) {
            if (to == null) {
                onMoveFailed(move, "Estoque de destino é inválido");
            } else if (from == null) {
                onMoveFailed(move, "Estoque de origem é inválido");
            } else if (to.equals(from)) {
                onMoveFailed(move, "Estoque de origem é igual ao estoque de destino");
            }

            if (!Objects.requireNonNull(to).getProduct()
                    .equals(Objects.requireNonNull(from).getProduct())) {
                onMoveFailed(move, "Estoques incompatíves. Os produtos informados são diferentes");
            } else if (quantity > from.getQuantity()) {
                onMoveFailed(move, "Quantidade solicitada é superior a quantidade disponível para movimentação em estoque");
            }

            from.setQuantity(from.getQuantity() - quantity);
            from.setBlockedQuantity(from.getBlockedQuantity() - quantity);

            to.setQuantity(to.getQuantity() + quantity);

            if (to.getEntryMoves() == null) {
                to.setEntryMoves(new ArrayList<>());
            }
            to.getEntryMoves().add(move);

            if (from.getOutgoingMoves() == null) {
                from.setOutgoingMoves(new ArrayList<>());
            }
            from.getOutgoingMoves().add(move);

        } else {
            onMoveFailed(move, "Tipo da movimentação é inválido");
        }

        move.setDescription(getMoveDescription(move));
        move.setStatus(MoveStatus.REALIZED);
        return Optional.of(move);
    }

    public Optional<T> approveAndExecute(T move) {
        if (!move.getStatus().equals(MoveStatus.REQUESTED)) {
            throw new IllegalArgumentException("Está movimentação não está mais pendente, portanto, não pode ser mais cancelada");
        }
        move.setStatus(MoveStatus.APPROVED);

        return execute(move);
    }

    @Override
    public Optional<T> approve(T move) {
        if (!move.getStatus().equals(MoveStatus.REQUESTED)) {
            throw new IllegalArgumentException("Está movimentação não está mais pendente, portanto, não pode ser mais cancelada");
        }
        move.setStatus(MoveStatus.APPROVED);

        return Optional.of(move);
    }

    @Override
    public Optional<T> cancel(T move) {
        if (!move.getStatus().equals(MoveStatus.REQUESTED)) {
            throw new IllegalArgumentException("Está movimentação não está mais pendente, portanto, não pode ser mais cancelada");

        } else if (!move.getType().equals(MoveType.ENTRY_ITEM) && move.getFrom() != null) {

            if (move.getType().equals(MoveType.BETWEEN_STOCKS) &&
                    move.getReason().equals(MoveReason.SERVICE_ORDER) &&
                    move.getFrom().getStock().isCustomer()) {
                move.getFrom().setBlockedQuantity(0d);

            } else {
                move.getFrom().setBlockedQuantity(
                        move.getFrom().getBlockedQuantity() != null ?
                                move.getFrom().getBlockedQuantity() - move.getQuantity() :
                                0d
                );
            }

        }
        move.setStatus(MoveStatus.CANCELED);
        return Optional.of(move);
    }

    @Override
    public Optional<T> reject(T move) {
        if (!move.getStatus().equals(MoveStatus.REQUESTED)) {
            throw new IllegalArgumentException("Está movimentação não está mais pendente, portanto, não pode ser mais cancelada");

        } else if (!move.getType().equals(MoveType.ENTRY_ITEM) && move.getFrom() != null) {

            if (move.getType().equals(MoveType.BETWEEN_STOCKS) &&
                    move.getReason().equals(MoveReason.SERVICE_ORDER) &&
                    move.getFrom().getStock().isCustomer()) {
                move.getFrom().setBlockedQuantity(0d);

            } else {
                move.getFrom().setBlockedQuantity(
                        move.getFrom().getBlockedQuantity() != null ?
                                move.getFrom().getBlockedQuantity() - move.getQuantity() :
                                0d
                );
            }

        }
        move.setStatus(MoveStatus.REJECTED);

        return Optional.of(move);
    }

    protected String getMoveDescription(T move) {
        final StockItem from = move.getFrom();
        final StockItem to = move.getTo();
        final MoveType type = move.getType();
        final double quantity = move.getQuantity();
        final Employee employee = move.getRequester();
        final Product product = from != null ? from.getProduct() : to.getProduct();
        String templateMessage = ":move : :employee :type :quantity :unit de :item ( :itemCode ) dia " +
                ZonedDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy à's' HH:mm:ss"));

        if (type == MoveType.ENTRY_ITEM) {
            templateMessage = templateMessage
                    .replaceAll(":move", "Entrada de estoque")
                    .replaceAll(":type", "adicionou ao estoque " +
                            Objects.requireNonNull(to).getStock().getName() + " ");

        } else if (type == MoveType.OUT_ITEM) {
            templateMessage = templateMessage
                    .replaceAll(":move", "Saída de estoque")
                    .replaceAll(":type", "removeu do estoque " +
                            Objects.requireNonNull(from).getStock().getName() + " ");

        } else if (type == MoveType.BETWEEN_STOCKS) {
            templateMessage = templateMessage
                    .replaceAll(":move", "Entre estoques")
                    .replaceAll(":type", "moveu do estoque " +
                            Objects.requireNonNull(from).getStock().getName() +
                            " para " + to.getStock().getName());
        }

        return templateMessage
                .replaceAll(":employee", employee.getName())
                .replaceAll(":quantity", Double.toString(quantity))
                .replaceAll(":unit", product.getUnit().getAbbreviation())
                .replaceAll(":itemCode", product.getCode())
                .replaceAll(":item", product.getName());
    }

    @Override
    public void onMoveFailed(T move, String error) throws IllegalArgumentException {
        move.setStatus(MoveStatus.FAILED);
        move.setDescription(error);
        if (move.getFrom() != null) {
            move.getFrom().setBlockedQuantity(
                    move.getFrom().getBlockedQuantity() != null ?
                            Math.max(move.getFrom().getBlockedQuantity() - move.getQuantity(), 0) :
                            0d
            );
        }

        throw new IllegalArgumentException(error);
    }

    @Override
    public boolean requesterHasPermission(T move) {
        final Permission root = new Permission("ROLE_ROOT");
        final Employee requester = employeeService.findById(move.getRequester().getId())
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));
        if (move.getType().equals(MoveType.ENTRY_ITEM)) {
            return requester.hasAnyRole(Arrays.asList(new Permission("ROLE_MOVES_WRITE_ENTRY_ITEMS"), root));
        } else if (move.getType().equals(MoveType.BETWEEN_STOCKS)) {
            return (requester.hasAnyRole(Arrays.asList(new Permission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"), root)) && move.isSameBranchOffice()) ||
                    requester.hasAnyRole(Arrays.asList(new Permission("ROLE_MOVES_WRITE_BETWEEN_BRANCH_OFFICE"), root)) ||
                    requester.hasAnyRole(Arrays.asList(new Permission(Permissions.ROLE_SALES_MODULE.name()), root));

        } else if (move.getType().equals(MoveType.OUT_ITEM)) {
            return requester.hasAnyRole(Arrays.asList(new Permission("ROLE_MOVES_WRITE_OUT_ITEM"), root));
        } else if (move.getType().equals(MoveType.SALE)) {
            return requester.hasAnyRole(Arrays.asList(new Permission(Permissions.ROLE_SALES_MODULE.name()), root));
        }

        return false;
    }

    @Override
    public boolean responsibleHasPermission(T move) {
        final Permission root = new Permission("ROLE_ROOT");
        final Employee responsible = employeeService.findById(move.getResponsible().getId())
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));
        if (move.getType().equals(MoveType.ENTRY_ITEM)) {
            return responsible.hasAnyRole(Arrays.asList(new Permission("ROLE_MOVES_WRITE_ENTRY_ITEMS"), root));
        } else if (move.getType().equals(MoveType.BETWEEN_STOCKS)) {
            return (responsible.hasAnyRole(Arrays.asList(new Permission("ROLE_MOVES_WRITE_BETWEEN_STOCKS"),
                    new Permission("ROLE_SALES_MODULE"), root)) && move.isSameBranchOffice()) ||
                    responsible.hasAnyRole(Arrays.asList(new Permission("ROLE_MOVES_WRITE_BETWEEN_BRANCH_OFFICE"), root));

        } else if (move.getType().equals(MoveType.OUT_ITEM)) {
            return responsible.hasAnyRole(Arrays.asList(new Permission("ROLE_MOVES_WRITE_OUT_ITEM"), root));
        }

        return false;
    }

}
