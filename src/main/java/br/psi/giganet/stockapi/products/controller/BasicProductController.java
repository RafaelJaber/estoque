package br.psi.giganet.stockapi.products.controller;

import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import br.psi.giganet.stockapi.products.controller.response.ProductProjectionWithoutUnit;
import br.psi.giganet.stockapi.products.controller.response.ProductResponse;
import br.psi.giganet.stockapi.products.controller.security.RoleProductsRead;
import br.psi.giganet.stockapi.products.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/basic/products")
public class BasicProductController {

    @Autowired
    private ProductService products;

    @Autowired
    private ProductAdapter adapter;

    @GetMapping
    @RoleProductsRead
    public Page<ProductProjection> findByName(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return this.products.findByNameContaining(name, page, pageSize).map(adapter::transform);
    }

    @GetMapping(params = { "filterCategory" })
    @RoleProductsRead
    public Page<ProductProjectionWithoutUnit> findByNameAndTechniciansCategory(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return this.products.findByNameContainingAndTechniciansCategory(name, page, pageSize)
                .map(adapter::transformWithoutUnit);
    }

    @GetMapping("/{id}")
    @RoleProductsRead
    public ProductResponse findById(@PathVariable String id) throws ResourceNotFoundException {
        return this.products.findById(id)
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    @GetMapping("/code/{code}")
    @RoleProductsRead
    public ProductResponse findByCode(@PathVariable String code) throws ResourceNotFoundException {
        return this.products.findByCode(code)
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

}
