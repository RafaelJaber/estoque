package br.psi.giganet.stockapi.technician.technician_product_category.controller;

import br.psi.giganet.stockapi.employees.controller.security.RoleRoot;
import br.psi.giganet.stockapi.technician.model.TechnicianSector;
import br.psi.giganet.stockapi.technician.technician_product_category.adapter.TechnicianSectorProductCategoryAdapter;
import br.psi.giganet.stockapi.technician.technician_product_category.controller.request.TechnicianSectorProductCategoryRequest;
import br.psi.giganet.stockapi.technician.technician_product_category.controller.response.TechnicianSectorProductCategoryResponse;
import br.psi.giganet.stockapi.technician.technician_product_category.service.TechnicianSectorProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/technicians/sectors/categories")
public class TechnicianSectorProductCategoryController {

    @Autowired
    private TechnicianSectorProductCategoryService service;

    @Autowired
    private TechnicianSectorProductCategoryAdapter adapter;

    @GetMapping
    public List<TechnicianSectorProductCategoryResponse> findAll() {
        return service.findAll()
                .stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{sector}")
    public List<TechnicianSectorProductCategoryResponse> findBySector(@PathVariable TechnicianSector sector) {
        return service.findBySector(sector)
                .stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

    @PutMapping("/{sector}")
    @RoleRoot
    public List<TechnicianSectorProductCategoryResponse> update(
            @PathVariable TechnicianSector sector,
            @RequestBody @Valid TechnicianSectorProductCategoryRequest request) {

        return service.update(sector, adapter.transform(request))
                .stream()
                .map(adapter::transform)
                .collect(Collectors.toList());
    }

}
