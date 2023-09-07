package br.psi.giganet.stockapi.products.repository;

import br.psi.giganet.stockapi.products.model.Product;
import br.psi.giganet.stockapi.technician.model.TechnicianSector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findByNameContainingIgnoreCase(String name);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "INNER JOIN Category c ON c = p.category " +
            "INNER JOIN TechnicianSectorProductCategory tecCat ON tecCat.category = c " +
            "WHERE UPPER(p.name) LIKE CONCAT('%', UPPER(:name), '%') AND " +
            "tecCat.sector = :sector")
    Page<Product> findByNameAndTechnicianCategory(String name, TechnicianSector sector, Pageable pageable);

    Optional<Product> findByCode(String code);
}
