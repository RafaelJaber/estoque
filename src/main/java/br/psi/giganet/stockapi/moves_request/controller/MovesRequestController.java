package br.psi.giganet.stockapi.moves_request.controller;

import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.moves_request.adapter.MovesRequestAdapter;
import br.psi.giganet.stockapi.moves_request.controller.request.InsertRequestedMoveRequest;
import br.psi.giganet.stockapi.moves_request.controller.response.RequestedMoveProjection;
import br.psi.giganet.stockapi.moves_request.controller.response.RequestedMoveResponse;
import br.psi.giganet.stockapi.moves_request.service.MovesRequestService;
import br.psi.giganet.stockapi.branch_offices.model.enums.CityOptions;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.stock_moves.controller.security.RoleMovesRead;
import br.psi.giganet.stockapi.stock_moves.controller.security.RoleMovesWrite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/moves/requests")
public class MovesRequestController {

    @Autowired
    private MovesRequestService movesRequestService;

    @Autowired
    private MovesRequestAdapter movesRequestAdapter;

    @GetMapping
    @RoleMovesRead
    public Page<RequestedMoveProjection> findAllByCurrentBranchOffice(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesRequestService.findAllByCurrentBranchOffice(page, pageSize)
                .map(movesRequestAdapter::transform);
    }

    @GetMapping("/from/city/{city}/pending")
    @RoleMovesRead
    public Page<RequestedMoveProjection> findAllPendingByCityStockFrom(
            @PathVariable CityOptions city,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesRequestService.findAllPendingByCityStockFrom(city, page, pageSize)
                .map(movesRequestAdapter::transform);
    }

    @GetMapping("/to/city/{city}/pending")
    @RoleMovesRead
    public Page<RequestedMoveProjection> findAllPendingByCityStockTo(
            @PathVariable CityOptions city,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesRequestService.findAllPendingByCityStockTo(city, page, pageSize)
                .map(movesRequestAdapter::transform);
    }

    @GetMapping("/from/pending")
    @RoleMovesRead
    public Page<RequestedMoveProjection> findAllPendingByStockTypeFrom(
            @RequestParam(defaultValue = "") List<StockType> types,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesRequestService.findAllPendingByStockTypeFrom(types, page, pageSize)
                .map(movesRequestAdapter::transform);
    }

    @GetMapping("/to/pending")
    @RoleMovesRead
    public Page<RequestedMoveProjection> findAllPendingByStockTypeTo(
            @RequestParam(defaultValue = "") List<StockType> types,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return movesRequestService.findAllPendingByStockTypeTo(types, page, pageSize)
                .map(movesRequestAdapter::transform);
    }

    @GetMapping("/{id}")
    @RoleMovesRead
    public RequestedMoveResponse findById(@PathVariable Long id) {
        return movesRequestService.findById(id)
                .map(movesRequestAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleMovesWrite
    public List<RequestedMoveProjection> insert(@Valid @RequestBody InsertRequestedMoveRequest request) {
        return movesRequestService.insert(movesRequestAdapter.transform(request))
                .stream()
                .map(movesRequestAdapter::transform)
                .collect(Collectors.toList());
    }


    @PostMapping("/{id}/approve")
    @RoleMovesWrite
    public RequestedMoveProjection approveRequest(@PathVariable Long id) {
        return movesRequestService.approve(id)
                .map(movesRequestAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
    }

    @PostMapping("/{id}/reject")
    @RoleMovesWrite
    public RequestedMoveProjection rejectRequest(@PathVariable Long id) {
        return movesRequestService.reject(id)
                .map(movesRequestAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
    }

    @DeleteMapping("/{id}")
    @RoleMovesWrite
    public RequestedMoveProjection cancelRequest(@PathVariable Long id) {
        return movesRequestService.cancel(id)
                .map(movesRequestAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
    }

}
