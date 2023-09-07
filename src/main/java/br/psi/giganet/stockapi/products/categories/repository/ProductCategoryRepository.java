package br.psi.giganet.stockapi.products.categories.repository;

import br.psi.giganet.stockapi.products.categories.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<Category, String> {

}
