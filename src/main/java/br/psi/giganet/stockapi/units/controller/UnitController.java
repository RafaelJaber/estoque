package br.psi.giganet.stockapi.units.controller;

import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.units.adapter.UnitAdapter;
import br.psi.giganet.stockapi.units.controller.response.UnitProjection;
import br.psi.giganet.stockapi.units.controller.response.UnitResponse;
import br.psi.giganet.stockapi.units.controller.security.RoleUnitsRead;
import br.psi.giganet.stockapi.units.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("units")
public class UnitController {

    @Autowired
    private UnitService unitService;

    @Autowired
    private UnitAdapter unitAdapter;

    @GetMapping
    @RoleUnitsRead
    public List<UnitProjection> findAll() {
        return unitService.findAll()
                .stream()
                .map(unitAdapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleUnitsRead
    public UnitResponse findById(@PathVariable String id) {
        return unitService.findById(id)
                .map(unitAdapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade não encontrada"));
    }
}
