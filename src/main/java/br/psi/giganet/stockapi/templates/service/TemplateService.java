package br.psi.giganet.stockapi.templates.service;

import br.psi.giganet.stockapi.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.stockapi.products.service.ProductService;
import br.psi.giganet.stockapi.stock.model.Stock;
import br.psi.giganet.stockapi.stock.model.StockItem;
import br.psi.giganet.stockapi.stock.service.StockService;
import br.psi.giganet.stockapi.templates.model.Template;
import br.psi.giganet.stockapi.templates.model.TemplateItem;
import br.psi.giganet.stockapi.templates.repository.TemplateRepository;
import br.psi.giganet.stockapi.templates.service.dto.TemplateMountDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    public List<Template> findAll() {
        return this.templateRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<Template> findById(Long id) {
        return this.templateRepository.findById(id);
    }

    public Optional<Template> insert(Template template) {
        template.getItems().forEach(item ->
                item.setProduct(productService.findById(item.getProduct().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"))));

        return Optional.of(this.templateRepository.save(template));
    }

    public Optional<Template> update(Long id, Template template) {
        return this.templateRepository.findById(id)
                .map(saved -> {

                    saved.setName(template.getName());

                    saved.getItems().removeIf(item -> !template.getItems().contains(item));

                    saved.getItems().stream()
                            .filter(item -> template.getItems().contains(item))
                            .forEach(savedItem -> {
                                final int index = template.getItems().indexOf(savedItem);
                                TemplateItem item = template.getItems().get(index);

                                savedItem.setQuantity(item.getQuantity());
                                savedItem.setProduct(
                                        productService.findById(item.getProduct().getId())
                                                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")));
                            });

                    template.getItems().stream()
                            .filter(item -> !saved.getItems().contains(item))
                            .forEach(item -> {
                                item.setProduct(productService.findById(item.getProduct().getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado")));

                                item.setTemplate(saved);
                                saved.getItems().add(item);
                            });

                    return templateRepository.save(saved);
                });
    }

    public void deleteById(Long id) {
        this.templateRepository.deleteById(id);
    }

}
