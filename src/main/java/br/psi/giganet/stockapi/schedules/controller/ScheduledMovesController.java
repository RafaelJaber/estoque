package br.psi.giganet.stockapi.schedules.controller;

import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.schedules.adapter.SchedulingMoveAdapter;
import br.psi.giganet.stockapi.schedules.controller.request.InsertScheduledMoveRequest;
import br.psi.giganet.stockapi.schedules.controller.request.UpdateScheduledMoveRequest;
import br.psi.giganet.stockapi.schedules.controller.response.ScheduledMoveProjection;
import br.psi.giganet.stockapi.schedules.service.ScheduledMoveService;
import br.psi.giganet.stockapi.stock_moves.controller.security.RoleMovesRead;
import br.psi.giganet.stockapi.stock_moves.controller.security.RoleMovesWrite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/moves/schedules")
public class ScheduledMovesController {

    @Autowired
    private ScheduledMoveService scheduledMoveService;

    @Autowired
    private SchedulingMoveAdapter schedulingMoveAdapter;

    @GetMapping
    @RoleMovesRead
    public Page<ScheduledMoveProjection> findAll(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "1000") Integer pageSize) {
        return scheduledMoveService.findAllByCurrentBranchOffice(page, pageSize)
                .map(schedulingMoveAdapter::transform);
    }

    @GetMapping("/{id}")
    @RoleMovesRead
    public ScheduledMoveProjection findById(@PathVariable Long id) {
        return scheduledMoveService.findById(id)
                .map(schedulingMoveAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));
    }

    @GetMapping(path = "/{id}", params = {"withCurrentQuantity"})
    @RoleMovesRead
    public ScheduledMoveProjection findByIdWithCurrentQuantity(@PathVariable Long id) {
        return scheduledMoveService.findById(id)
                .map(item -> schedulingMoveAdapter.transformToResponse(item, true))
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleMovesWrite
    public ScheduledMoveProjection insert(@Valid @RequestBody InsertScheduledMoveRequest request) {
        return scheduledMoveService.insert(schedulingMoveAdapter.transform(request))
                .map(schedulingMoveAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível salvar o agendamento"));
    }

    @PutMapping("/{id}")
    @RoleMovesWrite
    public ScheduledMoveProjection update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateScheduledMoveRequest request) {
        return scheduledMoveService.update(id, schedulingMoveAdapter.transform(request))
                .map(schedulingMoveAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));
    }

    @PostMapping("/{id}/execute")
    @RoleMovesWrite
    public ScheduledMoveProjection execute(@PathVariable Long id) {
        return scheduledMoveService.execute(id)
                .map(schedulingMoveAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));
    }

    @DeleteMapping("/{id}")
    @RoleMovesWrite
    public ScheduledMoveProjection cancel(@PathVariable Long id) {
        return scheduledMoveService.cancel(id)
                .map(schedulingMoveAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));
    }

}
