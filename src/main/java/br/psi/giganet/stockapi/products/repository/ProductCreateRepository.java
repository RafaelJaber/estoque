package br.psi.giganet.stockapi.products.repository;

import br.psi.giganet.stockapi.products.model.ProductCreate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCreateRepository extends JpaRepository<ProductCreate, Long> {
}
