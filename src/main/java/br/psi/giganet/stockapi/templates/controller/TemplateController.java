package br.psi.giganet.stockapi.templates.controller;

import br.psi.giganet.stockapi.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.stockapi.stock.factory.StockFactory;
import br.psi.giganet.stockapi.templates.adapter.TemplateAdapter;
import br.psi.giganet.stockapi.templates.controller.request.InsertTemplateRequest;
import br.psi.giganet.stockapi.templates.controller.request.UpdateTemplateRequest;
import br.psi.giganet.stockapi.templates.controller.response.TemplateMountResponse;
import br.psi.giganet.stockapi.templates.controller.response.TemplateProjection;
import br.psi.giganet.stockapi.templates.controller.security.RoleTemplatesRead;
import br.psi.giganet.stockapi.templates.controller.security.RoleTemplatesWrite;
import br.psi.giganet.stockapi.templates.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/moves/templates")
public class TemplateController {

    @Autowired
    private TemplateAdapter templateAdapter;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private StockFactory stockFactory;

    @GetMapping
    @RoleTemplatesRead
    public List<TemplateProjection> findAll() {
        return templateService.findAll().stream()
                .map(templateAdapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleTemplatesRead
    public TemplateProjection findById(@PathVariable Long id) {
        return this.templateService.findById(id)
                .map(templateAdapter::transformToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Template não encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleTemplatesWrite
    public TemplateProjection insert(@Valid @RequestBody InsertTemplateRequest request) {
        return this.templateService.insert(templateAdapter.transform(request))
                .map(templateAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Template não encontrado"));
    }

    @PutMapping("/{id}")
    @RoleTemplatesWrite
    public TemplateProjection update(@PathVariable Long id, @Valid @RequestBody UpdateTemplateRequest request) {
        return this.templateService.update(id, templateAdapter.transform(request))
                .map(templateAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Template não encontrado"));
    }

    @DeleteMapping("/{id}")
    @RoleTemplatesWrite
    public ResponseEntity<Object> deleteById(@PathVariable Long id) {
        this.templateService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


}
