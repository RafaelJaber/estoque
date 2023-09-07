package br.psi.giganet.stockapi.stock_moves.controller;

import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.stock_moves.adapter.StockMovesAdapter;
import br.psi.giganet.stockapi.stock_moves.controller.request.InsertMoveRequest;
import br.psi.giganet.stockapi.stock_moves.controller.response.StockMoveProjection;
import br.psi.giganet.stockapi.stock_moves.controller.security.RoleMovesRead;
import br.psi.giganet.stockapi.stock_moves.controller.security.RoleMovesWrite;
import br.psi.giganet.stockapi.stock_moves.model.MoveReason;
import br.psi.giganet.stockapi.stock_moves.model.MoveType;
import br.psi.giganet.stockapi.stock_moves.model.StockMove;
import br.psi.giganet.stockapi.stock_moves.service.StockMovesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/basic/stocks/moves")
public class BasicEmployeeMovesController {

    @Autowired
    private StockMovesService movesService;
    @Autowired
    private StockMovesAdapter stockMovesAdapter;

    @GetMapping("/employee/{userId}")
    @RoleMovesRead
    public Page<StockMoveProjection> findAllByDescription(
            @PathVariable String userId,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesService.findAllByUserAndDescription(userId, description, page, pageSize)
                .map(stockMovesAdapter::transformWithProductWithoutUnit);
    }

    @GetMapping("/employee/{userId}/realized")
    @RoleMovesRead
    public Page<StockMoveProjection> findAllRealizedByDescription(
            @PathVariable String userId,
            @RequestParam(defaultValue = "") String description,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesService.findAllRealizedByDescription(userId, description, page, pageSize)
                .map(stockMovesAdapter::transformWithProductWithoutUnit);
    }

    @GetMapping("/from/employee/{userId}/pending")
    @RoleMovesRead
    public Page<StockMoveProjection> findAllPendingByStockFrom(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesService.findAllPendingByStockFrom(userId, page, pageSize)
                .map(stockMovesAdapter::transformWithProductWithoutUnit);
    }

    @GetMapping("/to/employee/{userId}/pending")
    @RoleMovesRead
    public Page<StockMoveProjection> findAllPendingByStockToAndMoveReason(
            @PathVariable String userId,
            @RequestParam(defaultValue = "true") Boolean onlyDetached,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        Page<StockMove> response = onlyDetached ?
                movesService.findAllPendingByStockTo(
                        userId,
                        Arrays.asList(MoveReason.DETACHED, MoveReason.REQUEST),
                        page,
                        pageSize) :
                movesService.findAllPendingByStockTo(userId, page, pageSize);

        return response.map(stockMovesAdapter::transformWithProductWithoutUnit);
    }

    @GetMapping("/{id}")
    @RoleMovesRead
    public StockMoveProjection findById(@PathVariable Long id) {
        return movesService.findById(id)
                .map(stockMovesAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada"));
    }

    @PostMapping()
    @RoleMovesWrite
    @ResponseStatus(HttpStatus.CREATED)
    public List<StockMoveProjection> insert(
            @RequestHeader(name = "User-Id") String userId,
            @Valid @RequestBody InsertMoveRequest request) {
        if (request.getType().equals(MoveType.SALE)) {
            return movesService.insertSaleMove(stockMovesAdapter.transformToSaleMoves(request, userId)).stream()
                    .map(stockMovesAdapter::transform)
                    .collect(Collectors.toList());
        }
        return movesService.insertDetachedMove(stockMovesAdapter.transform(request, userId)).stream()
                .map(stockMovesAdapter::transform)
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/approve")
    @RoleMovesWrite
    public StockMoveProjection approveMove(@PathVariable Long id) {
        return movesService.approve(id)
                .map(stockMovesAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada"));
    }

    @PostMapping("/{id}/reject")
    @RoleMovesWrite
    public StockMoveProjection rejectMove(@PathVariable Long id) {
        return movesService.reject(id)
                .map(stockMovesAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Movimentação não encontrada"));
    }

}
