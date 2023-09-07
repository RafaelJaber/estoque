package br.psi.giganet.stockapi.nfe.repository;

import br.psi.giganet.stockapi.nfe.model.NFeContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface NFeContentRepository extends JpaRepository<NFeContent, Long> {

    Optional<NFeContent> findByAccessCode(String accessCode);

    Boolean existsByAccessCode(String accessCode);

}
