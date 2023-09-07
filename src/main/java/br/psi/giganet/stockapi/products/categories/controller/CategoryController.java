package br.psi.giganet.stockapi.products.categories.controller;

import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.products.categories.adapter.CategoryAdapter;
import br.psi.giganet.stockapi.products.categories.controller.response.CategoryResponse;
import br.psi.giganet.stockapi.products.categories.service.CategoryService;
import br.psi.giganet.stockapi.products.controller.security.RoleProductsRead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("products/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryAdapter categoryAdapter;

    @GetMapping
    @RoleProductsRead
    public List<CategoryResponse> findAll() {
        return categoryService.findAll()
                .stream()
                .map(categoryAdapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleProductsRead
    public CategoryResponse findById(@PathVariable String id) {
        return categoryService.findById(id)
                .map(categoryAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }

}
