package br.psi.giganet.stockapi.products.categories.service;

import br.psi.giganet.stockapi.products.categories.model.Category;
import br.psi.giganet.stockapi.products.categories.repository.ProductCategoryRepository;
import br.psi.giganet.stockapi.products.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private ProductCategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<Category> findById(String id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> insert(Category category) {
        return Optional.of(categoryRepository.save(category));
    }

    @Transactional
    public Optional<Category> update(String id, Category category) {
        return categoryRepository.findById(id)
                .map(saved -> {
                    saved.setName(category.getName());
                    saved.setPattern(category.getPattern());
                    saved.setDescription(category.getDescription());

                    return categoryRepository.save(saved);
                });
    }

    public void save(Category category) {
        this.findById(category.getId()).ifPresentOrElse(
                saved -> this.update(saved.getId(), category),
                () -> this.insert(category));
    }
}
