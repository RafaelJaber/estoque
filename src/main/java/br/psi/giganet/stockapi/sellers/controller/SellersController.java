package br.psi.giganet.stockapi.sellers.controller;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.controller.security.RoleRoot;
import br.psi.giganet.stockapi.sellers.adapter.SellerAdapter;
import br.psi.giganet.stockapi.sellers.controller.request.SellerCreateRequest;
import br.psi.giganet.stockapi.sellers.controller.request.SellerUpdateRequest;
import br.psi.giganet.stockapi.sellers.controller.response.SellerProjection;
import br.psi.giganet.stockapi.sellers.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sellers")
public class SellersController {

    @Autowired
    private SellerService sellerService;
    
    @Autowired
    private SellerAdapter sellerAdapter;

    @PostMapping()
    @RoleRoot
    public SellerProjection create(@Valid @RequestBody SellerCreateRequest request) {
        return this.sellerService.save(request)
                .map(sellerAdapter::transformToProjection)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor não foi encontrado"));
    }

    @GetMapping("/{id}")
    public SellerProjection findById(@PathVariable String id) {
        return this.sellerService.findById(id)
                .map(sellerAdapter::transformToProjection)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor não foi encontrado"));
    }

    @PutMapping("/{id}")
    @RoleRoot
    public SellerProjection update(@PathVariable String id, @Valid @RequestBody SellerUpdateRequest request) {
        return this.sellerService.update(id, request)
                .map(sellerAdapter::transformToProjection)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor não foi encontrado"));
    }

    @GetMapping
    public List<SellerProjection> findAll() {
        return this.sellerService.findAll()
                .stream()
                .map(sellerAdapter::transformToProjection)
                .collect(Collectors.toList());
    }

    @PostMapping("/remote")
    @RoleRoot
    public ResponseEntity<Object> remote() {
        sellerService.update();

        return ResponseEntity.noContent().build();
    }
}
