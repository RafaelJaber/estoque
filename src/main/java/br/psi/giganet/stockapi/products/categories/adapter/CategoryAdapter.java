package br.psi.giganet.stockapi.products.categories.adapter;

import br.psi.giganet.stockapi.products.categories.controller.request.CategoryWebhookRequest;
import br.psi.giganet.stockapi.products.categories.controller.response.CategoryResponse;
import br.psi.giganet.stockapi.products.categories.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryAdapter {

    public Category create(String id) {
        Category category = new Category();
        category.setId(id);

        return category;
    }

    public Category transform(CategoryWebhookRequest request) {
        Category category = new Category();
        category.setId(request.getId());
        category.setName(request.getName());
        category.setPattern(request.getPattern());
        category.setDescription(request.getDescription());
        return category;
    }

    public CategoryResponse transform(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());

        return response;
    }

}
