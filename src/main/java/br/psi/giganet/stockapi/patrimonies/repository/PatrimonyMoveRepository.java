package br.psi.giganet.stockapi.patrimonies.repository;

import br.psi.giganet.stockapi.patrimonies.model.PatrimonyMove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatrimonyMoveRepository extends JpaRepository<PatrimonyMove, Long> {
}
