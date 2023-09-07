package br.psi.giganet.stockapi.technician.controller;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.controller.security.RoleRoot;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.stock.model.TechnicianStock;
import br.psi.giganet.stockapi.stock.model.enums.StockType;
import br.psi.giganet.stockapi.technician.adapter.TechnicianAdapter;
import br.psi.giganet.stockapi.technician.controller.request.UpdateTechnicianRequest;
import br.psi.giganet.stockapi.technician.controller.response.TechnicianProjection;
import br.psi.giganet.stockapi.technician.service.TechnicianService;
import br.psi.giganet.stockapi.technician.service.dto.TechnicianScheduleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/technicians")
public class TechnicianController {

    @Autowired
    private TechnicianService technicianService;
    @Autowired
    private TechnicianAdapter technicianAdapter;

    @Autowired
    private StockFactory stockFactory;

    @PostMapping("/remote")
    @RoleRoot
    public ResponseEntity<Object> findAllRemote() {
        technicianService.updateTechniciansList();
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<TechnicianProjection> findAll() {
        return this.technicianService.findAll()
                .stream()
                .map(technicianAdapter::transformToProjection)
                .collect(Collectors.toList());
    }

    @GetMapping("/stocks/{id}")
    public List<TechnicianScheduleDTO> getTechnicianSchedule(
            @PathVariable Long id,
            @RequestParam LocalDate initialDate,
            @RequestParam LocalDate finalDate) {
        return this.technicianService.getTechnicianSchedule(
                (TechnicianStock) stockFactory.create(id, StockType.TECHNICIAN),
                initialDate,
                finalDate);
    }

    @GetMapping("/{id}")
    public TechnicianProjection findById(@PathVariable String id) {
        return this.technicianService.findById(id)
                .map(technicianAdapter::transformToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Técnico não foi encontrado"));
    }

    @PutMapping("/{id}")
    @RoleRoot
    public TechnicianProjection update(@PathVariable String id, @Valid @RequestBody UpdateTechnicianRequest request) {
        return this.technicianService.update(id, technicianAdapter.transform(request))
                .map(technicianAdapter::transformToProjection)
                .orElseThrow(() -> new IllegalArgumentException("Técnico não foi encontrado"));
    }

}
