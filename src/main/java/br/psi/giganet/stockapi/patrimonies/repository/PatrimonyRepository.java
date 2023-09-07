package br.psi.giganet.stockapi.patrimonies.repository;

import br.psi.giganet.stockapi.patrimonies.model.Patrimony;
import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import br.psi.giganet.stockapi.products.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatrimonyRepository extends JpaRepository<Patrimony, Long>, AdvancedPatrimonyRepository {

    Optional<Patrimony> findByCodeIgnoreCase(String code);

    @Query("SELECT p FROM Patrimony p WHERE p.isVisible = TRUE")
    Page<Patrimony> findAllByIsVisible(Pageable pageable);

    @Query("SELECT p FROM Patrimony p " +
            "JOIN FETCH Product pr ON p.product = pr " +
            "JOIN FETCH Unit u ON pr.unit = u " +
            "JOIN FETCH PatrimonyLocation loc ON p.currentLocation = loc " +
            "WHERE p.isVisible = TRUE")
    Page<Patrimony> findAllFetchByIsVisible(Pageable pageable);

    @Query("SELECT p FROM Patrimony p WHERE p.isVisible = TRUE AND p.currentLocation = :currentLocation")
    Page<Patrimony> findByCurrentLocationAndIsVisible(PatrimonyLocation currentLocation, Pageable pageable);

    @Query("SELECT p FROM Patrimony p WHERE " +
            "p.isVisible = TRUE AND " +
            "p.currentLocation = :currentLocation AND " +
            "p.product = :product")
    Page<Patrimony> findByCurrentLocationAndProductAndIsVisible(PatrimonyLocation currentLocation, Product product, Pageable pageable);

    @Query("SELECT p FROM Patrimony p WHERE p.isVisible = TRUE AND p.product = :product")
    Page<Patrimony> findByProductAndIsVisible(Product product, Pageable pageable);


}
