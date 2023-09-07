package br.psi.giganet.stockapi.moves_request.controller;

import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.moves_request.adapter.MovesRequestAdapter;
import br.psi.giganet.stockapi.moves_request.controller.request.BasicInsertRequestedMoveRequest;
import br.psi.giganet.stockapi.moves_request.controller.request.InsertRequestedMoveRequest;
import br.psi.giganet.stockapi.moves_request.controller.response.RequestedMoveProjection;
import br.psi.giganet.stockapi.moves_request.service.MovesRequestService;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock_moves.controller.security.RoleMovesRead;
import br.psi.giganet.stockapi.stock_moves.controller.security.RoleMovesWrite;
import br.psi.giganet.stockapi.stock_moves.model.MoveOrigin;
import br.psi.giganet.stockapi.stock_moves.model.MoveStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/basic/moves/requests")
public class BasicMovesRequestController {

    @Autowired
    private MovesRequestService movesRequestService;

    @Autowired
    private MovesRequestAdapter movesRequestAdapter;

    @Autowired
    private StockFactory stockFactory;

    @GetMapping("/to/technicians/{userId}")
    @RoleMovesRead
    public Page<RequestedMoveProjection> findAllByTechnicianTo(
            @PathVariable String userId,
            @RequestParam(required = false) MoveStatus status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return status != null ?
                movesRequestService.findAllByTechnicianToAndStatus(stockFactory.create(userId), status, page, pageSize)
                        .map(movesRequestAdapter::transform) :

                movesRequestService.findAllByTechnicianTo(stockFactory.create(userId), page, pageSize)
                        .map(movesRequestAdapter::transform);
    }

    @GetMapping("/from/technicians/{userId}")
    @RoleMovesRead
    public Page<RequestedMoveProjection> findAllByTechnicianFrom(
            @PathVariable String userId,
            @RequestParam(required = false) MoveStatus status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return status != null ?
                movesRequestService.findAllByTechnicianFromAndStatus(stockFactory.create(userId), status, page, pageSize)
                        .map(movesRequestAdapter::transform) :

                movesRequestService.findAllByTechnicianFrom(stockFactory.create(userId), page, pageSize)
                        .map(movesRequestAdapter::transform);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleMovesWrite
    public List<RequestedMoveProjection> insert(
            @RequestHeader("User-Id") String userId,
            @Valid @RequestBody BasicInsertRequestedMoveRequest request) {
        return movesRequestService.insertByTechnician(movesRequestAdapter.transform(request, userId))
                .stream()
                .map(movesRequestAdapter::transform)
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/approve")
    @RoleMovesWrite
    public RequestedMoveProjection approveMove(@PathVariable Long id) {
        return movesRequestService.approve(id)
                .map(movesRequestAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
    }

    @PostMapping("/{id}/reject")
    @RoleMovesWrite
    public RequestedMoveProjection rejectMove(@PathVariable Long id) {
        return movesRequestService.reject(id)
                .map(movesRequestAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
    }

}
