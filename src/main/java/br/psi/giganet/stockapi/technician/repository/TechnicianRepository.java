package br.psi.giganet.stockapi.technician.repository;

import br.psi.giganet.stockapi.technician.model.Technician;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, String> {

    Optional<Technician> findByUserId(String userId);

    Optional<Technician> findByEmail(String email);

    List<Technician> findByName(String name);

    Page<Technician> findByName(String name, Pageable pageable);

}
