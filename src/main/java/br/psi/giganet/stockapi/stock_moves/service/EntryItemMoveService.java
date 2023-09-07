package br.psi.giganet.stockapi.stock_moves.service;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.config.security.model.Permission;
import br.psi.giganet.stockapi.employees.model.Employee;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.stock.model.StockItem;
import br.psi.giganet.stockapi.stock_moves.model.EntryItemStockMove;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Service(value = "EntryItemMoveService")
public class EntryItemMoveService implements MoveHandler<EntryItemStockMove> {

    @Autowired
    private EmployeeService employeeService;

    @Override
    public Optional<EntryItemStockMove> create(EntryItemStockMove move) {
        validate(move);

        move.setBranchOffice(move.getTo().getStock().getBranchOffice());
        move.setStatus(MoveStatus.APPROVED);
        move.setResponsible(move.getRequester());
        execute(move);

        return Optional.of(move);
    }

    @Override
    public Optional<EntryItemStockMove> execute(EntryItemStockMove move) {
        if (move.getTo() == null) {
            onMoveFailed(move, "Estoque de destino é inválido");
        }

        Objects.requireNonNull(move.getTo())
                .setQuantity(move.getTo().getQuantity() + move.getQuantity());

        move.setDescription(getMoveDescription(move));
        move.setStatus(MoveStatus.REALIZED);

        if (move.getTo().getEntryMoves() == null) {
            move.getTo().setEntryMoves(new ArrayList<>());
        }
        move.getTo().getEntryMoves().add(move);

        return Optional.of(move);
    }

    @Override
    public void validate(EntryItemStockMove move) throws IllegalArgumentException {
        if (move.getTo() == null) {
            throw new IllegalArgumentException("Estoque de destino é inválido");
        } else if (!move.getType().equals(MoveType.ENTRY_ITEM)) {
            throw new IllegalArgumentException("Tipo informado para a movimentação é inválido");
        } else if (!move.getTo().getStock().isShed()) {
            throw new IllegalArgumentException("Esta movimentação é permitida somente para estoques do tipo GALPÃO");
        } else if (move.getTo().getStock().getBranchOffice() == null) {
            throw new IllegalArgumentException("Estoque de destino não possui nenhuma filial associada");
        } else if (!requesterHasPermission(move)) {
            throw new IllegalArgumentException("Solicitante não possui permissões para realizar esta movimentação");
        }
    }

    @Override
    public boolean requesterHasPermission(EntryItemStockMove move) {
        final Permission root = new Permission("ROLE_ROOT");
        final Employee requester = employeeService.findById(move.getRequester().getId())
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));

        return requester.hasAnyRole(Arrays.asList(
                new Permission("ROLE_MOVES_WRITE_ENTRY_ITEMS"),
                new Permission("ROLE_ENTRIES_WRITE"),
                new Permission("ROLE_ENTRIES_WRITE_MANUAL"),
                root));
    }

    @Override
    public boolean responsibleHasPermission(EntryItemStockMove move) {
        return requesterHasPermission(move);
    }

    @Override
    public Optional<EntryItemStockMove> approve(EntryItemStockMove move) {
        throw new RuntimeException("Método não permitido para este tipo de movimentação");
    }

    @Override
    public Optional<EntryItemStockMove> cancel(EntryItemStockMove move) {
        throw new RuntimeException("Método não permitido para este tipo de movimentação");
    }

    @Override
    public Optional<EntryItemStockMove> reject(EntryItemStockMove move) {
        throw new RuntimeException("Método não permitido para este tipo de movimentação");
    }

    @Override
    public void onMoveFailed(EntryItemStockMove move, String error) throws IllegalArgumentException {
        throw new IllegalArgumentException(error);
    }

    protected String getMoveDescription(EntryItemStockMove move) {
        final StockItem to = move.getTo();
        final double quantity = move.getQuantity();
        final Employee employee = move.getRequester();
        final Product product = to.getProduct();
        String templateMessage = "Entrada de estoque : :employee adicionou ao :stockName :quantity :unit de :item ( :itemCode ) dia " +
                ZonedDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy à's' HH:mm:ss"));

        return templateMessage
                .replaceAll(":employee", employee.getName())
                .replaceAll(":stockName", to.getStock().getName())
                .replaceAll(":employee", employee.getName())
                .replaceAll(":quantity", Double.toString(quantity))
                .replaceAll(":unit", product.getUnit().getAbbreviation())
                .replaceAll(":itemCode", product.getCode())
                .replaceAll(":item", product.getName());
    }
}
