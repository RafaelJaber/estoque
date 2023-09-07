package br.psi.giganet.stockapi.products.adapter;

import br.psi.giganet.stockapi.products.categories.adapter.CategoryAdapter;
import br.psi.giganet.stockapi.products.categories.service.CategoryService;
import br.psi.giganet.stockapi.products.controller.request.ProductProjectionWebhookRequest;
import br.psi.giganet.stockapi.products.controller.request.ProductRequest;
import br.psi.giganet.stockapi.products.controller.request.ProductWebHookRequest;
import br.psi.giganet.stockapi.products.controller.response.ProductProjection;
import br.psi.giganet.stockapi.products.controller.response.ProductProjectionWithoutUnit;
import br.psi.giganet.stockapi.products.controller.response.ProductResponse;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.units.adapter.UnitAdapter;
import br.psi.giganet.stockapi.units.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductAdapter {

    @Autowired
    private UnitAdapter unitAdapter;

    @Autowired
    private CategoryAdapter categoryAdapter;

    public Product create(final String id) {
        final Product p = new Product();
        p.setId(id);
        return p;
    }

    public Product transform(ProductRequest request) {
        Product product = new Product();
        product.setId(request.getId());
        product.setName(request.getName());
        product.setCode(request.getCode());
        product.setCategory(categoryAdapter.create(request.getCategory()));
        product.setDescription(request.getDescription());
        product.setManufacturer(request.getManufacturer());
        product.setUnit(unitAdapter.create(request.getUnit()));

        return product;
    }

    public Product transform(ProductWebHookRequest request) {
        Product p = new Product();
        p.setName(request.getName());
        p.setId(request.getId());
        p.setCode(request.getCode());
        p.setCategory(categoryAdapter.transform(request.getCategory()));
        p.setDescription(request.getDescription());
        p.setManufacturer(request.getManufacturer());
        p.setUnit(unitAdapter.transform(request.getUnit()));

        return p;
    }

    public ProductProjection transform(Product product) {
        ProductProjection p = new ProductProjection();
        p.setName(product.getName());
        p.setCode(product.getCode());
        p.setManufacturer(product.getManufacturer());
        p.setId(product.getId());
        p.setUnit(unitAdapter.transform(product.getUnit()));

        return p;
    }

    public ProductProjectionWithoutUnit transformWithoutUnit(Product product) {
        ProductProjectionWithoutUnit p = new ProductProjectionWithoutUnit();
        p.setName(product.getName());
        p.setCode(product.getCode());
        p.setId(product.getId());

        return p;
    }

    public ProductProjectionWithoutUnit transformWithoutUnit(String code, String name) {
        ProductProjectionWithoutUnit p = new ProductProjectionWithoutUnit();
        p.setName(name);
        p.setCode(code);

        return p;
    }

    public ProductResponse transformToFullResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setCode(product.getCode());
        response.setCategory(categoryAdapter.transform(product.getCategory()));
        response.setDescription(product.getDescription());
        response.setManufacturer(product.getManufacturer());
        response.setUnit(unitAdapter.transformToFullResponse(product.getUnit()));

        return response;
    }

}
