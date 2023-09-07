package br.psi.giganet.stockapi.templates.repository;

import br.psi.giganet.stockapi.templates.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
}
