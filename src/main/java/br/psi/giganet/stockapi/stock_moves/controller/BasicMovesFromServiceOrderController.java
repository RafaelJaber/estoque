package br.psi.giganet.stockapi.stock_moves.controller;

import br.psi.giganet.stockapi.patrimonies.adapter.PatrimonyAdapter;
import br.psi.giganet.stockapi.patrimonies.adapter.PatrimonyMoveAdapter;
import br.psi.giganet.stockapi.patrimonies.service.PatrimonyService;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock_moves.adapter.StockMovesAdapter;
import br.psi.giganet.stockapi.stock_moves.controller.request.BasicInsertMoveFromServiceOrderRequest;
import br.psi.giganet.stockapi.stock_moves.controller.response.ServiceOrderMoveResponse;
import br.psi.giganet.stockapi.stock_moves.controller.security.RoleMovesRead;
import br.psi.giganet.stockapi.stock_moves.controller.security.RoleMovesWrite;
import br.psi.giganet.stockapi.stock_moves.model.StockMove;
import br.psi.giganet.stockapi.stock_moves.model.TechnicianStockMove;
import br.psi.giganet.stockapi.stock_moves.service.StockMovesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/basic/moves/service-orders")
public class BasicMovesFromServiceOrderController {

    @Autowired
    private StockMovesService movesService;
    @Autowired
    private StockMovesAdapter stockMovesAdapter;
    @Autowired
    private StockFactory stockFactory;

    @Autowired
    private PatrimonyService patrimonyService;
    @Autowired
    private PatrimonyMoveAdapter patrimonyMoveAdapter;
    @Autowired
    private PatrimonyAdapter patrimonyAdapter;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleMovesWrite
    @Transactional
    public Map<String, Object> insert(
            @RequestHeader(name = "User-Id") String userId,
            @Valid @RequestBody BasicInsertMoveFromServiceOrderRequest request) {

        Map<String, Object> response = new HashMap<>();

        response.put("moves",
                movesService.insertTechnicianMove(stockMovesAdapter.transform(request, userId))
                        .stream()
                        .map(stockMovesAdapter::transform)
                        .collect(Collectors.toList()));

        response.put("patrimonies",
                patrimonyService.movePatrimonyFromServiceOrder(patrimonyMoveAdapter.transform(request, userId))
                        .stream()
                        .map(patrimonyAdapter::transform)
                        .collect(Collectors.toList()));

        return response;
    }

    @GetMapping("/{orderId}")
    @RoleMovesRead
    public List<ServiceOrderMoveResponse> findAllByServiceOrderId(
            @PathVariable String orderId, @RequestParam(required = false) String activation) {
        List<? extends StockMove> moves = activation != null ?
                movesService.findAllTechnicianStockMoveByOrderIdAndActivationId(orderId, activation) :
                movesService.findAllTechnicianStockMoveByOrderId(orderId);

        return moves.stream()
                .map(move -> stockMovesAdapter.transformToServiceOrderMoveResponse((TechnicianStockMove) move))
                .sorted((m1, m2) -> m1.getProduct().getName().compareToIgnoreCase(m2.getProduct().getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{orderId}/pending")
    @RoleMovesRead
    public List<ServiceOrderMoveResponse> findAllPendingByServiceOrderId(
            @PathVariable String orderId) {
        return movesService.findAllPendingTechnicianStockMoveByOrderId(orderId)
                .stream()
                .map(move -> stockMovesAdapter.transformToServiceOrderMoveResponse((TechnicianStockMove) move))
                .sorted((m1, m2) -> m1.getProduct().getName().compareToIgnoreCase(m2.getProduct().getName()))
                .collect(Collectors.toList());
    }

    @PostMapping("/{orderId}/approve")
    @RoleMovesWrite
    public List<ServiceOrderMoveResponse> approveAllByExternalOrderId(@PathVariable String orderId) {
        return movesService.approveAllByExternalOrderId(orderId)
                .stream()
                .map(move -> stockMovesAdapter.transformToServiceOrderMoveResponse((TechnicianStockMove) move))
                .collect(Collectors.toList());
    }

    @PostMapping("/{orderId}/reject")
    @RoleMovesWrite
    public List<ServiceOrderMoveResponse> rejectAllByExternalOrderId(@PathVariable String orderId) {
        return movesService.rejectAllByExternalOrderId(orderId)
                .stream()
                .map(move -> stockMovesAdapter.transformToServiceOrderMoveResponse((TechnicianStockMove) move))
                .collect(Collectors.toList());
    }

}
