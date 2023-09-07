package br.psi.giganet.stockapi.products.controller;

import br.psi.giganet.stockapi.common.messages.service.LogMessageService;
import br.psi.giganet.stockapi.config.exception.response.SimpleErrorResponse;
import br.psi.giganet.stockapi.products.adapter.ProductAdapter;
import br.psi.giganet.stockapi.products.controller.request.ProductRequest;
import br.psi.giganet.stockapi.products.controller.security.RoleProductsRead;
import br.psi.giganet.stockapi.products.controller.security.RoleProductsWrite;
import br.psi.giganet.stockapi.products.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService products;

    @Autowired
    private ProductAdapter adapter;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private LogMessageService logService;

    @GetMapping
    @RoleProductsRead
    public ResponseEntity<Object> findByName(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) throws JsonProcessingException {
        try {
            return this.products.remoteFindByName(name, page, pageSize);
        } catch (Exception ex) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("error", "Um erro interno ocorreu a consulta de produtos");
            errors.put("description", ex.getLocalizedMessage());
            ex.printStackTrace();
            logService.send(mapper.writeValueAsString(errors));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SimpleErrorResponse("Um erro interno ocorreu durante a consulta de produtos"));
        }
    }

    @GetMapping("/{id}")
    @RoleProductsRead
    public ResponseEntity<Object> findById(@PathVariable String id) throws JsonProcessingException {
        try {
            return this.products.remoteFindById(id);
        } catch (Exception ex) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("error", "Um erro interno ocorreu a consulta do produto por id");
            errors.put("description", ex.getLocalizedMessage());
            ex.printStackTrace();
            logService.send(mapper.writeValueAsString(errors));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SimpleErrorResponse("Um erro interno ocorreu durante a consulta do produto por id"));
        }
    }

    @GetMapping("/code/{code}")
    @RoleProductsRead
    public ResponseEntity<Object> findByCode(@PathVariable String code) throws JsonProcessingException {
        try {
            return this.products.remoteFindByCode(code);
        } catch (Exception ex) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("error", "Um erro interno ocorreu a consulta do produto pelo código");
            errors.put("description", ex.getLocalizedMessage());
            ex.printStackTrace();
            logService.send(mapper.writeValueAsString(errors));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SimpleErrorResponse("Um erro interno ocorreu durante a consulta do produto pelo código"));
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleProductsWrite
    public ResponseEntity<Object> insert(@RequestBody @Valid ProductRequest product) throws JsonProcessingException {
        try {
            return this.products.remoteInsert(product);
        } catch (Exception ex) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("error", "Um erro interno ocorreu durante o cadastro do produto");
            errors.put("description", ex.getLocalizedMessage());
            ex.printStackTrace();
            logService.send(mapper.writeValueAsString(errors));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SimpleErrorResponse("Um erro interno ocorreu durante o cadastro do produto"));
        }
    }

    @PutMapping("/{id}")
    @RoleProductsWrite
    public ResponseEntity<Object> update(
            @PathVariable String id,
            @RequestBody @Valid ProductRequest product) throws JsonProcessingException {
        try {
            return this.products.remoteUpdate(id, product);
        } catch (Exception ex) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("error", "Um erro interno ocorreu durante a atualização do produto");
            errors.put("description", ex.getLocalizedMessage());
            ex.printStackTrace();
            logService.send(mapper.writeValueAsString(errors));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SimpleErrorResponse("Um erro interno ocorreu durante a atualização do produto"));
        }
    }

    @GetMapping("/code/generate")
    @RoleProductsRead
    public ResponseEntity<Object> generateByCategory(@RequestParam String category) throws JsonProcessingException {
        try {
            return this.products.remoteGetNextProductCodeByCategory(category);
        } catch (Exception ex) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("error", "Um erro interno ocorreu durante a consulta por um novo código do produto");
            errors.put("description", ex.getLocalizedMessage());
            ex.printStackTrace();
            logService.send(mapper.writeValueAsString(errors));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SimpleErrorResponse("Um erro interno ocorreu durante a consulta por um novo código do produto"));
        }
    }
}
