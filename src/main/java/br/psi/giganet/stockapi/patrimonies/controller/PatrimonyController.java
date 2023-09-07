package br.psi.giganet.stockapi.patrimonies.controller;

import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.patrimonies.adapter.PatrimonyAdapter;
import br.psi.giganet.stockapi.patrimonies.controller.request.BatchInsertPatrimonyRequest;
import br.psi.giganet.stockapi.patrimonies.controller.request.InsertPatrimonyRequest;
import br.psi.giganet.stockapi.patrimonies.controller.request.MovePatrimonyRequest;
import br.psi.giganet.stockapi.patrimonies.controller.request.UpdatePatrimonyRequest;
import br.psi.giganet.stockapi.patrimonies.controller.response.PatrimonyProjection;
import br.psi.giganet.stockapi.patrimonies.controller.response.PatrimonyProjectionWithoutUnit;
import br.psi.giganet.stockapi.patrimonies.controller.response.PatrimonyResponse;
import br.psi.giganet.stockapi.patrimonies.controller.security.RolePatrimoniesRead;
import br.psi.giganet.stockapi.patrimonies.controller.security.RolePatrimoniesWrite;
import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import br.psi.giganet.stockapi.patrimonies.service.PatrimonyService;
import br.psi.giganet.stockapi.patrimonies_locations.factory.PatrimonyLocationFactory;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/patrimonies")
public class PatrimonyController {

    @Autowired
    private PatrimonyService patrimonyService;

    @Autowired
    private PatrimonyAdapter patrimonyAdapter;

    @Autowired
    private PatrimonyLocationFactory locationFactory;
    @Autowired
    private ProductAdapter productAdapter;

    @GetMapping
    @RolePatrimoniesRead
    public Page<PatrimonyProjection> findAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return patrimonyService.findAll(page, pageSize)
                .map(patrimonyAdapter::transform);
    }

    @GetMapping(params = {"queries", "page", "pageSize"})
    @RolePatrimoniesRead
    public Page<PatrimonyProjectionWithoutUnit> findAllByProductNameOrCodeOrLocation(
            @RequestParam(defaultValue = "", name = "queries") List<String> queries,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return patrimonyService.findAllByProductNameOrCodeOrLocation(queries, page, pageSize)
                .map(patrimonyAdapter::transformWithoutUnit);
    }

    @GetMapping("/current-locations/{location}")
    @RolePatrimoniesRead
    public Page<PatrimonyProjection> findAllByLocation(
            @PathVariable Long location,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return patrimonyService.findAllByCurrentLocation(locationFactory.create(location), page, pageSize)
                .map(patrimonyAdapter::transform);
    }

    @GetMapping("/current-locations/{location}/products/{product}")
    @RolePatrimoniesRead
    public Page<PatrimonyProjection> findAllByCurrentLocationAndProduct(
            @PathVariable Long location,
            @PathVariable String product,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return patrimonyService.findAllByCurrentLocationAndProduct(locationFactory.create(location), productAdapter.create(product), page, pageSize)
                .map(patrimonyAdapter::transform);
    }

    @GetMapping("/products/{product}")
    @RolePatrimoniesRead
    public Page<PatrimonyProjection> findAllByProduct(
            @PathVariable String product,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return patrimonyService.findAllByProduct(productAdapter.create(product), page, pageSize)
                .map(patrimonyAdapter::transform);
    }

    @GetMapping("/{id}")
    @RolePatrimoniesRead
    public PatrimonyResponse findById(@PathVariable Long id) {
        return patrimonyService.findById(id)
                .map(patrimonyAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Patrimonio não encontrado"));
    }

    @GetMapping(path = "/{id}", params = {"withHistory"})
    @RolePatrimoniesRead
    public PatrimonyResponse findByIdWithHistory(@PathVariable Long id) {
        return patrimonyService.findById(id)
                .map(p -> patrimonyAdapter.transformToResponse(p, true))
                .orElseThrow(() -> new ResourceNotFoundException("Patrimonio não encontrado"));
    }

    @GetMapping("/unique-codes/{code}")
    @RolePatrimoniesRead
    public PatrimonyResponse findByUniqueCode(@PathVariable String code) {
        return patrimonyService.findByUniqueCode(code)
                .map(patrimonyAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Patrimonio não encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolePatrimoniesWrite
    public PatrimonyResponse insert(@Valid @RequestBody InsertPatrimonyRequest request) {
        return patrimonyService.insert(patrimonyAdapter.transform(request))
                .map(patrimonyAdapter::transformToResponse)
                .orElseThrow(() -> new RuntimeException("Não foi possível cadastrar este patrimônio"));
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @RolePatrimoniesWrite
    public List<PatrimonyResponse> insertBatch(@Valid @RequestBody BatchInsertPatrimonyRequest request) {
        return patrimonyService.insert(patrimonyAdapter.transform(request))
                .stream()
                .map(patrimonyAdapter::transformToResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    @RolePatrimoniesWrite
    public PatrimonyResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePatrimonyRequest request) {
        return patrimonyService.update(id, patrimonyAdapter.transform(request))
                .map(patrimonyAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Patrimônio não encontrado"));
    }

    @PostMapping("/{id}/move")
    @RolePatrimoniesWrite
    public PatrimonyResponse movePatrimony(
            @PathVariable Long id,
            @Valid @RequestBody MovePatrimonyRequest request) {
        return patrimonyService.movePatrimony(id, patrimonyAdapter.transform(request))
                .map(p -> patrimonyAdapter.transformToResponse(p, true))
                .orElseThrow(() -> new ResourceNotFoundException("Patrimônio não encontrado"));
    }

    @DeleteMapping("/{id}")
    @RolePatrimoniesWrite
    public Object deleteById(@PathVariable Long id) {
        Optional<Patrimony> patrimony = patrimonyService.hidePatrimony(id);
        if(patrimony.isEmpty()){
            throw new ResourceNotFoundException("Patrimonio não encontrado");
        }
        return ResponseEntity.noContent().build();
    }

}
