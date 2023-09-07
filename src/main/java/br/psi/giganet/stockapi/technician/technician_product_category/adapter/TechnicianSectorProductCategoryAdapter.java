package br.psi.giganet.stockapi.technician.technician_product_category.adapter;

import br.psi.giganet.stockapi.products.categories.adapter.CategoryAdapter;
import br.psi.giganet.stockapi.technician.technician_product_category.controller.request.TechnicianSectorProductCategoryRequest;
import br.psi.giganet.stockapi.technician.technician_product_category.controller.response.TechnicianSectorProductCategoryResponse;
import br.psi.giganet.stockapi.technician.technician_product_category.model.TechnicianSectorProductCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TechnicianSectorProductCategoryAdapter {

    @Autowired
    private CategoryAdapter categoryAdapter;

    public TechnicianSectorProductCategoryResponse transform(TechnicianSectorProductCategory sectorCategory) {
        TechnicianSectorProductCategoryResponse response = new TechnicianSectorProductCategoryResponse();
        response.setCategory(categoryAdapter.transform(sectorCategory.getCategory()));
        response.setSector(sectorCategory.getSector());
        return response;
    }

    public List<TechnicianSectorProductCategory> transform(TechnicianSectorProductCategoryRequest request) {
        return request.getCategories().stream()
                .map(category -> {
                    TechnicianSectorProductCategory sectorCategory = new TechnicianSectorProductCategory();
                    sectorCategory.setSector(request.getSector());
                    sectorCategory.setCategory(categoryAdapter.create(category));
                    return sectorCategory;
                }).collect(Collectors.toList());
    }

}
