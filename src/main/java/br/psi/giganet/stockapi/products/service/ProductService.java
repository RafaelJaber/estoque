package br.psi.giganet.stockapi.products.service;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.employees.service.EmployeeService;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import br.psi.giganet.stockapi.products.categories.service.CategoryService;
import br.psi.giganet.stockapi.products.controller.request.ProductRequest;
import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.products.model.ProductCreate;
import br.psi.giganet.stockapi.products.repository.ProductCreateRepository;
import br.psi.giganet.stockapi.products.repository.ProductRepository;
import br.psi.giganet.stockapi.technician.model.Technician;
import br.psi.giganet.stockapi.technician.service.TechnicianService;
import br.psi.giganet.stockapi.units.service.UnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {

    private final static Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository products;

    @Autowired
    private ProductCreateRepository productCreateRepository;

    @Autowired
    private ProductAdapter adapter;

    @Autowired
    private UnitService unitService;

    @Autowired
    private RemotePurchaseService remotePurchaseService;

    @Autowired
    private RemoteProductService remoteProductService;

    @Autowired
    private TechnicianService technicianService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CategoryService categoryService;

    public Optional<Product> insert(Product product) {
        this.products.findByCode(product.getCode())
                .ifPresent(p -> {
                    throw new IllegalArgumentException("Este código já está sendo utilizado por outro produto");
                });

        product.setUnit(unitService
                .findById(product.getUnit().getId())
                .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));
        product.setCategory(categoryService
                .findById(product.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada")));

        ProductCreate p = new ProductCreate();
        p.setId(product.getId() == null ? null : Long.valueOf(product.getId()));
        p.setName(product.getName());
        p.setCode(product.getCode());
        p.setCategory(product.getCategory());
        p.setDescription(product.getDescription());
        p.setManufacturer(product.getManufacturer());
        p.setUnit(product.getUnit());

        p = this.productCreateRepository.save(p);

        product.setId(p.getId().toString());
        return Optional.of(product);
    }

    public ResponseEntity<Object> remoteInsert(ProductRequest request) {
        return remotePurchaseService.requestForPurchaseAPI(
                "/products",
                HttpMethod.POST,
                request);
    }

    public ResponseEntity<Object> remoteUpdate(String id, ProductRequest request) {
        return remotePurchaseService.requestForPurchaseAPI(
                "/products/" + id,
                HttpMethod.PUT,
                request);
    }

    public ResponseEntity<Object> remoteFindByName(String name, Integer page, Integer pageSize) {
        return remotePurchaseService.requestForPurchaseAPI(
                "/products",
                HttpMethod.GET,
                null,
                Map.of("name", name, "page", String.valueOf(page), "pageSize", String.valueOf(pageSize)));
    }

    public ResponseEntity<Object> remoteFindById(String id) {
        return remotePurchaseService.requestForPurchaseAPI(
                "/products/" + id,
                HttpMethod.GET,
                null);
    }

    public ResponseEntity<Object> remoteFindByCode(String code) {
        return remotePurchaseService.requestForPurchaseAPI(
                "/products/code/" + code,
                HttpMethod.GET,
                null);
    }

    public ResponseEntity<Object> remoteGetNextProductCodeByCategory(String category) throws IOException {
        return remoteProductService.getRemoteNextProductCode(category);
    }

    public Optional<Product> update(String id, Product product) {
        return this.findById(id)
                .map(saved -> {
                    saved.setUnit(unitService
                            .findById(product.getUnit().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));
                    saved.setManufacturer(product.getManufacturer());
                    saved.setDescription(product.getDescription());
                    saved.setCategory(product.getCategory());
                    saved.setCode(product.getCode());
                    saved.setName(product.getName());

                    return this.products.save(saved);
                });
    }

    public void save(Product product) {
        this.findById(product.getId()).ifPresentOrElse(
                saved -> this.update(saved.getId(), product),
                () -> this.insert(product));
    }

    public Page<Product> findAll(int page, int size) {
        return this.products.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    public Page<Product> findByNameContaining(String name, int page, int size) {
        return this.products.findByNameContainingIgnoreCase(name, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    public Page<Product> findByNameContainingAndTechniciansCategory(String name, int page, int size) {
        Technician technician = technicianService.findByEmail(
                        employeeService.getCurrentLoggedEmployee()
                                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"))
                                .getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado"));

        if (technician.getSector() == null) {
            return Page.empty();
        }

        return this.products.findByNameAndTechnicianCategory(
                name,
                technician.getSector(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    public Optional<Product> findById(String id) {
        return this.products.findById(id);
    }

    public Optional<Product> findByCode(String code) {
        return this.products.findByCode(code);
    }

}
