package br.psi.giganet.stockapi.units.repository;

import br.psi.giganet.stockapi.units.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends JpaRepository<Unit, String> {

}
