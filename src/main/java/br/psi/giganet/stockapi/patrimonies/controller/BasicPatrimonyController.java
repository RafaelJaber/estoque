package br.psi.giganet.stockapi.patrimonies.controller;

import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.patrimonies.adapter.PatrimonyAdapter;
import br.psi.giganet.stockapi.patrimonies.controller.request.BasicInsertPatrimonyRequest;
import br.psi.giganet.stockapi.patrimonies.controller.request.UpdatePatrimonyRequest;
import br.psi.giganet.stockapi.patrimonies.controller.response.PatrimonyProjection;
import br.psi.giganet.stockapi.patrimonies.controller.response.PatrimonyResponse;
import br.psi.giganet.stockapi.patrimonies.controller.security.RolePatrimoniesRead;
import br.psi.giganet.stockapi.patrimonies.controller.security.RolePatrimoniesWrite;
import br.psi.giganet.stockapi.patrimonies.service.PatrimonyService;
import br.psi.giganet.stockapi.patrimonies_locations.factory.PatrimonyLocationFactory;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/basic/patrimonies")
public class BasicPatrimonyController {

    @Autowired
    private PatrimonyService patrimonyService;

    @Autowired
    private PatrimonyAdapter patrimonyAdapter;

    @Autowired
    private PatrimonyLocationFactory locationFactory;
    @Autowired
    private ProductAdapter productAdapter;

    @GetMapping("/current-locations/technicians/{userId}/products/{product}")
    @RolePatrimoniesRead
    public Page<PatrimonyProjection> findAllFromTechnicianByProduct(
            @PathVariable String userId,
            @PathVariable String product,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return patrimonyService.findAllByCurrentLocationCodeAndProduct(
                locationFactory.create(userId),
                productAdapter.create(product),
                page,
                pageSize)
                .map(patrimonyAdapter::transform);
    }

    @GetMapping("/current-locations/customers/{userId}")
    @RolePatrimoniesRead
    public Page<PatrimonyProjection> findAllFromCustomerByCode(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return patrimonyService.findAllByCurrentLocationCode(
                locationFactory.create(userId),
                page,
                pageSize)
                .map(patrimonyAdapter::transform);
    }

    @GetMapping("/{id}")
    @RolePatrimoniesRead
    public PatrimonyResponse findById(@PathVariable Long id) {
        return patrimonyService.findById(id)
                .map(patrimonyAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Patrimonio não encontrado"));
    }

    @PostMapping("/technicians")
    @ResponseStatus(HttpStatus.CREATED)
    @RolePatrimoniesWrite
    public PatrimonyResponse insertByTechnician(@Valid @RequestBody BasicInsertPatrimonyRequest request) {
        return patrimonyService.insertByTechnician(patrimonyAdapter.transform(request))
                .map(patrimonyAdapter::transformToResponse)
                .orElseThrow(() -> new RuntimeException("Não foi possível cadastrar este patrimônio"));
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

}
