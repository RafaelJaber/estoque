package br.psi.giganet.stockapi.patrimonies_locations.repository;

import br.psi.giganet.stockapi.patrimonies_locations.model.PatrimonyLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatrimonyLocationRepository extends JpaRepository<PatrimonyLocation, Long> {

    Optional<PatrimonyLocation> findByCode(String code);

    Page<PatrimonyLocation> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
